package at.jku.dke.etutor.service;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.CourseDTO;
import at.jku.dke.etutor.service.dto.LearningGoalUpdateAssignmentDTO;
import at.jku.dke.etutor.service.dto.StudentSelfEvaluationLearningGoalDTO;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.LearningGoalAssignmentDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.ExerciseSheetAlreadyOpenedException;
import at.jku.dke.etutor.service.exception.NoFurtherTasksAvailableException;
import at.jku.dke.etutor.service.exception.NoUploadFileTypeException;
import at.jku.dke.etutor.service.exception.StudentCSVImportException;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.javatuples.Quartet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link StudentService}.
 *
 * @author fne
 */
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class StudentServiceIT {

    private static final String OWNER = "admin";

    @Autowired
    private RDFConnectionFactory rdfConnectionFactory;

    @Autowired
    private SPARQLEndpointService sparqlEndpointService;

    @Autowired
    private CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService;

    @Autowired
    private ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService;

    @Autowired
    private AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    @Autowired
    private SpringLiquibase springLiquibase;

    /**
     * Initializes the testing environment.
     *
     * @throws Exception must not be thrown
     */
    @BeforeAll
    public void setup() throws Exception {
        springLiquibase.setDropFirst(true);
        springLiquibase.afterPropertiesSet();

        rdfConnectionFactory.clearDataset();
        sparqlEndpointService.insertScheme();
    }

    /**
     * Tests the import students from file method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Transactional
    public void testImportStudentsFromFile() throws Exception {
        MultipartFile file = new MockMultipartFile(
            "file.csv",
            "file.csv",
            "text/csv",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("test_students.csv"))
        );

        var importedStudents = studentService.importStudentsFromFile(file);

        assertThat(importedStudents).hasSize(2);

        var students = userService.getAvailableStudents();

        assertThat(students).hasSize(2);
    }

    /**
     * Tests the import students from file method with an invalid file type
     * and a null value.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Transactional
    public void testImportStudentsFromFileInvalidFileTypeAndNull() throws Exception {
        MultipartFile file = new MockMultipartFile(
            "file.csv",
            "file.csv",
            "application/text",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("test_students.csv"))
        );

        assertThatThrownBy(() -> studentService.importStudentsFromFile(file)).isInstanceOf(StudentCSVImportException.class);

        assertThatThrownBy(() -> studentService.importStudentsFromFile(null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the import students from file method with a csv file
     * which contains an invalid column.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Transactional
    public void testImportStudentsFromFileWithInvalidColumns() throws Exception {
        MultipartFile file = new MockMultipartFile(
            "file.csv",
            "file.csv",
            "application/text",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("test_students_missing_email.csv"))
        );

        assertThatThrownBy(() -> studentService.importStudentsFromFile(file)).isInstanceOf(StudentCSVImportException.class);
    }

    /**
     * Tests the get courses from student method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Transactional
    public void testGetCoursesFromStudent() throws Exception {
        rdfConnectionFactory.clearDataset();
        sparqlEndpointService.insertScheme();

        MultipartFile file = new MockMultipartFile(
            "file.csv",
            "file.csv",
            "text/csv",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("test_students.csv"))
        );

        var importedStudents = studentService.importStudentsFromFile(file);

        String mNr = importedStudents.get(0).getMatriculationNumber();

        CourseDTO newCourseDTO = new CourseDTO();
        newCourseDTO.setName("Testcourse1");
        newCourseDTO.setCourseType("LVA");

        newCourseDTO = sparqlEndpointService.insertNewCourse(newCourseDTO, "admin");

        CourseDTO secondNewCourseDTO = new CourseDTO();
        secondNewCourseDTO.setName("Testcourse2");
        secondNewCourseDTO.setCourseType("LVA");

        secondNewCourseDTO = sparqlEndpointService.insertNewCourse(secondNewCourseDTO, "admin");

        NewCourseInstanceDTO firstInstance = new NewCourseInstanceDTO();
        firstInstance.setYear(2021);
        firstInstance.setCourseId(newCourseDTO.getId());
        firstInstance.setTermId(ETutorVocabulary.Summer.getURI());

        String firstInstanceId = courseInstanceSPARQLEndpointService.createNewCourseInstance(firstInstance);

        NewCourseInstanceDTO secondInstance = new NewCourseInstanceDTO();
        secondInstance.setYear(2021);
        secondInstance.setCourseId(secondNewCourseDTO.getId());
        secondInstance.setTermId(ETutorVocabulary.Summer.getURI());

        String secondInstanceId = courseInstanceSPARQLEndpointService.createNewCourseInstance(secondInstance);

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(mNr), firstInstanceId);
        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(mNr), secondInstanceId);

        var courses = studentService.getCoursesFromStudent(mNr);

        assertThat(courses).hasSize(2);
    }

    /**
     * Tests the get courses from student method with a null value.
     */
    @Test
    public void testGetCoursesFromStudentNull() {
        assertThatThrownBy(() -> studentService.getCoursesFromStudent(null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the get progress overview method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetProgressOverview() throws Exception {
        rdfConnectionFactory.clearDataset();
        sparqlEndpointService.insertScheme();
        MultipartFile file = new MockMultipartFile(
            "file.csv",
            "file.csv",
            "text/csv",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("test_students.csv"))
        );

        var importedStudents = studentService.importStudentsFromFile(file);

        String mNr = importedStudents.get(0).getMatriculationNumber();

        CourseDTO newCourseDTO = new CourseDTO();
        newCourseDTO.setName("Testcourse1");
        newCourseDTO.setCourseType("LVA");

        newCourseDTO = sparqlEndpointService.insertNewCourse(newCourseDTO, "admin");

        NewCourseInstanceDTO firstInstance = new NewCourseInstanceDTO();
        firstInstance.setYear(2021);
        firstInstance.setCourseId(newCourseDTO.getId());
        firstInstance.setTermId(ETutorVocabulary.Summer.getURI());

        String firstInstanceId = courseInstanceSPARQLEndpointService.createNewCourseInstance(firstInstance);

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(mNr), firstInstanceId);

        NewExerciseSheetDTO firstNewExerciseSheet = new NewExerciseSheetDTO();
        firstNewExerciseSheet.setName("TestSheet 1");
        firstNewExerciseSheet.setDifficultyId(ETutorVocabulary.Medium.getURI());
        firstNewExerciseSheet.setLearningGoals(new ArrayList<>());
        firstNewExerciseSheet.setTaskCount(1);

        var firstExerciseSheet = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(firstNewExerciseSheet, "admin");

        NewExerciseSheetDTO secondNewExerciseSheet = new NewExerciseSheetDTO();
        secondNewExerciseSheet.setName("TestSheet 2");
        secondNewExerciseSheet.setDifficultyId(ETutorVocabulary.Medium.getURI());
        secondNewExerciseSheet.setLearningGoals(new ArrayList<>());
        secondNewExerciseSheet.setTaskCount(1);

        var secondExerciseSheet = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(secondNewExerciseSheet, "admin");

        String courseInstanceUUID = firstInstanceId.substring(firstInstanceId.lastIndexOf('#') + 1);

        courseInstanceSPARQLEndpointService.addExerciseSheetCourseInstanceAssignments(
            courseInstanceUUID,
            Arrays.asList(firstExerciseSheet.getId(), secondExerciseSheet.getId())
        );

        var items = studentService.getProgressOverview(mNr, courseInstanceUUID);

        assertThat(items).hasSize(2);
    }

    /**
     * Tests the get progress overview method with
     * null values.
     */
    @Test
    public void testGetProgressOverviewNull() {
        assertThatThrownBy(() -> studentService.getProgressOverview(null, null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> studentService.getProgressOverview("k11804012", null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the has student opened the exercise sheet method with null values
     */
    @Test
    public void testHasStudentOpenedTheExerciseSheetNullValues() {
        assertThatThrownBy(() -> studentService.hasStudentOpenedTheExerciseSheet(null, null, null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> studentService.hasStudentOpenedTheExerciseSheet("test", null, null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> studentService.hasStudentOpenedTheExerciseSheet("test", "test", null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the open exercise sheet for student method with null values.
     */
    @Test
    public void testOpenExerciseSheetForStudentNullValues() {
        assertThatThrownBy(() -> studentService.openExerciseSheetForStudent(null, null, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> studentService.openExerciseSheetForStudent("test", null, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> studentService.openExerciseSheetForStudent("test", "test", null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the open exercise sheet for student method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Transactional
    public void testOpenExerciseSheetForStudent() throws Exception {
        MultipartFile file = new MockMultipartFile(
            "file.csv",
            "file.csv",
            "text/csv",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("test_students.csv"))
        );

        var importedStudents = studentService.importStudentsFromFile(file);

        String mNr = importedStudents.get(0).getMatriculationNumber();

        CourseDTO newCourseDTO = new CourseDTO();
        newCourseDTO.setName("New test course");
        newCourseDTO.setCourseType("LVA");

        newCourseDTO = sparqlEndpointService.insertNewCourse(newCourseDTO, "admin");

        NewCourseInstanceDTO firstInstance = new NewCourseInstanceDTO();
        firstInstance.setYear(2021);
        firstInstance.setCourseId(newCourseDTO.getId());
        firstInstance.setTermId(ETutorVocabulary.Summer.getURI());

        String firstInstanceId = courseInstanceSPARQLEndpointService.createNewCourseInstance(firstInstance);

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(mNr), firstInstanceId);

        NewExerciseSheetDTO firstNewExerciseSheet = new NewExerciseSheetDTO();
        firstNewExerciseSheet.setName("TestSheet 1");
        firstNewExerciseSheet.setDifficultyId(ETutorVocabulary.Medium.getURI());
        firstNewExerciseSheet.setTaskCount(1);
        firstNewExerciseSheet.setLearningGoals(new ArrayList<>());

        var exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(firstNewExerciseSheet, "admin");
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        String courseInstanceUUID = firstInstanceId.substring(firstInstanceId.lastIndexOf('#') + 1);
        courseInstanceSPARQLEndpointService.addExerciseSheetCourseInstanceAssignments(
            courseInstanceUUID,
            Collections.singletonList(exerciseSheetDTO.getId())
        );

        assertThatThrownBy(() -> studentService.openExerciseSheetForStudent(mNr, courseInstanceUUID, exerciseSheetUUID))
            .isInstanceOf(NoFurtherTasksAvailableException.class);

        assertThat(studentService.hasStudentOpenedTheExerciseSheet(mNr, courseInstanceUUID, exerciseSheetUUID)).isTrue();
    }

    /**
     * Tests the open exercise sheet method with an already opened
     * exercise sheet.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testOpenAlreadyOpenedExerciseSheet() throws Exception {
        var values = initTestGoalAndCourse();

        Model model = values.getValue0();
        CourseDTO course = values.getValue1();
        String courseInstanceId = values.getValue2();
        String courseInstanceUUID = values.getValue3();

        String matriculationNumber = importStudentsAndGetFirstStudentsMatriculationNumber();

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(matriculationNumber), courseInstanceId);

        // Self evaluation:
        List<StudentSelfEvaluationLearningGoalDTO> selfEvaluations = getStudentSelfEvaluations(model, "^(?!Basic SQL|outerjoin|join).*$");
        studentService.saveSelfEvaluation(courseInstanceUUID, matriculationNumber, selfEvaluations);

        // Create task and exercise sheet
        // Task
        NewTaskAssignmentDTO newTaskAssignmentDTO = new TaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator(OWNER);
        newTaskAssignmentDTO.setHeader("Join assignment");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.NoType.getURI());
        newTaskAssignmentDTO.setLearningGoalIds(Collections.singletonList(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join")));

        assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        // Create corresponding exercise sheet.
        NewExerciseSheetDTO newExerciseSheetDTO = new ExerciseSheetDTO();
        newExerciseSheetDTO.setName("Join exercise sheet");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        newExerciseSheetDTO.setLearningGoals(Collections.singletonList(new LearningGoalAssignmentDTO(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join"), 1)));
        newExerciseSheetDTO.setTaskCount(1);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        studentService.openExerciseSheetForStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID);

        assertThatThrownBy(() -> studentService.openExerciseSheetForStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID))
            .isInstanceOf(ExerciseSheetAlreadyOpenedException.class);
    }

    /**
     * Tests the close exercise sheet from an individual student method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testCloseExerciseSheetFromAnIndividualStudent() throws Exception {
        var values = initTestGoalAndCourse();

        Model model = values.getValue0();
        CourseDTO course = values.getValue1();
        String courseInstanceId = values.getValue2();
        String courseInstanceUUID = values.getValue3();

        String matriculationNumber = importStudentsAndGetFirstStudentsMatriculationNumber();

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(matriculationNumber), courseInstanceId);

        // Self evaluation:
        List<StudentSelfEvaluationLearningGoalDTO> selfEvaluations = getStudentSelfEvaluations(model, "^(?!Basic SQL|outerjoin|join).*$");
        studentService.saveSelfEvaluation(courseInstanceUUID, matriculationNumber, selfEvaluations);

        // Create task and exercise sheet
        // Task
        NewTaskAssignmentDTO newTaskAssignmentDTO = new TaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator(OWNER);
        newTaskAssignmentDTO.setHeader("Join assignment");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.NoType.getURI());
        newTaskAssignmentDTO.setLearningGoalIds(Collections.singletonList(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join")));

        assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        // Create corresponding exercise sheet.
        NewExerciseSheetDTO newExerciseSheetDTO = new ExerciseSheetDTO();
        newExerciseSheetDTO.setName("Join exercise sheet");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        newExerciseSheetDTO.setLearningGoals(Collections.singletonList(new LearningGoalAssignmentDTO(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join"), 1)));
        newExerciseSheetDTO.setTaskCount(2);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        studentService.openExerciseSheetForStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID);

        studentService.closeExerciseSheetFromAnIndividualStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID);
        boolean canAssignNext = studentService.canAssignNextTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber);
        assertThat(canAssignNext).isFalse();
    }

    /**
     * Tests the is task submitted method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testTestIsTaskSubmitted() throws Exception {
        var values = initTestGoalAndCourse();

        Model model = values.getValue0();
        CourseDTO course = values.getValue1();
        String courseInstanceId = values.getValue2();
        String courseInstanceUUID = values.getValue3();

        String matriculationNumber = importStudentsAndGetFirstStudentsMatriculationNumber();

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(matriculationNumber), courseInstanceId);

        // Self evaluation:
        List<StudentSelfEvaluationLearningGoalDTO> selfEvaluations = getStudentSelfEvaluations(model, "^(?!Basic SQL|outerjoin|join).*$");
        studentService.saveSelfEvaluation(courseInstanceUUID, matriculationNumber, selfEvaluations);

        // Create task and exercise sheet
        // Task
        NewTaskAssignmentDTO newTaskAssignmentDTO = new TaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator(OWNER);
        newTaskAssignmentDTO.setHeader("Join assignment");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.NoType.getURI());
        newTaskAssignmentDTO.setLearningGoalIds(Collections.singletonList(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join")));

        assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        // Create corresponding exercise sheet.
        NewExerciseSheetDTO newExerciseSheetDTO = new ExerciseSheetDTO();
        newExerciseSheetDTO.setName("Join exercise sheet");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        newExerciseSheetDTO.setLearningGoals(Collections.singletonList(new LearningGoalAssignmentDTO(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join"), 1)));
        newExerciseSheetDTO.setTaskCount(1);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        studentService.openExerciseSheetForStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID);

        boolean taskSubmitted = studentService.isTaskSubmitted(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1);
        assertThat(taskSubmitted).isFalse();

        // Submit task
        studentService.markTaskAssignmentAsSubmitted(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1);

        taskSubmitted = studentService.isTaskSubmitted(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1);
        assertThat(taskSubmitted).isTrue();
    }

    /**
     * Tests the can assign next task method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testCanAssignNextTask() throws Exception {
        var values = initTestGoalAndCourse();

        Model model = values.getValue0();
        CourseDTO course = values.getValue1();
        String courseInstanceId = values.getValue2();
        String courseInstanceUUID = values.getValue3();

        String matriculationNumber = importStudentsAndGetFirstStudentsMatriculationNumber();

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(matriculationNumber), courseInstanceId);

        // Self evaluation:
        List<StudentSelfEvaluationLearningGoalDTO> selfEvaluations = getStudentSelfEvaluations(model, "^(?!Basic SQL|outerjoin|join|innerjoin).*$");
        studentService.saveSelfEvaluation(courseInstanceUUID, matriculationNumber, selfEvaluations);

        // Create task and exercise sheet
        // Task
        NewTaskAssignmentDTO newTaskAssignmentDTO = new TaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator(OWNER);
        newTaskAssignmentDTO.setHeader("Join assignment1");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.NoType.getURI());
        newTaskAssignmentDTO.setLearningGoalIds(Collections.singletonList(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join")));

        assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        newTaskAssignmentDTO = new TaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator(OWNER);
        newTaskAssignmentDTO.setHeader("Join assignment2");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.NoType.getURI());
        newTaskAssignmentDTO.setLearningGoalIds(Collections.singletonList(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join")));

        assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        // Create corresponding exercise sheet.
        NewExerciseSheetDTO newExerciseSheetDTO = new ExerciseSheetDTO();
        newExerciseSheetDTO.setName("Join exercise sheet");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        newExerciseSheetDTO.setLearningGoals(Collections.singletonList(new LearningGoalAssignmentDTO(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join"), 1)));
        newExerciseSheetDTO.setTaskCount(2);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        studentService.openExerciseSheetForStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID);

        studentService.markTaskAssignmentAsSubmitted(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1);

        boolean canAssignNextTask = studentService.canAssignNextTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber);
        assertThat(canAssignNextTask).isTrue();

        studentService.assignNextTaskForStudent(courseInstanceUUID, exerciseSheetUUID, matriculationNumber);

        canAssignNextTask = studentService.canAssignNextTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber);
        assertThat(canAssignNextTask).isFalse();
    }

    /**
     * Tests the set file for upload task method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testSetFileForUploadTask() throws Exception {
        var values = initTestGoalAndCourse();

        Model model = values.getValue0();
        CourseDTO course = values.getValue1();
        String courseInstanceId = values.getValue2();
        String courseInstanceUUID = values.getValue3();

        String matriculationNumber = importStudentsAndGetFirstStudentsMatriculationNumber();

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(matriculationNumber), courseInstanceId);

        // Self evaluation:
        List<StudentSelfEvaluationLearningGoalDTO> selfEvaluations = getStudentSelfEvaluations(model, "^(?!Basic SQL|outerjoin|join).*$");
        studentService.saveSelfEvaluation(courseInstanceUUID, matriculationNumber, selfEvaluations);

        // Create task and exercise sheet
        // Task
        NewTaskAssignmentDTO newTaskAssignmentDTO = new TaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator(OWNER);
        newTaskAssignmentDTO.setHeader("Join assignment");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.UploadTask.getURI());
        newTaskAssignmentDTO.setLearningGoalIds(Collections.singletonList(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join")));

        assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        // Create corresponding exercise sheet.
        NewExerciseSheetDTO newExerciseSheetDTO = new ExerciseSheetDTO();
        newExerciseSheetDTO.setName("Join exercise sheet");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        newExerciseSheetDTO.setLearningGoals(Collections.singletonList(new LearningGoalAssignmentDTO(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join"), 1)));
        newExerciseSheetDTO.setTaskCount(1);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        studentService.openExerciseSheetForStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID);

        final int fileId = 2;

        // Submit task
        studentService.setFileForUploadTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1, fileId);

        var optionalFileId = studentService.getFileIdOfIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1);

        assertThat(optionalFileId).isPresent();
        assertThat(optionalFileId).hasValue(fileId);
    }

    /**
     * Tests the remove file from upload task method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testRemoveFileFromUploadTask() throws Exception {
        var values = initTestGoalAndCourse();

        Model model = values.getValue0();
        CourseDTO course = values.getValue1();
        String courseInstanceId = values.getValue2();
        String courseInstanceUUID = values.getValue3();

        String matriculationNumber = importStudentsAndGetFirstStudentsMatriculationNumber();

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(matriculationNumber), courseInstanceId);

        // Self evaluation:
        List<StudentSelfEvaluationLearningGoalDTO> selfEvaluations = getStudentSelfEvaluations(model, "^(?!Basic SQL|outerjoin|join).*$");
        studentService.saveSelfEvaluation(courseInstanceUUID, matriculationNumber, selfEvaluations);

        // Create task and exercise sheet
        // Task
        NewTaskAssignmentDTO newTaskAssignmentDTO = new TaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator(OWNER);
        newTaskAssignmentDTO.setHeader("Join assignment");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.UploadTask.getURI());
        newTaskAssignmentDTO.setLearningGoalIds(Collections.singletonList(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join")));

        assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        // Create corresponding exercise sheet.
        NewExerciseSheetDTO newExerciseSheetDTO = new ExerciseSheetDTO();
        newExerciseSheetDTO.setName("Join exercise sheet");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        newExerciseSheetDTO.setLearningGoals(Collections.singletonList(new LearningGoalAssignmentDTO(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join"), 1)));
        newExerciseSheetDTO.setTaskCount(1);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        studentService.openExerciseSheetForStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID);

        final int fileId = 3;

        // Submit task
        studentService.setFileForUploadTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1, fileId);
        studentService.removeFileFromUploadTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1, fileId);

        var optionalFileId = studentService.getFileIdOfIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1);

        assertThat(optionalFileId).isEmpty();
    }

    /**
     * Tests the setting and retrieval of a file id from a non upload task.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testSetAndGetFileIdOfNonUploadTask() throws Exception {
        var values = initTestGoalAndCourse();

        Model model = values.getValue0();
        CourseDTO course = values.getValue1();
        String courseInstanceId = values.getValue2();
        String courseInstanceUUID = values.getValue3();

        String matriculationNumber = importStudentsAndGetFirstStudentsMatriculationNumber();

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(matriculationNumber), courseInstanceId);

        // Self evaluation:
        List<StudentSelfEvaluationLearningGoalDTO> selfEvaluations = getStudentSelfEvaluations(model, "^(?!Basic SQL|outerjoin|join).*$");
        studentService.saveSelfEvaluation(courseInstanceUUID, matriculationNumber, selfEvaluations);

        // Create task and exercise sheet
        // Task
        NewTaskAssignmentDTO newTaskAssignmentDTO = new TaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator(OWNER);
        newTaskAssignmentDTO.setHeader("Join assignment");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.NoType.getURI());
        newTaskAssignmentDTO.setLearningGoalIds(Collections.singletonList(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join")));

        assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        // Create corresponding exercise sheet.
        NewExerciseSheetDTO newExerciseSheetDTO = new ExerciseSheetDTO();
        newExerciseSheetDTO.setName("Join exercise sheet");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        newExerciseSheetDTO.setLearningGoals(Collections.singletonList(new LearningGoalAssignmentDTO(new LearningGoalDisplayDTO("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Join", "Join"), 1)));
        newExerciseSheetDTO.setTaskCount(1);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        studentService.openExerciseSheetForStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID);

        assertThatThrownBy(() -> studentService.setFileForUploadTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1, 5))
            .isInstanceOf(NoUploadFileTypeException.class);

        var optionalFileId = studentService.getFileIdOfIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1);

        assertThat(optionalFileId).isEmpty();
    }

    //region Private helper methods

    /**
     * Imports the mock students and returns the matriculation number from the first student.
     *
     * @return the matriculation number from the first student
     * @throws Exception must not be thrown
     */
    private String importStudentsAndGetFirstStudentsMatriculationNumber() throws Exception {
        MultipartFile file = new MockMultipartFile(
            "file.csv",
            "file.csv",
            "text/csv",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("test_students.csv"))
        );

        var importedStudents = studentService.importStudentsFromFile(file);
        return importedStudents.get(0).getMatriculationNumber();
    }

    /**
     * Returns the self evaluations, based on the learning goals from the model.
     * The status (completed / not completed) is inferred from the given regex
     * pattern which is matched with the goal's name.
     *
     * @param model           the model
     * @param completePattern the completion pattern
     * @return list of evaluations
     */
    private List<StudentSelfEvaluationLearningGoalDTO> getStudentSelfEvaluations(Model model, String completePattern) {
        List<StudentSelfEvaluationLearningGoalDTO> selfEvaluations = new ArrayList<>();
        ResIterator resIterator = model.listSubjectsWithProperty(RDF.type, ETutorVocabulary.Goal);

        Pattern pattern = Pattern.compile(StringUtils.isBlank(completePattern) ? ".*" : completePattern, Pattern.CASE_INSENSITIVE);

        while (resIterator.hasNext()) {
            Resource resource = resIterator.nextResource();
            String goalId = resource.getURI();
            String goalName = resource.getProperty(RDFS.label).getString();

            var selfEvaluation = new StudentSelfEvaluationLearningGoalDTO();
            selfEvaluation.setId(goalId);
            selfEvaluation.setText(goalName);
            selfEvaluation.setCompleted(pattern.matcher(goalName).matches());

            selfEvaluations.add(selfEvaluation);
        }

        return selfEvaluations;
    }

    /**
     * Inserts the basic SQL hierarchy and creates a course and a course instance.
     *
     * @return {@link Quartet} containing the RDF model (learning goals), the course dto,
     * the course instance url (id) and the course instance uuid
     * @throws Exception must not be thrown
     */
    private Quartet<Model, CourseDTO, String, String> initTestGoalAndCourse() throws Exception {
        rdfConnectionFactory.clearDataset();
        sparqlEndpointService.insertScheme();

        //noinspection ConstantConditions
        Model model = RDFTestUtil.uploadLearningGoalHierarchy(rdfConnectionFactory, getClass().getResource("goal_hierarchy.ttl"));

        // Insert course
        CourseDTO dmCourse = new CourseDTO();
        dmCourse.setName("Datenmodellierung");
        dmCourse.setCourseType("Modul");
        dmCourse = sparqlEndpointService.insertNewCourse(dmCourse, OWNER);

        // Set learning goal assignment
        var goalAssignment = new LearningGoalUpdateAssignmentDTO();
        goalAssignment.setCourseId(dmCourse.getId());
        goalAssignment.setLearningGoalIds(Collections.singletonList("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Basic_SQL"));

        sparqlEndpointService.setGoalAssignment(goalAssignment);

        // Create course instance
        NewCourseInstanceDTO dmCourseInstance = new NewCourseInstanceDTO();
        dmCourseInstance.setCourseId(dmCourse.getId());
        dmCourseInstance.setYear(2021);
        dmCourseInstance.setTermId(ETutorVocabulary.Winter.getURI());

        String dmCourseInstanceUrl = courseInstanceSPARQLEndpointService.createNewCourseInstance(dmCourseInstance);
        String dmCourseInstanceUUID = dmCourseInstanceUrl.substring(dmCourseInstanceUrl.lastIndexOf('#') + 1);

        return Quartet.with(model, dmCourse, dmCourseInstanceUrl, dmCourseInstanceUUID);
    }
    //endregion
}
