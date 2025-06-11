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

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import org.jbpm.services.api.UserTaskService;

import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
//import org.kie.server.client.KieServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

//import us.dit.humanTasks.service.services.kie.KieUtilService;
import us.dit.humanTasks.service.services.kie.KieUtilFactoryService;

/**
 * @author Marco Antonio Maldonado Orozco
 * @author Juan Manuel Ostos Rabadán
 * @author Isabel Román
 * @version 19/12/2024
 * Hacia el cambio para no usar kieutilservice, por ahora se usa UserTaskService donde es posible
 * Las funciones no usadas se comentan
 */
@Service
public class TasksDAO {

	private static final Logger logger = LogManager.getLogger();
	
	private static final String TASK_URI = "taskURI";
	
	//Se intentará eliminar kie y usar sólo rtDS
	//@Autowired
	//private KieUtilService kie;
	
	@Autowired
	private KieUtilFactoryService kieUFS;

	public Map<Integer,List<TaskSummary>> findAllTasks(String user) {
		Map<Integer,List<TaskSummary>> allTaskMap = new LinkedHashMap<>();;
		//List<TaskSummary> finalTaskList = new ArrayList<>();
		List<String> statusList = Arrays.asList("Reserved", "Completed", "InProgress", "Ready");
		logger.info("Invocando findAllTasks con usuario: "+ user);
		List<UserTaskServicesClient> clientList = kieUFS.getUserTaskClientList();
		Integer serverIndex=0;
		for(UserTaskServicesClient client: clientList){
			List<TaskSummary> clientTaskList = client.findTasksAssignedAsPotentialOwner(user, statusList ,0, Integer.MAX_VALUE);
			allTaskMap.put(serverIndex, clientTaskList);
			serverIndex ++;
		}
		return allTaskMap;
    }

	/**
	 * Find all jBPM assigned tasks for user
	 * @param user
	 * @return Map<Integer, List<TaskSummary>>
	 */
	public Map<Integer, List<TaskSummary>> findAssignedTasks(String user) {
		logger.info("Invocando findAssignedTasks con usuario: " + user);
		Map<Integer, List<TaskSummary>> allTasksMap = findAllTasks(user);
		Map<Integer, List<TaskSummary>> assignedTasksMap = new LinkedHashMap<>();

		for (Map.Entry<Integer, List<TaskSummary>> entry : allTasksMap.entrySet()) {
			List<TaskSummary> filteredTasks = entry.getValue().stream()
				.filter(task -> !"Completed".equals(task.getStatus()))
				.filter(task -> user.equals(task.getActualOwner()))
				.collect(Collectors.toList());

			if (!filteredTasks.isEmpty()) {
				assignedTasksMap.put(entry.getKey(), filteredTasks);
			}
		}
		return assignedTasksMap;
	}
	
	/**
	 * Find all jBPM potential tasks for user
	 * @param user
	 * @return Map<Integer, List<TaskSummary>>
	 */
	public Map<Integer, List<TaskSummary>> findPotentialTasks(String user) {
		logger.info("Invocando findPotentialTasks con usuario: " + user);
		Map<Integer, List<TaskSummary>> allTasksMap = findAllTasks(user);
		Map<Integer, List<TaskSummary>> potentialTasksMap = new LinkedHashMap<>();

		for (Map.Entry<Integer, List<TaskSummary>> entry : allTasksMap.entrySet()) {
			List<TaskSummary> filteredTasks = entry.getValue().stream()
				.filter(task -> task.getActualOwner() == null)
				.collect(Collectors.toList());

			if (!filteredTasks.isEmpty()) {
				potentialTasksMap.put(entry.getKey(), filteredTasks);
			}
		}
		return potentialTasksMap;
	}

