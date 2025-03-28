# Escenario de pruebas adicional

En este directorio se proporcionan recursos adicionales para la verificación del proyecto utilizando los procesos de gestión de consentimientos disponibles en el repositorio [ConsentFlow-Hub](https://github.com/tfg-projects-dit-us/ConsentFlow-Hub), en lugar de los del kjar adjunto a Healthcare-Tasks

Para realizar las pruebas y continuar con el desarrollo, utilizando este kjar, se necesitan una serie de recursos en FHIR, disponibles en la carpeta resources. Puede crearlos uno a uno en una única transacción usando el Bundle disponible en el fichero Bundle4Transaction.json. Puede acceder a localhost:8888 y ejecutar una transacción copiando este Bundle (creará todos los recursos a la vez)

En http://localhost:8090/h2-console/ puede consultar la base de datos h2 (en memoria) durante la ejecución (mantener login y password por defecto, sa, sa)

# Descripción de los recursos

metaQuestionnaireConsent
Recurso de tipo Questionnaire. MetaCuestionario para la generación de la solicitud de consentimiento. 
El id de este Questionnaire tendrá que ir referenciado como el cuestionario de cierre de la tarea de solicitud del consentimiento 

TaskConsent
Tarea para ejecución del proceso de solicitud y revisión del consentimiento, el id de esta tarea será la entrada al procedimiento padre.
Identificará al médico implicado y a los N pacientes destino

TaskConsent4Doctor
Tarea para la ejecución de la tarea humana del médico, configurar el consentimiento, en la entrada tiene que ir el metacuestionario, en la salida tiene que estar la respuesta al metacuestionario

metaQuestionnaireResponse
Recurso QuestionnaireResponse resultado de la configuración del consentimiento, respuesta al metacuestionario. Debe generarse un questionnario a partir de este

patientQuestionnaireConsent
Recurso de tipo Questionnarie que contiene el cuestionario para el paciente sobre si acepta o no el consentimiento, sólo una pregunta y el resto mostrar el consentimiento solicitado (referencia al metaQuestionnaireResponse). Deberá ir en la entrada de las tareas destinadas a los pacientes

TaskConsent4Patient
n recursos de tipo Task, todos tienen a la entrada la referencia al patientQuestionnaireConsent, y cada uno tendrá en la salida su respuesta, como un QuestionnaireResponse. Si vence el temporizador en la salida se podría indicar algo como "no contestado a tiempo, caducado"

patientQuestionnaireResponse
N Recursos QuestionnaireResponse (uno por cada respuesta de usuario), su referencia irá en la salida de TaskConsent4Patient

