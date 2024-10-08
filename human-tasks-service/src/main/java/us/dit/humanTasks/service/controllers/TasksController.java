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
 * @author Marco Antonio Maldonado Orozco
 */
@Controller
@RequestMapping("/tasks")
public class TasksController {
	private static final Logger logger = LogManager.getLogger();
	//Nombre del parámetro de entrada del workItemHandler que contiene el ID de la tarea fhir
	private static final String TASK_URI = "taskURI";
	
	private static final String TASK_ID = "taskId";

		
	@Value("${fhir.server.base}")
	private String serverBase;
	
	@Autowired
	private TasksDAO taskDao;
	
	@Autowired
	private FhirTasksDAO fhirDao;
	
	@Autowired
	private TestService test;
	
	/**
	 * Shows the main tasks page
	 * @param session
	 * @param model
	 * @return String
	 */
	@GetMapping()
	public String TasksSelection(HttpSession session, Model model) {
		//Esto está aquí para iniciar las tareas.
		//Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		//UserDetails principal = (UserDetails) auth.getPrincipal();
		//review.newInstance2(principal.getUsername());
		return "tasks";
	}
	
	/**
	 * Shows the assignedTasks page
	 * @param session
	 * @param model
	 * @return String
	 */
	@GetMapping("/assignedTasks")
	public String getAssignedTasks(HttpSession session, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserDetails principal = (UserDetails) auth.getPrincipal();
		List<TaskSummary> tasks = taskDao.findAssignedTasks(principal.getUsername());
		model.addAttribute("tasks", tasks);
		return "assignedTasks";
	}
	
	/**
	 * Shows the potentialTasks page
	 * @param session
	 * @param model
	 * @return String
	 */
	@GetMapping("/potentialTasks")
	public String getPotentialTasks(HttpSession session, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserDetails principal = (UserDetails) auth.getPrincipal();
		List<TaskSummary> tasks = taskDao.findPotentialTasks(principal.getUsername());
		model.addAttribute("tasks", tasks);
		return "potentialTasks";
	}
	
	/**
	 * Manage the claim action from potentialTasks page
	 * @param taskId
	 * @param containerId
	 * @param processInstanceId
	 * @param model
	 * @return RedirectView
	 */
	@PostMapping("/claim")
    public RedirectView claimTask(@RequestParam("taskId") Long taskId, @RequestParam("containerId") String containerId, 
    		@RequestParam("processInstanceId") Long processInstanceId, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserDetails principal = (UserDetails) auth.getPrincipal();
		String user = principal.getUsername();
		taskDao.claimTask(taskId, user, containerId);
		String taskURI = taskDao.getTaskURIFromTaskInputContent(taskId, containerId, processInstanceId);
		fhirDao.updateTaskStatus(serverBase, taskURI, Task.TaskStatus.REQUESTED);
        return new RedirectView("/tasks/potentialTasks");
    }
	
	/**
	 * Manage the start action from AssignedTasks page.
	 * @param taskId
	 * @param actualOwner
	 * @param containerId
	 * @param processInstanceId
	 * @param redirectAttributes
	 * @return RedirectView
	 */
	@PostMapping("/start")
    public RedirectView startTask(@RequestParam("taskId") Long taskId, @RequestParam("actualOwner") String actualOwner, @RequestParam("containerId") String containerId,
    		@RequestParam("processInstanceId") Long processInstanceId, RedirectAttributes redirectAttributes) {     
		String taskURI = taskDao.startTask(taskId, actualOwner, containerId, processInstanceId);
		fhirDao.updateTaskStatus(serverBase, taskURI, Task.TaskStatus.INPROGRESS);
		redirectAttributes.addAttribute(TASK_ID, taskId);
        redirectAttributes.addAttribute(TASK_URI, taskURI);
        return new RedirectView("/questionnaire");
    }
	
	/**
	 * Manage the continue action from AssignedTasks page
	 * @param taskId
	 * @param actualOwner
	 * @param containerId
	 * @param processInstanceId
	 * @param redirectAttributes
	 * @return RedirectView
	 */
	@PostMapping("/continue")
    public RedirectView continueTask(@RequestParam("taskId") Long taskId, @RequestParam("actualOwner") String actualOwner, @RequestParam("containerId") String containerId, 
    		@RequestParam("processInstanceId") Long processInstanceId, RedirectAttributes redirectAttributes) {

        String taskURI = taskDao.continueTask(taskId, actualOwner, containerId, processInstanceId);
        redirectAttributes.addAttribute(TASK_ID, taskId);
        redirectAttributes.addAttribute(TASK_URI, taskURI);
        return new RedirectView("/questionnaire");
    }
	
	/**
	 * Manage the reject action from AssignedTasks page
	 * @param taskId
	 * @param actualOwner
	 * @param containerId
	 * @param processInstanceId
	 * @param model
	 * @return RedirectView
	 */
	@PostMapping("/reject")
    public RedirectView rejectTask(@RequestParam("taskId") Long taskId, @RequestParam("actualOwner") String actualOwner, @RequestParam("containerId") String containerId, 
    		@RequestParam("processInstanceId") Long processInstanceId, Model model) {
        taskDao.rejectTask(taskId, actualOwner, containerId);
        String taskURI = taskDao.getTaskURIFromTaskInputContent(taskId, containerId, processInstanceId);
        fhirDao.updateTaskStatus(serverBase, taskURI, Task.TaskStatus.READY);
        return new RedirectView("/tasks/assignedTasks");
    }

}
