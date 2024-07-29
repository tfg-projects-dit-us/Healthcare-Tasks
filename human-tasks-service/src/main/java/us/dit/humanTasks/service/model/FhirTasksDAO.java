/**
*  This file is part of Healthcare Tasks: Human task management in healthcare contexts.
*  Copyright (C) 2024  Universidad de Sevilla/Departamento de Ingeniería Telemática
*
*  Healthcare Tasks is free software: you can redistribute it and/or
*  modify it under the terms of the GNU General Public License as published
*  by the Free Software Foundation, either version 3 of the License, or (at
*  your option) any later version.
*
*  Healthcare Tasks is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
*  Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with Healthcare Tasks. If not, see <https://www.gnu.org/licenses/>.
**/
package us.dit.humanTasks.service.model;

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
	
	/**
	 * Update the FHIR task status
	 * @param serverBase
	 * @param taskId
	 * @param taskStatus
	 * @return String
	 */
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
	
	/**
	 * Complete the FHIR task from id and attaching a QuestionnaireResponse
	 * @param serverBase
	 * @param taskId
	 * @param questionnaireResponse
	 * @return String
	 * @throws Exception
	 */
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
	
	/**
	 * Persist in FHIR server the QuestionnaireResponse built from Questionnaire answers
	 * @param serverBase
	 * @param questionnaireResponse
	 * @return
	 */
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
