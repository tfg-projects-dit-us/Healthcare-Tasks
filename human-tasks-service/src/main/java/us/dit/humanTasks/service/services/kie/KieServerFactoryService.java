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
//import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.model.DeployedUnit;
import org.kie.api.runtime.manager.RuntimeManager;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UIServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.client.admin.UserTaskAdminServicesClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.ArrayList;
import us.dit.humanTasks.service.services.kie.KieUtilService;


/**
 * Esta clase debe contener los servicios que proporcionan capacidades para
 * interaccionar con el kie server Debe ser el único responsable de estos
 * aspectos, liberando a los demás de esta necesidad Debería ser un Bean, un
 * componente spring, para poder inyectarlo en todos aquellos que lo necesiten,
 * especialmente servicios
 */

@Service
public class KieServerFactoryService {
	@Value("${kieserver.location}")
	private String URL;
	@Value("${org.kie.server.user}")
	private String USERNAME;
	@Value("${org.kie.server.pwd}")
	private String PASSWORD;
	private static final Logger logger = LogManager.getLogger();
	@Autowired
	//private List<KieServicesClient> serverList;

	public KieServerFactoryService() {
		logger.info("Creando el KieServerFactoryService con los valores por defecto user "+ USERNAME + "pwd: "+PASSWORD);
	}
	
	/*public KieServicesClient buildClient(String url, String user, String password) {
		KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(url, user, password);
		config.setMarshallingFormat(MarshallingFormat.JSON);
		KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
		serverList.add(client);
		return client;
	}

	/*public List<KieServicesClient> getServerList() {
		return serverList;
	}

	public List<UserTaskServicesClient> getTaskClientList() {
		List<UserTaskServicesClient> TaskClientList = new ArrayList<>();;
		for(KieServicesClient client: serverList ){
			UserTaskServicesClient taskClient = client.getServicesClient(UserTaskServicesClient.class);
			TaskClientList.add(taskClient);
		}
		return TaskClientList;
	}*/

}
