# Resumen: contenedores de aplicaciones
Se presentan varias formas de distribuir y aislar aplicaciones en Linux. La idea central es distinguir **empaquetar una aplicación** de **aislarla**: AppImage facilita la distribución, mientras que Firejail, Flatpak y Docker añaden mecanismos de sandboxing.

**Empaquetar** es como preparar una maleta: llevas la aplicación y las bibliotecas que necesita para funcionar. **Aislar** es asignarle una habitación con reglas sobre qué puertas puede abrir. Tener la maleta preparada no impide entrar en otras habitaciones.

**Ejemplo:** quieres distribuir un editor de imágenes y evitar que lea tus documentos personales. Empaquetarlo resuelve cómo entregarlo; configurar un sandbox resuelve qué puede ver. Son dos problemas distintos que se pueden abordar juntos.

## Conceptos clave

- **Sandboxing:** ejecutar una aplicación con acceso restringido al sistema.
- **Máquina virtual:** aísla un sistema operativo completo.

Piensa en el host como un edificio y en cada contenedor como una oficina:

- **Namespaces, lo que ves:** limitan qué recursos ve un proceso. Imagina que cada oficina tiene su propio directorio de personas y espacios. Un proceso puede ver una lista de procesos (personas en una habitación) distinta de la del host (personas en el edificio). El mismo proceso puede tener un PID ``id de proceso`` (es la persona 4 en la habitación) dentro del contenedor y otro desde el host (es la persona 56 de todo el edificio).
- **Cgroups, cuánto consumes:** controlan CPU, memoria y Entrada/Salida de disco. Cada oficina tiene un presupuesto. Aunque una aplicación quiera ocupar toda la memoria o CPU, se le pueden imponer límites. En disco y red, los controles disponibles dependen del recurso y la configuración; no todos son simples cuotas de capacidad.
- **Capabilities, qué privilegios tienes:** permiten conceder privilegios concretos sin dar todos los permisos de superusuario. Una tarjeta permite una tarea concreta, como abrir una sala técnica, sin entregar la llave maestra. En Linux se pueden conceder privilegios específicos a un proceso.
- **Seccomp, qué operaciones puedes solicitar:** filtra las llamadas al sistema de un proceso. Imagina que en recepción, solo se aceptan ciertos tipos de trámites (si toman huellas, pero no emiten certificados digitales). El filtro puede bloquear determinadas llamadas al sistema, aunque el proceso intente realizarlas.

**Ejemplo:** un servidor web puede ver solo los procesos de su entorno, tener un límite de memoria, carecer de privilegios para modificar la configuración de red y tener bloqueadas ciertas llamadas al sistema. Cada mecanismo cubre una parte diferente del aislamiento.

Los contenedores comparten el kernel del host. Una **máquina virtual** se parece más a una vivienda con sus propias instalaciones: ejecuta su propio kernel sobre hardware virtualizado. Un contenedor no equivale a una máquina virtual pequeña.

**Caso para razonar:** Si una aplicación no ve otros procesos, pero consume demasiada CPU. Hay que revisar los límites de cgroups: limitar lo visible con namespaces no limita por sí solo el consumo.

| **Tecnología** |  **Para qué sirve** |  **Aislamiento según el documento** |
| - | - | - |
| AppImage |  Empaquetar y distribuir una aplicación con sus dependencias |  No incorpora sandboxing |
| Firejail |  Ejecutar programas con restricciones de seguridad |  Usa mecanismos del kernel, como namespaces y seccomp |
| Flatpak |  Distribuir y ejecutar aplicaciones en un sandbox |  Usa Bubblewrap, namespaces y puntos de montaje |
| Docker |  Ejecutar aplicaciones en contenedores |  Combina namespaces, cgroups, capabilities y seccomp |

## AppImage: empaquetar para distribuir
Una **AppImage** es un archivo ejecutable que contiene una aplicación y sus dependencias. Se construye a partir de un directorio **AppDir**.
Piensa en el AppDir como una mochila de herramientas que preparas para trabajar fuera de casa:

