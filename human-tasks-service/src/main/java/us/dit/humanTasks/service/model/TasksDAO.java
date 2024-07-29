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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import us.dit.humanTasks.service.services.kie.KieUtilService;

/**
 * @author Marco Antonio Maldonado Orozco
 */
@Service
public class TasksDAO {

	private static final Logger logger = LogManager.getLogger();
	
	private static final String TASK_URI = "taskURI";
	
	@Autowired
	private KieUtilService kie;

	/**
	 * Find all jBPM tasks assigned and potential for user
	 * @param user
	 * @return List<TaskSummary>
	 */
	public List<TaskSummary> findAllTasks(String user) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Invocando findAllTasks con usuario: "+ user);
		return client.findTasksAssignedAsPotentialOwner(user, 0, Integer.MAX_VALUE);
    }
	
	/**
	 * Find all jBPM assigned tasks for user
	 * @param user
	 * @return List<TaskSummary>
	 */
	public List<TaskSummary> findAssignedTasks(String user) {
		logger.info("Invocando findAssignedTasks con usuario: "+ user);
		List<TaskSummary> allTasks = findAllTasks(user);
		return allTasks.stream().filter(task -> task.getActualOwner() != null).collect(Collectors.toList());
    }
	
	/**
	 * Find all jBPM potential tasks for user
	 * @param user
	 * @return List<TaskSummary>
	 */
	public List<TaskSummary> findPotentialTasks(String user) {
		logger.info("Invocando findPotentialTasks con usuario: "+ user);
		List<TaskSummary> allTasks = findAllTasks(user);
		return allTasks.stream().filter(task -> task.getActualOwner() == null).collect(Collectors.toList());
    }
	
	/**
	 * Claim the jBPM Task with taskId for user
	 * @param taskId
	 * @param user
	 * @param containerId
	 */
	public void claimTask(Long taskId, String user, String containerId) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Reclamar la tarea con id " + taskId + " del contenedor con id " + containerId + " para el usuario " + user);
		client.claimTask(containerId, taskId, user);
	}
	
	/**
	 * Start the jBPM task with taskId for user and return the FHIR Task id associated
	 * @param taskId
	 * @param user
	 * @param containerId
	 * @param processInstanceId
	 * @return String
	 */
	public String startTask(Long taskId, String user, String containerId, Long processInstanceId) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Comenzar la tarea con id " + taskId + " del contenedor con id " + containerId);
		client.startTask(containerId, taskId, user);
		Map<String, Object> inputData = getTaskInputContent(client, taskId, containerId, processInstanceId);
        String taskURI = (String) inputData.get(TASK_URI);
        logger.info("La tarea con id " + taskId + " está relacionada con la tarea fhir con id " + taskURI);
        return taskURI;
	}
	
	/**
	 * Return the FHIR Task id associated to the jBPM Task
	 * @param taskId
	 * @param user
	 * @param containerId
	 * @param processInstanceId
	 * @return
	 */
	public String continueTask(Long taskId, String user, String containerId, Long processInstanceId) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Continuar la tarea con id " + taskId + " del contenedor con id " + containerId);
		Map<String, Object> inputData = getTaskInputContent(client, taskId, containerId, processInstanceId);
        String taskURI = (String) inputData.get(TASK_URI);
        logger.info("La tarea con id " + taskId + " está relacionada con la tarea fhir con id " + taskURI);
        return taskURI;
	}
	
	/**
	 * Reject the jBPM Task with taskId for user
	 * @param taskId
	 * @param user
	 * @param containerId
	 */
	public void rejectTask(Long taskId, String user, String containerId) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Rechazar la tarea con id " + taskId + " del contenedor con id " + containerId + " del usuario " + user);
		client.releaseTask(containerId, taskId, user);
	}
	
	/**
	 * Complete the jBPM task with taskId
	 * @param taskId
	 * @throws Exception
	 */
	public void completeTask(Long taskId) throws Exception {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		TaskInstance taskInstance = client.findTaskById(taskId);
		String containerId = taskInstance.getContainerId();
		String user = taskInstance.getActualOwner();
		logger.info("Completar la tarea con id " + taskId + " del contenedor con id " + containerId + " del usuario " + user);
		client.completeTask(containerId, taskId, user, new HashMap<>());
	}
	
	/**
	 * Finds FHIR task id from jBPM Task inputs.
	 * @param taskId
	 * @param containerId
	 * @param processInstanceId
	 * @return
	 */
	public String getTaskURIFromTaskInputContent(Long taskId, String containerId, Long processInstanceId) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		Map<String, Object> inputData = getTaskInputContent(client, taskId, containerId, processInstanceId);
        String taskURI = (String) inputData.get(TASK_URI);
        return taskURI;
	}
	
	/**
	 * Finds the input content from jBPM Task with taskId
	 * @param client
	 * @param taskId
	 * @param containerId
	 * @param processInstanceId
	 * @return
	 */
	private Map<String, Object> getTaskInputContent(UserTaskServicesClient client, Long taskId, String containerId, Long processInstanceId) {
		ProcessServicesClient processClient = kie.getProcessServicesClient();
		logger.info("Obtenemos las variables de entrada de la tarea " + taskId);
		TaskInstance taskInstance = client.findTaskById(taskId);
		WorkItemInstance workItem = processClient.getWorkItem(containerId, processInstanceId, taskInstance.getWorkItemId());
		Map<String, Object> inputData = workItem.getParameters();
		return inputData;
	}
	
	
	/************************PARA FUTURO******************************/
	/**
	 * Finds potentialTasks ordered by expiratoin date
	 * @param user
	 * @return List<TaskSummary>
	 */
	public List<TaskSummary> findAllPotentialPendingTasksExpirationDateOrdered(String user) {
		Comparator<TaskSummary> comparator = Comparator.nullsLast(
	            Comparator.comparing(TaskSummary::getExpirationTime, Comparator.nullsLast(Comparator.naturalOrder()))
	        );
		return this.findAllTasks(user).stream()
				.sorted(comparator)
				.collect(Collectors.toList());
	}
	
	/**
	 * Finds all pontential tasks ordered by type
	 * @param user
	 * @return List<TaskSummary>
	 */
	public Multimap<String, TaskSummary> findAllPontentialPendingTasksByType(String user) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		Multimap<String, TaskSummary> tasksByType = ArrayListMultimap.create();
		List<TaskSummary> tasksSummary = this.findAllTasks(user);
		Map<String, String> titleByType = new HashMap<>(); //Aquí habría que obtenerlo del fichero de configuración
		for (TaskSummary task : tasksSummary) {
		    Map<String, Object> inputData = client.findTaskById(task.getId()).getInputData(); //Esto es para obtener el ID de la tarea de hapifhir
			String containerId2="human-tasks-management-kjar-1.0.0-SNAPSHOT";
			Map<String, Object> inputData2 = client.getTaskInputContentByTaskId(containerId2, task.getId());
		    logger.info("MAPAAAAA: " + inputData);
		    logger.info("MAPA2: " + inputData2);
			String type = task.getSubject();
			String typeTitle = titleByType.get(type);
			if (typeTitle != null) {
				tasksByType.put(typeTitle, task);
			} else {
				tasksByType.put("noType", task);				
			}
		}
		return tasksByType;
	}
	
