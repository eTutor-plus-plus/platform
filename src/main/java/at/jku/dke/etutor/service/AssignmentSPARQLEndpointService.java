package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.TaskDisplayDTO;
import at.jku.dke.etutor.service.dto.taskassignment.*;
import at.jku.dke.etutor.service.exception.InternalTaskAssignmentNonexistentException;
import at.jku.dke.etutor.service.exception.TaskGroupAlreadyExistentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

/**
 * SPARQL endpoint for assignment related operations.
 *
 * @author fne
 */
@Service
public non-sealed class AssignmentSPARQLEndpointService extends AbstractSPARQLEndpointService {

    private static final String QRY_CONSTRUCT_TASK_ASSIGNMENTS_FROM_GOAL =
        """
            PREFIX etutor:            <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX etutor-difficulty: <http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#>
            PREFIX rdfs:              <http://www.w3.org/2000/01/rdf-schema#>

            CONSTRUCT { ?assignment ?predicate ?object.
            			?assignment etutor:isAssignmentOf ?othergoal.
            			?othergoal rdfs:label ?goalName.
            			?assignment etutor:hasTaskGroup ?taskGroup. }
            WHERE {
                {
                ?goal etutor:hasTaskAssignment ?assignment.
                ?assignment etutor:isPrivateTask false.
                ?assignment ?predicate ?object.
                OPTIONAL {
                  ?taskGroup etutor:hasTask ?assignment.
                }
                ?othergoal etutor:hasTaskAssignment ?assignment.
                ?othergoal rdfs:label ?goalName.
              } UNION {
                ?goal etutor:hasTaskAssignment ?assignment.
                ?assignment etutor:isPrivateTask true.
                ?assignment etutor:hasInternalTaskCreator ?internalTaskOwner.
                ?assignment ?predicate ?object.
                OPTIONAL {
                  ?taskGroup etutor:hasTask ?assignment.
                }
                ?othergoal etutor:hasTaskAssignment ?assignment.
                ?othergoal rdfs:label ?goalName.
              }
            }
            """;


    private static final String QRY_ASK_MAX_POINTS_FOR_TASK_ASSIGNMENT = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>


        SELECT ?maxPoints WHERE {
              ?courseInstance a etutor:CourseInstance.
                ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                ?individualAssignment etutor:fromExerciseSheet ?sheet;
            etutor:fromCourseInstance ?courseInstance.
                ?individualAssignment etutor:hasIndividualTask ?individualTask.
                ?individualTask etutor:hasOrderNo ?orderNo;
                            etutor:refersToTask ?taskAssignment.
                ?taskAssignment etutor:hasMaxPoints ?maxPoints.
        }
        """;

    private static final String QRY_ASK_CALC_INFORMATION = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>


        SELECT ?maxPoints WHERE {
              ?courseInstance a etutor:CourseInstance.
                ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                ?individualAssignment etutor:fromExerciseSheet ?sheet;
            etutor:fromCourseInstance ?courseInstance.
                ?individualAssignment etutor:hasIndividualTask ?individualTask.
                ?individualTask etutor:hasOrderNo ?orderNo;
                            etutor:refersToTask ?taskAssignment.
                ?taskAssignment etutor:hasMaxPoints ?maxPoints.
                ?taskAssignment etutor:hasStartTime ?startTime.
                ?taskAssignment etutor:hasEndTime ?endTime.
        }
        """;



    private static final String QRY_DELETE_ASSIGNMENT =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?assignment ?predicate ?object.
              ?goal etutor:hasTaskAssignment ?assignment.
            }
            WHERE {
              ?assignment ?predicate ?object.
              OPTIONAL {
                ?goal etutor:hasTaskAssignment ?assignment.
              }
            }
            """;

    private static final String QRY_ASK_ASSIGNMENT_EXISTS =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            ASK {
              ?assignment a etutor:TaskAssignment.
            }
            """;

    private static final String QRY_CONSTRUCT_TASK_ASSIGNMENTS =
        """
            PREFIX etutor:            <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX etutor-difficulty: <http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#>
            PREFIX rdfs:              <http://www.w3.org/2000/01/rdf-schema#>

            CONSTRUCT { ?assignment ?predicate ?object.
              ?assignment etutor:isAssignmentOf ?goal.
              ?othergoal rdfs:label ?goalName.
              ?assignment etutor:hasTaskGroup ?taskGroup. }
            WHERE {
              ?assignment ?predicate ?object.
              ?assignment a etutor:TaskAssignment.
              OPTIONAL {
                ?goal etutor:hasTaskAssignment ?assignment.
                ?othergoal rdfs:label ?goalName.
              }
              OPTIONAL {
                ?taskGroup etutor:hasTask ?assignment.
              }
            }
            """;

    private static final String QRY_SELECT_LEARNING_GOAL_IDS_OF_ASSIGNMENT =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT (STR(?goal) as ?goalId)
            WHERE {
              ?assignment a etutor:TaskAssignment.
              ?goal etutor:hasTaskAssignment ?assignment.
            }
            """;

    private static final String QRY_SELECT_TASK_HEADERS_IDS_OF_GOAL =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT DISTINCT ?assignmentHeader (STR(?assignment) as ?id)
            WHERE {
              ?goal etutor:hasTaskAssignment ?assignment.
              ?assignment etutor:hasTaskHeader ?assignmentHeader
            }
            ORDER BY (LCASE(?assignmentHeader))
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
     * @param internalCreator      the internal creator of this task assignment
     * @return the inserted task assignment dto
     */
    public TaskAssignmentDTO insertNewTaskAssignment(NewTaskAssignmentDTO newTaskAssignmentDTO, String internalCreator) {
        Objects.requireNonNull(newTaskAssignmentDTO);
        Objects.requireNonNull(internalCreator);

        Instant now = Instant.now();
        String newId = UUID.randomUUID().toString();

        Model model = ModelFactory.createDefaultModel();

        Resource newTaskAssignment = constructTaskAssignmentFromDTO(newTaskAssignmentDTO, newId, now, model, internalCreator);

        for (LearningGoalDisplayDTO learningGoalDisplayDTO : newTaskAssignmentDTO.getLearningGoalIds()) {
            Resource assignmentResource = model.createResource(learningGoalDisplayDTO.getId());
            assignmentResource.addProperty(ETutorVocabulary.hasTaskAssignment, newTaskAssignment);
        }

        try (RDFConnection connection = getConnection()) {
            connection.load(model);
        }

        return new TaskAssignmentDTO(newTaskAssignmentDTO, newTaskAssignment.getURI(), now, internalCreator);
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
        query.setLiteral("?internalTaskOwner", goalOwner);

        try (RDFConnection connection = getConnection()) {
            Model model = connection.queryConstruct(query.asQuery());
            SortedSet<TaskAssignmentDTO> taskAssignments = new TreeSet<>();

            ResIterator subjectIterator = model.listSubjects();

            try {
                while (subjectIterator.hasNext()) {
                    Resource taskAssignmentResource = subjectIterator.nextResource();
                    if (taskAssignmentResource.hasProperty(RDF.type, ETutorVocabulary.TaskAssignment)) {
                        try {
                            taskAssignments.add(new TaskAssignmentDTO(taskAssignmentResource));
                        } catch (ParseException | MalformedURLException e) {
                            throw new InternalModelException(e);
                        }
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
            query.append(
                """
                    PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

                    DELETE {
                      ?assignment ?predicate ?object.
                      ?taskGroup etutor:hasTask ?assignment.
                    } INSERT {
                    """
            );

            query.append("?assignment etutor:hasTaskHeader ");
            query.appendLiteral(taskAssignment.getHeader());
            query.append(".\n");

            if (StringUtils.isNotBlank(taskAssignment.getTaskIdForDispatcher())) {
                query.append("?assignment etutor:hasTaskIdForDispatcher ");
                query.appendLiteral(taskAssignment.getTaskIdForDispatcher().trim());
                query.append(".\n");
            }

            if (StringUtils.isNotBlank(taskAssignment.getSqlSolution())) {
                query.append("?assignment etutor:hasSQLSolution ");
                query.appendLiteral(taskAssignment.getSqlSolution().trim());
                query.append(".\n");
            }

            if(StringUtils.isNotBlank(taskAssignment.getMaxPoints())){
                query.append("?assignment etutor:hasMaxPoints ");
                query.appendLiteral(taskAssignment.getMaxPoints());
                query.append(".\n");
            }

            if(StringUtils.isNotBlank(taskAssignment.getDiagnoseLevelWeighting())){
                query.append("?assignment etutor:hasDiagnoseLevelWeighting ");
                query.appendLiteral(taskAssignment.getDiagnoseLevelWeighting());
                query.append(".\n");
            }

            if (StringUtils.isNotBlank(taskAssignment.getProcessingTime())) {
                query.append("?assignment etutor:hasTypicalProcessingTime ");
                query.appendLiteral(taskAssignment.getProcessingTime().trim());
                query.append(".\n");
            }

            query.append("?assignment etutor:hasTaskDifficulty ");
            query.appendIri(taskAssignment.getTaskDifficultyId());
            query.append(".\n");

            query.append("?assignment etutor:hasTaskAssignmentType ");
            query.appendIri(taskAssignment.getTaskAssignmentTypeId());
            query.append(".\n");

            query.append("?assignment etutor:hasTaskOrganisationUnit ");
            query.appendLiteral(taskAssignment.getOrganisationUnit());
            query.append(".\n");


            query.append("?assignment etutor:hasUploadFileId ");
            query.appendLiteral(taskAssignment.getUploadFileId());
            query.append(".\n");


            query.append("?assignment etutor:hasCalcSolutionFileId ");
            query.appendLiteral(taskAssignment.getCalcSolutionFileId());
            query.append(".\n");

            query.append("?assignment etutor:hasCalcInstructionFileId ");
            query.appendLiteral(taskAssignment.getCalcInstructionFileId());
            query.append(".\n");

            query.append("?assignment etutor:hasWriterInstructionFileId ");
            query.appendLiteral(taskAssignment.getWriterInstructionFileId());
            query.append(".\n");

            if (taskAssignment.getStartTime() != null) {
                query.append("?assignment etutor:hasStartTime ");
                query.appendLiteral(taskAssignment.getStartTime().toString());
                query.append(".\n");
            }

            if (taskAssignment.getEndTime() != null) {
                query.append("?assignment etutor:hasEndTime ");
                query.appendLiteral(taskAssignment.getEndTime().toString());
                query.append(".\n");
            }


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

            if(StringUtils.isNotBlank(taskAssignment.getxQuerySolution())){
                query.append("?assignment etutor:hasXQuerySolution ");
                query.appendLiteral(taskAssignment.getxQuerySolution().trim());
                query.append(".\n");
            }

            if(StringUtils.isNotBlank(taskAssignment.getxQueryXPathSorting())){
                query.append("?assignment etutor:hasXPathSorting ");
                query.appendLiteral(taskAssignment.getxQueryXPathSorting().trim());
                query.append(".\n");
            }

            if(StringUtils.isNotBlank(taskAssignment.getDatalogSolution())){
                query.append("?assignment etutor:hasDLGSolution ");
                query.appendLiteral(taskAssignment.getDatalogSolution().trim());
                query.append(".\n");
            }

            if(StringUtils.isNotBlank(taskAssignment.getDatalogQuery())){
                query.append("?assignment etutor:hasDLGQuery ");
                query.appendLiteral(taskAssignment.getDatalogQuery().trim());
                query.append(".\n");
            }

            if(StringUtils.isNotBlank(taskAssignment.getDatalogUncheckedTerms())){
                query.append("?assignment etutor:hasUncheckedDLGTerms ");
                query.appendLiteral(taskAssignment.getDatalogUncheckedTerms().trim());
                query.append(".\n");
            }

            if (StringUtils.isNotBlank(taskAssignment.getTaskGroupId())) {
                query.appendIri(taskAssignment.getTaskGroupId());
                query.append(" etutor:hasTask ?assignment.\n");
            }

            query.append("?assignment etutor:isPrivateTask ");
            query.appendLiteral(String.valueOf(taskAssignment.isPrivateTask()), XSDDatatype.XSDboolean);
            query.append(".\n");

            query.append(
                """
                    } WHERE {
                      ?assignment ?predicate ?object.
                      FILTER(?predicate NOT IN (etutor:hasTaskCreationDate, etutor:hasTaskCreator, rdf:type, etutor:hasInternalTaskCreator)).

                      OPTIONAL {
                        ?taskGroup etutor:hasTask ?assignment.
                      }
                    }
                    """
            );

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
            updateQuery.append(
                """
                    PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                    DELETE { ?goal etutor:hasTaskAssignment ?assignment }
                    INSERT {
                    """
            );

            for (String goalId : goalIds) {
                updateQuery.appendIri(goalId);
                updateQuery.append(" etutor:hasTaskAssignment ?assignment.\n");
            }

            updateQuery.append(
                """
                    }
                    WHERE {
                        OPTIONAL {
                    	    ?goal etutor:hasTaskAssignment ?assignment
                    	}
                    }
                    """
            );

            updateQuery.setIri("?assignment", assignment);
            connection.update(updateQuery.asUpdate());
        }
    }

    /**
     * Returns all tasks assignments. An optional header filter can be passed to this method.
     *
     * @param headerFilter the optional header filter, might be null
     * @param user         the currently logged-in user
     * @return {@code List} containing the task assignments which are found by the given filter
     * @throws MalformedURLException if a url can not be parsed (internal)
     * @throws ParseException        if a date can not be parsed (internal)
     */
    public List<TaskAssignmentDTO> getTaskAssignments(String headerFilter, String user) throws MalformedURLException, ParseException {
        ParameterizedSparqlString qry = new ParameterizedSparqlString();
        qry.append(
            """
                PREFIX text:   <http://jena.apache.org/text#>
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>

                CONSTRUCT { ?assignment ?predicate ?object.
                  ?assignment etutor:isAssignmentOf ?othergoal.
                  ?othergoal rdfs:label ?goalName. }
                WHERE {
                    {
                """
        );

        if (StringUtils.isNotBlank(headerFilter)) {
            qry.append(String.format("?assignment text:query (etutor:hasTaskHeader \"*%s*\").%n", headerFilter));
        }

        qry.append(
            """
                    ?assignment a etutor:TaskAssignment.
                    ?assignment ?predicate ?object.
                    ?assignment etutor:isPrivateTask false.
                    OPTIONAL {
                      ?othergoal etutor:hasTaskAssignment ?assignment.
                      ?othergoal rdfs:label ?goalName.
                    }
                  } UNION {
                """
        );

        if (StringUtils.isNotBlank(headerFilter)) {
            qry.append(String.format("?assignment text:query (etutor:hasTaskHeader \"*%s*\").%n", headerFilter));
        }

        qry.append(
            """
                    ?assignment a etutor:TaskAssignment.
                    ?assignment ?predicate ?object.
                    ?assignment etutor:isPrivateTask true.
                    ?assignment etutor:hasInternalTaskCreator ?internalTaskCreator
                    OPTIONAL {
                      ?othergoal etutor:hasTaskAssignment ?assignment.
                    }
                  }
                }
                """
        );

        qry.setLiteral("?hasInternalTaskCreator", user);

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
                    if (resource.hasProperty(RDF.type, ETutorVocabulary.TaskAssignment)) {
                        taskList.add(new TaskAssignmentDTO(resource));
                    }
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
            ResIterator iterator = model.listResourcesWithProperty(RDF.type, ETutorVocabulary.TaskAssignment);

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

    //TODO: check if this is needed
    /**
     * Returns the id of the calc solution file by its internal id
     *
     * @param assignmentId the task's internal id
     * @return {@link Optional} which contains the id of the calc solution of the task
     */
    public Optional<Integer> getFileIdOfCalcSolution (String assignmentId) {
        Objects.requireNonNull(assignmentId);
        try {
            return Optional.of(getTaskAssignmentByInternalId(assignmentId).get().getCalcSolutionFileId());
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }


    /**
     * Returns a paged task display (task header + id). An optional header filter may be passed to this method.
     *
     * @param headerFilter          the optional header filter, might be null
     * @param pageable              the mandatory pageable object
     * @param user                  the currently logged-in user
     * @param taskGroupHeaderFilter the optional task group header filter
     * @return {@code Slice} containing the elements
     */
    public Slice<TaskDisplayDTO> findAllTasks(String headerFilter, Pageable pageable, String user, String taskGroupHeaderFilter) {
        Objects.requireNonNull(pageable);
        Objects.requireNonNull(user);

        ParameterizedSparqlString qry = new ParameterizedSparqlString();
        qry.append(
            """
                PREFIX text:   <http://jena.apache.org/text#>
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>

                SELECT DISTINCT (STR(?assignment) AS ?assignmentId) ?header ?internalCreator ?privateTask
                WHERE {
                    {
                """
        );

        if (StringUtils.isNotBlank(headerFilter)) {
            qry.append(String.format("?assignment text:query (etutor:hasTaskHeader \"*%s*\").%n", headerFilter));
        }

        if (StringUtils.isNotBlank(taskGroupHeaderFilter)) {
            qry.append(String.format("?taskGroup text:query (etutor:hasTaskGroupName \"*%s*\").%n", taskGroupHeaderFilter));
            qry.append("?taskGroup a etutor:TaskGroup.\n");
            qry.append("?taskGroup etutor:hasTask ?assignment.\n");
        }

        qry.append(
            """
                    ?assignment a etutor:TaskAssignment.
                    ?assignment etutor:hasTaskHeader ?header.
                    ?assignment etutor:hasInternalTaskCreator ?internalCreator.
                    ?assignment etutor:isPrivateTask ?privateTask.
                    FILTER(?privateTask = false)
                  } UNION {
                """
        );

        if (StringUtils.isNotBlank(headerFilter)) {
            qry.append(String.format("?assignment text:query (etutor:hasTaskHeader \"*%s*\").%n", headerFilter));
        }

        if (StringUtils.isNotBlank(taskGroupHeaderFilter)) {
            qry.append(String.format("?taskGroup text:query (etutor:hasTaskGroupName \"*%s*\").%n", taskGroupHeaderFilter));
            qry.append("?taskGroup a etutor:TaskGroup.\n");
            qry.append("?taskGroup etutor:hasTask ?assignment.\n");
        }

        qry.append(
            """
                    ?assignment a etutor:TaskAssignment.
                    ?assignment etutor:hasTaskHeader ?header.
                    ?assignment etutor:hasInternalTaskCreator ?internalCreator.
                    ?assignment etutor:isPrivateTask ?privateTask.
                    FILTER(?internalCreator = ?loggedInUser && ?privateTask = true)
                  }
                }
                ORDER BY (LCASE(?header))
                """
        );

        if (pageable.isPaged()) {
            qry.append("LIMIT ");
            qry.append(pageable.getPageSize() + 1);
            qry.append("\nOFFSET ");
            qry.append(pageable.getOffset());
        }
        qry.setLiteral("?loggedInUser", user);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution queryExecution = connection.query(qry.asQuery())) {
                ResultSet set = queryExecution.execSelect();
                List<TaskDisplayDTO> resultList = new ArrayList<>();

                while (set.hasNext()) {
                    QuerySolution querySolution = set.nextSolution();

                    String assignmentId = querySolution.getLiteral("?assignmentId").getString();
                    String header = querySolution.getLiteral("?header").getString();
                    String internalCreator = querySolution.getLiteral("?internalCreator").getString();
                    boolean privateTask = querySolution.getLiteral("?privateTask").getBoolean();

                    resultList.add(new TaskDisplayDTO(assignmentId, header, internalCreator, privateTask));
                }

                boolean hasNext = pageable.isPaged() && resultList.size() > pageable.getPageSize();
                return new SliceImpl<>(hasNext ? resultList.subList(0, pageable.getPageSize()) : resultList, pageable, hasNext);
            }
        }
    }

    /**
     * Returns the list of assigned learning goal ids of the given task assignment.
     *
     * @param internalId the internal task assignment id
     * @return the list of assigned learning goal ids
     */
    public List<String> getAssignedLearningGoalIdsOfTaskAssignment(String internalId) {
        List<String> goalIds = new ArrayList<>();

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_SELECT_LEARNING_GOAL_IDS_OF_ASSIGNMENT);
        String taskAssignmentId = String.format("http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignment#%s", internalId);
        query.setIri("?assignment", taskAssignmentId);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    String goalId = solution.getLiteral("?goalId").getString();
                    goalIds.add(goalId);
                }

                return goalIds;
            }
        }
    }

    /**
     * Returns the list of task which are associated with the given learning goal.
     *
     * @param learningGoalName the learning goal's name
     * @param user             the user name
     * @return list of task assignment display dtos
     */
    public List<TaskAssignmentDisplayDTO> getTasksOfLearningGoal(String learningGoalName, String user) {
        Objects.requireNonNull(learningGoalName);
        Objects.requireNonNull(user);

        String encodedName = URLEncoder.encode(learningGoalName.replace(' ', '_'), StandardCharsets.UTF_8);

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_SELECT_TASK_HEADERS_IDS_OF_GOAL);
        Resource goalResource = ETutorVocabulary.createUserGoalResourceOfModel(user, encodedName, ModelFactory.createDefaultModel());
        query.setIri("?goal", goalResource.getURI());

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                List<TaskAssignmentDisplayDTO> taskHeaders = new ArrayList<>();
                ResultSet set = execution.execSelect();
                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    String header = solution.getLiteral("?assignmentHeader").getString();
                    String id = solution.getLiteral("?id").getString();
                    taskHeaders.add(new TaskAssignmentDisplayDTO(header, id));
                }
                return taskHeaders;
            }
        }
    }

    /**
     * Creates a new task group.
     *
     * @param newTaskGroupDTO the task group
     * @param creator         the creator
     * @return the newly created task group
     * @throws TaskGroupAlreadyExistentException if a task group with the name already exists
     */
    public TaskGroupDTO createNewTaskGroup(NewTaskGroupDTO newTaskGroupDTO, String creator) throws TaskGroupAlreadyExistentException {
        Objects.requireNonNull(newTaskGroupDTO);
        Objects.requireNonNull(creator);

        Model model = ModelFactory.createDefaultModel();

        Instant now = Instant.now();

        Resource resource = constructTaskGroupFromDTO(newTaskGroupDTO, creator, now, model);

        ParameterizedSparqlString existenceQuery = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            ASK {
              ?taskGroup a etutor:TaskGroup.
            }
            """);
        existenceQuery.setIri("?taskGroup", resource.getURI());

        try (RDFConnection connection = getConnection()) {

            if (connection.queryAsk(existenceQuery.asQuery())) {
                throw new TaskGroupAlreadyExistentException();
            }

            connection.load(model);
        }

        return new TaskGroupDTO(newTaskGroupDTO.getName(), newTaskGroupDTO.getDescription(), newTaskGroupDTO.getTaskGroupTypeId(),
            newTaskGroupDTO.getSqlCreateStatements(), newTaskGroupDTO.getSqlInsertStatementsSubmission(),
            newTaskGroupDTO.getSqlInsertStatementsDiagnose(), newTaskGroupDTO.getxQueryDiagnoseXML(), newTaskGroupDTO.getxQuerySubmissionXML(), newTaskGroupDTO.getDatalogFacts(), resource.getURI(), creator, now);
    }

    /**
     * Deletes a task group.
     *
     * @param name the name of the task group
     */
    public void deleteTaskGroup(String name) {
        Objects.requireNonNull(name);

        String id = ETutorVocabulary.getTaskGroupIdFromName(name);

        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?group ?predicate ?object.
            } WHERE {
              ?group ?predicate ?object.
            }
            """);
        query.setIri("?group", id);

        try (RDFConnection connection = getConnection()) {
            connection.update(query.asUpdate());
        }
    }

    /**
     * Persists the id which is assigned by the dispatcher for a task group
     * @param taskGroupDTO the task group to which to assign the id
     * @param id the id
     */
    public void setDispatcherIdForTaskGroup(TaskGroupDTO taskGroupDTO, int id){
        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            INSERT {
              ?group etutor:hasDispatcherTaskGroupId ?id.
            } WHERE {
              ?group a etutor:TaskGroup.
            }
            """);

        query.setIri("?group", taskGroupDTO.getId());
        query.setLiteral("?id", id);

        try (RDFConnection connection = getConnection()) {
            connection.update(query.asUpdate());
        }
    }

    /**
     * Returns the id that has been assigned by the dispatcher for a task group
     * @param taskGroupDTO the task group
     * @return the id if found, otherwise -1
     */
    public int getDispatcherIdForTaskGroup(TaskGroupDTO taskGroupDTO){
        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT ?id
            WHERE {
                ?group a etutor:TaskGroup;
                    etutor:hasDispatcherTaskGroupId ?id.

            }
            """);

        query.setIri("?group", taskGroupDTO.getId());

        try (RDFConnection connection = getConnection()) {
            try(var exec = connection.query(query.asQuery())){
                var set = exec.execSelect();
                if (set.hasNext()){
                    return set.next().get("?id").asLiteral().getInt();
                }
            }
        }
        return -1;
    }
    /**
     * Persists the modifications for task groups, currently only the description can be modified.
     *
     * @param taskGroupDTO the task group DTO
     * @return the modified task group
     */
    public TaskGroupDTO modifyTaskGroup(TaskGroupDTO taskGroupDTO) {
        Objects.requireNonNull(taskGroupDTO);

        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?group etutor:hasTaskGroupChangeDate ?oldChangeDate.
              ?group etutor:hasTaskGroupDescription ?oldDescription.
            } INSERT {
              ?group etutor:hasTaskGroupChangeDate ?newChangeDate.
            """);

        if (StringUtils.isNotBlank(taskGroupDTO.getDescription())) {
            query.append(" ?group etutor:hasTaskGroupDescription ?newDescription.");
            query.append("\n");
        }


        query.append("""
            } WHERE {
              ?group a etutor:TaskGroup.
              ?group etutor:hasTaskGroupChangeDate ?oldChangeDate.
              OPTIONAL {
                ?group etutor:hasTaskGroupDescription ?oldDescription.
              }
            }
            """);

        query.setIri("?group", taskGroupDTO.getId());
        Instant now = Instant.now();
        taskGroupDTO.setChangeDate(now);
        query.setLiteral("?newChangeDate", instantToRDFString(now), XSDDatatype.XSDdateTime);

        if (StringUtils.isNotBlank(taskGroupDTO.getDescription())) {
            query.setLiteral("?newDescription", taskGroupDTO.getDescription().trim());
        }

        try (RDFConnection connection = getConnection()) {
            connection.update(query.asUpdate());
        }
        return taskGroupDTO;
    }

    /**
     * Persists the modified taskGroup of type "SQL"
     * @param taskGroupDTO the taskGroupDTO
     * @return the modified taskGroup
     */
    public TaskGroupDTO modifySQLTaskGroup(TaskGroupDTO taskGroupDTO) {
        Objects.requireNonNull(taskGroupDTO);

        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?group etutor:hasSQLCreateStatements ?oldSQLCreateStatements.
              ?group etutor:hasSQLInsertStatementsSubmission ?oldSQLInsertStatementsSubmission.
              ?group etutor:hasSQLInsertStatementsDiagnose ?oldSQLInsertStatementsDiagnose.
            } INSERT {
            """);

        if (StringUtils.isNotBlank(taskGroupDTO.getSqlCreateStatements())) {
            query.append("  ?group etutor:hasSQLCreateStatements ?newSQLCreateStatements.");
            query.append("\n");
        }

        if (StringUtils.isNotBlank(taskGroupDTO.getSqlInsertStatementsSubmission())) {
            query.append("  ?group etutor:hasSQLInsertStatementsSubmission ?newSQLInsertStatementsSubmission.");
            query.append("\n");
        }

        if (StringUtils.isNotBlank(taskGroupDTO.getSqlInsertStatementsDiagnose())) {
            query.append("  ?group etutor:hasSQLInsertStatementsDiagnose ?newSQLInsertStatementsDiagnose.");
            query.append("\n");
        }
        query.append("""
            } WHERE {
              ?group a etutor:TaskGroup.
              OPTIONAL {
                ?group etutor:hasSQLCreateStatements ?oldSQLCreateStatements.
              }
              OPTIONAL {
                ?group etutor:hasSQLInsertStatementsSubmission ?oldSQLInsertStatementsSubmission.
              }
              OPTIONAL {
                ?group etutor:hasSQLInsertStatementsDiagnose ?oldSQLInsertStatementsDiagnose.
              }
            }
            """);

        query.setIri("?group", taskGroupDTO.getId());

        if (StringUtils.isNotBlank(taskGroupDTO.getSqlCreateStatements())) {
            query.setLiteral("?newSQLCreateStatements", taskGroupDTO.getSqlCreateStatements().trim());
        }
        if (StringUtils.isNotBlank(taskGroupDTO.getSqlInsertStatementsSubmission())) {
            query.setLiteral("?newSQLInsertStatementsSubmission", taskGroupDTO.getSqlInsertStatementsSubmission().trim());
        }
        if (StringUtils.isNotBlank(taskGroupDTO.getSqlInsertStatementsDiagnose())) {
            query.setLiteral("?newSQLInsertStatementsDiagnose", taskGroupDTO.getSqlInsertStatementsDiagnose().trim());
        }

        try (RDFConnection connection = getConnection()) {
            connection.update(query.asUpdate());
        }

        return taskGroupDTO;
    }

    /**
     * Adds the URL referencing a file to the task group
     * @param taskGroupDTO the task group
     * @return the task group
     */
    public TaskGroupDTO addXMLFileURL(TaskGroupDTO taskGroupDTO, String URL){
        Objects.requireNonNull(taskGroupDTO);

        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?group etutor:hasFileURL ?oldURL.
            } INSERT {
            """);

        if (StringUtils.isNotBlank(URL)) {
            query.append("  ?group etutor:hasFileURL ?newURL.");
            query.append("\n");
        }
        query.append("""
            } WHERE {
              ?group a etutor:TaskGroup.
              OPTIONAL {
                ?group etutor:hasFileURL ?oldURL.
              }
            }
            """);
        query.setIri("?group", taskGroupDTO.getId());

        if (StringUtils.isNotBlank(URL)) {
            query.setLiteral("?newURL", URL.trim());
        }
        try (RDFConnection connection = getConnection()) {
            connection.update(query.asUpdate());
        }

        return taskGroupDTO;
    }
    /**
     * Persists the modified taskGroup of type "XQuery"
     * @param taskGroupDTO the taskGroupDTO
     * @return the modified taskGroup
     */
    public TaskGroupDTO modifyXQueryTaskGroup(TaskGroupDTO taskGroupDTO) {
    Objects.requireNonNull(taskGroupDTO);

    ParameterizedSparqlString query = new ParameterizedSparqlString("""
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        DELETE {
          ?group etutor:hasDiagnoseXMLFile ?oldDiagnoseFile.
          ?group etutor:hasSubmissionXMLFile ?oldSubmissionFile.
        } INSERT {
        """);

    if (StringUtils.isNotBlank(taskGroupDTO.getxQueryDiagnoseXML())) {
        query.append("  ?group etutor:hasDiagnoseXMLFile ?newDiagnoseFile.");
        query.append("\n");
    }

    if (StringUtils.isNotBlank(taskGroupDTO.getxQuerySubmissionXML())) {
        query.append("  ?group etutor:hasSubmissionXMLFile ?newSubmissionFile.");
        query.append("\n");
    }

    query.append("""
        } WHERE {
          ?group a etutor:TaskGroup.
          OPTIONAL {
            ?group etutor:hasSubmissionXMLFile ?oldSubmissionFile.
          }
          OPTIONAL {
            ?group etutor:hasSubmissionXMLFile ?oldSubmissionFile.
          }
        }
        """);

    query.setIri("?group", taskGroupDTO.getId());

    if (StringUtils.isNotBlank(taskGroupDTO.getxQueryDiagnoseXML())) {
        query.setLiteral("?newDiagnoseFile", taskGroupDTO.getxQueryDiagnoseXML().trim());
    }
    if (StringUtils.isNotBlank(taskGroupDTO.getxQuerySubmissionXML())) {
        query.setLiteral("?newSubmissionFile", taskGroupDTO.getxQuerySubmissionXML().trim());
    }

    try (RDFConnection connection = getConnection()) {
        connection.update(query.asUpdate());
    }

    return taskGroupDTO;
    }

    public TaskGroupDTO modifyDLGTaskGroup(TaskGroupDTO taskGroupDTO){
        Objects.requireNonNull(taskGroupDTO);

        ParameterizedSparqlString query = new ParameterizedSparqlString("""
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        DELETE {
          ?group etutor:hasDatalogFacts ?oldFacts.
        } INSERT {
        """);

        if (StringUtils.isNotBlank(taskGroupDTO.getDatalogFacts())) {
            query.append("  ?group etutor:hasDatalogFacts ?newFacts.");
            query.append("\n");
        }


        query.append("""
        } WHERE {
          ?group a etutor:TaskGroup.
          OPTIONAL {
            ?group etutor:hasDatalogFacts ?oldFacts.
          }
        }
        """);

        query.setIri("?group", taskGroupDTO.getId());

        if (StringUtils.isNotBlank(taskGroupDTO.getDatalogFacts())) {
            query.setLiteral("?newFacts", taskGroupDTO.getDatalogFacts().trim());
        }

        try (RDFConnection connection = getConnection()) {
            connection.update(query.asUpdate());
        }

        return taskGroupDTO;
    }

    /**
     * Returns the task group by name.
     *
     * @param name the task group's name
     * @return {@link Optional} containing the task group, if found
     */
    public Optional<TaskGroupDTO> getTaskGroupByName(String name) {
        Objects.requireNonNull(name);
        String id = ETutorVocabulary.getTaskGroupIdFromName(name);

        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            CONSTRUCT {
              ?group ?predicate ?object.
            } WHERE {
              ?group ?predicate ?object.
            }
            """);
        query.setIri("?group", id);

        try (RDFConnection connection = getConnection()) {
            Model resultModel = connection.queryConstruct(query.asQuery());
            Resource resource = resultModel.getResource(id);

            if (resultModel.isEmpty() || resource == null) {
                return Optional.empty();
            }

            return Optional.of(new TaskGroupDTO(resource));
        }
    }

    /**
     * Returns the paged list of task groups.
     *
     * @param nameQry the name filter, may be null or blank
     * @param page    the page object, must be null
     * @return {@link Page} containing the task groups
     */
    public Page<TaskGroupDisplayDTO> getFilteredTaskGroupPaged(String nameQry, Pageable page) {
        Objects.requireNonNull(page);

        ParameterizedSparqlString countQry = new ParameterizedSparqlString("""
            PREFIX text:   <http://jena.apache.org/text#>
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT (COUNT(?taskGroup) AS ?cnt)
            WHERE {
            """);
        ParameterizedSparqlString selectQry = new ParameterizedSparqlString("""
            PREFIX text:   <http://jena.apache.org/text#>
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT (STR(?taskGroup) AS ?id) ?taskGroupName
            WHERE {
            """);

        if (StringUtils.isNotBlank(nameQry)) {
            countQry.append(String.format("?taskGroup text:query (rdfs:label \"*%s*\").%n", nameQry));
            selectQry.append(String.format("?taskGroup text:query (rdfs:label \"*%s*\").%n", nameQry));
        }

        countQry.append("""
              ?taskGroup a etutor:TaskGroup.
            }
            """);
        selectQry.append("""
              ?taskGroup a etutor:TaskGroup.
              ?taskGroup etutor:hasTaskGroupName ?taskGroupName
            }
            ORDER BY (LCASE(?taskGroupName))
            """);

        if (page.isPaged()) {
            selectQry.append("LIMIT ");
            selectQry.append(page.getPageSize());
            selectQry.append("\nOFFSET ");
            selectQry.append(page.getOffset());
        }

        try (RDFConnection connection = getConnection()) {
            List<TaskGroupDisplayDTO> taskGroupList = new ArrayList<>();
            long count;
            try (QueryExecution queryExecution = connection.query(selectQry.asQuery())) {
                ResultSet set = queryExecution.execSelect();

                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    String id = solution.getLiteral("?id").getString();
                    String taskGroupName = solution.getLiteral("?taskGroupName").getString();

                    taskGroupList.add(new TaskGroupDisplayDTO(id, taskGroupName));
                }
            }

            try (QueryExecution queryExecution = connection.query(countQry.asQuery())) {
                ResultSet set = queryExecution.execSelect();
                //noinspection ResultOfMethodCallIgnored
                set.hasNext();
                count = set.nextSolution().getLiteral("?cnt").getInt();
            }

            return PageableExecutionUtils.getPage(taskGroupList, page, () -> count);
        }
    }

    public Optional<Integer> getMaxPointsForTaskAssignmentByIndividualTask(String matriculationNumber, String courseInstanceUUID, String exerciseSheetUUID, int orderNo) {
        Objects.requireNonNull(matriculationNumber);
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        assert orderNo > 0;

        String courseInstanceURL = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetURL = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentURL = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_ASK_MAX_POINTS_FOR_TASK_ASSIGNMENT);
        query.setIri("?courseInstance", courseInstanceURL);
        query.setIri("?student", studentURL);
        query.setIri("?sheet", exerciseSheetURL);
        query.setLiteral("?orderNo", orderNo );

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                if (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    Literal pointsLiteral = solution.getLiteral("?maxPoints");

                    if (pointsLiteral == null) {
                        return Optional.of(0);
                    }
                    return Optional.of(pointsLiteral.getInt());
                } else {
                    return Optional.of(0);
                }
            }
        }
    }


    public Optional<String> getStartTimeForTaskAssignmentByIndividualTask(String matriculationNumber, String courseInstanceUUID, String exerciseSheetUUID, int orderNo) {
        Objects.requireNonNull(matriculationNumber);
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        assert orderNo > 0;

        String courseInstanceURL = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetURL = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentURL = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_ASK_MAX_POINTS_FOR_TASK_ASSIGNMENT);
        query.setIri("?courseInstance", courseInstanceURL);
        query.setIri("?student", studentURL);
        query.setIri("?sheet", exerciseSheetURL);
        query.setLiteral("?orderNo", orderNo );

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                if (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    Literal startTime = solution.getLiteral("?startTime");

                    if (startTime == null) {
                        return Optional.empty();
                    }
                    return Optional.of(startTime.getString());
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    public Optional<String> getEndTimeForTaskAssignmentByIndividualTask(String matriculationNumber, String courseInstanceUUID, String exerciseSheetUUID, int orderNo) {
        Objects.requireNonNull(matriculationNumber);
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        assert orderNo > 0;

        String courseInstanceURL = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetURL = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentURL = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_ASK_MAX_POINTS_FOR_TASK_ASSIGNMENT);
        query.setIri("?courseInstance", courseInstanceURL);
        query.setIri("?student", studentURL);
        query.setIri("?sheet", exerciseSheetURL);
        query.setLiteral("?orderNo", orderNo );

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                if (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    Literal endTime = solution.getLiteral("?endTime");

                    if (endTime == null) {
                        return Optional.empty();
                    }
                    return Optional.of(endTime.getString());
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    //region Helper methods

    /**
     * Constructs a task group resource based on the given parameters.
     *
     * @param newTaskGroupDTO the new task group DTO
     * @param creator         the creator
     * @param creationDate    the creation date
     * @param model           the model
     * @return {@link Resource} which represents the new task group
     */
    private Resource constructTaskGroupFromDTO(NewTaskGroupDTO newTaskGroupDTO, String creator, Instant creationDate, Model model) {
        Resource taskGroupResource = model.createResource(ETutorVocabulary.getTaskGroupIdFromName(newTaskGroupDTO.getName()));
        taskGroupResource.addProperty(RDF.type, ETutorVocabulary.TaskGroup);
        taskGroupResource.addProperty(ETutorVocabulary.hasTaskGroupName, newTaskGroupDTO.getName());
        taskGroupResource.addProperty(ETutorVocabulary.hasTaskGroupType, newTaskGroupDTO.getTaskGroupTypeId());

        if (StringUtils.isNotBlank(newTaskGroupDTO.getDescription())) {
            taskGroupResource.addProperty(ETutorVocabulary.hasTaskGroupDescription, newTaskGroupDTO.getDescription().trim());
        }
        if (StringUtils.isNotBlank(newTaskGroupDTO.getSqlCreateStatements())) {
            taskGroupResource.addProperty(ETutorVocabulary.hasSQLCreateStatements, newTaskGroupDTO.getSqlCreateStatements().trim());
        }
        if (StringUtils.isNotBlank(newTaskGroupDTO.getSqlInsertStatementsSubmission())) {
            taskGroupResource.addProperty(ETutorVocabulary.hasSQLInsertStatementsSubmission, newTaskGroupDTO.getSqlInsertStatementsSubmission().trim());
        }
        if (StringUtils.isNotBlank(newTaskGroupDTO.getSqlInsertStatementsDiagnose())) {
            taskGroupResource.addProperty(ETutorVocabulary.hasSQLInsertStatementsDiagnose, newTaskGroupDTO.getSqlInsertStatementsDiagnose().trim());
        }
        if(StringUtils.isNotBlank(newTaskGroupDTO.getxQueryDiagnoseXML())){
            taskGroupResource.addProperty(ETutorVocabulary.hasDiagnoseXMLFile, newTaskGroupDTO.getxQueryDiagnoseXML().trim());
        }
        if(StringUtils.isNotBlank(newTaskGroupDTO.getxQuerySubmissionXML())){
            taskGroupResource.addProperty(ETutorVocabulary.hasSubmissionXMLFile, newTaskGroupDTO.getxQuerySubmissionXML().trim());
        }
        if(StringUtils.isNotBlank(newTaskGroupDTO.getDatalogFacts())){
            taskGroupResource.addProperty(ETutorVocabulary.hasDatalogFacts, newTaskGroupDTO.getDatalogFacts().trim());
        }

        taskGroupResource.addProperty(ETutorVocabulary.hasTaskGroupCreator, creator);
        taskGroupResource.addProperty(ETutorVocabulary.hasTaskGroupChangeDate, instantToRDFString(creationDate), XSDDatatype.XSDdateTime);
        return taskGroupResource;
    }

    /**
     * Constructs a resource from the given data.
     *
     * @param newTaskAssignmentDTO the new task assignment dto
     * @param uuid                 the generated uuid
     * @param creationDate         the creation date
     * @param model                the rdf base model
     * @param user                 the creator
     * @return {@link Resource} which represents the new task assignment
     */
    private Resource constructTaskAssignmentFromDTO(
        NewTaskAssignmentDTO newTaskAssignmentDTO,
        String uuid,
        Instant creationDate,
        Model model,
        String user
    ) {
        Resource resource = ETutorVocabulary.createTaskAssignmentResourceOfModel(uuid, model);

        resource.addProperty(ETutorVocabulary.hasTaskCreator, newTaskAssignmentDTO.getCreator());
        resource.addProperty(ETutorVocabulary.hasTaskHeader, newTaskAssignmentDTO.getHeader());
        resource.addProperty(ETutorVocabulary.hasTaskCreationDate, instantToRDFString(creationDate), XSDDatatype.XSDdateTime);

        String uploadFileId = String.valueOf(newTaskAssignmentDTO.getUploadFileId());
        resource.addProperty(ETutorVocabulary.hasUploadFileId, uploadFileId);



        String calcSolutionFileId = String.valueOf(newTaskAssignmentDTO.getCalcSolutionFileId());
        resource.addProperty(ETutorVocabulary.hasUploadCalcSolutionFileId, calcSolutionFileId);

        String calcInstructionFileId = String.valueOf(newTaskAssignmentDTO.getCalcInstructionFileId());
        resource.addProperty(ETutorVocabulary.hasUploadCalcInstructionFileId, calcInstructionFileId);

        String writerInstructionFileId = String.valueOf(newTaskAssignmentDTO.getWriterInstructionFileId());
        resource.addProperty(ETutorVocabulary.hasUploadWriterInstructionFileId, writerInstructionFileId);

        if (newTaskAssignmentDTO.getStartTime() != null) {
            resource.addProperty(ETutorVocabulary.hasStartTime, newTaskAssignmentDTO.getStartTime().toString());
        }

        if (newTaskAssignmentDTO.getEndTime() != null) {
            resource.addProperty(ETutorVocabulary.hasEndTime, newTaskAssignmentDTO.getEndTime().toString());
        }


        if (StringUtils.isNotBlank(newTaskAssignmentDTO.getTaskIdForDispatcher())) {
            resource.addProperty(ETutorVocabulary.hasTaskIdForDispatcher, newTaskAssignmentDTO.getTaskIdForDispatcher().trim());
        }

        if (StringUtils.isNotBlank(newTaskAssignmentDTO.getSqlSolution())) {
            resource.addProperty(ETutorVocabulary.hasSQLSolution, newTaskAssignmentDTO.getSqlSolution().trim());
        }

        if(StringUtils.isNotBlank(newTaskAssignmentDTO.getxQuerySolution())){
            resource.addProperty(ETutorVocabulary.hasXQuerySolution, newTaskAssignmentDTO.getxQuerySolution().trim());
        }

        if(StringUtils.isNotBlank(newTaskAssignmentDTO.getxQueryXPathSorting())){
            resource.addProperty(ETutorVocabulary.hasXQueryXPathSorting, newTaskAssignmentDTO.getxQueryXPathSorting().trim());
        }

        if(StringUtils.isNotBlank(newTaskAssignmentDTO.getDatalogSolution())){
            resource.addProperty(ETutorVocabulary.hasDLGSolution, newTaskAssignmentDTO.getDatalogSolution().trim());
        }

        if(StringUtils.isNotBlank(newTaskAssignmentDTO.getDatalogQuery())){
            resource.addProperty(ETutorVocabulary.hasDLGQuery, newTaskAssignmentDTO.getDatalogQuery().trim());
        }

        if(StringUtils.isNotBlank(newTaskAssignmentDTO.getDatalogUncheckedTerms())){
            resource.addProperty(ETutorVocabulary.hasUncheckedDLGTerm, newTaskAssignmentDTO.getDatalogUncheckedTerms().trim());
        }

        if(StringUtils.isNotBlank(newTaskAssignmentDTO.getMaxPoints())){
            resource.addProperty(ETutorVocabulary.hasMaxPoints, newTaskAssignmentDTO.getMaxPoints());
        }

        if(StringUtils.isNotBlank(newTaskAssignmentDTO.getDiagnoseLevelWeighting())){
            resource.addProperty(ETutorVocabulary.hasDiagnoseLevelWeighting, newTaskAssignmentDTO.getDiagnoseLevelWeighting());
        }

        if (StringUtils.isNotBlank(newTaskAssignmentDTO.getProcessingTime())) {
            resource.addProperty(ETutorVocabulary.hasTypicalProcessingTime, newTaskAssignmentDTO.getProcessingTime().trim());
        }
        resource.addProperty(ETutorVocabulary.hasTaskDifficulty, model.createResource(newTaskAssignmentDTO.getTaskDifficultyId()));
        resource.addProperty(ETutorVocabulary.hasTaskOrganisationUnit, newTaskAssignmentDTO.getOrganisationUnit().trim());
        resource.addProperty(ETutorVocabulary.hasInternalTaskCreator, user);
        resource.addProperty(ETutorVocabulary.hasTaskAssignmentType, model.createResource(newTaskAssignmentDTO.getTaskAssignmentTypeId()));

        if (newTaskAssignmentDTO.getUrl() != null) {
            resource.addProperty(ETutorVocabulary.hasTaskUrl, newTaskAssignmentDTO.getUrl().toString());
        }

        if (StringUtils.isNotBlank(newTaskAssignmentDTO.getInstruction())) {
            resource.addProperty(ETutorVocabulary.hasTaskInstruction, newTaskAssignmentDTO.getInstruction().trim());
        }

        String privateStr = String.valueOf(newTaskAssignmentDTO.isPrivateTask());
        resource.addProperty(ETutorVocabulary.isPrivateTask, privateStr, XSDDatatype.XSDboolean);
        resource.addProperty(RDF.type, ETutorVocabulary.TaskAssignment);

        if (newTaskAssignmentDTO.getTaskGroupId() != null) {
            Resource taskGroup = model.createResource(newTaskAssignmentDTO.getTaskGroupId());
            taskGroup.addProperty(ETutorVocabulary.hasTask, resource);
        }

        return resource;
    }

    //endregion
}
