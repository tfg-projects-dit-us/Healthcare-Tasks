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
package us.dit.humanTasks.service.services.kie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.springframework.stereotype.Component;
/**
 * Ejemplo de un observador de procesos
 */
@Component
public class ObservadorDeProceso  implements ProcessEventListener {	
	private static final Logger logger = LogManager.getLogger();
	    @Override
	    public void beforeProcessStarted(ProcessStartedEvent event) {
	        logger.info("Va a arrancar un proceso con process Instace= " + event.getProcessInstance());
	        logger.info("Detalle del evento " + event);
	    }

	    @Override
	    public void afterProcessStarted(ProcessStartedEvent event) {
	        logger.info("Proceso arrancado con processInstance= " + event.getProcessInstance());
	        logger.info("Detalle del evento " + event);
	    }
	    @Override
	    public void beforeProcessCompleted(ProcessCompletedEvent event) {
	    	logger.info("Va a terminar un proceso " + event);
	    }

	    @Override
	    public void afterProcessCompleted(ProcessCompletedEvent event) {
	    	logger.info("Ha terminado un proceso " + event);
	    }

	    @Override
	    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
	    	logger.info("Antes de disparar un nodo " + event);
	    }

	    @Override
	    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
	    	logger.info("Después de disparar un nodo " + event);
	    }

	    @Override
	    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
	    	logger.info("Antes de abandonar un nodo " + event);
	    }

	    @Override
	    public void afterNodeLeft(ProcessNodeLeftEvent event) {
	    	logger.info("Después de abandonar un nodo " + event);
	    }

	    @Override
	    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
	    	logger.info("Antes del cambio de una variable " + event);
	    }

	    @Override
	    public void afterVariableChanged(ProcessVariableChangedEvent event) {
	    	logger.info("Después del cambio de una variable " + event);
	    }
}