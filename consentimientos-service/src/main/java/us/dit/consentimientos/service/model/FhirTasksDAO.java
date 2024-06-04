/**
 * 
 */
package us.dit.consentimientos.service.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.QuestionnaireResponse;
import org.hl7.fhir.r5.model.StringType;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;

import org.hl7.fhir.r5.model.Task;
import org.hl7.fhir.r5.model.Task.TaskOutputComponent;


/**
 * @author Marco Antonio Maldonado Orozco
 */
@Service
public class FhirTasksDAO {
	
	private static final Logger logger = LogManager.getLogger();
	
	public String updateTaskStatus(String serverBase, String taskId, Task.TaskStatus taskStatus) {
		String responseId = null;
		try {
			FhirContext ctx = FhirContext.forR5();
			IGenericClient client = ctx.newRestfulGenericClient(serverBase);
			
			Task task = client.read().resource(Task.class).withId(taskId).execute();
	        task.setStatus(taskStatus);

	        MethodOutcome outcome = client.update().resource(task).execute();
	        responseId = outcome.getId().getValueAsString();
			} catch (FhirClientConnectionException e) {
	            e.printStackTrace();
	        }
		
		return responseId;
	}
	
	public String completeTask(String serverBase, String taskId, QuestionnaireResponse questionnaireResponse) throws Exception {
		String questionnaireResponseId = saveQuestionnaireResponse(serverBase, questionnaireResponse);
		String responseId = null;
		logger.info("Se va a finalizar la tarea " + taskId + " con el cuestionario de respuesta " + questionnaireResponseId);
		
		FhirContext ctx = FhirContext.forR5();
		IGenericClient client = ctx.newRestfulGenericClient(serverBase);
		
		Task task = client.read().resource(Task.class).withId(taskId).execute();
        task.setStatus(Task.TaskStatus.COMPLETED);
        
        TaskOutputComponent outputComponent = new TaskOutputComponent();
        CodeableConcept type = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode("closingQuestionnaireResponse");
        type.addCoding(coding);
        type.setText("Id de la respuesta al cuestionario de cierre de la tarea");
        outputComponent.setType(type);

        outputComponent.setValue(new StringType(questionnaireResponseId));

        task.addOutput(outputComponent);

        MethodOutcome outcome = client.update().resource(task).execute();
        responseId = outcome.getId().getValueAsString();
	
		return responseId;
	}
	
	private String saveQuestionnaireResponse(String serverBase, QuestionnaireResponse questionnaireResponse) {
		String responseId = null;
		try {
		FhirContext ctx = FhirContext.forR5();
		IGenericClient client = ctx.newRestfulGenericClient(serverBase);
		
		String resourceString = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(questionnaireResponse);
        logger.info("Sending QuestionnaireResponse: \n" + resourceString);
        
		MethodOutcome outcome = client.create()
                .resource(questionnaireResponse)
                .execute();
	   
		responseId = outcome.getId().getValueAsString();
		} catch (FhirClientConnectionException e) {
            e.printStackTrace();
        }
	    logger.info("Id del QuestionnaireResponse persistido " + responseId);
		return responseId;	
	}

}
