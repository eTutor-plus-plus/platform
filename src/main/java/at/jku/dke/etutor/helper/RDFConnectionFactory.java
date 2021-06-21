package at.jku.dke.etutor.helper;

import org.apache.jena.rdfconnection.RDFConnection;

/**
 * Interface which is used to get an rdf connection.
 *
 * @author fne
 */
public interface RDFConnectionFactory {
    /**
     * Returns the newly created rdf connection.
     *
     * @return the newly created rdf connection
     */
    RDFConnection getRDFConnection();

    /**
     * Returns the rdf connection which is used to access
     * the original dataset (without inference)
     *
     * @return the connection
     */
    RDFConnection getRDFConnectionToOriginalDataset();

    /**
     * Clears the dataset (only works in embedded mode).
     */
    void clearDataset();
}
