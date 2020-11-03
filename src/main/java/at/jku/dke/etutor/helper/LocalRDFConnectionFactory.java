package at.jku.dke.etutor.helper;

import io.github.jhipster.config.JHipsterConstants;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Class which is used to created local RDF connections to an in-memory
 * dataset. For testing purposes only.
 *
 * @author fne
 */
public class LocalRDFConnectionFactory implements RDFConnectionFactory {

    private Dataset dataset;

    /**
     * Constructor.
     */
    public LocalRDFConnectionFactory() {
        dataset = DatasetFactory.createTxnMem();
    }

    /**
     * Constructor.
     *
     * @param dataset the dataset for the tests
     */
    public LocalRDFConnectionFactory(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * Returns the newly created RDF connection.
     *
     * @return the newly created RDF connection
     */
    @Override
    public RDFConnection getRDFConnection() {
        return org.apache.jena.rdfconnection.RDFConnectionFactory.connect(dataset);
    }
}
