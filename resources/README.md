# Recursos para la verificación de Healthcare Tasks

En este paquete encontrará diversos recursos para facilitarle la tarea de configuración del entorno de desarrollo y pruebas

## Servidores de respaldo:

### FHIR

Esta aplicación utiliza un servidor FHIR para persistir y consultar los cuestionarios y tareas FHIR, maneja los recursos `Questionnaire`, `Task` y `QuestionnaireResponse`

El servidor FHIR de respaldo se configura en el archivo `application.properties` del servicio. Por defecto se utiliza el <a href="https://hapi.fhir.org/baseR5/swagger-ui/">servidor de test, versión R5,</a> de Hapi Fhir, iniciativa respaldada por <a href="https://www.smiledigitalhealth.com/">Smile Digital</a> Si ha desplegado el entorno de desarrollo también puede conectarse al servidor local FHIR http://localhost:8888/fhir.

El servidor aloja los recursos `Task` asociados a una tarea humana en el proceso. La versión actual de los procesos de prueba genera estos recursos. Sin embargo, La versión actual de los procesos no generan los recursos `Questionnaire` que representan los datos solicitados al usuario para cerrar la tarea, por lo que también deben estar previamente alojados en el servidor.

El json proporcionado en este paquete (`Questionnaire.json`) es un ejemplo de este recurso, y debe estar almacenado previamente en el servidor FHIR. Para ello deberá:

* Abrir en el navegador el servidor de test (R5) y seleccionar el recurso `Questionnaire`. Elegir en la operación Post la opción Try y adjuntar el questionnario proporcionado. La respuesta devolverá el recurso creado en el servidor, donde podemos encontrar su identificador ("id": "765063" en el ejemplo mostrado).

<img src="https://github.com/tfg-projects-dit-us/Healthcare-Tasks/blob/master/resources/img/CrearQuestionnaire.jpg" width="500" />

<img src="https://github.com/tfg-projects-dit-us/Healthcare-Tasks/blob/master/resources/img/RespuestaCreacionQuestionnaire.jpg" width="500" />


* Configurar el fichero application.properties y en la propiedad test.questionnaireid poner el identificador del cuestionario que acaba de crear en el paso anterior

En futuras versiones de esta solución este procedimiento será automático y no será necesaria la configuración previa.

Tras realizar pruebas se creará la tarea y se observará que se ha añadido la respuesta del cuestionario de cierre en las salidas.

### Base de datos

La información de seguimiento de los procesos se almacena en una base de datos. La configuración de la misma se realiza también en el fichero `application.properties`.

En la configuración proporcionada se utiliza una base de datos local postgresql, de nombre `ht`

```
#data source configuration
spring.datasource.username=jbpm
spring.datasource.password=jbpm.2.DDBB*
spring.datasource.url=jdbc:postgresql://localhost:5432/ht
spring.datasource.driver-class-name=org.postgresql.xa.PGXADataSource

#hibernate configuration
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.show_sql=false
spring.jpa.properties.hibernate.hbm2ddl.auto=update
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
```

Es necesario que la base de datos esté creada, y configurar adecuadamente este fichero para incluir el usuario y password de la misma.

Se presenta también la configuración necesaria para JPA, api utilizada para la persistencia, cuando se utiliza postgresql.

## Procesos de test

Para verificar la aplicación será necesario poder instanciar procesos que contengan tareas humanas. Para ello se ha utilizado Business Central, de KIE, para crear el proyecto “human-tasks
management", incluido en esta distribución en el paquete human-tasks-management-kjar, donde se definen una serie de procesos simples entre los que destacan principalmente dos:
* <a href="https://github.com/tfg-projects-dit-us/Healthcare-Tasks/blob/master/human-tasks-management-kjar/src/main/resources/HumanTasksManagement.TareaARol-svg.svg">TareaARole</a>: que crea una tarea ligada al rol webadmin, de modo que cualquier usuario con este rol podrá reclamarla y ejecutarla
* <a href="https://github.com/tfg-projects-dit-us/Healthcare-Tasks/blob/master/human-tasks-management-kjar/src/main/resources/HumanTasksManagement.TareaAUsuario-svg.svg">TareaAUsuario</a>: que crea una tarea asignada al usuario que cree la instancia

Dado que el servicio está desarrollado como una aplicación de negocios con un motor kie embebido, al arrancar la aplicación se cargará un contenedor con estos procesos disponibles. La información necesaria para realizar los tests (id del contenedor y de los procesos) se configura en el fichero de propiedades de la aplicación:

```
#nombre del contenedor desplegado en el servidor kie
test.containerid=human-tasks-management-kjar-1.0.0-SNAPSHOT
#nombres de procesos que contienes una tarea asignada al role wbadmin
test.roleprocessWithoutTimer=HumanTasksManagement.TareaARoleSinTimer
test.roleprocessHigh=HumanTasksManagement.TareaARolUrgente
test.roleprocessMedium=HumanTasksManagement.TareaARolMedia
test.roleprocessLow=HumanTasksManagement.TareaARolLeve
#nombre del proceso que asigna una tarea al usuario que lo inicia
test.userprocess=human-tasks-management.TareaAUsuario
```
## Instanciar los procesos de test
Para poder crear instancias de estos procesos la aplicación publica dos endpoint que atienden peticiones get, están definidos en la clase `TestController`

* `/test/initTareaARol?serverIndex=..`: arranca en el servidor indicado una instancia del proceso `TareaARoleLeve`
* `/test/initTareaAUsuario?serverIndex=..`: arranca en el servidor indicado una instancia del proceso `TareaAUsuario`
* `/test/initTareasARolMuestra?serverIndex=..`: arranca en el servidor indicado instancias de los procesos `TareaARoleLeve`, `TareaARoleMedia`, `TareaARoleUrgente`
* `/test/initTareaARolSinTimer?serverIndex=..`: arranca en el servidor indicado una instancia del proceso `TareaARoleSinTimer`

Estos endpoint son sólo para facilitar las pruebas y deberán desaparecer en la versión en producción.

## Cierre de sesión

Durante las pruebas puede necesitar reiniciar la sesión para cambiar de usuario, en el enlace http://localhost:8090/logout puede cerrar la sesión para iniciar una nueva con otro usuario

