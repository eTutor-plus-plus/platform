package at.jku.dke.etutor.service;

import at.jku.dke.etutor.helper.RDFConnectionFactory;
import org.springframework.stereotype.Service;

/**
 * Service for managing lecturer overview related data.
 *
 * @author fne
 */
@Service
public class LecturerOverviewService extends AbstractSPARQLEndpointService {

    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public LecturerOverviewService(RDFConnectionFactory rdfConnectionFactory) {
        super(rdfConnectionFactory);
    }
}
