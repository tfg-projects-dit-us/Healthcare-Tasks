package us.dit.consentimientos.service.model;

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

import us.dit.consentimientos.service.services.kie.KieUtilService;

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
	 * 
	 * @param user
	 * @return
	 */
	public List<TaskSummary> findAllTasks(String user) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Invocando findAllTasks con usuario: "+ user);
		return client.findTasksAssignedAsPotentialOwner(user, 0, Integer.MAX_VALUE);
    }
	
	public List<TaskSummary> findAssignedTasks(String user) {
		logger.info("Invocando findAssignedTasks con usuario: "+ user);
		List<TaskSummary> allTasks = findAllTasks(user);
		return allTasks.stream().filter(task -> task.getActualOwner() != null).collect(Collectors.toList());
    }
	
	public List<TaskSummary> findPotentialTasks(String user) {
		logger.info("Invocando findPotentialTasks con usuario: "+ user);
		List<TaskSummary> allTasks = findAllTasks(user);
		return allTasks.stream().filter(task -> task.getActualOwner() == null).collect(Collectors.toList());
    }
	
	public void claimTask(Long taskId, String user, String containerId) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Reclamar la tarea con id " + taskId + " del contenedor con id " + containerId + " para el usuario " + user);
		client.claimTask(containerId, taskId, user);
	}
	
	public String startTask(Long taskId, String user, String containerId, Long processInstanceId) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Comenzar la tarea con id " + taskId + " del contenedor con id " + containerId);
		client.startTask(containerId, taskId, user);
		Map<String, Object> inputData = getTaskInputContent(client, taskId, containerId, processInstanceId);
        String taskURI = (String) inputData.get(TASK_URI);
        logger.info("La tarea con id " + taskId + " está relacionada con la tarea fhir con id " + taskURI);
        return taskURI;
	}
	
	public String continueTask(Long taskId, String user, String containerId, Long processInstanceId) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Continuar la tarea con id " + taskId + " del contenedor con id " + containerId);
		Map<String, Object> inputData = getTaskInputContent(client, taskId, containerId, processInstanceId);
        String taskURI = (String) inputData.get(TASK_URI);
        logger.info("La tarea con id " + taskId + " está relacionada con la tarea fhir con id " + taskURI);
        return taskURI;
	}
	
	public void rejectTask(Long taskId, String user, String containerId) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		logger.info("Rechazar la tarea con id " + taskId + " del contenedor con id " + containerId + " del usuario " + user);
		client.releaseTask(containerId, taskId, user);
	}
	
	public void completeTask(Long taskId) throws Exception {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		TaskInstance taskInstance = client.findTaskById(taskId);
		String containerId = taskInstance.getContainerId();
		String user = taskInstance.getActualOwner();
		logger.info("Completar la tarea con id " + taskId + " del contenedor con id " + containerId + " del usuario " + user);
		client.completeTask(containerId, taskId, user, new HashMap<>());
	}
	
	public String getTaskURIFromTaskInputContent(Long taskId, String containerId, Long processInstanceId) {
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		Map<String, Object> inputData = getTaskInputContent(client, taskId, containerId, processInstanceId);
        String taskURI = (String) inputData.get(TASK_URI);
        return taskURI;
	}
	
	private Map<String, Object> getTaskInputContent(UserTaskServicesClient client, Long taskId, String containerId, Long processInstanceId) {
		ProcessServicesClient processClient = kie.getProcessServicesClient();
		logger.info("Obtenemos las variables de entrada de la tarea " + taskId);
		TaskInstance taskInstance = client.findTaskById(taskId);
		WorkItemInstance workItem = processClient.getWorkItem(containerId, processInstanceId, taskInstance.getWorkItemId());
		Map<String, Object> inputData = workItem.getParameters();
		return inputData;
	}
	
	
	/************************PARA FUTURO******************************/
	
	public List<TaskSummary> findAllPotentialPendingTasksExpirationDateOrdered(String user) {
		Comparator<TaskSummary> comparator = Comparator.nullsLast(
	            Comparator.comparing(TaskSummary::getExpirationTime, Comparator.nullsLast(Comparator.naturalOrder()))
	        );
		return this.findAllTasks(user).stream()
				.sorted(comparator)
				.collect(Collectors.toList());
	}
	
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
	
	public List<TaskInstance> findAllPotentialPendingTaskInstances(String user) {
		List<TaskInstance> taskInstances = new ArrayList<>();
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		List<TaskSummary> tasks = client.findTasksAssignedAsPotentialOwner(user, 0, Integer.MAX_VALUE);
		for (TaskSummary task : tasks) {
			logger.info("Asunto y prioridad TaskSummary: " + task.getSubject() + ", " + task.getPriority());
			TaskInstance taskInstance = client.getTaskInstance("human-tasks-management-kjar-1.0.0-SNAPSHOT", task.getId(), true, true, true);
			logger.info("Asunto y prioridad TaskInstance: " + taskInstance.getSubject() + ", " + taskInstance.getPriority());
			taskInstances.add(taskInstance);
		}
		return taskInstances;
    }
	
	public List<TaskEventInstance> findAllPotentialPendingTaskEventInstances(String user) {
		List<TaskEventInstance> taskInstances = new ArrayList<>();
		UserTaskServicesClient client = kie.getUserTaskServicesClient();
		List<TaskSummary> tasks = client.findTasksAssignedAsPotentialOwner(user, 0, Integer.MAX_VALUE);
		for (TaskSummary task : tasks) {
			taskInstances.addAll(client.findTaskEvents("human-tasks-management-kjar-1.0.0-SNAPSHOT", task.getId(), 0, 100));
		}
		return taskInstances;
    }
	
	//Este método es por si en la descripción se le quiere pasar información extra de la tarea en fomra de JSON poder extraerla
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
