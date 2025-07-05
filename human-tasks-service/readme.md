# Servicio Gestión de tareas humanas
El componente "human-tasks-service" contiene la aplicación de negocio del proyecto para la gestión de tareas humanas en procesos jBPM
## Tecnologías
Utiliza Spring boot 2 y jBPM (kie server 7.74.1.Final)
## Generación
Se ha generado con el arquetipo mvn con las opciones:
* ` mvn archetype:generate -B "-DarchetypeGroupId=org.kie" "-DarchetypeArtifactId=kie-service-spring-boot-archetype" "-DarchetypeVersion=7.74.1.Final" "-DgroupId=us.dit" "-DartifactId=human-tasks-service" "-Dversion=1.0-SNAPSHOT" "-Dpackage=us.dit.humanTasks.service" "-DappType=bpm"`

Pero posteriormente se han hecho los siguientes cambios a la configuración por defecto:
1. En pom.xml se ha añadido, para que utilice Spring boot 2.6.15
2. Se ha cambiado la configuración de seguridad para que sea conforme a los nuevos mecanismos de Spring
3. Se ha cambiado el banner por defecto. Se ha usado la web: https://manytools.org/hacker-tools/ascii-banner/
4. Se ha añadido el fichero human-tasks-service.xml para incluir la configuración del servidor kie

## Ejecución
Se proporcionan dos modos de ejecución independientes. Un modo básico de ejecución que será utilizado en la etapa de producción final de la aplicación y un modo desarrollador centrado en el agilizar el método de trabajo al desarrollador. A demás de un modo de depuración para llevar un seguimiento del flujo del código.

### Ejecución en modo básico.
* En caso de ejecutar este modo, será necesario analizar el fichero ``application.properties`` del directorio resources para comprender la configuración de la aplicación Springboot.
* Se incluye como bbdd por defecto postgres. Previo a la ejecución debe estar creada la BBDD y el usuario en el servidor postgres en su equipo.
* Se necesita también un servidor FHIR de respaldo, cuya dirección también se configura en este fichero.
* En windows ejecutar ``.\launch.bat clean install -Ppostgres``
* Se ha añadido una clase SystemPropertiesInjector en el fichero ``application.properties`` para la configuración de las variables del sistema que configuran los servidores kie (o cualquier otra propiedad).
* La aplicación puede conectarse a varios servidores kie que debe configurar previamente en el fichero anterior. Por defecto se levantará un servidor kie embebido al arrancar la aplicación, accesible mediante la url http://localhost:8090/rest/server . 
Si desea probar con dos servidores simultáneamente (opcional), debe poner en marcha otro servidor levantando localmente el servicio de Business Bentral desde el repositorio [EntornoDesarrollojBPM], y posteriormente importar el kjar del proyecto desde el repositorio GitHub ``https://github.com/tfg-projects-dit-us/Healthcare-Tasks/`` e implementarlo en el servidor kie "sample-server" que proporciona business-central por defecto. Este segundo servidor debería ser accesible desde la url http://localhost:8080/kie-server/services/rest/server .
* Para el correcto funcionamiento de la aplicación, necesitará crear una serie de recursos en el servidor FHIR, disponibles en la carpeta [resources](../resources). En este directorio puede encontrar información adicional como soporte.

