package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * SPARQL endpoint service for managing course instances.
 */
@Service
public class CourseInstanceSPARQLEndpointService extends AbstractSPARQLEndpointService {
    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public CourseInstanceSPARQLEndpointService(RDFConnectionFactory rdfConnectionFactory) {
        super(rdfConnectionFactory);
    }

    /**
     * Creates a new course instance.
     *
     * @param newCourseInstanceDTO the new course dto
     * @return uri of the newly created course
     * @throws CourseNotFoundException if the course can not be found
     */
    public String createNewCourseInstance(NewCourseInstanceDTO newCourseInstanceDTO) throws CourseNotFoundException {
        Model model = ModelFactory.createDefaultModel();
        String uuid = UUID.randomUUID().toString();

        try (RDFConnection connection = getConnection()) {
            Resource resource = constructNewCourseInstanceFromDTO(newCourseInstanceDTO, model, uuid, connection);
            connection.load(model);

            copyLearningGoalsIntoNamedCourseInstanceGraph(resource.getURI(), connection);

            return resource.getURI();
        }
    }

    /**
     * Creates a new course instance from the given dto.
     *
     * @param newCourseInstanceDTO the dto
     * @param model                the base model
     * @param uuid                 the generated uuid
     * @return the new course instance resource
     * @throws CourseNotFoundException if the course does not exist
     */
    private Resource constructNewCourseInstanceFromDTO(NewCourseInstanceDTO newCourseInstanceDTO, Model model, String uuid, RDFConnection connection)
        throws CourseNotFoundException {
        Resource resource = ETutorVocabulary.createCourseInstanceOfModel(uuid, model);

        ParameterizedSparqlString courseQuery = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT ?courseName
            WHERE {
              ?course a etutor:Course.
              ?course rdfs:label ?courseName.
            }
            """);
        courseQuery.setIri("?course", newCourseInstanceDTO.getCourseId());

        String courseName;

        try (QueryExecution queryExecution = connection.query(courseQuery.asQuery())) {
            ResultSet set = queryExecution.execSelect();

            if (!set.hasNext()) {
                throw new CourseNotFoundException();
            }
            QuerySolution solution = set.nextSolution();
            courseName = solution.getLiteral("?courseName").getString();
        }

        resource.addProperty(ETutorVocabulary.hasCourse, model.createResource(newCourseInstanceDTO.getCourseId()));
        resource.addProperty(ETutorVocabulary.hasInstanceYear, String.valueOf(newCourseInstanceDTO.getYear()), XSDDatatype.XSDint);
        resource.addProperty(ETutorVocabulary.hasTerm, model.createResource(newCourseInstanceDTO.getTermId()));
        resource.addProperty(RDFS.label, String.format("%s %s %d", courseName, ETutorVocabulary.getTermTextFromUri(newCourseInstanceDTO.getTermId()),
            newCourseInstanceDTO.getYear()));
        resource.addProperty(RDF.type, ETutorVocabulary.CourseInstance);

        if (StringUtils.isNotBlank(newCourseInstanceDTO.getDescription())) {
            resource.addProperty(ETutorVocabulary.hasInstanceDescription, newCourseInstanceDTO.getDescription().trim());
        }

        return resource;
    }

    /**
     * Copies the learning goal structure into the named graph of the corresponding
     * course instance.
     *
     * @param courseInstanceId the internal course instance uri
     * @param connection       the rdf connection
     */
    private void copyLearningGoalsIntoNamedCourseInstanceGraph(String courseInstanceId, RDFConnection connection) {
        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            INSERT {
              GRAPH ?instance {
              	?goal a etutor:Goal.
              }
            }
            WHERE {
              ?instance a etutor:CourseInstance.
              ?instance etutor:hasCourse ?course.
              ?course etutor:hasGoal/etutor:hasSubGoal* ?goal.
            }
            """);
        query.setIri("?instance", courseInstanceId);

        connection.update(query.asUpdate());
    }
}
