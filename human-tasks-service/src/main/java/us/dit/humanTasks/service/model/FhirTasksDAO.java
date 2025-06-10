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

import java.net.URI;
import java.net.URISyntaxException;

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
import ca.uhn.fhir.util.UrlUtil;

import org.hl7.fhir.r5.model.Task;
import org.hl7.fhir.r5.model.Task.TaskInputComponent;
import org.hl7.fhir.r5.model.Task.TaskOutputComponent;
import org.hl7.fhir.r5.model.Task.TaskIntent;
import org.hl7.fhir.r5.model.Task.TaskStatus;


/**
 * @author Marco Antonio Maldonado Orozco
 * @author Juan Manuel Ostos Rabadán
 * @author Isabel Román Martínez
 * @version 19/12/2024
 * Se ha añadido método para obtener el servidor bas ea partir de la tarea
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
	        logger.debug("Cambiando estado de la tarea "+taskId+" en el servidor FHIR a "+taskStatus);
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
	public String completeTask(String url, QuestionnaireResponse questionnaireResponse) throws Exception {
		String serverBase = getServerBase(url);
		String questionnaireResponseId = saveQuestionnaireResponse(serverBase, questionnaireResponse);
		String responseId = null;
	
		logger.info("Se va a finalizar la tarea " + url + " con el cuestionario de respuesta " + questionnaireResponseId);
		
		FhirContext ctx = FhirContext.forR5();
		IGenericClient client = ctx.newRestfulGenericClient(getServerBase(url));
		Task task = getTask(url);
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
        responseId = outcome.getId().toVersionless().getValue();
	
		return responseId;
	}
	/**
	 * Busca el servidor base de la url de la tarea que se pasa, si no es la url de un Task devuelve null
	 * @param url de la tarea
	 * @return servidor base o null si la url no es de una Task
	 */
	public String getServerBase(String url) {
		
		String serverBase=null;
		logger.debug("Pregunto el servidor base del cuestionario con url ",url);		
		int pos=url.indexOf("Task");
		if (pos!=-1) {
			serverBase=url.substring(0,pos);
			logger.debug("Localizado servidor base "+ serverBase);
		}
		return serverBase;		
	}
	/**
	 * Busca una tarea a partir de su url completa
	 * @param url completa de la tarea
	 * @return la tarea localizada
	 */
	public Task getTask(String url) {
		
		// We're connecting to a DSTU1 compliant server in this example
				FhirContext ctx = FhirContext.forR5();
				//Necesito sacar del url por un lado el servidor y por otro el id
				String serverBase;
				Task task=null;
				try {
					logger.debug("Busco task " + url);		
					serverBase=getServerBase(url);					
					String taskId=UrlUtil.parseUrl(url).getResourceId();	
					logger.debug("serverBase: "+serverBase);
					logger.debug("task id "+taskId);
					IGenericClient client = ctx.newRestfulGenericClient(serverBase);			
					task =
					      client.read().resource(Task.class).withId(taskId).execute();
					logger.info("Localizada task " + task.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.debug("Error al recuperar la tarea del servidor");
					e.printStackTrace();
				}
				return task;		
	}

	/**
	 * Persiste en el servidor FHIR la tarea asociada a un id de cuestionario. Método de pruebas usado para los tests.
	 * @param questionnaireId //id del cuestionario ligado a la tarea
	 * @param serverBase //url del servidor fhir
	 * @return la uri de la tarea
	 */
    public String createTask(String serverBase, String questionnaireId) {
        FhirContext ctx = FhirContext.forR5();
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);
        // Crear Task
        Task task = new Task();
        task.setStatus(TaskStatus.READY);
        task.setIntent(TaskIntent.ORDER);
        // Crear entrada (input)
        TaskInputComponent input = new TaskInputComponent();
        input.setType(new CodeableConcept().addCoding(new Coding()
            .setSystem("http://terminology.hl7.org/CodeSystem/task-input-type")
            .setCode("closingQuestionnaire"))
            .setText("Id del cuestionario de cierre de la tarea"));
        input.setValue(new StringType(questionnaireId));
        task.addInput(input);
        // Enviar al servidor FHIR
        MethodOutcome outcome = client.create().resource(task).execute();
        logger.debug("Task creada con ID: " + outcome.getId().toVersionless().getValue());
		return outcome.getId().toVersionless().getValue();
    }

	/**
     * Obtiene la URI del cuestionario respuesta de una tarea a partir de su URI.
	 * @param url //url de la tarea fhir
	 * @return la uri del cuestionario respuesta
     */
    public String getQuestionnaireResponseId(String url) {
		Task task = getTask(url);
        // Buscar en outputs el tipo que contenga "closingQuestionnaireResponse"
        for (TaskOutputComponent output : task.getOutput()) {
            if (output.getType() != null &&
                output.getType().hasCoding() &&
                output.getType().getCodingFirstRep().getCode().equals("closingQuestionnaireResponse")) {

                if (output.getValue() instanceof StringType) {
                    return ((StringType) output.getValue()).getValue();
                }
            }
        }
        // Si no se encuentra
        return null;
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
