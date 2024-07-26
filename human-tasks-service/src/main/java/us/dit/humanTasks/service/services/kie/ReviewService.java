package us.dit.humanTasks.service.services.kie;


import java.util.HashMap;
import java.util.Map;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kie.server.client.ProcessServicesClient;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;



/**
 * ESTE SERVICIO ES PARA TESTS, PERMITE INSTANCIAR PROCESOS CON TAREAS HUMANAS
 */
@Service
public class ReviewService {

	private static final Logger logger = LogManager.getLogger();
	
	@Autowired
	private KieUtilService kie;
	
	private String containerId="human-tasks-management-kjar-1.0.0-SNAPSHOT";
	private String rolP="HumanTasksManagement.TareaARol";
	private String userP="HumanTasksManagement.TareaAOtroRole";
	/**
	 * Instancia un proceso con una tarea humana asignada al rol wbadmin
	 * @return el id del proceso instanciado
	 */
	public Long newTareaARol() {
		Map<String,Object> variables= new HashMap<String,Object>();
        logger.info("Entro en newTareaARol");
	    variables.put("taskURI", "758873");	  
		ProcessServicesClient client = kie.getProcessServicesClient();
		Long idInstanceProcess = client.startProcess(containerId, rolP,variables);
		logger.info("Instanciado proceso " + idInstanceProcess.toString());
		return idInstanceProcess;
	}
	/**
	 * Instancia un proceso con una tarea humana asignada al usuario que se pase como parámetro
	 * @param principal usuario al que se le asigna la tarea
	 * @return el id del proceso instanciado
	 */
	public Long newTareaAUsuario(String principal) {
		Map<String,Object> variables= new HashMap<String,Object>();
        logger.info("Entro en newTareaAUsuario");
	    variables.put("taskURI", "758873");
	    variables.put("user", principal);
		ProcessServicesClient client = kie.getProcessServicesClient();
		Long idInstanceProcess = client.startProcess(containerId, userP,variables);
		logger.info("Instanciado proceso " + idInstanceProcess.toString());
		return idInstanceProcess;
	}
	/*
	public Questionnaire initTask2(HttpSession session) {
		logger.info("Entro en init task con processId: "+processId2);
		WorkItemInstance wi=findNextTask((Long)session.getAttribute("processId2"));
		session.setAttribute("wi",wi);		
		Questionnaire questionnaire= fhir.getQuestionnaire((String)wi.getParameters().get("fhirbase"), (String)wi.getParameters().get("questionnaireId"));
		logger.info("Recuperado cuestionario con id "+questionnaire.getId());	
		session.setAttribute("questionnaire", questionnaire);		
		return questionnaire;
	}
	
	public Long newInstance(String principal) {
		Map<String,Object> variables= new HashMap<String,Object>();
        logger.info("Entro en newInstance");
	    variables.put("principal", principal);
		ProcessServicesClient client = kie.getProcessServicesClient();
		Long idInstanceProcess = client.startProcess(containerId, processId,variables);
		logger.info("conseguido??? " + idInstanceProcess.toString());
		return idInstanceProcess;
	}
	
	public Questionnaire initTask(HttpSession session) {
		logger.info("Entro en init task con processId: "+processId);
		WorkItemInstance wi=findNextTask((Long)session.getAttribute("processId"));
		session.setAttribute("wi",wi);		
		Questionnaire questionnaire= fhir.getQuestionnaire((String)wi.getParameters().get("fhirbase"), (String)wi.getParameters().get("questionnaireId"));
		logger.info("Recuperado cuestionario con id "+questionnaire.getId());	
		session.setAttribute("questionnaire", questionnaire);		
		return questionnaire;
	}
	
	private WorkItemInstance findNextTask(Long processId) {
		logger.info("Entro en findNextTask con processId: "+processId);				
		logger.info("Creo cliente de procesos");
		ProcessServicesClient processClient = kie.getProcessServicesClient();
		logger.info("Llamo a findNodeInstances del cliente de procesos");
		
		WorkItemInstance wi=processClient.getWorkItemByProcessInstance(containerId, processId).get(0);
		logger.info("WI: "+wi.toString());		
		return wi;
	}
	*/
}
