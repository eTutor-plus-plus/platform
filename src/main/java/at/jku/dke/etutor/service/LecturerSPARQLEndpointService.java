package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.LecturerGradingInfoDTO;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.StudentAssignmentOverviewInfoDTO;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.TaskPointEntryDTO;
import one.util.streamex.StreamEx;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.*;

/**
 * Service endpoint for managing lecturer related data.
 *
 * @author fne
 */
@Service
public /*non-sealed */class LecturerSPARQLEndpointService extends AbstractSPARQLEndpointService {

    private static final String QRY_SELECT_LECTURER_OVERVIEW = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        SELECT ?student ?submitted ?fullyGraded ?expectedTaskCount ?actualCount ?submissionCount
        WHERE {
          {
            ?student etutor:hasIndividualTaskAssignment [
              etutor:fromExerciseSheet ?sheet;
              etutor:fromCourseInstance ?courseInstance;
            ].
            ?sheet etutor:hasExerciseSheetTaskCount ?expectedTaskCount.
            OPTIONAL {
              {
                SELECT (COUNT(*) AS ?cnt) ?student
                WHERE {
                  ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                  ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                        etutor:fromCourseInstance ?courseInstance.
                  FILTER(NOT EXISTS{
                      ?individualAssignment etutor:hasIndividualTask [
                        etutor:isGraded false
                      ]
                    })
                }
                GROUP BY ?student
              }
            }
        	{
               OPTIONAL {
                SELECT ?student (COUNT(?individualTask) AS ?actualCount)
                WHERE {
                  ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                  ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                        etutor:fromCourseInstance ?courseInstance;
                                        etutor:hasIndividualTask ?individualTask.
                }
                GROUP BY ?student
              }
            }
            BIND(bound(?cnt) AS ?fullyGraded)
          }
          {
            OPTIONAL {
              SELECT ?student (COUNT(?submitted) AS ?submissionCount)
              WHERE {
                ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                      etutor:fromCourseInstance ?courseInstance;
                                      etutor:hasIndividualTask ?individualTask.
                ?individualTask etutor:isSubmitted ?submitted.
                FILTER(?submitted = true)
              }
              GROUP BY ?student
            }
          }
          BIND(BOUND(?actualCount) && BOUND(?submissionCount) && ?expectedTaskCount = ?actualCount && ?actualCount = ?submissionCount as ?submitted).
        }
        ORDER BY (?student)
        """;

    private static final String QRY_COUNT_LECTURER_OVERVIEW =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT (COUNT(*) as ?cnt)
            WHERE {
              {
                ?student etutor:hasIndividualTaskAssignment [
                  etutor:fromExerciseSheet ?sheet;
                  etutor:fromCourseInstance ?courseInstance;
                ]
              }
            }
            """;

    private static final String QRY_GRADING_OVERVIEW =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT ?task ?taskTitle ?completed ?graded ?orderNo ?submitted ?taskType
            WHERE {
              ?student etutor:hasIndividualTaskAssignment [
                etutor:fromExerciseSheet ?sheet;
                etutor:fromCourseInstance ?courseInstance;
                etutor:hasIndividualTask [
                  etutor:isGraded ?graded;
                  etutor:refersToTask ?task;
                  etutor:hasOrderNo ?orderNo;
                  etutor:isLearningGoalCompleted ?completed;
                  etutor:isSubmitted ?submitted
                ]
              ].
              ?task etutor:hasTaskHeader ?taskTitle.
              ?task etutor:hasTaskAssignmentType ?taskType.
            }
            ORDER BY (?orderNo)
            """;

    private static final String QRY_UPDATE_GRADE =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?task etutor:isLearningGoalCompleted ?goalComplete.
              ?task etutor:isGraded ?graded.
            } INSERT {
              ?task etutor:isLearningGoalCompleted ?newCompleted.
              ?task etutor:isGraded true.
            } WHERE {
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?courseInstance.
              ?individualAssignment etutor:hasIndividualTask ?task.
              ?task etutor:hasOrderNo ?orderNo ;
                    etutor:isLearningGoalCompleted ?goalComplete ;
                    etutor:isGraded ?graded.
              FILTER(?orderNo = ?filterOrderNo)
            }
            """;

    private static final String QRY_ADJUST_LEARNING_GOALS_GOAL_COMPLETED =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

            INSERT {
              GRAPH ?courseinstance {
                ?goal etutor:isCompletedFrom ?student.
                ?subGoal etutor:isCompletedFrom ?student.
              }
            }
            WHERE {
              ?courseinstance a etutor:CourseInstance.
              ?courseinstance etutor:hasCourse ?course.
              ?goal a etutor:Goal.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?courseinstance.
              ?individualAssignment etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo.
              ?individualTask etutor:refersToTask ?task.
              ?goal etutor:hasTaskAssignment ?task.
              OPTIONAL {
              	?goal etutor:hasSubGoal+ ?subGoal.
              }
              GRAPH ?courseinstance {
              	    ?goal a etutor:Goal.
              }

            }
            """;
    private static final String QRY_ADJUST_LEARNING_GOALS_GOAL_FAILED =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

            DELETE {
              GRAPH ?courseinstance {
                ?goal etutor:isCompletedFrom ?student.
              }
            }
            WHERE {
              ?courseinstance a etutor:CourseInstance.
              ?courseinstance etutor:hasCourse ?course.
              ?goal a etutor:Goal.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?courseinstance.
              ?individualAssignment etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo.
              ?individualTask etutor:refersToTask ?task.
              ?goal etutor:hasTaskAssignment ?task.
              GRAPH ?courseinstance {
              	?goal a etutor:Goal.
              }
            }
            """;


    private static final String QRY_SELECT_POINTS_FOR_EXERCISE_SHEET = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

        SELECT ?student ?maxPoints ?points ?taskAssignment ?taskAssignmentHeader ?orderNo
                 WHERE{
                                          ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                                          ?individualAssignment etutor:fromExerciseSheet ?exerciseSheetId;
                                                                etutor:fromCourseInstance ?courseInstanceId;
                                                      			etutor:hasIndividualTask ?individualTask.
                                		  ?individualTask etutor:hasOrderNo ?orderNo.

                                		  ?individualTask etutor:refersToTask ?taskAssignment.
                                		  ?taskAssignment etutor:hasMaxPoints ?maxPoints;
                                                   etutor:hasTaskHeader ?taskAssignmentHeader.

                                        OPTIONAL{
                                            ?individualTask etutor:hasDispatcherPoints ?points.
                                        }

                          }
        """;

    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public LecturerSPARQLEndpointService(RDFConnectionFactory rdfConnectionFactory) {
        super(rdfConnectionFactory);
    }

    /**
     * Returns the page of the lecturer overview.
     *
     * @param courseInstanceUUID the course instance uuid
     * @param exerciseSheetUUID  the exercise sheet uuid
     * @param page               the paging information
     * @return page of the lecturer overview
     */
    public Page<StudentAssignmentOverviewInfoDTO> getPagedLecturerOverview(
        String courseInstanceUUID,
        String exerciseSheetUUID,
        Pageable page
    ) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(page);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_SELECT_LECTURER_OVERVIEW);
        ParameterizedSparqlString countQry = new ParameterizedSparqlString(QRY_COUNT_LECTURER_OVERVIEW);

        countQry.setIri("?courseInstance", courseInstanceId);
        countQry.setIri("?sheet", exerciseSheetId);

        if (page.isPaged()) {
            qry.append("LIMIT ");
            qry.append(page.getPageSize());
            qry.append("\nOFFSET ");
            qry.append(page.getOffset());
        }
        qry.setIri("?courseInstance", courseInstanceId);
        qry.setIri("?sheet", exerciseSheetId);

        List<StudentAssignmentOverviewInfoDTO> entries = new ArrayList<>();
        long count;
        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(qry.asQuery())) {
                ResultSet set = execution.execSelect();

                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();

                    String studentURI = solution.getResource("?student").getURI();
                    String matriculationNo = studentURI.substring(studentURI.lastIndexOf('#') + 1);

                    boolean submitted = solution.getLiteral("?submitted").getBoolean();
                    boolean fullyGraded = solution.getLiteral("?fullyGraded").getBoolean();
                    int expectedTaskCount = solution.getLiteral("?expectedTaskCount").getInt();

                    Literal submissionTaskCountLiteral = solution.getLiteral("?submissionCount");
                    int submissionTaskCount = 0;
                    if (submissionTaskCountLiteral != null) {
                        submissionTaskCount = submissionTaskCountLiteral.getInt();
                    }

                    entries.add(new StudentAssignmentOverviewInfoDTO(matriculationNo, submitted, fullyGraded, expectedTaskCount, submissionTaskCount));
                }
            }
            try (QueryExecution execution = connection.query(countQry.asQuery())) {
                ResultSet set = execution.execSelect();
                //noinspection ResultOfMethodCallIgnored
                set.hasNext();
                QuerySolution solution = set.nextSolution();
                count = solution.getLiteral("?cnt").getInt();
            }
        }
        return PageableExecutionUtils.getPage(entries, page, () -> count);
    }

    /**
     * Retrieves the grading info for a specific exercise sheet from a given student from a course instance.
     *
     * @param courseInstanceUUID the course instance uuid
     * @param exerciseSheetUUID  the exercise sheet uuid
     * @param matriculationNo    the student's matriculation number
     * @return the list of grading infos
     */
    public List<LecturerGradingInfoDTO> getGradingInfo(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_GRADING_OVERVIEW);
        qry.setIri("?student", studentId);
        qry.setIri("?sheet", exerciseSheetId);
        qry.setIri("?courseInstance", courseInstanceId);

        try (RDFConnection connection = getConnection()) {
            List<LecturerGradingInfoDTO> list = new ArrayList<>();
            try (QueryExecution execution = connection.query(qry.asQuery())) {
                ResultSet set = execution.execSelect();

                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    String taskUrl = solution.getResource("?task").getURI();
                    String taskTitle = solution.getLiteral("?taskTitle").getString();
                    boolean completed = solution.getLiteral("?completed").getBoolean();
                    boolean graded = solution.getLiteral("?graded").getBoolean();
                    int orderNo = solution.getLiteral("?orderNo").getInt();
                    boolean submitted = solution.getLiteral("?submitted").getBoolean();
                    String taskTypeId = solution.getResource("?taskType").getURI();

                    list.add(new LecturerGradingInfoDTO(taskUrl, taskTitle, completed, graded, orderNo, submitted, taskTypeId));
                }
            }
            return list;
        }
    }

    /**
     * Sets the grading info for a specific assignment.
     *
     * @param courseInstanceUUID the course instance uuid
     * @param exerciseSheetUUID  the exercise sheet uuid
     * @param matriculationNo    the matriculation number
     * @param orderNo            the order number
     * @param goalCompleted      indicates whether the goal has been completed or not
     */
    public void updateGradeForAssignment(
        String courseInstanceUUID,
        String exerciseSheetUUID,
        String matriculationNo,
        int orderNo,
        boolean goalCompleted
    ) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceURL = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetURL = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentURL = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_UPDATE_GRADE);

        qry.setIri("?student", studentURL);
        qry.setIri("?sheet", exerciseSheetURL);
        qry.setIri("?courseInstance", courseInstanceURL);
        qry.setLiteral("?filterOrderNo", orderNo);
        qry.setLiteral("?newCompleted", goalCompleted);

        try (RDFConnection connection = getConnection()) {
            connection.update(qry.asUpdate());

            if (goalCompleted) {
                ParameterizedSparqlString updateCompletedGoalQry = new ParameterizedSparqlString(QRY_ADJUST_LEARNING_GOALS_GOAL_COMPLETED);
                updateCompletedGoalQry.setIri("?courseinstance", courseInstanceURL);
                updateCompletedGoalQry.setIri("?student", studentURL);
                updateCompletedGoalQry.setIri("?sheet", exerciseSheetURL);
                updateCompletedGoalQry.setLiteral("?orderNo", orderNo);

                connection.update(updateCompletedGoalQry.asUpdate());

                adjustParentGoals(courseInstanceURL, exerciseSheetURL, studentURL, orderNo, connection);
            } else {
                ParameterizedSparqlString updatedFailedGoalQry = new ParameterizedSparqlString(QRY_ADJUST_LEARNING_GOALS_GOAL_FAILED);
                updatedFailedGoalQry.setIri("?courseinstance", courseInstanceURL);
                updatedFailedGoalQry.setIri("?student", studentURL);
                updatedFailedGoalQry.setIri("?sheet", exerciseSheetURL);
                updatedFailedGoalQry.setLiteral("?orderNo", orderNo);

                connection.update(updatedFailedGoalQry.asUpdate());

                ParameterizedSparqlString updateFailedCountQry = new ParameterizedSparqlString("""
                    PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

                    DELETE {
                      GRAPH ?courseinstance {
                        ?goal etutor:hasFailedCount ?oldCount.
                      }
                    }
                    INSERT {
                      GRAPH ?courseinstance {
                        ?goal etutor:hasFailedCount ?newCount.
                      }
                    }
                    WHERE {
                      ?courseinstance a etutor:CourseInstance.
                      ?courseinstance etutor:hasCourse ?course.
                      ?goal a etutor:Goal.
                      ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                      ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                            etutor:fromCourseInstance ?courseinstance.
                      ?individualAssignment etutor:hasIndividualTask ?individualTask.
                      ?individualTask etutor:hasOrderNo ?orderNo.
                      ?individualTask etutor:refersToTask ?task.
                      ?goal etutor:hasTaskAssignment ?task.
                      GRAPH ?courseinstance {
                        ?goal a etutor:Goal.
                        ?goal etutor:hasFailedCount ?oldCount.
                      }
                      BIND((?oldCount + 1) AS ?newCount)
                    }
                    """);

                updateFailedCountQry.setIri("?courseinstance", courseInstanceURL);
                updateFailedCountQry.setIri("?student", studentURL);
                updateFailedCountQry.setIri("?sheet", exerciseSheetURL);
                updateFailedCountQry.setLiteral("?orderNo", orderNo);

                connection.update(updateFailedCountQry.asUpdate());
            }
        }
    }

    /**
     * Adjusts the completion status of parent goals.
     *
     * @param courseInstanceURL the course instance URL
     * @param exerciseSheetURL  the exercise sheet URL
     * @param studentURL        the student URL
     * @param orderNo           the order no of the corresponding assigned task
     * @param connection        the RDF connection to the fuseki server
     */
    private void adjustParentGoals(@NotNull String courseInstanceURL, @NotNull String exerciseSheetURL, @NotNull String studentURL,
                                   int orderNo, @NotNull RDFConnection connection) {
        ParameterizedSparqlString selectUpdatableGoalsQuery = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT ?parentGoal
            WHERE {
              ?courseinstance a etutor:CourseInstance.
              ?courseinstance etutor:hasCourse ?course.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?courseinstance.
              ?individualAssignment etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo.
              ?individualTask etutor:refersToTask ?task.
              ?completedGoals etutor:hasTaskAssignment ?task.

              ?parentGoal a etutor:Goal.
              ?parentGoal etutor:needsVerificationBeforeCompletion false.
              ?parentGoal etutor:hasSubGoal ?completedGoals.

              FILTER(NOT EXISTS {
            	?parentGoal etutor:hasSubGoal ?subGoal.
                FILTER(NOT EXISTS {
                  GRAPH ?courseinstance {
                    ?subGoal etutor:isCompletedFrom ?student
                  }
                })
              }).
              FILTER(NOT EXISTS {
                GRAPH ?courseinstance {
                  ?parentGoal etutor:isCompletedFrom ?student
                }
              }).
            }
            """);

        selectUpdatableGoalsQuery.setIri("?courseinstance", courseInstanceURL);
        selectUpdatableGoalsQuery.setIri("?student", studentURL);
        selectUpdatableGoalsQuery.setIri("?sheet", exerciseSheetURL);
        selectUpdatableGoalsQuery.setLiteral("?orderNo", orderNo);

        Model model = ModelFactory.createDefaultModel();
        Resource studentResource = model.createResource(studentURL);
        List<String> goals = new ArrayList<>();

        try (QueryExecution queryExecution = connection.query(selectUpdatableGoalsQuery.asQuery())) {
            ResultSet set = queryExecution.execSelect();

            while (set.hasNext()) {
                QuerySolution solution = set.nextSolution();
                String goal = solution.getResource("?parentGoal").getURI();
                goals.add(goal);
                Resource resource = model.createResource(goal);
                resource.addProperty(ETutorVocabulary.isCompletedFrom, studentResource);
            }
        }

        if (!model.isEmpty()) {
            connection.load(replaceHashtagInGraphUrlIfNeeded(courseInstanceURL), model);
        }

        if (goals.size() > 0) {
            adjustParentGoalsRecursive(courseInstanceURL, studentURL, goals, connection);
        }
    }

    /**
     * Adjusts the parents goal recursive based on the given list of previously as
     * successful marked goals.
     *
     * @param courseInstanceURL the course instance URL
     * @param studentURL        the student URL
     * @param goals             the list of goals
     * @param connection        the RDF connection to the Fuseki server
     */
    private void adjustParentGoalsRecursive(@NotNull String courseInstanceURL, @NotNull String studentURL, @NotNull List<String> goals, @NotNull RDFConnection connection) {
        ParameterizedSparqlString selectUpdatableGoalsQuery = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT ?parentGoal
            WHERE {
              ?completedGoal a etutor:Goal.
              FILTER(?completedGoal IN (?valuesForCompletedGoals)).
              ?parentGoal a etutor:Goal.
              ?parentGoal etutor:needsVerificationBeforeCompletion false.
              ?parentGoal etutor:hasSubGoal ?completedGoal.

              FILTER(NOT EXISTS {
            	?parentGoal etutor:hasSubGoal ?subGoal.
                FILTER(NOT EXISTS {
                  GRAPH ?courseinstance {
                    ?subGoal etutor:isCompletedFrom ?student
                  }
                })
              }).
              FILTER(NOT EXISTS {
                GRAPH ?courseinstance {
                  ?parentGoal etutor:isCompletedFrom ?student
                }
              }).
            }
            """);

        selectUpdatableGoalsQuery.setIri("?student", studentURL);
        selectUpdatableGoalsQuery.setIri("?courseinstance", courseInstanceURL);

        String query = selectUpdatableGoalsQuery.toString();
        query = query.replace("?valuesForCompletedGoals", StreamEx.of(goals).map(x -> String.format("<%s>", x)).joining(", "));

        List<String> newGoals = new ArrayList<>();
        Model model = ModelFactory.createDefaultModel();
        Resource studentResource = model.createResource(studentURL);
        try (QueryExecution execution = connection.query(query)) {
            ResultSet set = execution.execSelect();

            while (set.hasNext()) {
                QuerySolution solution = set.nextSolution();
                String goal = solution.getResource("?parentGoal").getURI();
                newGoals.add(goal);
                Resource resource = model.createResource(goal);
                resource.addProperty(ETutorVocabulary.isCompletedFrom, studentResource);
            }
        }

        if (!model.isEmpty()) {
            connection.load(replaceHashtagInGraphUrlIfNeeded(courseInstanceURL), model);
        }

        if (newGoals.size() > 0) {
            adjustParentGoalsRecursive(courseInstanceURL, studentURL, newGoals, connection);
        }
    }

    /**
     * Returns an overview about all the assigned tasks for a given exercise sheet and course instance with the achieved points and maximum points
     *
     * @param exerciseSheetUUID  the exercise sheet
     * @param courseInstanceUUID the course instance
     * @return an Optional containing the information
     */
    public Optional<List<TaskPointEntryDTO>> getPointsOverviewForExerciseSheet(String exerciseSheetUUID, String courseInstanceUUID) {
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(courseInstanceUUID);

        String exerciseSheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_SELECT_POINTS_FOR_EXERCISE_SHEET);
        query.setIri("?courseInstanceId", courseInstanceId);
        query.setIri("?exerciseSheetId", exerciseSheetId);

        List<TaskPointEntryDTO> overviewInfo = new ArrayList<>();

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();
                while (set.hasNext()) {
                    QuerySolution solution = set.next();

                    Resource studentResource = solution.getResource("?student");
                    String student = studentResource.getURI();
                    Literal pointsLiteral = solution.getLiteral("?points");
                    double points = 0;
                    if (pointsLiteral != null) points = pointsLiteral.getDouble();
                    Literal maxPointsLiteral = solution.getLiteral("?maxPoints");
                    Literal taskAssignmentHeaderLiteral = solution.getLiteral("?taskAssignmentHeader");
                    Literal orderNoLiteral = solution.getLiteral("?orderNo");
                    Resource taskAssignmentResource = solution.getResource("?taskAssignment");
                    String taskAssignment = taskAssignmentResource.getURI();

                    overviewInfo.add(new TaskPointEntryDTO(
                        student.substring(student.lastIndexOf("#") + 1),
                        maxPointsLiteral.getDouble(),
                        points,
                        taskAssignmentHeaderLiteral.getString(),
                        taskAssignment.substring(taskAssignment.lastIndexOf("#") + 1),
                        orderNoLiteral.getInt()));
                }
                if (overviewInfo.isEmpty()) return Optional.empty();
            }
        }
        return Optional.of(overviewInfo);
    }

    /**
     * Closes an exercise sheet from a given course instance.
     *
     * @param courseInstanceUUID the course instance's UUID - must not be null
     * @param exerciseSheetUUID  the exercise sheet's UUID - must not be null
     */
    public void closeExerciseSheetOfCourseInstance(String courseInstanceUUID, String exerciseSheetUUID) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);

        String courseInstanceURL = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetURL = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);

        ParameterizedSparqlString updateQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?eAssignment etutor:isExerciseSheetClosed ?oldIsClosed.
              ?eAssignment etutor:hasExerciseSheetCloseDateTime ?oldCloseTime.
            }
            INSERT {
              ?eAssignment etutor:isExerciseSheetClosed true.
              ?eAssignment etutor:hasExerciseSheetCloseDateTime ?newCloseTime.
            }
            WHERE {
              ?instance etutor:hasExerciseSheetAssignment ?eAssignment.
              ?eAssignment a etutor:ExerciseSheetAssignment.
              ?eAssignment etutor:hasExerciseSheet ?sheet.
              OPTIONAL {
                ?eAssignment etutor:isExerciseSheetClosed ?oldIsClosed.
              }
              OPTIONAL {
                ?eAssignment etutor:hasExerciseSheetCloseDateTime ?oldCloseTime.
              }
            }
            """);

        updateQry.setIri("?instance", courseInstanceURL);
        updateQry.setIri("?sheet", exerciseSheetURL);
        String nowStr = instantToRDFString(Instant.now());
        updateQry.setLiteral("?newCloseTime", nowStr, XSDDatatype.XSDdateTime);

        try (RDFConnection connection = getConnection()) {
            connection.update(updateQry.asUpdate());
        }
    }

    /**
     * Re-opens an already closed exercise sheet.
     *
     * @param courseInstanceUUID the course instance's UUID
     * @param exerciseSheetUUID  the exercise sheet's UUID
     */
    public void openExerciseSheetOfCourseInstance(String courseInstanceUUID, String exerciseSheetUUID) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);

        String courseInstanceURL = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetURL = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);

        ParameterizedSparqlString updateQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?eAssignment etutor:isExerciseSheetClosed ?oldIsClosed.
              ?eAssignment etutor:hasExerciseSheetCloseDateTime ?oldCloseTime.
              ?eAssignment etutor:hasExerciseSheetOpenDateTime ?oldOpenTime.
            }
            INSERT {
              ?eAssignment etutor:isExerciseSheetClosed false.
              ?eAssignment etutor:hasExerciseSheetOpenDateTime ?newOpenTime.
            }
            WHERE {
              ?instance etutor:hasExerciseSheetAssignment ?eAssignment.
              ?eAssignment a etutor:ExerciseSheetAssignment.
              ?eAssignment etutor:hasExerciseSheet ?sheet.
              OPTIONAL {
                ?eAssignment etutor:isExerciseSheetClosed ?oldIsClosed.
              }
              OPTIONAL {
                ?eAssignment etutor:hasExerciseSheetCloseDateTime ?oldCloseTime.
              }
              OPTIONAL {
               ?eAssignment etutor:hasExerciseSheetOpenDateTime ?oldOpenTime.\s
              }
            }
            """);

        updateQry.setIri("?instance", courseInstanceURL);
        updateQry.setIri("?sheet", exerciseSheetURL);
        String nowStr = instantToRDFString(Instant.now());
        updateQry.setLiteral("?newOpenTime", nowStr, XSDDatatype.XSDdateTime);

        try (RDFConnection connection = getConnection()) {
            connection.update(updateQry.asUpdate());
        }
    }

    public InputStreamResource getAchievedPointsForExerciseSheet(String courseInstanceUUID, String exerciseSheetUUID, String type) {
        Optional<List<TaskPointEntryDTO>> optionalPointsOverviewInfo = getPointsOverviewForExerciseSheet(exerciseSheetUUID, courseInstanceUUID);
        List<TaskPointEntryDTO> pointsOverviewInfo = optionalPointsOverviewInfo.orElse(null);

        if(type.equals("aggregated")){
            return getAchievedPointsForExerciseSheetAggregatedByStudent(pointsOverviewInfo);
        }else{
            return getAchievedPointsForExerciseSheetNonAggregated(pointsOverviewInfo);
        }


    }

    private InputStreamResource getAchievedPointsForExerciseSheetNonAggregated(List<TaskPointEntryDTO> pointsOverviewInfo) {
        String[] csvHeader = {
            "matriculationNo", "taskHeader", "maxPoints", "points"
        };

        ByteArrayInputStream byteArrayOutputStream;

        try (
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(
                new PrintWriter(out),
                CSVFormat.DEFAULT.withHeader(csvHeader)
            )
        ) {
            List<String> printableRecord;
            for (TaskPointEntryDTO record : pointsOverviewInfo) {
                printableRecord = new ArrayList<>();
                printableRecord.add(record.getMatriculationNo());
                printableRecord.add(record.getTaskHeader());
                printableRecord.add(Double.toString(record.getMaxPoints()));
                printableRecord.add(Double.toString(record.getPoints()));
                csvPrinter.printRecord(printableRecord);
            }

            csvPrinter.flush();

            byteArrayOutputStream = new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return new InputStreamResource(byteArrayOutputStream);
    }

    private InputStreamResource getAchievedPointsForExerciseSheetAggregatedByStudent(List<TaskPointEntryDTO> pointsOverviewInfo) {
        OptionalInt optMaxOrderNo = pointsOverviewInfo.stream().mapToInt(TaskPointEntryDTO::getOrderNo).max();
        int maxOrderNo = optMaxOrderNo.orElse(0);

        List<String> columnHeaders = new ArrayList<>();
        columnHeaders.add("matriculationNo");
        for(int i = 1; i <= maxOrderNo; i++){
            columnHeaders.add("task %d".formatted(i));
        }
        columnHeaders.add("sum");

        String[] csvHeader = columnHeaders.toArray(new String[0]);
        ByteArrayInputStream byteArrayOutputStream;

        try (
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(
                new PrintWriter(out),
                CSVFormat.DEFAULT.withHeader(csvHeader)
            )
        ) {
            List<String> students = pointsOverviewInfo.stream()
                .map(TaskPointEntryDTO::getMatriculationNo)
                .distinct()
                .toList();
            for(String matriculationNo : students){
                List<String> printableRecord = new ArrayList<>();
                printableRecord.add(matriculationNo);

                List<TaskPointEntryDTO> subListForCurrentStudent = pointsOverviewInfo.stream()
                    .filter(dto -> dto.getMatriculationNo().equals(matriculationNo))
                    .sorted(Comparator.comparingInt(TaskPointEntryDTO::getOrderNo))
                    .toList();
                for (TaskPointEntryDTO taskPointEntryDTO : subListForCurrentStudent) {
                    printableRecord.add(Double.toString(taskPointEntryDTO.getPoints()));
                }

                // fill potentially missing columns due to unequal amount of assigned tasks
                int maxOrderNoOfStudent = subListForCurrentStudent.stream()
                    .mapToInt(TaskPointEntryDTO::getOrderNo)
                    .max().orElse(0);
                int difference = maxOrderNo - maxOrderNoOfStudent;
                for(int i = 0; i < difference; i++){
                    printableRecord.add("");
                }

                // add sum of achieved points for exercise sheet
                double sumOfPoints = subListForCurrentStudent.stream()
                        .mapToDouble(TaskPointEntryDTO::getPoints)
                            .sum();
                printableRecord.add(Double.toString(sumOfPoints));

                csvPrinter.printRecord(printableRecord);
            }

            csvPrinter.flush();

            byteArrayOutputStream = new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return new InputStreamResource(byteArrayOutputStream);
    }
}