//	public List<TaskInstance> findAllPotentialPendingTaskInstances(String user) {
//		List<TaskInstance> taskInstances = new ArrayList<>();
//		UserTaskServicesClient client = kie.getUserTaskServicesClient();
//		List<TaskSummary> tasks = client.findTasksAssignedAsPotentialOwner(user, 0, Integer.MAX_VALUE);
//		for (TaskSummary task : tasks) {
//			logger.info("Asunto y prioridad TaskSummary: " + task.getSubject() + ", " + task.getPriority());
//			TaskInstance taskInstance = client.getTaskInstance("human-tasks-management-kjar-1.0.0-SNAPSHOT", task.getId(), true, true, true);
//			logger.info("Asunto y prioridad TaskInstance: " + taskInstance.getSubject() + ", " + taskInstance.getPriority());
//			taskInstances.add(taskInstance);
//		}
//		return taskInstances;
//    }
	
//	public List<TaskEventInstance> findAllPotentialPendingTaskEventInstances(String user) {
//		List<TaskEventInstance> taskInstances = new ArrayList<>();
//		UserTaskServicesClient client = kie.getUserTaskServicesClient();
//		List<TaskSummary> tasks = client.findTasksAssignedAsPotentialOwner(user, 0, Integer.MAX_VALUE);
//		for (TaskSummary task : tasks) {
//			taskInstances.addAll(client.findTaskEvents("human-tasks-management-kjar-1.0.0-SNAPSHOT", task.getId(), 0, 100));
//		}
//		return taskInstances;
//    }
	
	/**
	 * In case jBPM Task has Json format information in Description field this method extract its and return in Map format.
	 * @param taskDescription
	 * @return Map<String, String>
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
//	Este método es por si en la descripción se le quiere pasar información extra de la tarea en fomra de JSON poder extraerla
//	Por ejemplo:
//	{
//	  "metadata": {
//		 "type": "consulta",
//		 "campoExtra": "info extra",
//		 "prioridad": "99"
//	   }
//	}
	private Map<String, String> extractMetadataJson(String taskDescription) throws JsonMappingException, JsonProcessingException {
        int start = taskDescription.indexOf('{');
        int end = taskDescription.lastIndexOf('}');
        ObjectMapper mapper = new ObjectMapper();
        if (start >= 0 && end > start) {
        	String taskDescriptionSubstring = taskDescription.substring(start, end + 1);
        	Map<String, Object> extractMetadataJson = mapper.readValue(taskDescriptionSubstring, Map.class);
        	logger.info("METADATA MAP" + extractMetadataJson);
        	Map<String, String> metadata = (Map<String, String>) extractMetadataJson.get("metadata");
            logger.info("METADATA" + metadata);
            return metadata;
        }

        return new HashMap<>();
    }
	
	
}
