package at.jku.dke.etutor.helper;

import at.jku.dke.etutor.config.ApplicationProperties;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;

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
        return RDFConnectionFuseki.create().destination(applicationProperties.getFuseki().getBaseUrl()).build();
    }

    /**
     * Clears the dataset (only works in embedded mode).
     */
    @Override
    public void clearDataset() {
        //Not implemented!
    }

    /**
     * Returns whether a hashtag replacement is needed or not.
     *
     * @return {@code true} if a hashtag replacement is needed, otherwise {@code false}
     */
    @Override
    public boolean needsHashtagReplacement() {
        return true;
    }
}
