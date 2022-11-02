package at.jku.dke.etutor.sparql.impl;

import at.jku.dke.etutor.sparql.FulltextParameters;

import java.util.Objects;

/**
 * Class which implements the SPARQL wrapper builder for Fuseki.
 *
 * @author fne
 */
public final class FusekiSPARQLWrapperBuilder extends AbstractSPARQLWrapperBuilder {

    private static final String PREFIX_ENTRY = "PREFIX text: <http://jena.apache.org/text#>";

    /**
     * Returns the fulltext prefix which must be implemented
     * by the corresponding builder wrapper.
     *
     * @return the fulltext prefix.
     */
    @Override
    protected String getFulltextPrefix() {
        return PREFIX_ENTRY;
    }

    /**
     * Returns the fulltext query part for the current SPARQL engine.
     *
     * @param queryParameter the query parameters
     * @return the substituted string ready for insertion
     */
    @Override
    protected String getFulltextQueryPart(FulltextParameters queryParameter) {
        Objects.requireNonNull(queryParameter.queryText());

        var queryPredicate = queryParameter.queryPredicate();

        if (queryPredicate == null) {
            queryPredicate = "rdfs:label";
        }

        return String.format("%s text:query (%s \"*%s*\")", queryParameter.querySubject(), queryPredicate,
            queryParameter.queryText());
    }
}
