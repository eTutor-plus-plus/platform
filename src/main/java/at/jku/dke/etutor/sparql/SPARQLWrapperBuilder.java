package at.jku.dke.etutor.sparql;

import org.apache.jena.query.ParameterizedSparqlString;

import java.util.Arrays;

/**
 * The SPARQL wrapper builder interface.
 *
 * @author fne
 */
public interface SPARQLWrapperBuilder {

    /**
     * Sets the prefixes.
     *
     * @param prefixes the prefixes to set
     */
    void setPrefixes(Iterable<String> prefixes);

    /**
     * Sets the prefixes.
     *
     * @param prefixes all prefixes as a single string to set
     */
    void setPrefixes(String prefixes);

    /**
     * Sets the prefixes.
     *
     * @param prefixes the var args prefixes.
     */
    default void setPrefixes(String... prefixes)
    {
        setPrefixes(Arrays.asList(prefixes));
    }

    /**
     * Sets the query body.
     *
     * @param queryBody the query body to set
     */
    void setQueryBody(String queryBody);

    /**
     * Adds a new fulltext query by replacing the token from the
     * query string.
     *
     * @param tokenToReplace the token to replace
     * @param fulltextQuery  the query
     * @param querySubject   the query's subject
     * @param queryPredicate the optional query predicate (currently only used in Fuseki)
     */
    void addFullTextQuery(String tokenToReplace, String fulltextQuery, String querySubject, String queryPredicate);

    /**
     * Adds a new fulltext query by replacing the token from the
     * query string.
     *
     * @param tokenToReplace the token to replace
     * @param fulltextQuery  the query
     * @param querySubject   the query's subject
     */
    default void addFullTextQuery(String tokenToReplace, String fulltextQuery, String querySubject) {
        addFullTextQuery(tokenToReplace, fulltextQuery, querySubject, null);
    }

    /**
     * Builds the SPARQL query as a String.
     *
     * @return the SPARQL query as a String
     */
    String build();

    /**
     * Builds the SPARQL query string.
     *
     * @return the SPARQL query string
     */
    ParameterizedSparqlString buildSparqlString();
}
