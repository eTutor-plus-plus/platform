package at.jku.dke.etutor.sparql;

import at.jku.dke.etutor.config.ApplicationProperties;
import at.jku.dke.etutor.sparql.impl.FusekiSPARQLWrapperFactory;
import at.jku.dke.etutor.sparql.impl.GraphDBSPARQLWrapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration bean of the SPARQL wrapper which
 * ensures that the correct SPARQLWrapper implementation
 * is instantiated.
 *
 * @author fne
 */
@Configuration
public class SPARQLWrapperConfiguration {

    private final SPARQLWrapperFactory sparqlWrapper;

    /**
     * Constructor.
     *
     * @param applicationProperties the injected application properties
     */
    public SPARQLWrapperConfiguration(ApplicationProperties applicationProperties) {
        sparqlWrapper = switch (applicationProperties.getSPARQLEndpointConfiguration().getSparqlEndpointType()) {
            case FUSEKI -> new FusekiSPARQLWrapperFactory();
            case GRAPHDB -> new GraphDBSPARQLWrapperFactory();
        };
    }

    /**
     * Returns the SPARQL wrapper factory.
     *
     * @return the SPARQL wrapper factory
     */
    @Bean
    public SPARQLWrapperFactory getSPARQLWrapper() {
        return sparqlWrapper;
    }
}
