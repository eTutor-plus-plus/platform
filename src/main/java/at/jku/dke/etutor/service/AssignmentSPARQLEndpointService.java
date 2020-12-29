package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.TaskDisplayDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.InternalTaskAssignmentNonexistentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

/**
 * SPARQL endpoint for assignment related operations.
 *
 * @author fne
 */
@Service
public class AssignmentSPARQLEndpointService extends AbstractSPARQLEndpointService {

    private static final String QRY_CONSTRUCT_TASK_ASSIGNMENTS_FROM_GOAL = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
        PREFIX etutor-difficulty: <http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#>

        CONSTRUCT { ?assignment ?predicate ?object.
        			?assignment etutor:isAssignmentOf ?othergoal.}
        WHERE {
          ?goal etutor:hasTaskAssignment ?assignment.
          ?assignment ?predicate ?object.
          ?othergoal etutor:hasTaskAssignment ?assignment.
        }
        """;

    private static final String QRY_DELETE_ASSIGNMENT = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        DELETE {
          ?assignment ?predicate ?object.
          ?goal etutor:hasTaskAssignment ?assignment.
        }
        WHERE {
          ?assignment ?predicate ?object.
          ?goal etutor:hasTaskAssignment ?assignment.
        }
        """;

    private static final String QRY_ASK_ASSIGNMENT_EXISTS = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        ASK {
          ?assignment a etutor:TaskAssignment.
        }
        """;

    private static final String QRY_CONSTRUCT_TASK_ASSIGNMENTS = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
        PREFIX etutor-difficulty: <http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#>

        CONSTRUCT { ?assignment ?predicate ?object.
          ?assignment etutor:isAssignmentOf ?goal.}
        WHERE {
          ?assignment ?predicate ?object.
          ?assignment a etutor:TaskAssignment.
          OPTIONAL {
            ?goal etutor:hasTaskAssignment ?assignment.
          }
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

        for (String learningGoalId : newTaskAssignmentDTO.getLearningGoalIds()) {
            Resource assignmentResource = model.createResource(learningGoalId);
            assignmentResource.addProperty(ETutorVocabulary.hasTaskAssignment, newTaskAssignment);
        }

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

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_CONSTRUCT_TASK_ASSIGNMENTS_FROM_GOAL);
        query.setIri("?goal", goalId);

        try (RDFConnection connection = getConnection()) {
            Model model = connection.queryConstruct(query.asQuery());
            SortedSet<TaskAssignmentDTO> taskAssignments = new TreeSet<>();

            ResIterator subjectIterator = model.listSubjects();

            try {
                while (subjectIterator.hasNext()) {
                    Resource taskAssignmentResource = subjectIterator.nextResource();
                    try {
                        taskAssignments.add(new TaskAssignmentDTO(taskAssignmentResource));
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

    /**
     * Removes the task assignment.
     *
     * @param assignmentUuid the internal generated uuid of a task assignment
     */
    public void removeTaskAssignment(String assignmentUuid) {
        Objects.requireNonNull(assignmentUuid);
        String assignmentId = String.format("http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignment#%s", assignmentUuid);

        ParameterizedSparqlString parameterizedQry = new ParameterizedSparqlString(QRY_DELETE_ASSIGNMENT);
        parameterizedQry.setIri("?assignment", assignmentId);

        try (RDFConnection connection = getConnection()) {
            connection.update(parameterizedQry.asUpdate());
        }
    }

    /**
     * Updates the given task assignment. Task learning goal assignment changes are ignored!
     *
     * @param taskAssignment the task assignment to update
     * @throws InternalTaskAssignmentNonexistentException if the given assignment does not exist
     */
    public void updateTaskAssignment(TaskAssignmentDTO taskAssignment) throws InternalTaskAssignmentNonexistentException {
        Objects.requireNonNull(taskAssignment);

        ParameterizedSparqlString existQuery = new ParameterizedSparqlString(QRY_ASK_ASSIGNMENT_EXISTS);
        existQuery.setIri("?assignment", taskAssignment.getId());

        try (RDFConnection connection = getConnection()) {
            boolean assignmentExist = connection.queryAsk(existQuery.asQuery());

            if (!assignmentExist) {
                throw new InternalTaskAssignmentNonexistentException();
            }

            ParameterizedSparqlString query = new ParameterizedSparqlString();
            query.append("""
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

