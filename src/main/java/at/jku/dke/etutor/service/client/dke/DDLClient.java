package at.jku.dke.etutor.service.client.dke;

import at.jku.dke.etutor.config.ApplicationProperties;
import org.springframework.stereotype.Service;

/**
 * Client for interacting with the sql ddl endpoint of the dispatcher.
 */
@Service
public non-sealed class DDLClient extends AbstractDispatcherClient {
    public DDLClient(ApplicationProperties properties) {
        super(properties);
    }


}
