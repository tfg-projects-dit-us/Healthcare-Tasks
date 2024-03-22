package us.dit.consentimientos.service.services.fhir;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.QuestionnaireResponse;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.Task;
import org.hl7.fhir.r5.model.Task.TaskInputComponent;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import us.dit.consentimientos.model.TaskInputTypes;

@Service
public class FhirClient {
	private static final Logger logger = LogManager.getLogger();
	public Questionnaire getQuestionnaire(String serverBase,String questionnaireId) {
		// We're connecting to a DSTU1 compliant server in this example
		FhirContext ctx = FhirContext.forR5();
		IGenericClient client = ctx.newRestfulGenericClient(serverBase);
		logger.info("Busco cuestionario " + questionnaireId+" en "+serverBase);
		Questionnaire questionnaire =
		      client.read().resource(Questionnaire.class).withId(questionnaireId).execute();

		

		logger.info("Localizado cuestionario " + questionnaire.getId());
		return questionnaire;

	}
	public String saveQuestionnaireResponse(String serverBase,QuestionnaireResponse resp) {
		FhirContext ctx = FhirContext.forR5();
		IGenericClient client = ctx.newRestfulGenericClient(serverBase);
	    MethodOutcome outcome = client.create()
		      .resource(resp)
		      .prettyPrint()
		      .encodedJson()
		      .execute();

		// The MethodOutcome object will contain information about the
		// response from the server, including the ID of the created
		// resource, the OperationOutcome response, etc. (assuming that
		// any of these things were provided by the server! They may not
		// always be)
	   
	    String respId=outcome.getId().getValueAsString();
	    logger.info("Id del recurso persistido "+respId);
		return respId;	
	}
	
	public Questionnaire getQuestionnaireFromTask(String taskId, String serverBase) {
		try {
			logger.info("Buscar task por Id para buscar el questionnaire a partir de la task");
			FhirContext ctx = FhirContext.forR5();
			IGenericClient client = ctx.newRestfulGenericClient(serverBase);
			String questionnaireId = "";
			logger.info("Busco la task " + taskId + " en " + serverBase);
			Task myTask = client.read().resource(Task.class).withId(taskId).execute();
			List<TaskInputComponent> myTaskInputs = myTask.getInput();
			for (TaskInputComponent taskInput : myTaskInputs) {
				if (TaskInputTypes.CLOSINGQUESTIONNAIRE.name().equals(taskInput.getType().getCodingFirstRep().getCode().toUpperCase())) {
					if (taskInput.hasValue() && taskInput.getValue() instanceof StringType) {
			            StringType stringValue = (StringType) taskInput.getValue();
			            questionnaireId = stringValue.getValue();
			            break;
			        }
				}
			}
			if (!questionnaireId.isEmpty()) {
				logger.info("Busco cuestionario " + questionnaireId + " en " + serverBase);
				Questionnaire myQuestionnaire =
				      client.read().resource(Questionnaire.class).withId(questionnaireId).execute();
				logger.info("Localizado cuestionario con ID " + myQuestionnaire.getId() + " Y titulo " + myQuestionnaire.getTitle());
				return myQuestionnaire;
			} else {
				logger.error("No se ha localizado en la tarea " + taskId + " ningún input de tipo 'closingQuestionnaire'");
				return null;
			}
		} catch (Exception e) {
            logger.error("Se ha producido un error al obtener el cuestionario: " + e.getMessage(), e);
            return null;
        }
	}

}
