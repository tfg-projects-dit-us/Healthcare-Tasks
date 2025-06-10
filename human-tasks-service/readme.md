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
* Es necesario analizar el fichero xxxx.properties del directorio resources para comprender la configuración de la aplicación Springboot
* application.postgres se ha editado para incluir como bbdd por defecto postgres (el orginal se ha mantenido con la extensión BU). Previo a la ejecución debe estar creada la BBDD y el usuario en el servidor postgres y después ejecutar. 
* Se necesita también un servidor FHIR de respaldo, cuya dirección también se configura en este fichero.
* En windows ejecutar .\launch.bat clean install -Ppostgres
* Se ha añadido una clase application.properties para la configuración de las variables del sistema que configuran los servidores kie (o cualquier otra propiedad). Es necesario añadir en application.properties la variable con el prefijo system.properties.
* La aplicación puede conectarse a varios servidores kie que debe configurar previamente en el fichero anterior. Por defecto se levantará un servidor kie embebido al arrancar la aplicación accesible mediante la url http://localhost:8090/rest/server . Si desea probar con dos servidores simultáneamente, debe poner en marcha otro servidor levantando localmente el servicio de Business Bentral desde el repositorio [EntornoDesarrollojBPM], importar el kjar del proyecto e implementarlo en el servidor kie "sample-server" que proporciona business-central por defecto. Este segundo servidor debería ser accesible desde la url http://localhost:8080/kie-server/services/rest/server .

## Entorno de desarrollo
### Despliegue del entorno
Para seguir desarrollando el servicio de gestión de tareas humanas (human-tasks-service), se recomienda utilizar el repositorio [EntornoDesarrollojBPM](https://github.com/tfg-projects-dit-us/EntornoDesarrollojBPM) para levantar localmente (en contenedores docker) los servicios que dan soporte al desarrollo y verificación del proyecto. Esto podrá a su disposición, localmente, los servicios de:
* FHIR
* Business Central
* Repositorio de Artefactos (opcional)

### Configuración local de maven si se utiliza repositorio de artefactos
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

### Verificación
Para ejecutar en modo development deberá usar ``launch-dev.bat clean install`` En este caso el fichero de configuración utilizado es ``application-dev.properties``. Es recomendable mirar que su variable de entorno JAVA_HOME se encuentre bien configurada conforme a la de su dispositivo en ``launch-dev.bat``, que los contenedores Docker desplegados en [EntornoDesarrollojBPM] estén funcionando, y haber configurado el usuario con nombre y contraseña "controllerUser" y con rol "rest-all" en Business Central".

Al ejecutar el modo developer, este abrirá el puerto 5005 para escuchar. Será necesario conectarse a este puerto para ejecutar el modo debug. Se proporciona el archivo ``launch.json`` para ejecutar en modo debug (IDE VisualStudioCode).

Una vez tenga el modo developer corriendo en su máquina será necesario crear un contenedor kie en Business Central dentro del servidor Docker. Para ello puede importar su proyecto desde el repositorio GitHub ``https://github.com/tfg-projects-dit-us/Healthcare-Tasks/``y posteriormente debe implementarlo en el servidor pulsando el botón implementar.

En ``http://localhost:8090/h2-console/`` puede consultar la base de datos h2 (en memoria) durante la ejecución (mantener login y password por defecto, sa, sa).

Para realizar las pruebas y continuar con el desarrollo podría necesitar una serie de recursos en FHIR, disponibles en la carpeta [resources](../resources). En este directorio puede encontrar información adicional como soporte a la verificación.





