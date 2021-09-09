package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.CSVHelper;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.repository.StudentRepository;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.dto.AdminUserDTO;
import at.jku.dke.etutor.service.dto.StudentSelfEvaluationLearningGoalDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceInformationDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceProgressOverviewDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentImportDTO;
import at.jku.dke.etutor.service.dto.student.IndividualTaskSubmissionDTO;
import at.jku.dke.etutor.service.dto.student.StudentTaskListInfoDTO;
import at.jku.dke.etutor.service.exception.*;
import one.util.streamex.StreamEx;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;

/**
 * Service class for managing students.
 *
 * @author fne
 */
@Service
public class StudentService extends AbstractSPARQLEndpointService {

    private static final String QRY_STUDENTS_COURSES =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT DISTINCT (STR(?term) AS ?termId) ?courseName ?instructor (STR(?instance) AS ?instanceId) ?year (COALESCE(?completed, false) AS ?testCompleted)
            WHERE {
              ?instance etutor:hasStudent ?student.
              ?instance etutor:hasTerm ?term.
              ?instance etutor:hasCourse ?course.
              ?instance etutor:hasInstanceYear ?year.
              ?course rdfs:label ?courseName.
              ?course etutor:hasCourseCreator ?instructor.
              OPTIONAL {
                GRAPH ?instance {
                  ?student etutor:isInitialTestCompleted ?completed.
                }
              }
            }
            ORDER BY(?courseName)
            """;

    private static final String QRY_SELECT_STUDENT_COURSE_ASSIGNMENT_OVERVIEW = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

        SELECT (STR(?exerciseSheet) AS ?exerciseSheetId) ?exerciseSheetName (STR(?difficulty) AS ?difficultyURI) ?completed ?shouldTaskCount ?actualCount ?submissionCount ?gradedCount ?closed
        WHERE {
          {
            ?instance a etutor:CourseInstance.
            ?instance etutor:hasStudent ?student.
            ?instance etutor:hasExerciseSheet ?exerciseSheet.
            ?exerciseSheet rdfs:label ?exerciseSheetName.
            ?exerciseSheet etutor:hasExerciseSheetDifficulty ?difficulty.
            ?exerciseSheet etutor:hasExerciseSheetTaskCount ?shouldTaskCount.

            ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
            ?individualAssignment etutor:fromExerciseSheet ?exerciseSheet;
                                  etutor:fromCourseInstance ?instance;
                                  etutor:hasIndividualTask ?individualTask;
                                  etutor:isClosed ?closed.
            {
              OPTIONAL {
                SELECT ?exerciseSheet (COUNT(?individualTask) AS ?actualCount)
                WHERE {
                  ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                  ?individualAssignment etutor:fromExerciseSheet ?exerciseSheet;
                                        etutor:fromCourseInstance ?instance;
                                        etutor:hasIndividualTask ?individualTask.
                }
                GROUP BY ?exerciseSheet
              }
            }
            {
              OPTIONAL {
                SELECT ?exerciseSheet (COUNT(?submitted) AS ?submissionCount)
                WHERE {
                  ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                  ?individualAssignment etutor:fromExerciseSheet ?exerciseSheet;
                                        etutor:fromCourseInstance ?instance;
                                        etutor:hasIndividualTask ?individualTask.
                  ?individualTask etutor:isSubmitted ?submitted.
                  FILTER(?submitted = true)
                }
                GROUP BY ?exerciseSheet
              }
            }
            BIND(?shouldTaskCount = ?actualCount && ?actualCount = ?submissionCount AS ?completed).
            {
              OPTIONAL {
                SELECT ?exerciseSheet (COUNT(?graded) AS ?gradedCount)
                WHERE {
                  ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                  ?individualAssignment etutor:fromExerciseSheet ?exerciseSheet;
                                        etutor:fromCourseInstance ?instance;
                                        etutor:hasIndividualTask ?individualTask.
                  ?individualTask etutor:isGraded ?graded.
                  FILTER(?graded = true)
                }
                GROUP BY ?exerciseSheet
              }
            }
          } UNION {
            ?instance a etutor:CourseInstance.
            ?instance etutor:hasStudent ?student.
            ?instance etutor:hasExerciseSheet ?exerciseSheet.
            ?exerciseSheet rdfs:label ?exerciseSheetName.
            ?exerciseSheet etutor:hasExerciseSheetDifficulty ?difficulty.
            ?exerciseSheet etutor:hasExerciseSheetTaskCount ?shouldTaskCount.
            FILTER(NOT EXISTS{
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?exerciseSheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:hasIndividualTask ?individualTask.
            }).
            BIND(false AS ?completed).
            BIND(0 AS ?actualCount).
            BIND(0 AS ?submissionCount).
            BIND(0 AS ?gradedCount).
            BIND(false as ?closed).
          }
        }
        ORDER BY (LCASE(?exerciseSheetName))
        """;

    private static final String QRY_ASK_STUDENT_OPENED_EXERCISE_SHEET =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            ASK {
              ?instance etutor:hasStudent ?student.
              ?student etutor:hasIndividualTaskAssignment [
                a etutor:IndividualTaskAssignment ;
                etutor:fromCourseInstance ?instance;
              	etutor:fromExerciseSheet ?exerciseSheet
              ]
            }
            """;

    private static final String QRY_UPDATE_SELF_EVALUATION_STATUS =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              GRAPH ?instance {
                ?student etutor:isInitialTestCompleted ?completed.
              }
            } INSERT {
              GRAPH ?instance {
                ?student etutor:isInitialTestCompleted true.
              }
            }
            WHERE {
              GRAPH ?instance {
                OPTIONAL {
                  ?student etutor:isInitialTestCompleted ?completed.
                }
              }
            }
            """;

    private static final String QRY_SELECT_STUDENT_TASK_LIST =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT ?orderNo (STR(?task) AS ?taskId) ?graded ?goalCompleted ?taskHeader ?submitted
            WHERE {
              ?courseInstance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?courseInstance;
                                    etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo;
                              etutor:refersToTask ?task;
                              etutor:isGraded ?graded;
                              etutor:isSubmitted ?submitted;
                              etutor:isLearningGoalCompleted ?goalCompleted.
              ?task etutor:hasTaskHeader ?taskHeader.
            }
            """;

    private static final String QRY_SUBMIT_TASK_ASSIGNMENT = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        DELETE {
          ?individualTask etutor:isSubmitted ?submitted.
        } INSERT {
          ?individualTask etutor:isSubmitted true.
        }
        WHERE {
          ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
          ?individualAssignment etutor:fromExerciseSheet ?sheet;
          	                    etutor:fromCourseInstance ?courseInstance;
                                etutor:hasIndividualTask ?individualTask.
          ?individualTask etutor:hasOrderNo ?orderNo;
                          etutor:isSubmitted ?submitted.
        }
        """;

