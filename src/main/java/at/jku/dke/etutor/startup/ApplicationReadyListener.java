package at.jku.dke.etutor.startup;

import at.jku.dke.etutor.service.SPARQLEndpointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Listener class which performs operations after the application is ready.
 * <p>
 * Implemented logic:
 * <ul>
 *     <li>SPARQL scheme update</li>
 * </ul>
 *
 * @author fne
 */
@Component
@Order(0)
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    private final Logger log = LoggerFactory.getLogger(ApplicationReadyListener.class);

    private final SPARQLEndpointService sparqlEndpointService;

    /**
     * Constructor.
     *
     * @param sparqlEndpointService the inject sparql endpoint service
     */
    public ApplicationReadyListener(SPARQLEndpointService sparqlEndpointService) {
        this.sparqlEndpointService = sparqlEndpointService;
    }

    /**
     * See {@link ApplicationListener#onApplicationEvent(ApplicationEvent)}
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        sparqlEndpointService.insertScheme();
    }
}
