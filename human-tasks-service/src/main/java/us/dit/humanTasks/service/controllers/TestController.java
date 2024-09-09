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

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Task;
import org.kie.server.api.model.instance.TaskSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import us.dit.humanTasks.service.model.FhirTasksDAO;
import us.dit.humanTasks.service.model.TasksDAO;
import us.dit.humanTasks.service.services.kie.TestService;

/**
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
	
	
	/**
	 * Método para test, permite iniciar el proceso TareaAUsuario, que asigna la tarea al usuario que lo invoca
	 * @param session
	 * @param model
	 * @return String
	 */
	@GetMapping("/initTareaAUsuario")
	public RedirectView TestAUsu() {
		logger.info("entro en /initTareaAUsuario");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserDetails principal = (UserDetails) auth.getPrincipal();
		test.newTareaAUsuario(principal.getUsername());
		return new RedirectView("/tasks");
	}
	
	/**
	 * Método para test, permite iniciar el proceso TareaAUsuario, que asigna la tarea al rol webadmin
	 * @param session
	 * @param model
	 * @return String
	 */
	@GetMapping("/initTareaARol")
	public RedirectView TestARol() {	
		logger.info("entro en /initTareaARol");
		test.newTareaARol();
		return new RedirectView("/tasks");
	}
}