1. **AppDir** reúne el programa, bibliotecas, icono y archivo **`.desktop`**. Este último describe el nombre, icono y forma de lanzar la aplicación: es su tarjeta de identificación.
2. **linuxdeploy** analiza el ejecutable y copia dependencias al AppDir, como quien comprueba qué herramientas necesita llevar. **`ldd`** muestra bibliotecas dinámicas y **`readelf -d`** permite inspeccionar atributos como `RPATH` o `RUNPATH`, relacionados con su búsqueda.
3. **AppRun** es la instrucción de inicio: prepara variables como `PATH` y `LD_LIBRARY_PATH` y lanza el programa.
4. **appimagetool** cierra y empaqueta la mochila como un archivo AppImage.
5. **FUSE** permite montar el sistema de archivos SquashFS del paquete y acceder a su contenido, en las configuraciones que lo utilizan.

El laboratorio aplica este flujo a `xeyes`: revisar dependencias, crear el AppDir, incorporar bibliotecas, empaquetarlo y comprobar que funciona incluso tras mover el archivo.

**Ejemplo:** copiar solo el ejecutable puede fallar en otro equipo si faltan bibliotecas. Incorporarlas al AppDir facilita su distribución. Aun así, una AppImage no incluye un sistema operativo completo: sigue necesitando compatibilidad con el sistema donde se ejecuta.

**Caso para razonar:** si una AppImage puede leer tus documentos, no implica que esté mal empaquetada: el empaquetado no restringe los permisos del usuario. Necesitas configurar aislamiento para limitar ese acceso.

## Firejail: restringir la ejecución
Firejail permite limitar el acceso de una aplicación a archivos, red y otros recursos. El laboratorio combina Firejail con Wine para ejecutar un `.exe`.

- `--noprofile`: no aplica el perfil de seguridad específico de la aplicación.
- `--private=DIR`: usa un directorio como HOME privado del sandbox.
- `--net=none`: desactiva el acceso a la red.

La prueba con `prueba.txt` permite observar qué archivos puede ver la aplicación y dónde guarda los nuevos.

Supón que quieres abrir un programa con Wine y darle un espacio propio para sus archivos. `--private=DIR` hace que ese directorio aparezca como su HOME privado: es como asignarle una mesa con sus propios cajones. Los archivos que guarde allí permanecen en ese directorio del host.

Si además usas `--net=none`, le quitas la conexión de red. Son restricciones independientes: tener un HOME privado no desactiva la red y quitar la red no oculta automáticamente todos los archivos.

`--noprofile` omite el perfil específico; no significa que deje de existir todo aislamiento.

## Flatpak: distribuir y ejecutar en un sandbox
Flatpak distribuye aplicaciones, runtimes y SDKs mediante repositorios llamados **remotes**. Flathub es un repositorio habitual.
El **runtime** es una cocina equipada para preparar un plato siguiendo una receta ya lista: aporta las bibliotecas que necesita la aplicación. El **SDK** añade el taller para desarrollar esa receta: compiladores, cabeceras y herramientas.

**Ejemplo:** para usar GNOME Clocks necesitas su entorno de ejecución; para compilar una aplicación Flatpak necesitas también las herramientas del SDK correspondiente. Instalar herramientas de desarrollo no es necesario solo para abrir la aplicación.

Dentro del sandbox, **`/app`** es el espacio de la aplicación y **`/usr`** corresponde al runtime. Que exista `/usr` dentro del sandbox no significa que sea el mismo `/usr` que ves desde el host.

Los identificadores suelen usar dominios invertidos, por ejemplo `org.gnome.clocks`.

Un **remote** es como un catálogo de distribución y el **manifest** como una receta de construcción. Los permisos son otra cuestión: determinan el acceso a archivos, dispositivos y servicios. Por ejemplo, un portal (es el nombre que se le da a los gateways de acceso, en android el portal es cuando la aplicación te pide permiso para acceder a un folder) puede permitir que el usuario elija un archivo mediante un diálogo sin dar acceso general a todo su directorio personal.

