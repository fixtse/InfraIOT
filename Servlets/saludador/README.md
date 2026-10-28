# Saludador

This is an exploded Tomcat web application. Java sources live in
`WEB-INF/classes`; the existing VS Code build task runs `tomcat-compile`
and writes deployed classes there.

`pom.xml` also describes the project for both the Oracle and Red Hat Java
extensions. It targets Java 17 and references the servlet API and Catalina
JARs from the installed Tomcat 10.1.60 at `/opt/tomcat`. These dependencies
are supplied by Tomcat and are not packaged into the application.

With Maven installed, `mvn package` compiles into `target/classes` and
creates `target/saludador.war`. This does not deploy the application.
For another Tomcat installation, override `tomcat.home` with
`mvn -Dtomcat.home=/path/to/tomcat package`.

After adding the project configuration, run **Developer: Reload Window**
in VS Code so the Java extensions import the Maven project.