    private static final String QRY_ASK_TASK_ALREADY_SUBMITTED = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        ASK {
          ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
          ?individualAssignment etutor:fromExerciseSheet ?sheet;
          	                    etutor:fromCourseInstance ?courseInstance;
                                etutor:hasIndividualTask ?individualTask.
          ?individualTask etutor:isSubmitted true;
                          etutor:hasOrderNo ?orderNo.
        }
        """;

    private static final String QRY_SELECT_TOTAL_AND_ASSIGNED_TASK_COUNT = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        SELECT ?taskCount (COUNT(?orderNo) as ?assignedCount)
        WHERE {
          ?courseInstance a etutor:CourseInstance.
          ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
          ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                etutor:fromCourseInstance ?courseInstance.
          OPTIONAL {
            ?individualAssignment etutor:hasIndividualTask [
              etutor:hasOrderNo ?orderNo
            ].
          }
          ?sheet etutor:hasExerciseSheetTaskCount ?taskCount.
        }
        GROUP BY ?taskCount
        """;

    private static final String QRY_SELECT_MAX_ORDER_NO = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        SELECT (COALESCE(MAX(?orderNo), 0) AS ?maxOrderNo)
        WHERE {
          ?courseInstance a etutor:CourseInstance.
          ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
          ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                etutor:fromCourseInstance ?courseInstance;
                                etutor:hasIndividualTask ?individualTask.
          ?individualTask etutor:hasOrderNo ?orderNo.
        }
        """;

    private static final String QRY_INSERT_NEW_INDIVIDUAL_ASSIGNMENT = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        INSERT {
          ?individualAssignment etutor:hasIndividualTask [
            a etutor:IndividualTask;
            etutor:hasOrderNo ?newOrderNo;
            etutor:isSubmitted false;
            etutor:isLearningGoalCompleted false;
            etutor:isGraded false;
            etutor:refersToTask ?newTask
          ].
        }
        WHERE {
          ?courseInstance a etutor:CourseInstance.
          ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
          ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                etutor:fromCourseInstance ?courseInstance;
        }
        """;

    private static final String QRY_INSERT_NEW_INDIVIDUAL_TASK_SUBMISSION = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        INSERT {
          ?individualTask etutor:hasIndividualTaskSubmission [
            a etutor:IndividualTaskSubmission;
            etutor:hasSubmission ?submission;
            etutor:hasInstant ?instant;
            etutor:isSubmitted ?isSubmitted;
            etutor:isSolved ?isSolved;
          ].
        }
        WHERE {
          ?courseInstance a etutor:CourseInstance.
          ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
          ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                etutor:fromCourseInstance ?courseInstance.
          ?individualAssignment etutor:hasIndividualTask ?individualTask.
          ?individualTask etutor:hasOrderNo ?orderNo.
        }
        """;

    private static final String QRY_ASK_INDIVIDUAL_TASK_SUBMISSIONS = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        SELECT ?instant ?submission ?taskInstruction ?isSubmitted ?isSolved
        WHERE{
        	?student etutor:hasIndividualTaskAssignment ?individualAssignment.
          	?individualAssignment a etutor:IndividualTaskAssignment.
          	?individualAssignment etutor:fromExerciseSheet ?exerciseSheet;
                                 etutor:fromCourseInstance ?courseInstance.
          	?individualAssignment etutor:hasIndividualTask ?individualTask.
          	?individualTask a etutor:IndividualTask;
                           etutor:hasOrderNo ?orderNo;
                           etutor:refersToTask ?taskAssignment;
                           etutor:hasIndividualTaskSubmission ?taskSubmission.
          	?taskSubmission etutor:hasSubmission ?submission;
                           etutor:hasInstant ?instant.
            OPTIONAL{
                ?taskSubmission etutor:isSubmitted ?isSubmitted.
                ?taskSubmission etutor:isSolved ?isSolved.
            }
          	OPTIONAL{
            	?taskAssignment etutor:hasTaskInstruction ?taskInstruction.
          	}
        }
        """;

