package at.jku.dke.etutor.sparql.impl;

import at.jku.dke.etutor.sparql.FulltextParameters;
import at.jku.dke.etutor.sparql.SPARQLWrapperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.ParameterizedSparqlString;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract base class of all SPARQL wrapper builders.
 *
 * @author fne
 */
public abstract sealed class AbstractSPARQLWrapperBuilder implements SPARQLWrapperBuilder
    permits FusekiSPARQLWrapperBuilder, GraphDBSPARQLWrapperBuilder {

    private String prefixes;
    private String queryBody;
    protected Map<String, FulltextParameters> fulltextQueries = new HashMap<>();

    /**
     * Sets the prefixes.
     *
     * @param prefixes the prefixes to set
     */
    @Override
    public void setPrefixes(Iterable<String> prefixes) {
        var builder = new StringBuilder();
        for (var entry : prefixes) {
            builder.append(entry);
            builder.append("\n");
        }
        this.prefixes = builder.toString();
    }

    /**
     * Sets the prefixes.
     *
     * @param prefixes all prefixes as a single string to set
     */
    @Override
    public void setPrefixes(String prefixes) {
        this.prefixes = prefixes;
    }

    /**
     * Sets the query body.
     *
     * @param queryBody the query body to set
     */
    @Override
    public void setQueryBody(String queryBody) {
        this.queryBody = queryBody;
    }

    /**
     * Adds a new fulltext query by replacing the token from the
     * query string.
     *
     * @param tokenToReplace the token to replace
     * @param fulltextQuery  the query
     * @param querySubject   the query's subject
     * @param queryPredicate the optional query predicate (currently only used in Fuseki)
     */
    @Override
    public void addFullTextQuery(String tokenToReplace, String fulltextQuery, String querySubject, String queryPredicate) {
        Objects.requireNonNull(tokenToReplace);
        Objects.requireNonNull(fulltextQuery);
        Objects.requireNonNull(querySubject);

        fulltextQueries.put(tokenToReplace, new FulltextParameters(fulltextQuery, querySubject, queryPredicate));
    }

    /**
     * Builds the SPARQL query as a String.
     *
     * @return the SPARQL query as a String
     */
    @Override
    public String build() {
        if (StringUtils.isBlank(queryBody)) {
            throw new IllegalArgumentException("A query body must be provided!");
        }

        var builder = new StringBuilder();
        if (prefixes != null) {
            builder.append(prefixes);
        }

        if (fulltextQueries.isEmpty()) {
            builder.append(queryBody);
        } else {
            builder.append(getFulltextPrefix());

            var body = queryBody;
            for (var entry : fulltextQueries.entrySet()) {
                var substitutedQuery = getFulltextQueryPart(entry.getValue());

                body = body.replace(entry.getKey(), substitutedQuery);
            }
        }

        return builder.toString();
    }

    /**
     * Builds the SPARQL query string.
     *
     * @return the SPARQL query string
     */
    @Override
    public ParameterizedSparqlString buildSparqlString() {
        return new ParameterizedSparqlString(build());
    }

    /**
     * Returns the fulltext prefix which must be implemented
     * by the corresponding builder wrapper.
     *
     * @return the fulltext prefix.
     */
    protected abstract String getFulltextPrefix();

    /**
     * Returns the fulltext query part for the current SPARQL engine.
     *
     * @param queryParameter the query parameters
     * @return the substituted string ready for insertion
     */
    protected abstract String getFulltextQueryPart(FulltextParameters queryParameter);
}
