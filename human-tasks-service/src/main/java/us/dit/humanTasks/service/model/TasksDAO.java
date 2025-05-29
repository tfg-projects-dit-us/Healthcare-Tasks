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

import us.dit.humanTasks.service.services.kie.KieServerFactoryService;
//import us.dit.humanTasks.service.services.kie.KieUtilService;

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

	private static final String QUESTIONNAIRE_RESPONSE_URI = "questionnaireResponseURI";
	
	//Se intentará eliminar kie y usar sólo rtDS
	//@Autowired
	//private KieUtilService kie;
	
	@Autowired
	private KieServerFactoryService kieSFS;

	/**
	 * Find all jBPM tasks completed, assigned and potential for user
	 * @param user
	 * @return List<TaskSummary>
	 */
	public List<TaskSummary> findAllTasks(String user) {
		List<TaskSummary> finalTaskList = new ArrayList<>();
		List<String> statusList = Arrays.asList("Reserved", "Completed", "InProgress", "Ready");
		logger.info("Invocando findAllTasks con usuario: "+ user);
		List<UserTaskServicesClient> clientList = kieSFS.getTaskClientList();
		for(UserTaskServicesClient client: clientList){
			List<TaskSummary> clientTaskList = client.findTasksAssignedAsPotentialOwner(user, statusList ,0, Integer.MAX_VALUE);
			finalTaskList.addAll(clientTaskList);
		}
		return finalTaskList;
    }
	/**
	 * Find all jBPM assigned tasks for user
	 * @param user
	 * @return List<TaskSummary>
	 */
	public List<TaskSummary> findAssignedTasks(String user) {
		logger.info("Invocando findAssignedTasks con usuario: "+ user);
		List<TaskSummary> allTasks = findAllTasks(user);
	    return allTasks.stream()
			.filter(task -> !"Completed".equals(task.getStatus()))
			.filter(task -> user.equals(task.getActualOwner()))
			.collect(Collectors.toList());
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
	 * Find all jBPM completed tasks for user
	 * @param user
	 * @return List<TaskSummary>
	 */
	public List<TaskSummary> findCompletedTasks(String user) {
		logger.info("Invocando findCompletedTasks con usuario: "+ user);
		List<TaskSummary> allTasks = findAllTasks(user);
		return allTasks.stream()
		    .filter(task -> "Completed".equals(task.getStatus()))
        	.filter(task -> user.equals(task.getActualOwner()))
        	.collect(Collectors.toList());
    }
	
	/**
	 * Claim the jBPM Task with taskId for user
	 * @param taskId
	 * @param user
	 * @param containerId
	 */
	public void claimTask(Long taskId, String user, String containerId) {
	logger.info("Entrando en reclamar tarea ...");
	List<UserTaskServicesClient> clientList = kieSFS.getTaskClientList();
	for (UserTaskServicesClient client : clientList) {
		try {
			client.claimTask(containerId, taskId, user);
			logger.info("Reclamada la tarea con id " + taskId + " del contenedor " + containerId + " para el usuario " + user);
			return; // Salimos si se logra reclamar
		} catch (Exception e) {
		}
    }
		/*UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Reclamar la tarea con id " + taskId + " del contenedor con id " + containerId + " para el usuario " + user);
		client.claimTask(containerId, taskId, user);*/
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
		logger.info("Comenzar la tarea con id " + taskId + " del contenedor con id " + containerId);
		List<UserTaskServicesClient> clientList = kieSFS.getTaskClientList();
		for (UserTaskServicesClient client : clientList) {
			try {
				client.startTask(containerId,taskId,user);
				Map<String, Object> inputData = client.getTaskInputContentByTaskId(containerId,taskId);
				logger.info("La tarea "+taskId+" tiene como entrada "+inputData);
				/**
				 * Las tareas humanas tienen que tener una entrada TASK_URI en la que se pase la url de la tarea
				 */
				String taskURI= inputData.get(TASK_URI).toString();
				logger.info("La tarea con id " + taskId + " está relacionada con la tarea fhir con id " + taskURI);
				return taskURI;
			} catch (Exception e) {
			}
		}
		return null;
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
		logger.info("Continuar la tarea con id " + taskId + " del contenedor con id " + containerId);
		List<UserTaskServicesClient> clientList = kieSFS.getTaskClientList();
		for (UserTaskServicesClient client : clientList) {
			try {
				String taskURI=client.getTaskInputContentByTaskId(containerId,taskId).get(TASK_URI).toString();
				logger.info("La tarea con id " + taskId + " está relacionada con la tarea fhir con id " + taskURI);
        		return taskURI;
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	/**
	 * Reject the jBPM Task with taskId for user
	 * @param taskId
	 * @param user
	 * @param containerId
	 */
	public void rejectTask(Long taskId, String user, String containerId) {
		/*UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Rechazar la tarea con id " + taskId + " del contenedor con id " + containerId + " del usuario " + user);
		client.releaseTask(containerId, taskId, user);*/
		logger.info("Entrando en rechazar tarea ...");
		List<UserTaskServicesClient> clientList = kieSFS.getTaskClientList();
		for (UserTaskServicesClient client : clientList) {
			try {
				client.releaseTask(containerId, taskId, user);
				logger.info("Rechazada la tarea con id " + taskId + " del contenedor con id " + containerId + " del usuario " + user);
				return;
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * Complete the jBPM task with taskId
	 * @param taskId
	 * @throws Exception
	 */
	public void completeTask(Long taskId, String questionnaireResponseId) throws Exception {
		logger.info("Entrando en completar tarea ...");
		List<UserTaskServicesClient> clientList = kieSFS.getTaskClientList();
		for (UserTaskServicesClient client : clientList) {
			try {
				TaskInstance taskInstance = client.findTaskById(taskId);
				String containerId = taskInstance.getContainerId();
				String user = taskInstance.getActualOwner();
				//We store as a process variable the questionnaireResponseId generated when we complete the task
				Map<String,Object> variables= new HashMap<String,Object>();
				/**
				 * Las tareas humanas tienen que tener una salida QUESTIONNAIRE_RESPONSE_URI en la que se pase la url del cuestionario respuesta
				 */
				variables.put(QUESTIONNAIRE_RESPONSE_URI, questionnaireResponseId);
				//Expiration Date is now a Completion Date
				Date completionDate = new Date();
				client.setTaskExpirationDate(containerId, taskId, completionDate);
				client.completeTask(containerId, taskId, user, variables);
				logger.info("Completada la tarea con id " + taskId + " del contenedor con id " + containerId + " del usuario " + user);
				return;
			} catch (Exception e) {
			}
		}
		/*UserTaskServicesClient client = kie.getUserTaskServicesClient();
		TaskInstance taskInstance = client.findTaskById(taskId);
		String containerId = taskInstance.getContainerId();
		String user = taskInstance.getActualOwner();
		logger.info("Completar la tarea con id " + taskId + " del contenedor con id " + containerId + " del usuario " + user);
		//We store as a process variable the questionnaireResponseId generated when we complete the task
		Map<String,Object> variables= new HashMap<String,Object>();
	    variables.put("questionnaireResponseURI", questionnaireResponseId);
		//Expiration Date is now a Completion Date
		Date completionDate = new Date();
		client.setTaskExpirationDate(containerId, taskId, completionDate);
		client.completeTask(containerId, taskId, user, variables);*/
	}

	/**
	 * Return the FHIR Task id associated to the jBPM Task
	 * @param taskId
	 * @param user
	 * @param containerId
	 * @param processInstanceId
	 * @return
	 */
	public String viewTask(Long taskId, String user, String containerId, Long processInstanceId) {
		logger.info("Ver la tarea con id " + taskId + " del contenedor con id " + containerId);
		List<UserTaskServicesClient> clientList = kieSFS.getTaskClientList();
		for (UserTaskServicesClient client : clientList) {
			try {
				String taskURI=client.getTaskInputContentByTaskId(containerId,taskId).get(TASK_URI).toString();
				String questionnaireResponseURI=client.getTaskOutputContentByTaskId(containerId,taskId).get(QUESTIONNAIRE_RESPONSE_URI).toString();
				logger.info("La tarea con id " + taskId +" y URI"+ taskURI + " está relacionada con el cuestionario respuesta fhir con id " + questionnaireResponseURI);
				return questionnaireResponseURI;
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	/**
	 * Finds FHIR task id from jBPM Task inputs.
	 * @param taskId
	 * @param containerId
	 * @param processInstanceId
	 * @return
	 */
	public String getTaskURIFromTaskInputContent(Long taskId, String containerId, Long processInstanceId) {
		logger.debug("Entrando en getTaskUri con taskId "+taskId+" containerId "+" processInstanceId "+processInstanceId);
		List<UserTaskServicesClient> clientList = kieSFS.getTaskClientList();
		for (UserTaskServicesClient client : clientList) {
			try {
				String taskURI = client.getTaskInputContentByTaskId(containerId,taskId).get(TASK_URI).toString();   
				logger.debug("TaskURI con el cliente inyectado " + taskURI);
				return taskURI;
			} catch (Exception e) {
			}
		}
		return null;
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