    private static final String QRY_ASK_EXERCISE_SHEET_OPENED = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        ASK {
          ?courseInstance a etutor:CourseInstance.
          ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
          ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                etutor:fromCourseInstance ?courseInstance;
        }
        """;

    private final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final UserService userService;
    private final StudentRepository studentRepository;
    private final Random random;

    /**
     * Constructor.
     *
     * @param userService          the injected user service
     * @param studentRepository    the injected student repository
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public StudentService(UserService userService, StudentRepository studentRepository, RDFConnectionFactory rdfConnectionFactory) {
        super(rdfConnectionFactory);
        this.userService = userService;
        this.studentRepository = studentRepository;

        random = new Random();
    }

    /**
     * Reads the students from the given CSV file and imports the student into the database if
     * they do not exist in the database.
     *
     * @param file the multipart csv file containing the students
     * @return list of loaded students
     * @throws StudentCSVImportException if an import error occurs
     */
    @Transactional
    public List<StudentImportDTO> importStudentsFromFile(MultipartFile file) throws StudentCSVImportException {
        if (!CSVHelper.hasCSVFileFormat(file)) {
            throw new StudentCSVImportException();
        }
        List<StudentImportDTO> students = CSVHelper.getStudentsFromCSVFile(file);

        for (StudentImportDTO student : students) {
            if (!studentRepository.studentExists(student.getMatriculationNumber())) {
                AdminUserDTO userDTO = new AdminUserDTO();
                userDTO.setFirstName(student.getFirstName());
                userDTO.setLastName(student.getLastName());
                userDTO.setEmail(student.getEmail());
                userDTO.setAuthorities(Set.of(AuthoritiesConstants.STUDENT));
                userDTO.setLogin(student.getMatriculationNumber());
                userService.createUser(userDTO);
            }
        }

        return students;
    }

    /**
     * Returns the courses of a given student.
     *
     * @param matriculationNumber the student's matriculation number - must not be null
     * @return list of courses
     */
    public List<CourseInstanceInformationDTO> getCoursesFromStudent(String matriculationNumber) {
        Objects.requireNonNull(matriculationNumber);

        String studentURI = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);
        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_STUDENTS_COURSES);
        query.setIri("?student", studentURI);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();
                List<CourseInstanceInformationDTO> retList = new ArrayList<>();
                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    String termId = solution.getLiteral("?termId").getString();
                    String courseName = solution.getLiteral("?courseName").getString();
                    String instructor = solution.getLiteral("?instructor").getString();
                    String instanceId = solution.getLiteral("?instanceId").getString();
                    int year = solution.getLiteral("?year").getInt();
                    boolean testCompleted = solution.getLiteral("?testCompleted").getBoolean();

                    retList.add(new CourseInstanceInformationDTO(courseName, termId, instructor, instanceId, year, testCompleted));
                }
                return retList;
            }
        }
    }

    /**
     * Returns the progress overview of the single exercise sheets from a course instance of a
     * specific student.
     *
     * @param matriculationNumber the student's matriculation number
     * @param courseInstanceUUID  the course instance uuid
     * @return list containing the elements
     */
    public List<CourseInstanceProgressOverviewDTO> getProgressOverview(String matriculationNumber, String courseInstanceUUID) {
        Objects.requireNonNull(matriculationNumber);
        Objects.requireNonNull(courseInstanceUUID);

        String studentURI = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);
        String courseInstanceURI = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_SELECT_STUDENT_COURSE_ASSIGNMENT_OVERVIEW);
        qry.setIri("?instance", courseInstanceURI);
        qry.setIri("?student", studentURI);

        ParameterizedSparqlString exerciseSheetOpenedQry = new ParameterizedSparqlString(QRY_ASK_EXERCISE_SHEET_OPENED);
        exerciseSheetOpenedQry.setIri("?courseInstance", courseInstanceURI);
        exerciseSheetOpenedQry.setIri("?student", studentURI);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(qry.asQuery(Syntax.syntaxARQ))) {
                ResultSet set = execution.execSelect();
                List<CourseInstanceProgressOverviewDTO> items = new ArrayList<>();
                while (set.hasNext()) {
                    var solution = set.nextSolution();

                    String sheetId = solution.getLiteral("?exerciseSheetId").getString();
                    String sheetName = solution.getLiteral("?exerciseSheetName").getString();
                    String difficultyUri = solution.getLiteral("?difficultyURI").getString();
                    Literal completedLiteral = solution.getLiteral("?completed");
                    boolean completed = false;
                    if (completedLiteral != null) {
                        completed = completedLiteral.getBoolean();
                    }
                    boolean opened = true;

                    if (!completed) {
                        exerciseSheetOpenedQry.setIri("?sheet", sheetId);
                        opened = connection.queryAsk(exerciseSheetOpenedQry.asQuery());
                    }

                    int actualCount = 0;
                    int submissionCount = 0;
                    int gradedCount = 0;

                    Literal actualCountLiteral = solution.getLiteral("?actualCount");
                    Literal submissionCountLiteral = solution.getLiteral("?submissionCount");
                    Literal gradedCountLiteral = solution.getLiteral("?gradedCount");

                    if (actualCountLiteral != null) {
                        actualCount = actualCountLiteral.getInt();
                    }
                    if (submissionCountLiteral != null) {
                        submissionCount = submissionCountLiteral.getInt();
                    }
                    if (gradedCountLiteral != null) {
                        gradedCount = gradedCountLiteral.getInt();
                    }

                    boolean closed = solution.getLiteral("?closed").getBoolean();

                    items.add(new CourseInstanceProgressOverviewDTO(sheetId, sheetName, difficultyUri, completed, opened, actualCount, submissionCount, gradedCount, closed));
                }
                return items;
            }
        }
    }

    /**
     * Returns whether a student opened an exercise sheet and therefore, the exercise sheet's questions were assigned.
     *
     * @param matriculationNumber the student's matriculation number
     * @param courseInstanceUUID  the course instance uuid
     * @param exerciseSheetUUID   the exercise sheet uuid
     * @return {@code true} if the student has already opened the sheet, otherwise {@code false}
     */
    public boolean hasStudentOpenedTheExerciseSheet(String matriculationNumber, String courseInstanceUUID, String exerciseSheetUUID) {
        Objects.requireNonNull(matriculationNumber);
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);

        String courseInstanceURL = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetURL = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentURL = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_ASK_STUDENT_OPENED_EXERCISE_SHEET);
        query.setIri("?instance", courseInstanceURL);
        query.setIri("?student", studentURL);
        query.setIri("?exerciseSheet", exerciseSheetURL);

        try (RDFConnection connection = getConnection()) {
            return connection.queryAsk(query.asQuery());
        }
    }

    /**
     * Opens a new exercise sheet for a student and  assigns the tasks according
     * to the student's learning curve.
     *
     * @param matriculationNumber the student's matriculation number
     * @param courseInstanceUUID  the course instance uuid
     * @param exerciseSheetUUID   the exercise sheet uuid
     * @throws ExerciseSheetAlreadyOpenedException if the exercise sheet has already been opened
     * @throws NoFurtherTasksAvailableException    if no further tasks are available for assignment
     */
    public void openExerciseSheetForStudent(String matriculationNumber, String courseInstanceUUID, String exerciseSheetUUID)
        throws ExerciseSheetAlreadyOpenedException, NoFurtherTasksAvailableException {

        Objects.requireNonNull(matriculationNumber);
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);

        String courseInstanceURL = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetURL = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentURL = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        ParameterizedSparqlString exerciseSheetOpenedQry = new ParameterizedSparqlString(QRY_ASK_EXERCISE_SHEET_OPENED);
        exerciseSheetOpenedQry.setIri("?courseInstance", courseInstanceURL);
        exerciseSheetOpenedQry.setIri("?student", studentURL);
        exerciseSheetOpenedQry.setIri("?sheet", exerciseSheetURL);

        try (RDFConnection connection = getConnection()) {
            boolean result = connection.queryAsk(exerciseSheetOpenedQry.asQuery());

            if (result) {
                throw new ExerciseSheetAlreadyOpenedException();
            }

            Model model = ModelFactory.createDefaultModel();
            Resource studentResource = model.createResource(studentURL);

            Resource individualTaskAssignmentResource = model.createResource();
            individualTaskAssignmentResource.addProperty(RDF.type, ETutorVocabulary.IndividualTaskAssignment);
            individualTaskAssignmentResource.addProperty(ETutorVocabulary.fromCourseInstance, model.createResource(courseInstanceURL));
            individualTaskAssignmentResource.addProperty(ETutorVocabulary.fromExerciseSheet, model.createResource(exerciseSheetURL));
            individualTaskAssignmentResource.addProperty(ETutorVocabulary.isClosed, "false", XSDDatatype.XSDboolean);

            studentResource.addProperty(ETutorVocabulary.hasIndividualTaskAssignment, individualTaskAssignmentResource);
            connection.load(model);

            try {
                assignNextTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, connection);
            } catch (AllTasksAlreadyAssignedException ex) {
                //Ignore, must not happen -> warn
                log.warn("When creating a new individual assignment of an exercise sheet, no tasks can be individually assigned!", ex);
            }
        }
    }

    /**
     * Saves a self evaluation.
     *
     * @param courseInstanceUUID      the course instance uuid, must not be null
     * @param matriculationNumber     the student's matriculation number, must not be null
     * @param evaluationLearningGoals the evaluation learning goal DTOs, must not be null
     */
    public void saveSelfEvaluation(
        String courseInstanceUUID,
        String matriculationNumber,
        List<StudentSelfEvaluationLearningGoalDTO> evaluationLearningGoals
    ) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(matriculationNumber);
        Objects.requireNonNull(evaluationLearningGoals);

        String courseInstanceUri = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String studentUri = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        Model model = ModelFactory.createDefaultModel();
        Resource studentResource = model.createResource(studentUri);

        for (var goal : evaluationLearningGoals) {
            if (goal.isCompleted()) {
                model.add(model.createResource(goal.getId()), ETutorVocabulary.isCompletedFrom, studentResource);
            }
        }

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_UPDATE_SELF_EVALUATION_STATUS);
        qry.setIri("?instance", courseInstanceUri);
        qry.setIri("?student", studentUri);

        try (RDFConnection connection = getConnection()) {
            connection.load(courseInstanceUri.replace("#", "%23"), model);

            connection.update(qry.asUpdate());
        }
    }

    /**
     * Returns the student task list.
     *
     * @param courseInstanceUUID  the corresponding course instance uuid
     * @param exerciseSheetUUID   the corresponding exercise sheet uuid
     * @param matriculationNumber the student's matriculation number
     * @return list of task list entries
     */
    public List<StudentTaskListInfoDTO> getStudentTaskList(
        String courseInstanceUUID,
        String exerciseSheetUUID,
        String matriculationNumber
    ) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNumber);

        String courseInstanceURL = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetURL = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentURL = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_SELECT_STUDENT_TASK_LIST);
        qry.setIri("?courseInstance", courseInstanceURL);
        qry.setIri("?sheet", exerciseSheetURL);
        qry.setIri("?student", studentURL);

        List<StudentTaskListInfoDTO> list = new ArrayList<>();

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(qry.asQuery())) {
                ResultSet set = execution.execSelect();
                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    int orderNo = solution.getLiteral("?orderNo").getInt();
                    String taskId = solution.getLiteral("?taskId").getString();
                    boolean graded = solution.getLiteral("?graded").getBoolean();
                    boolean goalCompleted = solution.getLiteral("?goalCompleted").getBoolean();
                    String taskHeader = solution.getLiteral("?taskHeader").getString();
                    boolean submitted = solution.getLiteral("?submitted").getBoolean();

                    list.add(new StudentTaskListInfoDTO(orderNo, taskId, graded, goalCompleted, taskHeader, submitted));
                }
            }
        }
        return list;
    }

    /**
     * Marks a task assignment as submitted.
     *
     * @param courseInstanceUUID the course instance uuid
     * @param exerciseSheetUUID  the exercise sheet uuid
     * @param matriculationNo    the matriculation number
     * @param orderNo            the order no
     */
    public void markTaskAssignmentAsSubmitted(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int orderNo) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);
        assert orderNo > 0;

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String sheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentUrl = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_SUBMIT_TASK_ASSIGNMENT);
        qry.setIri("?student", studentUrl);
        qry.setIri("?sheet", sheetId);
        qry.setIri("?courseInstance", courseInstanceId);
        qry.setLiteral("?orderNo", orderNo);

        try (RDFConnection connection = getConnection()) {
            connection.update(qry.asUpdate());
        }
    }

    /**
     * Returns whether a task has already been submitted or not.
     *
     * @param courseInstanceUUID the course instance uuid
     * @param exerciseSheetUUID  the exercise sheet uuid
     * @param matriculationNo    the matriculation number
     * @param orderNo            the order number
     * @return {@code true} if the task has already been submitted, otherwise {@code false}
     */
    public boolean isTaskSubmitted(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int orderNo) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);
        assert orderNo > 0;

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String sheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentUrl = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_ASK_TASK_ALREADY_SUBMITTED);
        qry.setIri("?student", studentUrl);
        qry.setIri("?sheet", sheetId);
        qry.setIri("?courseInstance", courseInstanceId);
        qry.setLiteral("?orderNo", orderNo);

        try (RDFConnection connection = getConnection()) {
            return connection.queryAsk(qry.asQuery());
        }
    }

    /**
     * Assigns a new task.
     *
     * @param courseInstanceUUID  the course instance uuid
     * @param exerciseSheetUUID   the exercise sheet uuid
     * @param matriculationNumber the student's matriculation number
     * @throws AllTasksAlreadyAssignedException if all available tasks are already assigned,
     *                                          i.e. exercise sheet task count = assigned task count
     * @throws NoFurtherTasksAvailableException if no further tasks are available for assignment
     */
    public void assignNextTaskForStudent(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNumber) throws AllTasksAlreadyAssignedException, NoFurtherTasksAvailableException {
        try (RDFConnection connection = getConnection()) {
            assignNextTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, connection);
        }
    }

    /**
     * Returns whether a new task can be assigned or not.
     *
     * @param courseInstanceUUID  the course instance uuid
     * @param exerciseSheetUUID   the exercise sheet uuid
     * @param matriculationNumber the matriculation number
     * @return {@code true} if a new task can be assigned, otherwise {@code false}
     */
    public boolean canAssignNextTask(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNumber) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNumber);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String sheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentUrl = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        ParameterizedSparqlString alreadyAssignedTaskQuery = new ParameterizedSparqlString(QRY_SELECT_TOTAL_AND_ASSIGNED_TASK_COUNT);
        alreadyAssignedTaskQuery.setIri("?courseInstance", courseInstanceId);
        alreadyAssignedTaskQuery.setIri("?sheet", sheetId);
        alreadyAssignedTaskQuery.setIri("?student", studentUrl);

        ParameterizedSparqlString isClosedTaskQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT ?closed
            WHERE {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:isClosed ?closed.
            }
            """);
        isClosedTaskQry.setIri("?instance", courseInstanceId);
        isClosedTaskQry.setIri("?sheet", sheetId);
        isClosedTaskQry.setIri("?student", studentUrl);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(isClosedTaskQry.asQuery())) {
                ResultSet set = execution.execSelect();

                if (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    Literal closedLiteral = solution.getLiteral("?closed");
                    if (closedLiteral != null && closedLiteral.getBoolean()) {
                        return false;
                    }
                }
            }

            try (QueryExecution execution = connection.query(alreadyAssignedTaskQuery.asQuery())) {
                ResultSet set = execution.execSelect();
                if (!set.hasNext()) {
                    return false;
                }
                QuerySolution querySolution = set.nextSolution();

                int taskCount = querySolution.getLiteral("?taskCount").getInt();
                int assignedCount = querySolution.getLiteral("?assignedCount").getInt();

                if (taskCount == assignedCount) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Closes a given exercise sheet from an individual student.
     *
     * @param matriculationNumber the student's matriculation number
     * @param courseInstanceUUID  the course instance UUID
     * @param exerciseSheetUUID   the exercise sheet UUID
     */
    public void closeExerciseSheetFromAnIndividualStudent(String matriculationNumber, String courseInstanceUUID, String exerciseSheetUUID) {
        Objects.requireNonNull(matriculationNumber);
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String sheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentUrl = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        ParameterizedSparqlString updateQuery = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?individualAssignment etutor:isClosed ?closed.
            } INSERT {
              ?individualAssignment etutor:isClosed true.
            } WHERE {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:isClosed ?closed.
            }
            """);

        updateQuery.setIri("?instance", courseInstanceId);
        updateQuery.setIri("?student", studentUrl);
        updateQuery.setIri("?sheet", sheetId);

        try (RDFConnection connection = getConnection()) {
            connection.update(updateQuery.asUpdate());
        }
    }

    /**
     * Removes a file from an upload task.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param matriculationNo    the student's matriculation no
     * @param taskNo             the task no
     * @param fileId             the file id
     */
    public void removeFileFromUploadTask(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int taskNo, int fileId) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString removeQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?individualTask etutor:hasFileAttachmentId ?attachmentId.
            } WHERE {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo;
                              etutor:hasFileAttachmentId ?attachmentId.
            }
            """);

        removeQry.setIri("?instance", courseInstanceId);
        removeQry.setIri("?student", studentId);
        removeQry.setIri("?sheet", exerciseSheetId);
        removeQry.setLiteral("?orderNo", taskNo);
        removeQry.setLiteral("?attachmentId", fileId);

        try (RDFConnection connection = getConnection()) {
            connection.update(removeQry.asUpdate());
        }
    }

    /**
     * Sets the file for an upload task.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param matriculationNo    the matriculation no
     * @param taskNo             the task no
     * @param fileId             the file id
     * @throws NoUploadFileTypeException if the given task is not an upload task
     */
    public void setFileForUploadTask(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int taskNo, int fileId) throws NoUploadFileTypeException {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString typeAskQuery = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX etutor-task-assingment-type: <http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#>

            ASK {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo;
                              etutor:refersToTask ?task.
              ?task etutor:hasTaskAssignmentType etutor-task-assingment-type:UploadTask.
            }
            """);

        typeAskQuery.setIri("?instance", courseInstanceId);
        typeAskQuery.setIri("?sheet", exerciseSheetId);
        typeAskQuery.setIri("?student", studentId);
        typeAskQuery.setLiteral("?orderNo", taskNo);

        ParameterizedSparqlString insertQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?individualTask etutor:hasFileAttachmentId ?attachmentId.
            } INSERT {
              ?individualTask etutor:hasFileAttachmentId ?newAttachmentId.
            } WHERE {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo.

              OPTIONAL {
                ?individualTask etutor:hasFileAttachmentId ?attachmentId.
              }
            }
            """);

        insertQry.setIri("?instance", courseInstanceId);
        insertQry.setIri("?sheet", exerciseSheetId);
        insertQry.setIri("?student", studentId);
        insertQry.setLiteral("?orderNo", taskNo);
        insertQry.setLiteral("?newAttachmentId", fileId);

        try (RDFConnection connection = getConnection()) {

            boolean isUploadTask = connection.queryAsk(typeAskQuery.asQuery());

            if (!isUploadTask) {
                throw new NoUploadFileTypeException();
            }

            connection.update(insertQry.asUpdate());
        }
    }

    /**
     * Return the file id of an assignment.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param matriculationNo    the matriculation no
     * @param taskNo             the task no
     * @return {@link Optional} containing the file id
     */
    public Optional<Integer> getFileIdOfAssignment(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int taskNo) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String sheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT ?attachmentId
            WHERE {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo;
                              etutor:hasFileAttachmentId ?attachmentId.
            }
            """);

        query.setIri("?instance", courseInstanceId);
        query.setIri("?student", studentId);
        query.setIri("?sheet", sheetId);
        query.setLiteral("?orderNo", taskNo);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                if (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    Literal attachmentIdLiteral = solution.getLiteral("?attachmentId");

                    if (attachmentIdLiteral == null) {
                        return Optional.empty();
                    }
                    return Optional.of(attachmentIdLiteral.getInt());
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    /**
     * Handles a submission for an individual task
      * @param courseInstanceUUID the course instance
     * @param exerciseSheetUUID the exercise sheet
     * @param matriculationNo the matriculation number
     * @param taskNo the task number
     * @param submission the submission
     */
    public void setSubmissionForIndividualTask(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int taskNo, String submission, boolean isSubmitted, boolean isSolved){
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString insertNewSubmissionForIndividualTaskQry = new ParameterizedSparqlString(QRY_INSERT_NEW_INDIVIDUAL_TASK_SUBMISSION);
        insertNewSubmissionForIndividualTaskQry.setIri("?courseInstance", courseInstanceId);
        insertNewSubmissionForIndividualTaskQry.setLiteral("?submission", submission);
        insertNewSubmissionForIndividualTaskQry.setIri("?student", studentId);
        insertNewSubmissionForIndividualTaskQry.setIri("?sheet", exerciseSheetId);
        insertNewSubmissionForIndividualTaskQry.setLiteral("?instant", Instant.now().toString());
        insertNewSubmissionForIndividualTaskQry.setLiteral("?orderNo", taskNo);
        insertNewSubmissionForIndividualTaskQry.setLiteral("?isSubmitted", isSubmitted);
        insertNewSubmissionForIndividualTaskQry.setLiteral("?isSolved", isSolved);

        try (RDFConnection connection = getConnection()) {
            connection.update(insertNewSubmissionForIndividualTaskQry.asUpdate());
        }

        setLatestSubmissionForIndividualTask(courseInstanceId, exerciseSheetId, studentId, taskNo, submission);
    }

    /**
     * Updates the latest submission for an individual task
     * @param courseInstanceId the course instance
     * @param exerciseSheetId the exercise sheet
     * @param studentId the student id
     * @param taskNo the task number
     * @param submission the submission
     */
    public void setLatestSubmissionForIndividualTask(String courseInstanceId, String exerciseSheetId, String studentId, int taskNo, String submission){
        ParameterizedSparqlString insertQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?individualTask etutor:hasSubmission ?oldSubmission.
            } INSERT {
              ?individualTask etutor:hasSubmission ?newSubmission.
            } WHERE {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo.

              OPTIONAL {
                ?individualTask etutor:hasSubmission ?oldSubmission.
              }
            }
            """);


        insertQry.setIri("?instance", courseInstanceId);
        insertQry.setIri("?sheet", exerciseSheetId);
        insertQry.setIri("?student", studentId);
        insertQry.setLiteral("?orderNo", taskNo);
        insertQry.setLiteral("?newSubmission", submission);


        try (RDFConnection connection = getConnection()) {
            connection.update(insertQry.asUpdate());
        }
    }

    /**
     * Returns all submissions for an individual task
     * @param courseInstanceUUID the course instance
     * @param exerciseSheetUUID the exercise sheet
     * @param matriculationNo the matriculation number
     * @param taskNo the task number
     * @return a list containing the submissions
     */
    public Optional<List<IndividualTaskSubmissionDTO>> getAllSubmissionsForAssignment(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int taskNo){
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String sheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_ASK_INDIVIDUAL_TASK_SUBMISSIONS);
        query.setIri("?student", studentId);
        query.setIri("?exerciseSheet", sheetId);
        query.setIri("?courseInstance", courseInstanceId);
        query.setLiteral("?orderNo", taskNo);

        List<IndividualTaskSubmissionDTO> submissions = new ArrayList<>();

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();
                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    Literal submissionLiteral = solution.getLiteral("?submission");
                    Literal instantLiteral = solution.getLiteral("?instant");
                    Literal hasBeenSubmittedLiteral = solution.getLiteral("?isSubmitted");
                    Literal hasBeenSolvedLiteral = solution.getLiteral("?isSolved");

                    boolean hasBeenSubmitted = false;
                    boolean hasBeenSolved = false;

                    if(hasBeenSubmittedLiteral != null) hasBeenSubmitted = hasBeenSubmittedLiteral.getBoolean();
                    if(hasBeenSolvedLiteral != null) hasBeenSolved = hasBeenSolvedLiteral.getBoolean();
                    submissions.add(new IndividualTaskSubmissionDTO(Instant.parse(instantLiteral.getString()),
                        submissionLiteral.getString(),
                        hasBeenSubmitted,
                        hasBeenSolved
                    ));
                }
            }
        }
        if(submissions.isEmpty()) return Optional.empty();
        return Optional.of(submissions);
    }

    /**
     * Returns the latest/current submission for an individual task assignment
     * @param courseInstanceUUID the course instance
     * @param exerciseSheetUUID the exercise sheet
     * @param matriculationNo the matriculation number
     * @param taskNo the task number
     * @return {@link Optional} containing the submission
     */
    public Optional<String> getLatestSubmissionForAssignment(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int taskNo){
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String sheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT ?submission
            WHERE {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo;
                              etutor:hasSubmission ?submission.
            }
            """);
        query.setIri("?instance", courseInstanceId);
        query.setIri("?student", studentId);
        query.setIri("?sheet", sheetId);
        query.setLiteral("?orderNo", taskNo);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                if (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    Literal submissionLiteral = solution.getLiteral("?submission");

                    if (submissionLiteral == null) {
                        return Optional.empty();
                    }
                    return Optional.of(submissionLiteral.getString());
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    /**
     * Sets the points for an individual task that are assigned by the dispatcher
     * @param courseInstanceUUID the course instance UUID
     *      * @param exerciseSheetUUID  the exercise sheet UUID
     *      * @param matriculationNo    the matriculation no
     *      * @param taskNo             the task no
     *      * @param points             the points
     */
    public void setDispatcherPointsForAssignment(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int taskNo, int points){
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString insertQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?individualTask etutor:hasDispatcherPoints ?oldDispatcherPoints.
            } INSERT {
              ?individualTask etutor:hasDispatcherPoints ?newDispatcherPoints.
            } WHERE {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo.

              OPTIONAL {
                ?individualTask etutor:hasDispatcherPoints ?oldDispatcherPoints.
              }
            }
            """);


        insertQry.setIri("?instance", courseInstanceId);
        insertQry.setIri("?sheet", exerciseSheetId);
        insertQry.setIri("?student", studentId);
        insertQry.setLiteral("?orderNo", taskNo);
        insertQry.setLiteral("?newDispatcherPoints", points);


        try (RDFConnection connection = getConnection()) {
            connection.update(insertQry.asUpdate());
        }
    }


    /**
     * Returns the points for an individual task assignment
     * @param courseInstanceUUID the course instance
     * @param exerciseSheetUUID the exercise sheet
     * @param matriculationNo the matriculation number
     * @param taskNo the task number
     * @return {@link Optional} containing the points
     */
    public Optional<Integer> getDispatcherPoints(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int taskNo){
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String sheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT ?dispatcherPoints
            WHERE {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo;
                              etutor:hasDispatcherPoints ?dispatcherPoints.
            }
            """);
        query.setIri("?instance", courseInstanceId);
        query.setIri("?student", studentId);
        query.setIri("?sheet", sheetId);
        query.setLiteral("?orderNo", taskNo);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                if (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    Literal pointsLiteral = solution.getLiteral("?dispatcherPoints");

                    if (pointsLiteral == null) {
                        return Optional.empty();
                    }
                    return Optional.of(pointsLiteral.getInt());
                } else {
                    return Optional.empty();
                }
            }
        }
    }
    /**
     * Sets the (highest) chosen diagnose level for an individual task
     * @param courseInstanceUUID the course instance UUID
     *      * @param exerciseSheetUUID  the exercise sheet UUID
     *      * @param matriculationNo    the matriculation no
     *      * @param taskNo             the task no
     *      * @param points             the diagnose level
     */

    public void setHighestDiagnoseLevel(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int taskNo, int diagnoseLevel){
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString insertQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?individualTask etutor:hasDiagnoseLevel ?oldDiagnoseLevel.
            } INSERT {
              ?individualTask etutor:hasDiagnoseLevel ?newDiagnoseLevel.
            } WHERE {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo.

              OPTIONAL {
                ?individualTask etutor:hasDiagnoseLevel ?oldDiagnoseLevel.
              }
            }
            """);


        insertQry.setIri("?instance", courseInstanceId);
        insertQry.setIri("?sheet", exerciseSheetId);
        insertQry.setIri("?student", studentId);
        insertQry.setLiteral("?orderNo", taskNo);
        insertQry.setLiteral("?newDiagnoseLevel", diagnoseLevel);


        try (RDFConnection connection = getConnection()) {
            connection.update(insertQry.asUpdate());
        }
    }

    /**
     * Returns the highest chosen diagnose level an individual task assignment
     * @param courseInstanceUUID the course instance
     * @param exerciseSheetUUID the exercise sheet
     * @param matriculationNo the matriculation number
     * @param taskNo the task number
     * @return {@link Optional} containing the points
     */
    public Optional<Integer> getDiagnoseLevel(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo, int taskNo){
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String sheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT ?diagnoseLevel
            WHERE {
              ?instance a etutor:CourseInstance.
              ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
              ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                    etutor:fromCourseInstance ?instance;
                                    etutor:hasIndividualTask ?individualTask.
              ?individualTask etutor:hasOrderNo ?orderNo;
                              etutor:hasDiagnoseLevel ?diagnoseLevel.
            }
            """);
        query.setIri("?instance", courseInstanceId);
        query.setIri("?student", studentId);
        query.setIri("?sheet", sheetId);
        query.setLiteral("?orderNo", taskNo);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                if (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    Literal diagnoseLevelLiteral = solution.getLiteral("?diagnoseLevel");

                    if (diagnoseLevelLiteral== null) {
                        return Optional.empty();
                    }
                    return Optional.of(diagnoseLevelLiteral.getInt());
                } else {
                    return Optional.empty();
                }
            }
        }
    }
    /**
     * Returns the reached goal ids of a student from a given course instance.
     *
     * @param courseInstanceUrl   the course instance URL
     * @param matriculationNumber the student's matriculation number
     * @return {@link List} containing the reached goals' ids
     */
    public @NotNull List<String> getReachedGoalsOfStudentAndCourseInstance(@NotNull String courseInstanceUrl, @NotNull String matriculationNumber) {
        String studentUrl = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        try (RDFConnection connection = getConnection()) {
            return getReachedGoalsOfStudentAndCourseInstance(courseInstanceUrl, studentUrl, connection);
        }
    }

    //region Private methods

    /**
     * Assigns the next available task according to the student's current learning curve.
     *
     * @param courseInstanceUUID  the course instance uuid
     * @param exerciseSheetUUID   the exercise sheet uuid
     * @param matriculationNumber the student's matriculation number
     * @param connection          the RDF connection to the fuseki instance
     * @throws AllTasksAlreadyAssignedException if all available tasks are already assigned,
     *                                          i.e. exercise sheet task count = assigned task count
     * @throws NoFurtherTasksAvailableException if no further tasks are available for assignment
     */
    private void assignNextTask(String courseInstanceUUID, String exerciseSheetUUID, String
        matriculationNumber, RDFConnection connection) throws AllTasksAlreadyAssignedException, NoFurtherTasksAvailableException {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNumber);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String sheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentUrl = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        ParameterizedSparqlString alreadyAssignedTaskQuery = new ParameterizedSparqlString(QRY_SELECT_TOTAL_AND_ASSIGNED_TASK_COUNT);
        alreadyAssignedTaskQuery.setIri("?courseInstance", courseInstanceId);
        alreadyAssignedTaskQuery.setIri("?sheet", sheetId);
        alreadyAssignedTaskQuery.setIri("?student", studentUrl);

        try (QueryExecution execution = connection.query(alreadyAssignedTaskQuery.asQuery())) {
            ResultSet set = execution.execSelect();
            if (!set.hasNext()) {
                log.warn("Task assignment on an exercise sheet which does not exist!");
                throw new IllegalStateException("No exercise sheet assigned!");
            }
            QuerySolution querySolution = set.nextSolution();

            int taskCount = querySolution.getLiteral("?taskCount").getInt();
            int assignedCount = querySolution.getLiteral("?assignedCount").getInt();

            if (taskCount == assignedCount) {
                throw new AllTasksAlreadyAssignedException();
            }

            String taskToAssign = getNextTaskAssignmentForAllocation(courseInstanceId, sheetId, studentUrl, connection);

            if (taskToAssign != null) {
                insertNewAssignedTask(courseInstanceId, sheetId, studentUrl, taskToAssign, connection);
            } else {
                throw new NoFurtherTasksAvailableException();
            }
        }
    }

    /**
     * Method which is looking for the best fitting assignment for the current exercise sheet
     * based on the student's current knowledge.
     *
     * @param courseInstanceUrl the course instance URL
     * @param exerciseSheetUrl  the exercise sheet URL
     * @param studentUrl        the student URL
     * @param connection        the RDF connection to the fuseki instance
     * @return resource as string of the the task assignment which should be allocated.
     */
    private String getNextTaskAssignmentForAllocation(@NotNull String courseInstanceUrl, @NotNull String exerciseSheetUrl,
                                                      @NotNull String studentUrl, @NotNull RDFConnection connection) {

        List<String> reachedGoals = getReachedGoalsOfStudentAndCourseInstance(courseInstanceUrl, studentUrl, connection);

        ParameterizedSparqlString getPossibleAssignmentsQuery = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

            SELECT ?goalOfCourse ?task ?distance
            WHERE {
              SELECT ?goalOfCourse ?task (COUNT(DISTINCT ?mid) AS ?distance)
              WHERE {
                ?courseInstance a etutor:CourseInstance.
                ?courseInstance etutor:hasCourse ?course.
                ?course etutor:hasGoal/etutor:hasSubGoal* ?goalOfCourse.
                ?sheet etutor:containsLearningGoal/etutor:hasSubGoal*/etutor:dependsOn* ?goalOfCourse.
                ?sheet etutor:hasExerciseSheetDifficulty/rdf:value ?sheetDifficultyValue.

                ?task a etutor:TaskAssignment.
                ?task etutor:hasTaskDifficulty/rdf:value ?taskDifficultyValue.

                ?goalOfCourse (^etutor:hasSubGoal|^etutor:dependsOn)? ?mid.
                ?mid (^etutor:hasSubGoal|^etutor:dependsOn)* ?endGoal.
                ?endGoal etutor:hasTaskAssignment ?task.

                FILTER(?goalOfCourse NOT IN (?reachedGoals))
                FILTER(?taskDifficultyValue <= ?sheetDifficultyValue)

                FILTER (NOT EXISTS {
                    ?goalOfCourse (^etutor:hasSubGoal/etutor:dependsOn+|etutor:dependsOn+) ?dependentGoal.
                    GRAPH ?courseInstance {
                      ?dependentGoal a etutor:Goal
                    }
                    FILTER(?dependentGoal NOT IN (?reachedGoals))
                }).
                FILTER (NOT EXISTS {
                    ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                    ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                          etutor:fromCourseInstance ?courseInstance;
                                          etutor:hasIndividualTask ?individualTask.
                    ?individualTask etutor:refersToTask ?task.
                }).
              }
              GROUP BY ?goalOfCourse ?task
            }
            ORDER BY ?distance
            """);
        getPossibleAssignmentsQuery.setIri("?courseInstance", courseInstanceUrl);
        getPossibleAssignmentsQuery.setIri("?sheet", exerciseSheetUrl);
        getPossibleAssignmentsQuery.setIri("?student", studentUrl);
        String query = getPossibleAssignmentsQuery.toString();
        query = query.replace("?reachedGoals", StreamEx.of(reachedGoals).map(x -> String.format("<%s>", x)).joining(", "));

        try (QueryExecution execution = connection.query(query)) {
            ResultSet set = execution.execSelect();
            int minDistance = Integer.MAX_VALUE;
            List<String> taskSheets = new ArrayList<>();

            if (set.hasNext()) {
                do {
                    QuerySolution solution = set.nextSolution();
                    int distance = solution.getLiteral("?distance").getInt();

                    if (distance <= minDistance) {
                        minDistance = distance;
                        taskSheets.add(solution.getResource("?task").getURI());
                    }
                } while (set.hasNext());

                if (taskSheets.size() > 1) {
                    return taskSheets.get(random.nextInt(taskSheets.size()));
                } else { // taskSheets.size() == 1
                    return taskSheets.get(0);
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Returns the list of reached goals of a student from a specific course instance.
     *
     * @param courseInstanceUrl the course instance URL
     * @param studentUrl        the student URL
     * @param connection        the RDF connection to the fuseki instance
     * @return list of reached learning goals
     */
    private @NotNull List<String> getReachedGoalsOfStudentAndCourseInstance(@NotNull String courseInstanceUrl, @NotNull String studentUrl,
                                                                            @NotNull RDFConnection connection) {
        List<String> reachedGoals = new ArrayList<>();

        ParameterizedSparqlString reachedGoalQuery = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT ?reachedGoal
            WHERE {
              GRAPH ?courseInstance {
                ?reachedGoal a etutor:Goal.
                ?reachedGoal etutor:isCompletedFrom ?student.
              }
            }
            """);
        reachedGoalQuery.setIri("?courseInstance", courseInstanceUrl);
        reachedGoalQuery.setIri("?student", studentUrl);

        try (QueryExecution execution = connection.query(reachedGoalQuery.asQuery())) {
            ResultSet set = execution.execSelect();

            while (set.hasNext()) {
                QuerySolution solution = set.nextSolution();
                reachedGoals.add(solution.getResource("?reachedGoal").getURI());
            }
        }

        return reachedGoals;
    }

    /**
     * Inserts the newly assigned task.
     *
     * @param courseInstanceUrl  the course instance url
     * @param exerciseSheetUrl   the exercise sheet url
     * @param studentUrl         the student's url
     * @param newTaskResourceUrl the task url
     * @param connection         the RDF connection to the fuseki server, will not be closed
     */
    private void insertNewAssignedTask(@NotNull String courseInstanceUrl, @NotNull String
        exerciseSheetUrl, @NotNull String studentUrl, @NotNull String newTaskResourceUrl, @NotNull RDFConnection
                                           connection) {
        ParameterizedSparqlString latestOrderNoQry = new ParameterizedSparqlString(QRY_SELECT_MAX_ORDER_NO);
        latestOrderNoQry.setIri("?courseInstance", courseInstanceUrl);
        latestOrderNoQry.setIri("?student", studentUrl);
        latestOrderNoQry.setIri("?sheet", exerciseSheetUrl);

        int orderNo;

        try (QueryExecution queryExecution = connection.query(latestOrderNoQry.asQuery())) {
            ResultSet set = queryExecution.execSelect();
            //noinspection ResultOfMethodCallIgnored
            set.hasNext();

            QuerySolution solution = set.nextSolution();
            orderNo = solution.getLiteral("?maxOrderNo").getInt() + 1;
        }

        ParameterizedSparqlString individualAssignmentInsertQry = new ParameterizedSparqlString(QRY_INSERT_NEW_INDIVIDUAL_ASSIGNMENT);
        individualAssignmentInsertQry.setIri("?courseInstance", courseInstanceUrl);
        individualAssignmentInsertQry.setIri("?student", studentUrl);
        individualAssignmentInsertQry.setIri("?sheet", exerciseSheetUrl);
        individualAssignmentInsertQry.setIri("?newTask", newTaskResourceUrl);
        individualAssignmentInsertQry.setLiteral("?newOrderNo", orderNo);

        connection.update(individualAssignmentInsertQry.asUpdate());
    }
    //endregion
}
