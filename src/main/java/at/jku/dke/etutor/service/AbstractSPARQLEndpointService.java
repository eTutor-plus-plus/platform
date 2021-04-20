package at.jku.dke.etutor.service;

import at.jku.dke.etutor.helper.RDFConnectionFactory;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.jena.rdfconnection.RDFConnection;

/**
 * Abstract base class of all SPARQL endpoint services.
 *
 * @author fne
 */
public abstract class AbstractSPARQLEndpointService {

    private final RDFConnectionFactory rdfConnectionFactory;

    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    protected AbstractSPARQLEndpointService(RDFConnectionFactory rdfConnectionFactory) {
        this.rdfConnectionFactory = rdfConnectionFactory;
    }

    /**
     * Creates a new rdf connection to the configured fuseki server.
     *
     * @return new rdf connection
     */
    protected RDFConnection getConnection() {
        return rdfConnectionFactory.getRDFConnection();
    }

    /**
     * Converts the given instant into an rdf conform string representation.
     *
     * @param instant the instant to convert.
     * @return the string representation of the given instant
     */
    protected String instantToRDFString(Instant instant) {
        Objects.requireNonNull(instant);

        return DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(Date.from(instant));
    }

    /**
     * Returns the instant from the formatted instant string.
     *
     * @param instantAsString the formatted string
     * @return the instant of the formatted string
     * @throws ParseException if a parsing error occurs
     */
    protected Instant instantFromRDFString(String instantAsString) throws ParseException {
        Objects.requireNonNull(instantAsString);

        return DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(instantAsString).toInstant();
    }
}
