package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 * SPARQL endpoint for assignment related operations.
 *
 * @author fne
 */
@Service
public class AssignmentSPARQLEndpointService extends AbstractSPARQLEndpointService {

    private static final String QRY_DESCRIBE_TASK_ASSIGNMENTS_FROM_GOAL = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        DESCRIBE ?assignment
        WHERE {
          ?goal etutor:hasTaskAssignment ?assignment
        }
        """;

    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public AssignmentSPARQLEndpointService(RDFConnectionFactory rdfConnectionFactory) {
        super(rdfConnectionFactory);
    }

    /**
     * Inserts a new task assignment.
     *
     * @param newTaskAssignmentDTO the task assignment dto to persist
     * @return the inserted task assignment dto
     */
    public TaskAssignmentDTO insertNewTaskAssignment(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        Objects.requireNonNull(newTaskAssignmentDTO);

        Instant now = Instant.now();
        String newId = UUID.randomUUID().toString();

        Model model = ModelFactory.createDefaultModel();

        Resource newTaskAssignment = constructTaskAssignmentFromDTO(newTaskAssignmentDTO, newId, now, model);

        Resource assignmentResource = model.createResource(newTaskAssignmentDTO.getLearningGoalId());
        assignmentResource.addProperty(ETutorVocabulary.hasTaskAssignment, newTaskAssignment);

        try (RDFConnection connection = getConnection()) {

            connection.load(model);
        }

        return new TaskAssignmentDTO(newTaskAssignmentDTO, newTaskAssignment.getURI(), now);
    }

    /**
     * Returns all assignments of a given learning goal.
     *
     * @param goalName  the learning goal's name
     * @param goalOwner the learning goal's owner
     * @return the sorted set of task assignments of the given learning goal
     * @throws InternalModelException if an internal parsing exception occurs
     */
    public SortedSet<TaskAssignmentDTO> getTaskAssignmentsOfGoal(String goalName, String goalOwner) throws InternalModelException {
        Objects.requireNonNull(goalName);
        Objects.requireNonNull(goalOwner);

        String goalId = String.format("http://www.dke.uni-linz.ac.at/etutorpp/%s/Goal#%s", goalOwner, goalName);

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_DESCRIBE_TASK_ASSIGNMENTS_FROM_GOAL);
        query.setIri("?goal", goalId);

        try (RDFConnection connection = getConnection()) {
            Model model = connection.queryDescribe(query.asQuery());
            SortedSet<TaskAssignmentDTO> taskAssignments = new TreeSet<>();

            ResIterator subjectIterator = model.listSubjects();

            try {
                while (subjectIterator.hasNext()) {
                    Resource taskAssignmentResource = subjectIterator.nextResource();
                    try {
                        taskAssignments.add(new TaskAssignmentDTO(taskAssignmentResource, goalId));
                    } catch (ParseException | MalformedURLException e) {
                        throw new InternalModelException(e);
                    }
                }
            } finally {
                subjectIterator.close();
            }

            return taskAssignments;
        }
    }

    //region Helper methods

    /**
     * Constructs a resource from the given data.
     *
     * @param newTaskAssignmentDTO the new task assignment dto
     * @param uuid                 the generated uuid
     * @param creationDate         the creation date
     * @param model                the rdf base model
     * @return {@link Resource} which represents the new task assignment
     */
    private Resource constructTaskAssignmentFromDTO(NewTaskAssignmentDTO newTaskAssignmentDTO, String uuid, Instant creationDate, Model model) {
        Resource resource = ETutorVocabulary.createTaskAssignmentResourceOfModel(uuid, model);

        resource.addProperty(ETutorVocabulary.hasTaskAssignment, model.createResource(newTaskAssignmentDTO.getLearningGoalId()));
        resource.addProperty(ETutorVocabulary.hasTaskCreator, newTaskAssignmentDTO.getCreator());
        resource.addProperty(ETutorVocabulary.hasTaskHeader, newTaskAssignmentDTO.getHeader());
        resource.addProperty(ETutorVocabulary.hasTaskCreationDate, instantToRDFString(creationDate), XSDDatatype.XSDdateTime);

        if (StringUtils.isNotBlank(newTaskAssignmentDTO.getProcessingTime())) {
            resource.addProperty(ETutorVocabulary.hasTypicalProcessingTime, newTaskAssignmentDTO.getProcessingTime().trim());
        }
        resource.addProperty(ETutorVocabulary.hasTaskDifficulty, model.createResource(newTaskAssignmentDTO.getTaskDifficultyId()));
        resource.addProperty(ETutorVocabulary.hasTaskOrganisationUnit, newTaskAssignmentDTO.getOrganisationUnit().trim());

        if (newTaskAssignmentDTO.getUrl() != null) {
            resource.addProperty(ETutorVocabulary.hasTaskUrl, newTaskAssignmentDTO.getUrl().toString());
        }

        if (StringUtils.isNotBlank(newTaskAssignmentDTO.getInstruction())) {
            resource.addProperty(ETutorVocabulary.hasTaskInstruction, newTaskAssignmentDTO.getInstruction().trim());
        }

        String privateStr = String.valueOf(newTaskAssignmentDTO.isPrivateTask());
        resource.addProperty(ETutorVocabulary.isPrivateTask, privateStr, XSDDatatype.XSDboolean);
        resource.addProperty(RDF.type, ETutorVocabulary.TaskAssignment);

        return resource;
    }
    //endregion
}
