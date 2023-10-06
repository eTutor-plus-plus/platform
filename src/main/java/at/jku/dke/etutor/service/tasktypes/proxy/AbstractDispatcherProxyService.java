package at.jku.dke.etutor.service.tasktypes.proxy;
import at.jku.dke.etutor.config.ApplicationProperties;

/**
 * Abstract class offering some utility methods for the proxy services,
 * to interact with the dke-dispatcher.
 */
public abstract sealed class AbstractDispatcherProxyService extends AbstractProxyService permits
    DatalogProxyService,
    DkeSubmissionProxyService,
    PmProxyService,
    SqlProxyService,
    XQueryProxyService {
    protected AbstractDispatcherProxyService(ApplicationProperties properties){
        super(properties.getDispatcher().getUrl());
    }
}
