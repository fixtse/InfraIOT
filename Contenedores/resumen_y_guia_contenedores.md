# Resumen: contenedores de aplicaciones
Se presentan varias formas de distribuir y aislar aplicaciones en Linux. La idea central es distinguir **empaquetar una aplicación** de **aislarla**: AppImage facilita la distribución, mientras que Firejail, Flatpak y Docker añaden mecanismos de sandboxing.  

## Conceptos clave 
- **Sandboxing:** ejecutar una aplicación con acceso restringido al sistema.  
- **Máquina virtual:** aísla un sistema operativo completo.  
- **Namespaces:** limitan qué recursos ve un proceso.  
- **Cgroups:** limitan recursos como CPU, memoria, disco y red.  
- **Capabilities:** permiten conceder privilegios concretos sin dar todos los permisos de superusuario.  
- **Seccomp:** filtra las llamadas al sistema que puede realizar un proceso.  
  
|  |  |  |  
| - | - | - |  
| **Tecnología** |  **Para qué sirve** |  **Aislamiento según el documento** |  
| AppImage |  Empaquetar y distribuir una aplicación con sus dependencias |  No incorpora sandboxing |  
| Firejail |  Ejecutar programas con restricciones de seguridad |  Usa mecanismos del kernel, como namespaces y seccomp |  
| Flatpak |  Distribuir y ejecutar aplicaciones en un sandbox |  Usa Bubblewrap, namespaces y puntos de montaje |  
| Docker |  Ejecutar aplicaciones en contenedores |  Combina namespaces, cgroups, capabilities y seccomp |  
  
   
### AppImage: empaquetar para distribuir
Una **AppImage** es un archivo ejecutable que contiene una aplicación y sus dependencias. Se construye a partir de un directorio **AppDir**.  
- **AppRun** inicia la aplicación y puede preparar variables como `PATH` y `LD_LIBRARY_PATH`.  
- El archivo **`.desktop`** describe la aplicación, su nombre, icono y cómo lanzarla.  
- **linuxdeploy** analiza el ejecutable y copia sus dependencias al AppDir.  
- **appimagetool** empaqueta el AppDir en una AppImage.  
- **FUSE** permite montar el sistema de archivos SquashFS de la AppImage.  
- **`ldd`** muestra las bibliotecas dinámicas usadas; **`readelf -d`** permite inspeccionar atributos como `RPATH` o `RUNPATH`.  
El laboratorio sigue este flujo: revisar dependencias de `xeyes`, crear su AppDir, incorporar bibliotecas, empaquetarlo y comprobar que funciona incluso tras mover el archivo.  
**Firejail: restringir la ejecución**  
Firejail permite limitar el acceso de una aplicación a archivos, red y otros recursos. El laboratorio combina Firejail con Wine para ejecutar un `.exe`.  
- `--noprofile`: no aplica el perfil de seguridad específico de la aplicación.  
- `--private=DIR`: usa un directorio como HOME privado del sandbox.  
- `--net=none`: desactiva el acceso a la red.  
La prueba con `prueba.txt` permite observar qué archivos puede ver la aplicación y dónde guarda los nuevos.  
### Flatpak: distribuir y ejecutar en un sandbox
Flatpak distribuye aplicaciones, runtimes y SDKs mediante repositorios llamados **remotes**. Flathub es un repositorio habitual.  
- El **runtime** aporta las bibliotecas necesarias para ejecutar la aplicación.  
- El **SDK** añade compiladores, cabeceras y herramientas de desarrollo.  
- **`/app`** contiene los archivos de la aplicación y **`/usr`** los del runtime.  
- Los identificadores suelen usar dominios invertidos, por ejemplo `org.gnome.clocks`.  
- Un **manifest** describe cómo construir y ejecutar una aplicación.  
El laboratorio instala GNOME Clocks, consulta sus metadatos y permisos, y compara los archivos visibles en el host con los directorios que ve desde su sandbox.  
### Docker: imágenes, contenedores y arquitectura 
Una **imagen** contiene el sistema de archivos y la configuración necesarios para ejecutar una aplicación. Un **contenedor** es una instancia creada a partir de una imagen; una misma imagen puede crear varios contenedores.  
Docker tiene arquitectura cliente-servidor:  
- El cliente `docker` envía órdenes.  
- `dockerd` gestiona los contenedores.  
- `containerd` gestiona su ciclo de vida.  
- `runc` inicia o detiene los procesos del contenedor.  
- Un registro, como Docker Hub, almacena y distribuye imágenes.  
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
**Políticas de reinicio**  
- `no`: no reinicia automáticamente.  
- `on-failure[:n]`: reinicia si termina con error; `n` limita los intentos.  
- `unless-stopped`: reinicia automáticamente salvo que se haya detenido manualmente.  
- `always`: reinicia automáticamente, incluso cuando se reinicia el daemon.  
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
   
 `stop` intenta terminar el proceso de forma ordenada y, tras esperar, puede forzarlo. `kill` envía SIGKILL directamente. Ambos detienen el contenedor, pero no lo eliminan.  

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
