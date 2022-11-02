package at.jku.dke.etutor.sparql.impl;

import at.jku.dke.etutor.sparql.FulltextParameters;

import java.util.Objects;

/**
 * Class which implements the SPARQL wrapper builder for Graph DB.
 *
 * @author fne
 */
public final class GraphDBSPARQLWrapperBuilder extends AbstractSPARQLWrapperBuilder {

    private static final String INDEX_NAME = "etutor_fts";
    private static final String PREFIX_ENTRY = """
        PREFIX con: <http://www.ontotext.com/connectors/lucene#>
        PREFIX con-inst: <http://www.ontotext.com/connectors/lucene/instance#>
        """;

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

        return String.format("""
            [] a con-inst:%s ;
               con:query \"*%s*\" ;
               con:entities %s .
            """);
    }
}
