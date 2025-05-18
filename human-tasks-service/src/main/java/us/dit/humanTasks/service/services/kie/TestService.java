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
/**
* TODO: Añadir un método para arrancar un proceso enviando un id de tarea
* Se puede utilizar como referencia lo que hice para el proyecto de Mariana
**/
package us.dit.humanTasks.service.services.kie;


import java.util.HashMap;
import java.util.Map;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kie.server.client.ProcessServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;





/**
 * ESTE SERVICIO ES PARA TESTS, PERMITE INSTANCIAR PROCESOS CON TAREAS HUMANAS
 * @Author Isabel Román
 */
@Service
public class TestService {

	private static final Logger logger = LogManager.getLogger();
	
	@Autowired
	private KieUtilService kie;

	@Value("${test.taskid}")
	private String taskId;

	@Value("${test.containerid}")
	private String containerId;

	@Value("${test.roleprocess}")
	private String rolP;

	@Value("${test.userprocess}")		
	private String userP;
	
	/**
	 * Instancia un proceso con una tarea humana asignada al rol kie-server
	 * @return el id del proceso instanciado
	 */
	public Long newTareaARol() {
		Map<String,Object> variables= new HashMap<String,Object>();
        logger.info("Entro en newTareaARol");
	    variables.put("taskURI", taskId);	  
		ProcessServicesClient client = kie.getProcessServicesClient();
		Long idInstanceProcess = client.startProcess(containerId, rolP,variables);
		logger.info("Instanciado proceso " + idInstanceProcess.toString());
		return idInstanceProcess;
	}

	public Long newTareaARolWithExpirationTimer(String roleprocess, int days,int hours, int minutes) {
		// Obtain the current date and time in UTC
        ZonedDateTime expirationDateTime = ZonedDateTime.now(ZoneOffset.UTC)
				.plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes);
        // Format the updated date and time in ISO 8601 format without milliseconds
        String expirationDateTimeISO8601 = expirationDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"));
		Map<String,Object> variables= new HashMap<>();
        logger.info("Entro en newTareaARolWithParameters");
	    variables.put("taskURI", taskId);
		variables.put("p_DueDate", expirationDateTimeISO8601);
		variables.put("p_Subject", "Medicaciones");
		ProcessServicesClient client = kie.getProcessServicesClient();
		Long idInstanceProcess = client.startProcess(containerId, roleprocess ,variables);
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
	    variables.put("taskURI", taskId);
	    variables.put("user", principal);
		ProcessServicesClient client = kie.getProcessServicesClient();
		Long idInstanceProcess = client.startProcess(containerId, userP,variables);
		logger.info("Instanciado proceso " + idInstanceProcess.toString());
		return idInstanceProcess;
	}	
}
