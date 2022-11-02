package at.jku.dke.etutor.config;

import at.jku.dke.etutor.helper.FusekiRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class of the RDF connection factory bean.
 *
 * @author fne
 */
@Configuration
public class RDFConnectionConfiguration {

    private RDFConnectionFactory connectionFactory;

    /**
     * Constructor.
     *
     * @param applicationProperties the injected application properties
     */
    public RDFConnectionConfiguration(ApplicationProperties applicationProperties) {
        connectionFactory = new FusekiRDFConnectionFactory(applicationProperties);
    }

    /**
     * Returns the RDF connection factory.
     *
     * @return the RDF connection factory
     */
    @Bean
    public RDFConnectionFactory getRDFConnection() {
        return connectionFactory;
    }
}
