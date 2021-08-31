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
     * Clears the dataset (only works in embedded mode).
     */
    void clearDataset();

    /**
     * Returns whether a hashtag replacement is needed or not.
     *
     * @return {@code true} if a hashtag replacement is needed, otherwise {@code false}
     */
    boolean needsHashtagReplacement();
}
