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
import at.jku.dke.etutor.service.dto.student.StudentTaskListInfoDTO;
import at.jku.dke.etutor.service.exception.StudentCSVImportException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    private static final String QRY_SELECT_STUDENT_COURSE_ASSIGNMENT_OVERVIEW =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT (STR(?exerciseSheet) AS ?exerciseSheetId) ?exerciseSheetName (STR(?difficulty) AS ?difficultyURI) ?completed
            WHERE {
              ?instance a etutor:CourseInstance.
              ?instance etutor:hasStudent ?student.
              ?instance etutor:hasExerciseSheet ?exerciseSheet.
              ?exerciseSheet rdfs:label ?exerciseSheetName.
              ?exerciseSheet etutor:hasExerciseSheetDifficulty ?difficulty.
              BIND(false AS ?completed).
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

        SELECT ?orderNo (STR(?task) AS ?taskId) ?graded ?goalCompleted ?taskHeader
        WHERE {
          ?courseInstance a etutor:CourseInstance.
          ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
          ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                etutor:fromCourseInstance ?courseInstance;
                                etutor:hasIndividualTask ?individualTask.
          ?individualTask etutor:hasOrderNo ?orderNo;
                          etutor:refersToTask ?task;
                          etutor:isGraded ?graded;
                          etutor:isLearningGoalCompleted ?goalCompleted.
          ?task etutor:hasTaskHeader ?taskHeader.
        }
        """;

    private final UserService userService;
    private final StudentRepository studentRepository;

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

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(qry.asQuery())) {
                ResultSet set = execution.execSelect();
                List<CourseInstanceProgressOverviewDTO> items = new ArrayList<>();
                while (set.hasNext()) {
                    var solution = set.nextSolution();

                    String sheetId = solution.getLiteral("?exerciseSheetId").getString();
                    String sheetName = solution.getLiteral("?exerciseSheetName").getString();
                    String difficultyUri = solution.getLiteral("?difficultyURI").getString();
                    boolean completed = solution.getLiteral("?completed").getBoolean();

                    items.add(new CourseInstanceProgressOverviewDTO(sheetId, sheetName, difficultyUri, completed));
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
     */
    public void openExerciseSheetForStudent(String matriculationNumber, String courseInstanceUUID, String exerciseSheetUUID) {
        Objects.requireNonNull(matriculationNumber);
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);

        String courseInstanceURL = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetURL = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentURL = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);

        Model model = ModelFactory.createDefaultModel();
        Resource studentResource = model.createResource(studentURL);

        Resource individualTaskAssignmentResource = model.createResource();
        individualTaskAssignmentResource.addProperty(RDF.type, ETutorVocabulary.IndividualTaskAssignment);
        individualTaskAssignmentResource.addProperty(ETutorVocabulary.fromCourseInstance, model.createResource(courseInstanceURL));
        individualTaskAssignmentResource.addProperty(ETutorVocabulary.fromExerciseSheet, model.createResource(exerciseSheetURL));

        studentResource.addProperty(ETutorVocabulary.hasIndividualTaskAssignment, individualTaskAssignmentResource);

        // TODO: Implement task assignment according to the individual learning curve

        try (RDFConnection connection = getConnection()) {
            connection.load(model);
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

                    list.add(new StudentTaskListInfoDTO(orderNo, taskId, graded, goalCompleted, taskHeader));
                }
            }
        }
        return list;
    }
}
