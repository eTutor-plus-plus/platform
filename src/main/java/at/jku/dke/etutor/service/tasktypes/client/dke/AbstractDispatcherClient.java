package at.jku.dke.etutor.service.tasktypes.client.dke;
import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.service.tasktypes.client.AbstractClient;

/**
 * Abstract class offering some utility methods for the proxy services,
 * to interact with the dke-dispatcher.
 */
public abstract sealed class AbstractDispatcherClient extends AbstractClient permits
    DatalogClient,
    DkeSubmissionClient,
    PmClient,
    SqlClient,
    XQueryClient {
    protected AbstractDispatcherClient(ApplicationProperties properties){
        super(properties.getDispatcher().getUrl());
    }
}