                DELETE {
                  ?assignment ?predicate ?object
                } INSERT {
                """);

            query.append("?assignment etutor:hasTaskHeader ");
            query.appendLiteral(taskAssignment.getHeader());
            query.append(".\n");

            if (StringUtils.isNotBlank(taskAssignment.getProcessingTime())) {
                query.append("?assignment etutor:hasTypicalProcessingTime ");
                query.appendLiteral(taskAssignment.getProcessingTime().trim());
                query.append(".\n");
            }

            query.append("?assignment etutor:hasTaskDifficulty ");
            query.appendIri(taskAssignment.getTaskDifficultyId());
            query.append(".\n");

            query.append("?assignment etutor:hasTaskOrganisationUnit ");
            query.appendLiteral(taskAssignment.getOrganisationUnit());
            query.append(".\n");

            if (taskAssignment.getUrl() != null) {
                query.append("?assignment etutor:hasTaskUrl ");
                query.appendLiteral(taskAssignment.getUrl().toString());
                query.append(".\n");
            }

            if (StringUtils.isNotBlank(taskAssignment.getInstruction())) {
                query.append("?assignment etutor:hasTaskInstruction ");
                query.appendLiteral(taskAssignment.getInstruction().trim());
                query.append(".\n");
            }

            query.append("?assignment etutor:isPrivateTask ");
            query.appendLiteral(String.valueOf(taskAssignment.isPrivateTask()), XSDDatatype.XSDboolean);
            query.append(".\n");

            query.append("""
                } WHERE {
                  ?assignment ?predicate ?object.
                  FILTER(?predicate NOT IN (etutor:hasTaskCreationDate, etutor:hasTaskCreator, rdf:type))
                }
                """);

            query.setIri("?assignment", taskAssignment.getId());

            connection.update(query.asUpdate());
        }
    }

    /**
     * Updates the task assignment.
     *
     * @param taskAssignmentId the id of the initial task assignment.
     * @param goalIds          the list of goals which should be associated with the given task
     * @throws InternalTaskAssignmentNonexistentException if the initial task assignment can not be found
     */
    public void setTaskAssignment(String taskAssignmentId, List<String> goalIds) throws InternalTaskAssignmentNonexistentException {
        Objects.requireNonNull(taskAssignmentId);
        Objects.requireNonNull(goalIds);

        ParameterizedSparqlString existQuery = new ParameterizedSparqlString(QRY_ASK_ASSIGNMENT_EXISTS);
        String assignment = String.format("http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignment#%s", taskAssignmentId);
        existQuery.setIri("?assignment", assignment);

        try (RDFConnection connection = getConnection()) {
            boolean assignmentExist = connection.queryAsk(existQuery.asQuery());

            if (!assignmentExist) {
                throw new InternalTaskAssignmentNonexistentException();
            }

            ParameterizedSparqlString updateQuery = new ParameterizedSparqlString();
            updateQuery.append("""
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                DELETE { ?goal etutor:hasTaskAssignment ?assignment }
                INSERT {
                """);

            for (String goalId : goalIds) {
                updateQuery.appendIri(goalId);
                updateQuery.append(" etutor:hasTaskAssignment ?assignment.\n");
            }

            updateQuery.append("""
                }
                WHERE {
                	?goal etutor:hasTaskAssignment ?assignment
                }
                """);

            updateQuery.setIri("?assignment", assignment);
            connection.update(updateQuery.asUpdate());
        }
    }

    /**
     * Returns all tasks assignments. An optional header filter can be passed to this method.
     *
     * @param headerFilter the optional header filter, might be null
     * @return {@code List} containing the task assignments which are found by the given filter
     * @throws MalformedURLException if a url can not be parsed (internal)
     * @throws ParseException        if a date can not be parsed (internal)
     */
    public List<TaskAssignmentDTO> getTaskAssignments(String headerFilter) throws MalformedURLException, ParseException {
        ParameterizedSparqlString qry = new ParameterizedSparqlString();
        qry.append("""
            PREFIX text:   <http://jena.apache.org/text#>
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            CONSTRUCT { ?assignment ?predicate ?object.
              ?assignment etutor:isAssignmentOf ?othergoal.}
            WHERE {
            """);

        if (StringUtils.isNotBlank(headerFilter)) {
            qry.append(String.format("?assignment text:query (etutor:hasTaskHeader \"*%s*\").%n", headerFilter));
        }

        qry.append("""
              ?assignment a etutor:TaskAssignment.
              ?assignment ?predicate ?object.
              OPTIONAL {
                ?othergoal etutor:hasTaskAssignment ?assignment.
              }
            }
            """);

        try (RDFConnection connection = getConnection()) {
            List<TaskAssignmentDTO> taskList = new ArrayList<>() {
                @Serial
                private static final long serialVersionUID = 1L;

                /**
                 * Appends the specified element to the end of this list.
                 *
                 * @param e element to be appended to this list
                 * @return {@code true} (as specified by {@link Collection#add})
                 */
                @Override
                public boolean add(TaskAssignmentDTO e) {
                    int index = Collections.binarySearch(this, e);
                    if (index < 0) {
                        index = ~index;
                    }
                    super.add(index, e);
                    return true;
                }
            };

            Model model = connection.queryConstruct(qry.asQuery());

            ResIterator iterator = model.listSubjects();

            try {
                while (iterator.hasNext()) {
                    Resource resource = iterator.nextResource();
                    taskList.add(new TaskAssignmentDTO(resource));
                }
            } finally {
                iterator.close();
            }

            return taskList;
        }
    }

    /**
     * Returns the task by its internal id.
     *
     * @param id the task's internal id
     * @return {@link Optional} which contains the task, if a task with the given id exists
     */
    public Optional<TaskAssignmentDTO> getTaskAssignmentByInternalId(String id) {
        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_CONSTRUCT_TASK_ASSIGNMENTS);

        String taskAssignmentId = String.format("http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignment#%s", id);
        query.setIri("?assignment", taskAssignmentId);

        try (RDFConnection connection = getConnection()) {
            Model model = connection.queryConstruct(query.asQuery());

            if (model.isEmpty()) {
                return Optional.empty();
            }
            ResIterator iterator = model.listSubjects();

            try {
                Resource resource = iterator.nextResource();
                return Optional.of(new TaskAssignmentDTO(resource));
            } finally {
                iterator.close();
            }
        } catch (ParseException | MalformedURLException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns a paged task display (task header + id). An optional header filter may be passed to this method.
     *
     * @param headerFilter the optional header filter, might be null
     * @param pageable     the mandatory pageable object
     * @return {@code Slice} containing the elements
     */
    public Slice<TaskDisplayDTO> findAllTasks(String headerFilter, Pageable pageable) {
        Objects.requireNonNull(headerFilter);
        Objects.requireNonNull(pageable);

        ParameterizedSparqlString qry = new ParameterizedSparqlString();
        qry.append("""
            PREFIX text:   <http://jena.apache.org/text#>
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT DISTINCT (STR(?assignment) AS ?assignmentId) ?header
            WHERE {
            """);

        if (StringUtils.isNotBlank(headerFilter)) {
            qry.append(String.format("?assignment text:query (etutor:hasTaskHeader \"*%s*\").%n", headerFilter));
        }

        qry.append("""
              ?assignment a etutor:TaskAssignment.
              ?assignment etutor:hasTaskHeader ?header
            }
            ORDER BY (LCASE(?header))
            """);

        if (pageable.isPaged()) {
            qry.append("LIMIT ");
            qry.append(pageable.getPageSize() + 1);
            qry.append("\nOFFSET ");
            qry.append(pageable.getOffset());
        }

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution queryExecution = connection.query(qry.asQuery())) {
                ResultSet set = queryExecution.execSelect();
                List<TaskDisplayDTO> resultList = new ArrayList<>();

                while (set.hasNext()) {
                    QuerySolution querySolution = set.nextSolution();

                    resultList.add(new TaskDisplayDTO(querySolution.getLiteral("?assignmentId").getString(),
                        querySolution.getLiteral("?header").getString()));
                }

                boolean hasNext = pageable.isPaged() && resultList.size() > pageable.getPageSize();
                return new SliceImpl<>(hasNext ? resultList.subList(0, pageable.getPageSize()) : resultList, pageable, hasNext);
            }
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
