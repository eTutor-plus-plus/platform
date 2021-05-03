package at.jku.dke.etutor.service;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.CourseDTO;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import at.jku.dke.etutor.service.exception.StudentCSVImportException;
import liquibase.integration.spring.SpringLiquibase;
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

    @Autowired
    private RDFConnectionFactory rdfConnectionFactory;

    @Autowired
    private SPARQLEndpointService sparqlEndpointService;

    @Autowired
    private CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService;

    @Autowired
    private ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService;

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

        studentService.openExerciseSheetForStudent(mNr, courseInstanceUUID, exerciseSheetUUID);

        assertThat(studentService.hasStudentOpenedTheExerciseSheet(mNr, courseInstanceUUID, exerciseSheetUUID)).isTrue();
    }
}