	/**
	 * Find all jBPM completed tasks for user
	 * @param user
	 * @return Map<Integer, List<TaskSummary>>
	 */
	public Map<Integer, List<TaskSummary>> findCompletedTasks(String user) {
		logger.info("Invocando findCompletedTasks con usuario: " + user);
		Map<Integer, List<TaskSummary>> allTasksMap = findAllTasks(user);
		Map<Integer, List<TaskSummary>> completedTasksMap = new LinkedHashMap<>();

		for (Map.Entry<Integer, List<TaskSummary>> entry : allTasksMap.entrySet()) {
			List<TaskSummary> filteredTasks = entry.getValue().stream()
				.filter(task -> "Completed".equals(task.getStatus()))
				.filter(task -> user.equals(task.getActualOwner()))
				.collect(Collectors.toList());

			if (!filteredTasks.isEmpty()) {
				completedTasksMap.put(entry.getKey(), filteredTasks);
			}
		}
		return completedTasksMap;
	}

	
	/**
	 * Claim the jBPM Task with taskId for user
	 * @param taskId
	 * @param user
	 * @param containerId
	 * @param serverIndex
	 */
	public void claimTask(Long taskId, String user, String containerId, Integer serverIndex) {
		logger.info("Entrando en reclamar tarea ...");
		UserTaskServicesClient client = kieUFS.getUserTaskClientList().get(serverIndex);
		client.claimTask(containerId, taskId, user);
		logger.info("Reclamada la tarea con id " + taskId + " del contenedor " + containerId + " para el usuario " + user);
	}
	
	/**
	 * Start the jBPM task with taskId for user and return the FHIR Task id associated
	 * @param taskId
	 * @param user
	 * @param containerId
	 * @param processInstanceId
	 * @param serverIndex
	 * @return String
	 */
	public String startTask(Long taskId, String user, String containerId, Long processInstanceId, Integer serverIndex) {
		logger.info("Comenzar la tarea con id " + taskId + " del contenedor con id " + containerId);
		UserTaskServicesClient client = kieUFS.getUserTaskClientList().get(serverIndex);
		client.startTask(containerId,taskId,user);
		Map<String, Object> inputData = client.getTaskInputContentByTaskId(containerId,taskId);
		logger.info("La tarea "+taskId+" tiene como entrada "+inputData);
		/**
		 * Las tareas humanas tienen que tener una entrada TASK_URI en la que se pase la url de la tarea
		 */
		String taskURI= inputData.get(TASK_URI).toString();
		logger.info("La tarea con id " + taskId + " está relacionada con la tarea fhir con id " + taskURI);
		return taskURI;
	}
	
	/**
	 * Return the FHIR Task id associated to the jBPM Task
	 * @param taskId
	 * @param user
	 * @param containerId
	 * @param processInstanceId
	 * @param serverIndex
	 * @return String
	 */
	public String continueTask(Long taskId, String user, String containerId, Long processInstanceId, Integer serverIndex) {
		logger.info("Continuar la tarea con id " + taskId + " del contenedor con id " + containerId);
		UserTaskServicesClient client = kieUFS.getUserTaskClientList().get(serverIndex);
		String taskURI=client.getTaskInputContentByTaskId(containerId,taskId).get(TASK_URI).toString();
		logger.info("La tarea con id " + taskId + " está relacionada con la tarea fhir con id " + taskURI);
		return taskURI;
	}
	
	/**
	 * Reject the jBPM Task with taskId for user
	 * @param taskId
	 * @param user
	 * @param containerId
	 * @param serverIndex
	 */
	public void rejectTask(Long taskId, String user, String containerId, Integer serverIndex) {
		logger.info("Entrando en rechazar tarea ...");
		UserTaskServicesClient client = kieUFS.getUserTaskClientList().get(serverIndex);
		client.releaseTask(containerId, taskId, user);
		logger.info("Rechazada la tarea con id " + taskId + " del contenedor con id " + containerId + " del usuario " + user);
	}
	
	/**
	 * Complete the jBPM task with taskId
	 * @param taskId
	 * @param questionnaireResponseId
	 * @param serverIndex
	 */
	public void completeTask(Long taskId, Integer serverIndex) throws Exception {
		logger.info("Entrando en completar tarea ...");
		UserTaskServicesClient client = kieUFS.getUserTaskClientList().get(serverIndex);
				TaskInstance taskInstance = client.findTaskById(taskId);
				String containerId = taskInstance.getContainerId();
				String user = taskInstance.getActualOwner();
				//We store as a process variable the questionnaireResponseId generated when we complete the task
				Map<String,Object> variables= new HashMap<String,Object>();

				//La fecha de expiración será representativa de la fecha de entrega una vez completada la tarea
				Date completionDate = new Date();
				client.setTaskExpirationDate(containerId, taskId, completionDate);
				client.completeTask(containerId, taskId, user, variables);
				logger.info("Completada la tarea con id " + taskId + " del contenedor con id " + containerId + " del usuario " + user);
	}
	
