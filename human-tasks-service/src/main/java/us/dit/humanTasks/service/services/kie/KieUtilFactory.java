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
package us.dit.humanTasks.service.services.kie;
//import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.client.helper.KieServicesClientBuilder;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.List;

//import javax.annotation.PostConstruct;

import java.util.ArrayList;


/**
 * @author Juan Manuel Ostos Rabadán
 * 
 * Esta clase debe contener los servicios que proporcionan capacidades para
 * interaccionar con los múltiples kie server declarados en la configuración de nuestra aplicación.
 * especialmente servicios
 */

@Service
public class KieUtilFactory implements KieUtilFactoryService {

	@Autowired
    private Environment env;

	private static final Logger logger = LogManager.getLogger();  	
	
	private List<KieServicesClient> serverList;

    public KieUtilFactory() {
        logger.info("Constructor de KieUtilFactory invocado.");
    }

    @EventListener(ApplicationReadyEvent.class)
    private void init() {
        logger.info("Inicializando KieUtilFactory...");
        serverList = new ArrayList<>();
        int index = 0;
        boolean exit = false;
        while (!exit) {
            String prefix = "kieserver[" + index + "]";
            String location = env.getProperty(prefix + ".location");
            String user = env.getProperty(prefix + ".user");
            String pwd = env.getProperty(prefix + ".pwd");
            if (location == null || user == null || pwd == null) {
                exit = true;
            }else{
                KieServicesClient client = buildClient(location, user, pwd);
                if(client != null){
                    serverList.add(client);
                    logger.info("Creado cliente para servidor KIE " + location + " con indice " + (serverList.size()-1)); 
                }
                index++;
            }
        }
        logger.info("Total de servidores KIE cargados: " + serverList.size());
    }

    private KieServicesClient buildClient(String location, String user, String password) {
        try {
            KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(location, user, password);
            config.setMarshallingFormat(MarshallingFormat.JSON);
            return KieServicesFactory.newKieServicesClient(config);
        } catch (Exception e) {
            logger.info("No se pudo establecer la conexión con el KIE Server: " + location);
            return null;
        }
    }

	public List<KieServicesClient> getKieClientList() {
		return serverList;
	}

	public List<UserTaskServicesClient> getUserTaskClientList() {
		List<UserTaskServicesClient> taskClientList = new ArrayList<>();
		for(KieServicesClient client: serverList){
			UserTaskServicesClient taskClient = client.getServicesClient(UserTaskServicesClient.class);
			taskClientList.add(taskClient);
		}
		return taskClientList;
	}

    public List<ProcessServicesClient> getProcessClientList() {
		List<ProcessServicesClient> processClientList = new ArrayList<>();
		for(KieServicesClient client: serverList){
			ProcessServicesClient processClient = client.getServicesClient(ProcessServicesClient.class);
			processClientList.add(processClient);
		}
		return processClientList;
	}

}
