package at.jku.dke.etutor.service;

import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.lectureroverview.StatisticsOverviewModelDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Objects;

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


    public StatisticsOverviewModelDTO getCourseInstanceOverviewStatistics(String courseInstanceID) {
        Objects.requireNonNull(courseInstanceID);

        return null;
    }
}
