/**
 * 
 */
package us.dit.consentimientos.service.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.StringType;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import us.dit.consentimientos.model.TaskInputTypes;

import org.hl7.fhir.r5.model.Task;
import org.hl7.fhir.r5.model.Task.TaskInputComponent;


/**
 * @author Marco Antonio Maldonado Orozco
 */
@Service
public class QuestionnaireDAO {
	private static final Logger logger = LogManager.getLogger();
	
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
