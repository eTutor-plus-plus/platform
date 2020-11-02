package at.jku.dke.etutor.config;

import at.jku.dke.etutor.helper.LocalRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * q
 *
 * @author fne
 */
@TestConfiguration
public class RDFConnectionTestConfiguration {

    private RDFConnectionFactory connectionFactory;

    /**
     * Constructor.
     */
    public RDFConnectionTestConfiguration() {
        connectionFactory = new LocalRDFConnectionFactory();
    }

    /**
     *
     *
     * @return
     */
    @Bean
    public RDFConnectionFactory getRDFConnection() {
        return connectionFactory;
    }
}
