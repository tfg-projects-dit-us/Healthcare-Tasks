/**
*  This file is part of Healthcare Tasks: Human task management in healthcare contexts.
*  Copyright (C) 2025  Universidad de Sevilla/Departamento de Ingeniería Telemática
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

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hl7.fhir.r5.model.QuestionnaireResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import us.dit.humanTasks.service.model.FhirQuestionnaireResponseDAO;

/**
 * @author Juan Manuel Ostos Rabadán
 */
@Controller
@RequestMapping("/questionnaireResponse")
public class QuestionnaireResponseController {
	private static final Logger logger = LogManager.getLogger();
	
	@Value("${fhir.server.base}")
	private String serverBase;
	
	@Autowired
	FhirQuestionnaireResponseDAO questionnaireResponseDAO;
	
	/**
	 * Get the questionnaire from FHIR QuestionnaireRespone id and return the QuestionnaireResponse page.

	 * @param model
	 * @return String
	 */
	@GetMapping() 
	public String getQuestionnaireResponse(@RequestParam("questionnaireResponseURI") String questionnaireResponseURI, Model model) {
		logger.info("Entro en getQuestionnaireResponse del controlador Http para la questionnaireResponseURI "+questionnaireResponseURI);	
		
		QuestionnaireResponse questionnaireResponse = questionnaireResponseDAO.getQuestionnaireResponse(questionnaireResponseURI);

		if (questionnaireResponse != null) {
			HashMap<String,List<String>> FormattedItems = questionnaireResponseDAO.getQuestionnaireResponseItems(questionnaireResponse);

			model.addAttribute("questionnaireResponseURI", questionnaireResponseURI);
			model.addAttribute("FormattedItems", FormattedItems);

			return "questionnaireResponseForm";
		} else {
			return "paginaDeError";
		}
	}

}
