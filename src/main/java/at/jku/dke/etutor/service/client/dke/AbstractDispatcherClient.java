package at.jku.dke.etutor.service.client.dke;
import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.client.AbstractClient;

/**
 * Abstract class to interact with the dke-dispatcher
 */
public abstract sealed class AbstractDispatcherClient extends AbstractClient permits DatalogClient, DkeSubmissionClient, PmClient, RTClient, SqlClient, XQueryClient {
    protected AbstractDispatcherClient(ApplicationProperties properties){
        super(properties.getDispatcher().getUrl());
    }
}
