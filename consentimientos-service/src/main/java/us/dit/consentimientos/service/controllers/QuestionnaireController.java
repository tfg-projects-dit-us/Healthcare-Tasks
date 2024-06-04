/**
 * 
 */
package us.dit.consentimientos.service.controllers;

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

import us.dit.consentimientos.service.model.QuestionnaireDAO;

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
