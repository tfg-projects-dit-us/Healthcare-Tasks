/**
 * 
 */
package us.dit.consentimientos.service.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Questionnaire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import us.dit.consentimientos.service.services.fhir.FhirClient;

/**
 * 
 */
@Controller
@RequestMapping("/questionnaire")
public class QuestionnaireHttp {
	private static final Logger logger = LogManager.getLogger();
	@Autowired
	FhirClient fhirClient;
	
	@GetMapping() 
	public String getQuestionnaire(HttpSession session, Model model) {
		logger.info("Entro en getQuestionnaire del controlador Http");	
		String serverBase = "http://hapi.fhir.org/baseR5/";
		String taskId = "739883";
		Questionnaire questionnaire = fhirClient.getQuestionnaireFromTask(taskId, serverBase);
		if (questionnaire != null) {
			model.addAttribute("questionnaire", questionnaire);
			return "questionnaireForm";
		} else {
			return "paginaDeError";
		}
	}

}