	/**
	 * Finds FHIR task id from jBPM Task inputs.
	 * @param taskId
	 * @param containerId
	 * @param processInstanceId
	 * @param serverIndex
	 * @return String
	 */
	public String getTaskURIFromTaskInputContent(Long taskId, String containerId, Long processInstanceId, Integer serverIndex) {
		logger.debug("Entrando en getTaskUri con taskId "+taskId+" containerId "+" processInstanceId "+processInstanceId);
		UserTaskServicesClient client = kieUFS.getUserTaskClientList().get(serverIndex);
		String taskURI = client.getTaskInputContentByTaskId(containerId,taskId).get(TASK_URI).toString();   
		logger.debug("TaskURI con el cliente inyectado " + taskURI);
		return taskURI;
	}
	
	/************************PARA FUTURO******************************/
	/**
	 * Find all jBPM tasks completed, assigned and potential for user
	 * @param user
	 * @return Map<Integer, List<TaskSummary>>
	 */
	/*public Map<Integer, List<TaskSummary>> findAllTasks(String user) {
		Map<Integer, List<TaskSummary>> allTaskMap = new LinkedHashMap<>();
		List<String> statusList = Arrays.asList("Reserved", "Completed", "InProgress", "Ready");
		logger.info("Invocando findAllTasks con usuario: " + user);
		List<UserTaskServicesClient> clientList = kieUFS.getUserTaskClientList();
		Integer serverIndex = 0;
		Date now = new Date(); // fecha actual

		for (UserTaskServicesClient client : clientList) {
			List<TaskSummary> clientTaskList = client.findTasksAssignedAsPotentialOwner(user, statusList, 0, Integer.MAX_VALUE);
			
			// Filtrar con fecha de expiración válida o que estén completadas
			List<TaskSummary> filteredTasks = clientTaskList.stream()
				.filter(task -> (task.getExpirationTime()==null || now.before(task.getExpirationTime())) || "Completed".equals(task.getStatus()) )
				.collect(Collectors.toList());

			// Se abortarán de las tareas con fecha de expiracion pasadas y que no estén completadas
			List<TaskSummary> expiredTasks = clientTaskList.stream()
				.filter(task -> !"Completed".equals(task.getStatus()))
				.filter(task -> task.getExpirationTime()!=null) 
				.filter(task -> now.after(task.getExpirationTime()))
				.collect(Collectors.toList());
			if(expiredTasks.size()>0){
				exitTasks(user,client,expiredTasks);
			}

			allTaskMap.put(serverIndex, filteredTasks);
			serverIndex++;
		}
		return allTaskMap;
	}*/
	/**
	 * Exit the jBPM task list when a task expires
	 * @param user
	 * @param client
	 * @param tasks
	 */
	/*private void exitTasks(String user, UserTaskServicesClient client, List<TaskSummary> tasks){
		for (TaskSummary expiredTask : tasks) {
			String containerId = expiredTask.getContainerId();
			Long taskId = expiredTask.getId();
			client.exitTask(containerId, taskId, user);
		}
	}*/
	/**
	 * Finds potentialTasks ordered by expiratoin date
	 * @param user
	 * @return List<TaskSummary>
	 */
	/*public List<TaskSummary> findAllPotentialPendingTasksExpirationDateOrdered(String user) {
		Comparator<TaskSummary> comparator = Comparator.nullsLast(
	            Comparator.comparing(TaskSummary::getExpirationTime, Comparator.nullsLast(Comparator.naturalOrder()))
	        );
		return this.findAllTasks(user).stream()
				.sorted(comparator)
				.collect(Collectors.toList());
	}*/
	 
	/**
	 * TODO: aquí aparece el nombre de contenedor, esto no debería estar aquí....
	 * Finds all potential tasks ordered by type
	 * @param user
	 * @return List<TaskSummary>
	 */
	/*public Multimap<String, TaskSummary> findAllPontentialPendingTasksByType(String user) {
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
	*/
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
	/*
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
	*/
}
