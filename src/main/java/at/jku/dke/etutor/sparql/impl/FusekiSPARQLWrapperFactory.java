package at.jku.dke.etutor.sparql.impl;

import at.jku.dke.etutor.sparql.SPARQLWrapperBuilder;
import at.jku.dke.etutor.sparql.SPARQLWrapperFactory;

/**
 * Concrete SPARQL wrapper factory implementation for the Fuseki
 * specific SPARQL dialect.
 *
 * @author fne
 */
public class FusekiSPARQLWrapperFactory implements SPARQLWrapperFactory {
    /**
     * Creates the corresponding SPARQL wrapper builder.
     *
     * @return the corresponding SPARQL wrapper builder
     */
    @Override
    public SPARQLWrapperBuilder createBuilder() {
        return null;
    }
}
