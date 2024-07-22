/**
 * 
 */
package us.dit.humanTasks.service.controllers;

import java.util.List;

import javax.servlet.http.HttpSession;

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
import us.dit.humanTasks.service.services.kie.ReviewService;

/**
 * @author Marco Antonio Maldonado Orozco
 */
@Controller
@RequestMapping("/tasks")
public class TasksController {
	
	//Nombre del parámetro de entrada del workItemHandler que contiene el ID de la tarea fhir
	private static final String TASK_URI = "taskURI";
	
	private static final String TASK_ID = "taskId";

		
	@Value("${fhir.server.base}")
	private String serverBase;
	
	@Autowired
	private TasksDAO taskDao;
	
	@Autowired
	private FhirTasksDAO fhirDao;
	
	//@Autowired
	//private ReviewService review;
		
	@GetMapping()
	public String TasksSelection(HttpSession session, Model model) {
		//Esto está aquí para iniciar las tareas.
		//Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		//UserDetails principal = (UserDetails) auth.getPrincipal();
		//review.newInstance2(principal.getUsername());
		return "tasks";
	}
	
	@GetMapping("/assignedTasks")
	public String getAssignedTasks(HttpSession session, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserDetails principal = (UserDetails) auth.getPrincipal();
		List<TaskSummary> tasks = taskDao.findAssignedTasks(principal.getUsername());
		model.addAttribute("tasks", tasks);
		return "assignedTasks";
	}
	
	@GetMapping("/potentialTasks")
	public String getPotentialTasks(HttpSession session, Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserDetails principal = (UserDetails) auth.getPrincipal();
		List<TaskSummary> tasks = taskDao.findPotentialTasks(principal.getUsername());
		model.addAttribute("tasks", tasks);
		return "potentialTasks";
	}
	
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
	
	@PostMapping("/start")
    public RedirectView startTask(@RequestParam("taskId") Long taskId, @RequestParam("actualOwner") String actualOwner, @RequestParam("containerId") String containerId,
    		@RequestParam("processInstanceId") Long processInstanceId, RedirectAttributes redirectAttributes) {     
		String taskURI = taskDao.startTask(taskId, actualOwner, containerId, processInstanceId);
		fhirDao.updateTaskStatus(serverBase, taskURI, Task.TaskStatus.INPROGRESS);
		redirectAttributes.addAttribute(TASK_ID, taskId);
        redirectAttributes.addAttribute(TASK_URI, taskURI);
        return new RedirectView("/questionnaire");
    }
	
	@PostMapping("/continue")
    public RedirectView continueTask(@RequestParam("taskId") Long taskId, @RequestParam("actualOwner") String actualOwner, @RequestParam("containerId") String containerId, 
    		@RequestParam("processInstanceId") Long processInstanceId, RedirectAttributes redirectAttributes) {

        String taskURI = taskDao.continueTask(taskId, actualOwner, containerId, processInstanceId);
        redirectAttributes.addAttribute(TASK_ID, taskId);
        redirectAttributes.addAttribute(TASK_URI, taskURI);
        return new RedirectView("/questionnaire");
    }
	
	@PostMapping("/reject")
    public RedirectView rejectTask(@RequestParam("taskId") Long taskId, @RequestParam("actualOwner") String actualOwner, @RequestParam("containerId") String containerId, 
    		@RequestParam("processInstanceId") Long processInstanceId, Model model) {
        taskDao.rejectTask(taskId, actualOwner, containerId);
        String taskURI = taskDao.getTaskURIFromTaskInputContent(taskId, containerId, processInstanceId);
        fhirDao.updateTaskStatus(serverBase, taskURI, Task.TaskStatus.READY);
        return new RedirectView("/tasks/assignedTasks");
    }
}
