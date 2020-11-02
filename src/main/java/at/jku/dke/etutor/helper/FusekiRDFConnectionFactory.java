package at.jku.dke.etutor.helper;

import at.jku.dke.etutor.config.ApplicationProperties;
import io.github.jhipster.config.JHipsterConstants;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Class which is used to create an rdf connection to the configured fuseki server.
 *
 * @author fne
 */
public class FusekiRDFConnectionFactory implements RDFConnectionFactory {

    private final ApplicationProperties applicationProperties;

    /**
     * Constructor.
     *
     * @param applicationProperties the application properties.
     */
    public FusekiRDFConnectionFactory(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Returns the newly created rdf connection.
     *
     * @return the newly created rdf connection
     */
    @Override
    public RDFConnection getRDFConnection() {
        return RDFConnectionFuseki.create()
            .destination(applicationProperties.getFuseki().getBaseUrl()).build();
    }
}