## Docker: imágenes, contenedores y arquitectura
Una **imagen** contiene el sistema de archivos y la configuración necesarios para ejecutar una aplicación. Un **contenedor** es una instancia creada a partir de una imagen; una misma imagen puede crear varios contenedores.
Una **imagen** se parece a un molde y cada **contenedor** a una pieza creada a partir de él. Puedes crear dos contenedores desde la misma imagen; cada uno tiene sus propios procesos y su propia capa de escritura. Modificar archivos en uno no cambia automáticamente el otro ni la imagen original.

Docker tiene arquitectura cliente-servidor:

- El cliente `docker` envía órdenes.
- `dockerd` gestiona los contenedores.
- `containerd` gestiona su ciclo de vida.
- `runc` inicia o detiene los procesos del contenedor.
- Un registro, como Docker Hub, almacena y distribuye imágenes.

El **registro** es el almacén donde se publican los moldes. Descargar una imagen no crea por sí solo un servicio en ejecución.

En la arquitectura de Docker, el cliente es el mostrador donde das la orden, `dockerd` la coordina, `containerd` gestiona el ciclo de vida y `runc` realiza operaciones de bajo nivel para arrancar los procesos con el aislamiento configurado. Son componentes con funciones distintas dentro de la misma cadena.

Los contenedores comparten el kernel Linux del host. Por defecto reciben una IP privada en una red virtual; para hacerlos accesibles desde fuera se mapean sus puertos al host.
Comandos que aparecen en las prácticas:

- `docker run`: crear un contenedor y ejecutar su proceso.
- `docker ps`: listar contenedores activos.
- `docker images`: listar imágenes descargadas.
- `docker exec -it`: abrir una sesión dentro de un contenedor activo.
- `docker top`: ver sus procesos desde el host.
- `docker stop`, `start`, `kill`, `pause` y `unpause`: controlar su ciclo de vida.
- `docker rm` elimina contenedores; `docker rmi` elimina imágenes.

`docker run -d` ejecuta un contenedor en segundo plano. Opciones como `--name`, `--memory` y `--cpus` permiten asignar nombre y limitar recursos.

**Privilegios dentro del contenedor:** ser `root` dentro no equivale siempre a poder administrarlo todo en el host. Influyen los namespaces de usuario, las capabilities, los montajes y otras restricciones. Dar privilegios adicionales o acceso a recursos del host puede reducir mucho el aislamiento.

### Puertos y ciclo de vida: dos servidores web independientes

Al ejecutar:

```bash
docker run -d --name web-a -p 127.0.0.1:8080:80 nginx:stable
docker run -d --name web-b -p 127.0.0.1:8081:80 nginx:stable
docker ps
```

Ambos contenedores usan la misma imagen y sirven HTTP en su puerto interno **80**, pero puedes visitarlos desde el host en `http://127.0.0.1:8080` y `http://127.0.0.1:8081`.

**Analogía:** dos locales pueden tener una puerta interior con el mismo número porque están en edificios distintos. El mapeo indica a qué puerta interior conduce cada entrada del host. En `127.0.0.1:8080:80`, la dirección es la del host, `8080` es su puerto y `80` es el del contenedor. Este ejemplo publica el servicio solo en la interfaz local del host.

```bash
docker stop web-a
docker ps -a
docker start web-a
docker logs web-a
```

Detener `web-a` deja su contenedor creado y no detiene `web-b`. `docker ps -a` incluye los contenedores detenidos; `docker start` arranca de nuevo el existente y `docker logs` consulta la salida registrada. En cambio, volver a usar `docker run` intenta crear otro contenedor: no reutiliza automáticamente el anterior.

**Caso para razonar:** que la web escuche en el puerto 80 del contenedor no basta para abrirla en el puerto 8080 del host. Hay que publicar el puerto y comprobar que el proceso escucha en una interfaz accesible dentro del contenedor.

### Persistencia:

La capa de escritura del contenedor es como su taquilla: los archivos sobreviven a un `stop` y un `start`, pero desaparecen al eliminar ese contenedor. Para conservar datos independientemente de él se usan, entre otras opciones, **volúmenes** o **bind mounts**.

- Un **volumen** es un almacén administrado por Docker que puede seguir existiendo después de eliminar el contenedor.
- Un **bind mount** conecta una ruta concreta del host con una ruta dentro del contenedor. Los cambios pueden afectar a los archivos del host, según los permisos y si el montaje permite escritura.

