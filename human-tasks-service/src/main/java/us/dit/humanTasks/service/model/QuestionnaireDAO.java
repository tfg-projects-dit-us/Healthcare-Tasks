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

import java.util.List;

import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.StringType;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import us.dit.humanTasks.model.TaskInputTypes;

import org.hl7.fhir.r5.model.Task;
import org.hl7.fhir.r5.model.Task.TaskInputComponent;


/**
 * @author Marco Antonio Maldonado Orozco
 */
@Service
public class QuestionnaireDAO {
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * Find Questionnaire resource from FHIR Task id.
	 * @param taskId
	 * @param serverBase
	 * @return Questionnaire
	 */
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
