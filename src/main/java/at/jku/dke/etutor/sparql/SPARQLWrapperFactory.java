package at.jku.dke.etutor.sparql;

/**
 * Interface of the SPARQLWrapper factory which is used
 * to provider individual operations for different
 * SPARQL endpoints.
 *
 * @author fne
 */
public interface SPARQLWrapperFactory {

    /**
     * Creates the corresponding SPARQL wrapper builder.
     *
     * @return the corresponding SPARQL wrapper builder
     */
    SPARQLWrapperBuilder createBuilder();
}