### Ejecución en modo desarrollador.
#### Despliegue del entorno
Para seguir desarrollando el servicio de gestión de tareas humanas (human-tasks-service), se recomienda utilizar el repositorio [EntornoDesarrollojBPM](https://github.com/tfg-projects-dit-us/EntornoDesarrollojBPM) para levantar localmente (en contenedores docker) los servicios que dan soporte al desarrollo y verificación del proyecto. Esto podrá a su disposición, localmente, los servicios de:
* FHIR: http://localhost:8888/
* Business Central: http://localhost:8080/business-central/
* Repositorio de Artefactos (opcional): http://localhost:8081/

#### Ejecución
Para ejecutar en modo development deberá usar el comando ``launch-dev.bat clean install`` En este caso el fichero de configuración utilizado es ``application-dev.properties``. Es recomendable mirar que su variable de entorno JAVA_HOME se encuentre bien configurada conforme a la de su dispositivo en ``launch-dev.bat``, que los contenedores Docker desplegados desde [EntornoDesarrollojBPM] estén funcionando, y haber configurado el usuario con nombre y contraseña "controllerUser" y con rol "rest-all" en Business Central".

#### Implementar el servidor KIE en BC 
Una vez tenga el modo developer corriendo en su máquina, será necesario conectar el servidor KIE interno, presente en nuestra aplicación con una implementación de nuestro proyecto en Business Central para poder manejar los procesos en tiempo de ejecución directamente desde su interfaz.

Una vez ejecutada la aplicación se debe de haber creado automáticamente una nueva configuración de Servidor en business-central llamada "healthcareTasks Dev". Debe comprobar que esté conectada a un servidor Docker remoto interno en el puerto 8090, iniciado por la ejecución de la aplicación. Se puede ver accediendo al menú principal de "Business Central > Implementar".

En él debe implementar su proyecto kjar. Para ello puede importar su proyecto desde el repositorio GitHub ``https://github.com/tfg-projects-dit-us/Healthcare-Tasks/``. Se puede hacer accediendo al menú principal de "Business Central > Diseño". Y pulsando el botón "Implementar".

#### Verificación
En ``http://localhost:8090/h2-console/`` puede consultar la base de datos h2 (en memoria) durante la ejecución (mantener login y password por defecto, sa, sa).

Para realizar las pruebas y continuar con el desarrollo podría necesitar una serie de recursos en FHIR, disponibles en la carpeta [resources](../resources). En este directorio puede encontrar información adicional como soporte.

#### Configuración local de maven si se utiliza repositorio de artefactos
En el fichero settings.xml de maven debe configurar el acceso al reposilite, el repositorio de artefactos que se le proporciona en el despliegue del entorno de desarrollo.
``xml
<activeProfiles>
	<activeProfile>reposilite-profile</activeProfile>
</activeProfiles>
<profiles>
	<profile>
		<id>reposilite-profile</id>
		<repositories>
			<repository>
				<id>reposilite</id>
				<url>http://localhost:8081/kie</url>
				<releases>
					<enabled>true</enabled>
				</releases>
				<snapshots>
					<enabled>true</enabled>
				</snapshots>
			</repository>
		</repositories>
	</profile>		
</profiles>
<server>
	<id>reposilite</id>
	<username>jbpm</username>
	<password>Qkjbf2gGjBIIufQJD79K0Js7KLlINal2l4AmchNJ74HUP67NRBfmgaL+c7hpET+q</password>
</server>
</servers>
</settings>
.
``

### Ejecución en modo depuración
Con la aplicación corriendo en cualquiera de los modos previos de ejecución, se abrirá el puerto 5005 para escuchar. Será necesario conectarse a este puerto para ejecutar el modo debug. La forma de conexión dependerá del IDE usado.
#### VisualStudioCode 
Permite conectarse través del archivo de configuración proporcionado ``launch.json``. En el menú Run and Debug, seleccionar en el desplegable que aparece junto al botón Start Debugging, la opción correspondiente a nuestro archivo de configuración “attach to Java Debug (port 5005)”. Una vez seleccionado presionamos este botón y el depurador se conectará con nuestra aplicación.
### Eclipse
* Inicia Eclipse y ve al menú Run, luego elige Debug Configurations.
* Haz clic derecho sobre Remote Java Application  y selecciona New.
* Asigna un nombre a tu configuración de depuración para distinguirla de otras.
* En la configuración de conexión, ingresa el nombre del host o la dirección IP del 	servidor donde se está ejecutando la aplicación, y el tipo de conexión “Socket Attach”.
* Ingresa el número de puerto de depuración que configuraste en los argumentos JVM de tu aplicación Spring Boot.
* Guarda la configuración haciendo clic en Apply.
* Para iniciar la sesión de depuración, selecciona la configuración recién creada desde la lista de configuraciones de depuración y haz clic en el botón Debug.




