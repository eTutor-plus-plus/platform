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
        return RDFConnectionFuseki.create().destination(getURLWithSlash() + applicationProperties.getFuseki().getInferenceDatasetName()).build();
    }

    /**
     * Returns the rdf connection which is used to access
     * the original dataset (without inference)
     *
     * @return the connection
     */
    @Override
    public RDFConnection getRDFConnectionToOriginalDataset() {
        return RDFConnectionFuseki.create().destination(getURLWithSlash() + applicationProperties.getFuseki().getOriginalDatasetName()).build();
    }

    /**
     * Clears the dataset (only works in embedded mode).
     */
    @Override
    public void clearDataset() {
        //Not implemented!
    }

    /**
     * Returns the base URL and ensures that it ends with a '/'.
     *
     * @return the base URL to the fuseki instance
     */
    private String getURLWithSlash() {
        String baseUrl = applicationProperties.getFuseki().getBaseUrl();
        if (baseUrl.length() > 0 && baseUrl.charAt(baseUrl.length() - 1) != '/') {
            baseUrl += '/';
        }

        return baseUrl;
    }
}
