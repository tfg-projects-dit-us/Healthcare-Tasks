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
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

import us.dit.consentimientos.service.model.TasksDAO;
import us.dit.consentimientos.service.services.fhir.FhirClient;
import us.dit.consentimientos.service.services.kie.ReviewService;

/**
 * 
 */
@Controller
@RequestMapping("/questionnaire")
public class QuestionnaireHttp {
	private static final Logger logger = LogManager.getLogger();
	@Autowired
	FhirClient fhirClient;
	
	@Autowired
	TasksDAO tasksDao;
	
	@Autowired
	ReviewService reviewService;
	
	@GetMapping() 
	public String getQuestionnaire(@RequestParam("taskURI") String taskURI, Model model) {
		logger.info("Entro en getQuestionnaire del controlador Http");	
		String serverBase = "http://hapi.fhir.org/baseR5/";
		
//		logger.info("****************************************************");
//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//		UserDetails principal = (UserDetails) auth.getPrincipal();
//		logger.info("Datos de usuario (principal)" + principal);
//		reviewService.newInstance2(principal.getUsername());
//		logger.info("****************************************************");
//		logger.info("findAllPendingTasks");
//		logger.info(tasksDao.findAllPotentialPendingTasks(principal.getUsername()));
//		logger.info("****************************************************");
//		logger.info("findAllPendingTasks con null");
//		logger.info(tasksDao.findAllPotentialPendingTasks(null));
//		logger.info("****************************************************");
//		logger.info("findAllPendingTasksExpirationDateOrdered");
//		logger.info(tasksDao.findAllPotentialPendingTasksExpirationDateOrdered(principal.getUsername()));
//		logger.info("****************************************************");
//		logger.info("findAllPontentialPendingTasksByType");
//		logger.info(tasksDao.findAllPontentialPendingTasksByType(principal.getUsername()));
//		logger.info("****************************************************");
//		logger.info("findAllPotentialPendingTaskInstances");
//		logger.info(tasksDao.findAllPotentialPendingTaskInstances(principal.getUsername()));
//		logger.info("****************************************************");
//		logger.info("findAllPotentialPendingTaskEventInstances");
//		logger.info(tasksDao.findAllPotentialPendingTaskEventInstances(principal.getUsername()));
//		logger.info("****************************************************");
		
		Questionnaire questionnaire = fhirClient.getQuestionnaireFromTask(taskURI, serverBase);
		if (questionnaire != null) {
			model.addAttribute("questionnaire", questionnaire);
			return "questionnaireForm";
		} else {
			return "paginaDeError";
		}
	}

}
