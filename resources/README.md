# Recursos para la verificación de Healthcare Tasks

En este paquete encontrará diversos recursos para facilitarle la tarea de configuración del entorno de desarrollo y pruebas

## Servidores de respaldo: FHIR

Esta aplicación utiliza un servidor FHIR para persistir y consultar los cuestionarios y tareas FHIR, maneja los recursos `Questionnaire`, `Task` y `QuestionnaireResponse`

El servidor FHIR de respaldo se configura en el archivo application.properties del servicio. Por defecto se utiliza el <a href="https://hapi.fhir.org/baseR5/swagger-ui/">servidor de test, versión R5,</a> de Hapi Fhir, iniciativa respaldada por <a href="https://www.smiledigitalhealth.com/">Smile Digital</a>  

El servidor aloja los recursos `Task` asociados a una tarea humana en el proceso. La versión actual de los procesos de prueba no genera estos recursos, por los que deben encontrarse previamente en el servidor. Del mismo modo la versión actual de los procesos no generan los recursos `Questionnaire` que representan los datos solicitados al usuario para cerrar la tarea, por lo que también deben estar previamente alojados en el servidor.

Los json proporcionados en este paquete (`Questionnaire.json` y `Task.json`) son ejemplos de estos recursos, y deben estar almacenados previamente en el servidor FHIR. Para ello deberá:

* Abrir en el navegador el servidor de test (R5) y seleccionar el recurso `Questionnaire`. Elegir en la operación Post la opción Try y adjuntar el questionnario proporcionado. La respuesta devolverá el recurso creado en el servidor, donde podemos encontrar su identificador ("id": "765063" en el ejemplo mostrado).

* A continuación repetir la operación para el recurso Task. El fichero json proporcionado debe editarse para que el id del cuestionario de cierre sea el que se acaba de crear en el paso anterior. Una vez ejecutado el post el servidor devuelve el recurso creado, donde podemos encontrar su identificador ("id": "765064" en el ejemplo presentado)

* Configurar el fichero application.properties y en la propiedad test.taskid poner el identificador de la tarea que acaba de crear en el paso anterior

En futuras versiones de esta solución este procedimiento será automático y no será necesaria la configuración previa.

Tras realizar pruebas podrá recuperarse la tarea y se observará que se ha añadido la respuesta del cuestionario de cierre en las salidas. Es necesario tener en cuenta que dado que todas las tareas que se crean en las pruebas usan el mismo recurso el campo de salida de la tarea irá acumulando referencias a respuestas. Lógicamente este comportamiento cambiará en próximas versiones.

Puede usar el identificador de tarea actualmente configurado, siempre que en el backend no se haya realizado una limpieza tras la publicación de este documento de ayuda.

## Procesos de test

Para verificar la aplicación será necesario poder instanciar procesos que contengan tareas humanas. Para ello se ha utilizado Business Central, de KIE, para crear el proyecto “human-tasks
management", incluido en esta distribución en el paquete human-tasks-management-kjar, donde se definen una serie de procesos simples entre los que destacan principalmente dos:
* <a href="https://github.com/tfg-projects-dit-us/Healthcare-Tasks/blob/master/human-tasks-management-kjar/src/main/resources/HumanTasksManagement.TareaARol-svg.svg">TareaARole</a>: que crea una tarea ligada al rol webadmin, de modo que cualquier usuario con este rol podrá reclamarla y ejecutarla
* <a href="https://github.com/tfg-projects-dit-us/Healthcare-Tasks/blob/master/human-tasks-management-kjar/src/main/resources/HumanTasksManagement.TareaAUsuario-svg.svg">TareaAUsuario</a>: que crea una tarea asignada al usuario que cree la instancia

Dado que el servicio está desarrollado como una aplicación de negocios con un motor kie embebido, en el que al arrancar la aplicación se cargará un contenedor con estos procesos disponibles. La información necesaria para realizar los tests (id del contenedor y de los procesos) se hace en el fichero de propiedades de la aplicación:

#nombre del contenedor desplegado en el servidor kie
test.containerid=human-tasks-management-kjar-1.0.0-SNAPSHOT
#nombre del proceso que contiene una tarea asignada al role wbadmin
test.roleprocess=HumanTasksManagement.TareaARol
#nombre del proceso que asigna una tarea al usuario que lo inicia
test.userprocess=human-tasks-management.TareaAUsuario

## Instanciar los procesos de test
Para poder crear instancias de estos procesos la aplicación publica dos endpoint que atienden peticiones get, están definidos en la clase `TasksController`

* /tasks/initTareaARole: arranca una instancia del proceso TareaARole
* /tasks/initTareaAUsuario: arranca una isntancia del proceso TareaAUsuario

Estos endpoint son sólo para facilitar las pruebas y deberán desaparecer en la versión en producción.
TODO: crear una clase controladora específica para las pruebas, y que no estén dentro del controlador de tareas

## Cierre de sesión

Durante las pruebas puede necesitar reiniciar la sesión para cambiar de usuario, en el enlace http://localhost:8090/logout puede cerrar la sesión para iniciar una nueva con otro usuario

