package us.dit.humanTasks.service.services.kie;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Questionnaire;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import us.dit.humanTasks.service.services.fhir.FhirClient;

/**
 * ESTE SERVICIO DESAPARECERÁ, ES SÓLO PARA PRUEBAS
 */
@Service
public class ReviewService {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private KieUtilService kie;
	
	@Autowired
	private FhirClient fhir;
	
	private String containerId="human-tasks-kjar-1_0-SNAPSHOT";
	private String processId="consentimientos-kjar.solicitudConsentimiento";
	
	private String containerId2="human-tasks-management-kjar-1.0.0-SNAPSHOT";
	//private String processId2="human-tasks-management.TareaAUsuario";
	//private String processId2="HumanTasksManagement.TareaAOtroRole";
	private String processId2="HumanTasksManagement.TareaARol";
	
	public Long newInstance2(String principal) {
		Map<String,Object> variables= new HashMap<String,Object>();
        logger.info("Entro en newInstance");
	    variables.put("taskURI", "758873");
	    //variables.put("user", principal);
		ProcessServicesClient client = kie.getProcessServicesClient();
		Long idInstanceProcess = client.startProcess(containerId2, processId2,variables);
		logger.info("conseguido??? " + idInstanceProcess.toString());
		return idInstanceProcess;
	}
	
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
}
