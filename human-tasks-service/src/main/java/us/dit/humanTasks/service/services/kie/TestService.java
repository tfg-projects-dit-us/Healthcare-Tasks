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
public class TestService {

	private static final Logger logger = LogManager.getLogger();
	
	@Autowired
	private KieUtilService kie;
	
	private String containerId="human-tasks-management-kjar-1.0.0-SNAPSHOT";
	private String rolP="HumanTasksManagement.TareaARol";
	private String userP="human-tasks-management.TareaAUsuario";
	/**
	 * Instancia un proceso con una tarea humana asignada al rol kie-server
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
}