**Ejemplo:** una base de datos escribe sus registros en un volumen. Puedes sustituir el contenedor y conectar el nuevo al mismo volumen para conservar los datos, siempre que la nueva versión sea compatible. Esto no sustituye una copia de seguridad.

La relación con Firejail ayuda a recordarlo: en ambos casos hay que distinguir **qué ruta ve la aplicación** de **dónde se guardan realmente los archivos**.

### Límites de recursos

`--memory=256m` establece un límite de memoria; no reserva automáticamente esa cantidad para uso exclusivo. `--cpus=0.5` limita el tiempo de CPU aproximadamente al equivalente de media CPU; no asigna necesariamente la mitad de un núcleo concreto. Si una aplicación necesita más memoria de la permitida, puede sufrir fallos por falta de memoria o terminar.

### Políticas de reinicio

Las políticas de reinicio se parecen a las instrucciones de un encargado:

- **`no`:** «Si se para, espera a que alguien lo arranque».
- **`on-failure[:n]`:** reinicia si el proceso termina con error; `n` limita los intentos. Por ejemplo, **`on-failure:3`:** «Si termina con error, intenta reiniciarlo hasta tres veces».
- **`unless-stopped`:** «Mantenlo funcionando, pero respeta que yo lo haya parado, también tras reiniciar Docker».
- **`always`:** «Mantenlo funcionando; si lo paro manualmente, no lo reinicies de inmediato, pero vuelve a arrancarlo cuando se reinicie Docker».

**Ejemplo:** si paras un servicio para hacer mantenimiento, `unless-stopped` conserva esa decisión tras reiniciar el daemon. La política de reinicio reacciona a la salida del proceso; no detecta por sí sola todos los casos en los que una aplicación sigue ejecutándose pero no responde.

## Para estudiar:

1. **¿Cuál es la diferencia entre AppImage y Docker?**

AppImage empaqueta una aplicación para distribuirla; Docker ejecuta una instancia aislada desde una imagen. AppImage no proporciona sandboxing por sí misma.

2. **¿Qué diferencia hay entre una imagen y un contenedor?**

La imagen es la plantilla; el contenedor es una instancia en ejecución o detenida creada a partir de ella.

3. **¿Qué hace cada mecanismo del kernel?**

Namespaces aíslan lo visible; cgroups limitan recursos; capabilities separan privilegios; seccomp filtra llamadas al sistema.

4. **¿Qué aportan runtime y SDK en Flatpak?**

El runtime sirve para ejecutar; el SDK añade herramientas de desarrollo.

5. **¿Qué comprueban `ldd` y `readelf -d` en el laboratorio AppImage?**

`ldd` muestra qué bibliotecas se cargan; `readelf -d` permite inspeccionar atributos ELF relacionados con la búsqueda de bibliotecas, como RPATH o RUNPATH.

6. **¿Qué diferencia hay entre `docker stop` y `docker kill`?**

`stop` intenta terminar el proceso de forma ordenada y, tras esperar, puede forzarlo. `kill` envía SIGKILL (forzado de apagado, como mantener presionado el boton de encendido) directamente. Ambos detienen el contenedor, pero no lo eliminan.

7. **¿Cómo se accede a una aplicación en un contenedor desde el exterior?**

Mediante un mapeo de puertos entre el contenedor y el host.

### **Repaso activo**
Intenta responder sin mirar el resumen:

- Nombra los cuatro mecanismos de aislamiento del kernel y explica uno con un ejemplo.
- Describe el flujo para convertir un AppDir en una AppImage.
- Si una app Flatpak necesita compilarse, ¿usarías el runtime o el SDK? ¿Por qué?
- Si quieres ver los procesos de un contenedor desde el host, ¿qué comando usarías?
- ¿Qué política de reinicio elegirías si quieres que un contenedor vuelva a arrancar automáticamente salvo que lo hayas detenido a mano?











**Respuestas:** namespaces, cgroups, capabilities y seccomp; AppDir → `linuxdeploy` para incorporar dependencias → `appimagetool`; el SDK, porque incluye herramientas de desarrollo; `docker top`; `unless-stopped`.
