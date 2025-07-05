/**
*  This file is part of Healthcare Tasks: Human task management in healthcare contexts.
*  Copyright (C) 2025  Universidad de Sevilla/Departamento de Ingeniería Telemática
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

import java.util.List;

import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.Questionnaire;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.UrlUtil;
import us.dit.humanTasks.model.TaskInputTypes;

import org.hl7.fhir.r5.model.Task;
import org.hl7.fhir.r5.model.Task.TaskInputComponent;


/**
 * @author Marco Antonio Maldonado Orozco
 * @author Isabel Román
 * @version 19/12/2024
 * 
 * En esta versión se ha cambiado de nombre la clase, para ser coherente con el dao de tarea
 * Además se prepara que la tarea tenga identificado el cuestionario con la url completa o sólo con el id en el servidor base de la propia tarea
 */
@Service
public class FhirQuestionnaireDAO {
	private static final Logger logger = LogManager.getLogger();
	@Autowired
	us.dit.humanTasks.service.model.FhirTasksDAO taskDao;
	/**
	 * Find Questionnaire resource from FHIR Task id.
	 * @param taskId
	 * @param serverBase
	 * @return Questionnaire
	 */
	public Questionnaire getQuestionnaireFromTask(String taskUrl) {
		Questionnaire myQuestionnaire=null;
		try {
			logger.info("Buscar taskUrl "+taskUrl+" para localizar cuestionario");
		
			String questionnaireId = "";
			Task myTask=taskDao.getTask(taskUrl);
			List<TaskInputComponent> myTaskInputs = myTask.getInput();
			logger.debug("La tarea tiene "+myTaskInputs.size()+" entradas y son "+myTaskInputs);
			for (TaskInputComponent taskInput : myTaskInputs) {
				if (TaskInputTypes.CLOSINGQUESTIONNAIRE.name().equals(taskInput.getType().getCodingFirstRep().getCode().toUpperCase())) {
					if (taskInput.hasValue() && taskInput.getValue() instanceof StringType) {
			            StringType stringValue = (StringType) taskInput.getValue();
			            questionnaireId = stringValue.getValue();
			            logger.debug("LOCALIZADO cuestionario con id "+questionnaireId);
			            break;
			        }
				}
			}
			if (!questionnaireId.isEmpty()) {
				//Si el cuestionario se identifica con una uri completa se usa el servidor base que indique, pero si se indica sólo el id se usa el servidor base de la tarea
				String serverBase=getServerBase(questionnaireId);
				if (serverBase==null) {
						serverBase=taskDao.getServerBase(taskUrl);
				}
				logger.info("Busco cuestionario " + questionnaireId + " en " + serverBase);
				myQuestionnaire = getQuestionnaire(serverBase,questionnaireId);				      
				logger.info("Localizado cuestionario con ID " + myQuestionnaire.getId() + " Y titulo " + myQuestionnaire.getTitle());
			} else {
				logger.error("No se ha localizado en la tarea " + taskUrl + " ningún input de tipo 'closingQuestionnaire'");
			}
		} catch (Exception e) {
            logger.error("Se ha producido un error al obtener el cuestionario: " + e.getMessage(), e);
        }
		return myQuestionnaire;
	}
	

	/**
	 * Busca un cuestionario a partir de su url completa
	 * @param url completa del cuestionario
	 * @return el cuestionario
	 */
	public Questionnaire getQuestionnaire(String url) {	
				FhirContext ctx = FhirContext.forR5();
				//Necesito sacar del url por un lado el servidor y por otro el id
				String serverBase;
				Questionnaire questionnaire=null;
				try {
					logger.debug("Busco questionnaire " + url);		
					serverBase=getServerBase(url);					
					String questionnaireId=UrlUtil.parseUrl(url).getResourceId();	
					logger.debug("serverBase: "+serverBase);
					logger.debug("questionnaire id "+questionnaireId);
					IGenericClient client = ctx.newRestfulGenericClient(serverBase);			
					questionnaire =
					      client.read().resource(Questionnaire.class).withId(questionnaireId).execute();
					logger.info("Localizado questionnaire " + questionnaire.getId());
				} catch (Exception e) {
			
					logger.debug("Error al recuperar questionnaire del servidor");
					e.printStackTrace();
				}
				return questionnaire;		
	}
	
	/**
	 * Busca un cuestionario por id y servidor base
	 * @param serveBase servidor
	 * @param id Id del cuestionario en el servidor
	 * @return el cuestionario
	 */
	public Questionnaire getQuestionnaire(String serverBase, String questionnaireId) {
		logger.debug("Busco questionnaire con server base "+serverBase+" e id "+questionnaireId);
		
		// We're connecting to a DSTU1 compliant server in this example
				FhirContext ctx = FhirContext.forR5();
				
				Questionnaire questionnaire=null;
				try {					
					IGenericClient client = ctx.newRestfulGenericClient(serverBase);			
					questionnaire =
					      client.read().resource(Questionnaire.class).withId(questionnaireId).execute();
					logger.info("Localizado questionnaire " + questionnaire.getId());
				} catch (Exception e) {
				
					logger.debug("Error al recuperar questionnaire del servidor");
					e.printStackTrace();
				}
				return questionnaire;		
	}

	/**
	 * Busca el servidor base de la url del cuestionario que se pasa, si no es la url de un Questionnaire devuelve null
	 * @param url del questionnaire
	 * @return servidor base o null si la url no es de un Questionnaire
	 */
	public String getServerBase(String url) {
		
		String serverBase=null;
		logger.debug("Pregunto el servidor base del cuestionario con url ",url);		
		int pos=url.indexOf("Questionnaire");
		if (pos!=-1) {
			serverBase=url.substring(0,pos);
			logger.debug("Localizado servidor base "+ serverBase);
		}
		return serverBase;		
	}
}
