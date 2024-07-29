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
import org.hl7.fhir.r5.model.Questionnaire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import us.dit.humanTasks.service.model.QuestionnaireDAO;

/**
 * @author Marco Antonio Maldonado Orozco
 */
@Controller
@RequestMapping("/questionnaire")
public class QuestionnaireController {
	private static final Logger logger = LogManager.getLogger();
	
	@Value("${fhir.server.base}")
	private String serverBase;
	
	@Autowired
	QuestionnaireDAO questionnaireDAO;
	
	/**
	 * Obtaine the questionnaire from FHIR Task id and return the Questionnaire page.
	 * @param taskId
	 * @param taskURI
	 * @param model
	 * @return String
	 */
	@GetMapping() 
	public String getQuestionnaire(@RequestParam("taskId") String taskId, @RequestParam("taskURI") String taskURI, Model model) {
		logger.info("Entro en getQuestionnaire del controlador Http");	
		
		Questionnaire questionnaire = questionnaireDAO.getQuestionnaireFromTask(taskURI, serverBase);
		if (questionnaire != null) {
			
			//Id de la tarea de businessCentral para saber qué tarea jbpm hay que completar cuando se envíe el cuestionario
			model.addAttribute("taskId", taskId);
			
			//Id de la tarea fhir para saber qué tarea fhir hay que completar cuando se envíe el cuestionario
			model.addAttribute("taskURI", taskURI);
			
			model.addAttribute("questionnaire", questionnaire);
			return "questionnaireForm";
		} else {
			return "paginaDeError";
		}
	}

}
