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
package us.dit.humanTasks.service.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.server.client.ProcessServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import us.dit.humanTasks.service.services.kie.TestService;
import us.dit.humanTasks.service.services.kie.KieUtilFactoryService;

//import java.util.List;

/**
 * @author Juan Manuel Ostos Rabadán
 * @author Isabel Román Martínez
 */
@Controller
@RequestMapping("/test")
public class TestController {
	private static final Logger logger = LogManager.getLogger();
			
	@Value("${fhir.server.base}")
	private String serverBase;

	
	@Autowired
	private TestService test;
	
	//@Autowired
	//private KieUtilService kie;

	@Value("${test.roleprocessLow}")
	private String rolPL;

	@Value("${test.roleprocessMedium}")
	private String rolPM;

	@Value("${test.roleprocessHigh}")
	private String rolPH;

	@Value("${test.roleprocessWithoutTimer}")
	private String rolPwT;
	
	/**
	 * Método para test, permite iniciar el proceso TareaAUsuario, que asigna la tarea al usuario que lo invoca
	 * @param session
	 * @param model
	 * @return String
	 */
	@GetMapping("/initTareaAUsuario")
	public RedirectView TestAUsu(@RequestParam("serverIndex") Integer serverIndex) {
		logger.info("entro en /initTareaAUsuario");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserDetails principal = (UserDetails) auth.getPrincipal();
		test.newTareaAUsuario(serverIndex, principal.getUsername(),"Tratamientos",0,0,5);
		return new RedirectView("/tasks");
	}
	
	/**
	 * Método para test, permite iniciar el proceso TareaARol, que asigna la tarea al rol wbadmin
	 * @param session
	 * @param model
	 * @return String
	 */
	@GetMapping("/initTareaARol")
	public RedirectView TestARol(@RequestParam("serverIndex") Integer serverIndex) {	
		logger.info("entro en /initTareaARol");
		test.newTareaARol(serverIndex, rolPL,"Citas",1,0,0);
		return new RedirectView("/tasks");
	}

	/**
	 * Método para test, permite iniciar el proceso TareaARol sin timer, que asigna la tarea al rol wbadmin
	 * @param session
	 * @param model
	 * @return String
	 */
	@GetMapping("/initTareaARolSinTimer")
	public RedirectView TestARolSinTimer(@RequestParam("serverIndex") Integer serverIndex) {	
		logger.info("entro en /initTareaARol");
		test.newTareaARol(serverIndex, rolPwT,"Citas",0,0,5);
		return new RedirectView("/tasks");
	}

	/**
	 * Método para test, permite iniciar los procesos a rol de muestra con distintas prioridades, que asigna la tarea al rol webadmin
	 * @param session
	 * @param model
	 * @return String
	 */
	@GetMapping("/initTareasARolMuestra")
	public RedirectView TestARolMuestras(@RequestParam("serverIndex") Integer serverIndex) {	
		logger.info("entro en /initTareasARolMuestra");
		test.newTareaARol(serverIndex, rolPH,"Tratamientos",0,0,5);
		test.newTareaARol(serverIndex, rolPM,"Citas",0,8,0);
		test.newTareaARol(serverIndex, rolPL,"Tratamientos",1,0,0);
		return new RedirectView("/tasks");
	}

	/**
	 * Método para test, permite enviar la señal ConsentRequest al motor kie, adjuntando el 
	 */
	/*@GetMapping("/sendConsentRequest")
	public RedirectView sendSignal() {
	
		// Se envía la señal al motor KIE
		logger.info("Enviando una señal al motor KIE");
		//La tarea tiene que estar previametne creada en el servidor fhir local, puerto 8888
		kie.sendSignal("ConsentRequest", "http://localhost:8888/fhir/Task/5");

		return new RedirectView("/tasks");
	}*/
}
