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
5. En el script de arranque para windows _launch.bat_ se ha cambiado la sentencia de arranque local, incluyendo opciones _call java -Dorg.kie.server.bypass.auth.user=true -Dorg.kie.server.pwd=consentimientos -Dorg.kie.server.user=consentimientos -jar target\!latestjar!_

## Ejecución
* application.properties está preparado para usar postgres como bbdd por defecto (se ha manenido el original como BU).
* Debe estar creada la BBDD y el usuario en el servidor postgres y después ejecutar (base de datos consentimientos, usuario jbpm). Pero se puede cambiar la configuración de base de datos en el fichero de propiedades
* En windows ejecutar .\launch.bat clean install -Ppostgres
* Se ha añadido una clase para la configuración de las variables del sistema que configuran el servidor kie (o cualquier otra). Es necesario añadir en application.properties la variable con el prefijo system.properties

## Entorno de desarrollo
### Despliegue del entorno
Para seguir desarrollando el servicio de gestión de tareas humanas (human-tasks-service), se recomienda utilizar el repositorio [EntornoDesarrollojBPM](https://github.com/tfg-projects-dit-us/EntornoDesarrollojBPM) para levantar localmente (en contenedores docker) los servicios que dan soporte al desarrollo y verificación del proyecto. Esto podrá a su disposición, localmente, los servicios de:
* FHIR
* Repositorio de Artefactos
* Business Central

### Configuración local de maven
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
Para ejecutar en modo development deberá usar ``launch-dev.bat clean install`` En este caso el fichero de configuración utilizado es ``application-dev.properties``

Para realizar las pruebas y continuar con el desarrollo se necesitan una serie de recursos en FHIR, disponibles en la carpeta [resources](./resources). Puede crearlos uno a uno en una única transacción usando el ``Bundle`` disponible en el fichero ``Bundle4Transaction.json``. 
Puede acceder a localhost:8888 y ejecutar una transacción copiando este Bundle (creará todos los recursos a la vez)

Si desea iniciar una instancia del proceso ConsentRequest puede realizar un GET con su navegador a: ``localhost:8090/test/sendConsentRequest`` Esto viola completamente los principios REST y nunca debe ser utilizado en producción, sólo se realiza para facilitar la verificación y depuración

En ``http://localhost:8090/h2-console/`` puede consultar la base de datos h2 (en memoria) durante la ejecución (mantener login y password por defecto, sa, sa)
