/**
 * 
 */
package us.dit.humanTasks.service.conf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import org.springframework.stereotype.Service;
/**
 * Esta clase permite que la aplicación se auto registre en un business central 
 * que se ejecuta en docker, en el mismo host que la aplicación
 * Pero posteriormente los clientes usan la url como localhost:8090, para poder hacer consultas
 * al servidor
 */
//@Service
public class KieServerClientConfig {
	private static final Logger logger = LogManager.getLogger();

    @Value("${client.kieserver.location}")
    private String clientUrl;

    @Value("${kieserver.user}")
    private String username;

    @Value("${kieserver.pwd}")
    private String password;

    @Bean
    KieServicesClient kieServicesClient() {
    	logger.info("Creando el kieservicesclient con ", clientUrl, username, password);
        // Configura el cliente para conectarse con la URL ajustada, después de haber hecho el registro en business central (sólo es necesario en modo desarrollo)    	
        KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(clientUrl, username, password);
    	config.setMarshallingFormat(MarshallingFormat.JSON);
        config.setTimeout(600000000); // Ajusta el timeout si es necesario
        return KieServicesFactory.newKieServicesClient(config);
    }
}
