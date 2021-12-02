package at.jku.dke.etutor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.User;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.LocalRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.dto.*;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDisplayDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import at.jku.dke.etutor.service.exception.CourseInstanceNotFoundException;
import java.util.*;

import at.jku.dke.etutor.service.exception.CourseNotFoundException;
import liquibase.integration.spring.SpringLiquibase;
import one.util.streamex.StreamEx;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdfconnection.RDFConnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests for the {@code CourseInstanceSPARQLEndpointService} class.
 *
 * @author fne
 */
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CourseInstanceSPARQLEndpointServiceTest {

    private static final String OWNER = "admin";

    private CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService;
    private ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService;
    private SPARQLEndpointService sparqlEndpointService;
    private RDFConnectionFactory rdfConnectionFactory;

    @Autowired
    private UserService userService;

    @Autowired
    private SpringLiquibase springLiquibase;

    private LearningGoalDTO goal1;
    private LearningGoalDTO goal2;
    private CourseDTO course;
    private User student1;
    private User student2;

    /**
     * Initializes the test class.
     *
     * @throws Exception must not be thrown
     */
    @BeforeAll
    public void init() throws Exception {
        springLiquibase.setDropFirst(true);
        springLiquibase.afterPropertiesSet();

        // Create students
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setFirstName("Max");
        userDTO.setLastName("Mustermann");
        userDTO.setLogin("k11805540");
        userDTO.setEmail("k11805540@students.jku.at");
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.STUDENT));
        student1 = userService.createUser(userDTO);

        userDTO = new AdminUserDTO();
        userDTO.setFirstName("Lisa");
        userDTO.setLastName("Musterfrau");
        userDTO.setLogin("k11805541");
        userDTO.setEmail("k11805541@students.jku.at");
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.STUDENT));
        student2 = userService.createUser(userDTO);
    }

    /**
     * Method which initializes the dataset and endpoint service before each run.
     *
     * @throws Exception must not be thrown
     */
    @BeforeEach
    public void setup() throws Exception {
        Dataset dataset = DatasetFactory.createTxnMem();
        rdfConnectionFactory = new LocalRDFConnectionFactory(dataset);
        sparqlEndpointService = new SPARQLEndpointService(rdfConnectionFactory);
        courseInstanceSPARQLEndpointService = new CourseInstanceSPARQLEndpointService(rdfConnectionFactory, userService);
        exerciseSheetSPARQLEndpointService = new ExerciseSheetSPARQLEndpointService(rdfConnectionFactory);

        sparqlEndpointService.insertScheme();

        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("TestGoal1");
        goal1 = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, OWNER);

        newLearningGoalDTO.setName("TestGoal2");
        goal2 = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, OWNER);

        course = new CourseDTO();
        course.setCourseType("LVA");
        course.setName("Testkurs");
        course = sparqlEndpointService.insertNewCourse(course, OWNER);

        LearningGoalUpdateAssignmentDTO learningGoalUpdateAssignmentDTO = new LearningGoalUpdateAssignmentDTO();
        learningGoalUpdateAssignmentDTO.setCourseId(course.getId());
        learningGoalUpdateAssignmentDTO.setLearningGoalIds(Arrays.asList(goal1.getId(), goal2.getId()));

        sparqlEndpointService.setGoalAssignment(learningGoalUpdateAssignmentDTO);
    }

    /**
     * Tests the successful insertion of a new course instance.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testInsertNewCourseInstance() throws Exception {
        NewCourseInstanceDTO newCourseInstanceDTO = new NewCourseInstanceDTO();
        newCourseInstanceDTO.setCourseId(course.getId());
        newCourseInstanceDTO.setTermId(ETutorVocabulary.Summer.getURI());
        newCourseInstanceDTO.setYear(2021);

        String uri = courseInstanceSPARQLEndpointService.createNewCourseInstance(newCourseInstanceDTO);

        assertThat(uri).isNotBlank();

        ParameterizedSparqlString graphQry = new ParameterizedSparqlString(
            """
            ASK {
                GRAPH ?graph {
                }
            }
            """
        );
        graphQry.setIri("?graph", uri);
        try (RDFConnection connection = rdfConnectionFactory.getRDFConnection()) {
            assertThat(connection.queryAsk(graphQry.asQuery())).isTrue();
        }
    }

    /**
     * Tests the creation of a new course instance with null values.
     */
    @Test
    public void testInsertNewCourseInstanceNullValues() {
        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.createNewCourseInstance(null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the set student of course instance method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testSetStudentsOfCourseInstance() throws Exception {
        NewCourseInstanceDTO newCourseInstanceDTO = new NewCourseInstanceDTO();
        newCourseInstanceDTO.setCourseId(course.getId());
        newCourseInstanceDTO.setTermId(ETutorVocabulary.Summer.getURI());
        newCourseInstanceDTO.setYear(2021);

        String uri = courseInstanceSPARQLEndpointService.createNewCourseInstance(newCourseInstanceDTO);
        String uuid = uri.substring(uri.lastIndexOf('#') + 1);

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Arrays.asList(student1.getLogin(), student2.getLogin()), uri);

        var optionalCourseInstance = courseInstanceSPARQLEndpointService.getCourseInstance(uuid);
        assertThat(optionalCourseInstance).isPresent();
        assertThat(optionalCourseInstance.get().getStudents()).hasSize(2);
        assertThat(optionalCourseInstance.get().getStudents().stream().map(StudentInfoDTO::getMatriculationNumber).toArray())
            .containsExactlyInAnyOrder(student1.getLogin(), student2.getLogin());

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.emptyList(), uri);
        optionalCourseInstance = courseInstanceSPARQLEndpointService.getCourseInstance(uuid);
        assertThat(optionalCourseInstance).isPresent();
        assertThat(optionalCourseInstance.get().getStudents()).isEmpty();
    }

    /**
     * Tests the set students of course instance method with null values.
     */
    @Test
    public void testSetStudentsOfCourseInstanceNullValues() {
        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(null, null))
            .isInstanceOf(NullPointerException.class);

        List<String> emptyList = Collections.emptyList();
        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(emptyList, null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the get displayable course instances of course method with null values.
     */
    @Test
    public void testGetDisplayableCourseInstancesOfCourseNullValues() {
        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.getDisplayableCourseInstancesOfCourse(null, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.getDisplayableCourseInstancesOfCourse("Test", null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the get displayable course instances of course method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetDisplayableCourseInstancesOfCourse() throws Exception {
        for (int i = 0; i < 6; i++) {
            NewCourseInstanceDTO newCourseInstanceDTO = new NewCourseInstanceDTO();
            newCourseInstanceDTO.setCourseId(course.getId());
            newCourseInstanceDTO.setTermId(ETutorVocabulary.Summer.getURI());
            newCourseInstanceDTO.setYear(2021 + i);
            courseInstanceSPARQLEndpointService.createNewCourseInstance(newCourseInstanceDTO);
        }

        PageRequest pageRequest = PageRequest.of(0, 5);

        var result = courseInstanceSPARQLEndpointService.getDisplayableCourseInstancesOfCourse(course.getName(), pageRequest);

        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(5);
    }

    /**
     * Tests the add exercise sheet course instance assignments method with null values.
     */
    @Test
    public void testAddExerciseSheetCourseInstanceAssignmentsNullValues() {
        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.addExerciseSheetCourseInstanceAssignments(null, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.addExerciseSheetCourseInstanceAssignments("test", null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the add exercise sheet course instance assignments method with an invalid course instance.
     */
    @Test
    public void testAddExerciseSheetCourseInstanceAssignmentsWithInvalidCourseInstance() {
        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.addExerciseSheetCourseInstanceAssignments("test", new ArrayList<>()))
            .isInstanceOf(CourseInstanceNotFoundException.class);
    }

    /**
     * Tests the add exercise sheet course instance assignments and get exercise
     * sheets of course instance methods.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testAddExerciseSheetCourseInstanceAssignmentsAndGetExerciseSheetsOf() throws Exception {
        NewCourseInstanceDTO newCourseInstanceDTO = new NewCourseInstanceDTO();
        newCourseInstanceDTO.setCourseId(course.getId());
        newCourseInstanceDTO.setTermId(ETutorVocabulary.Summer.getURI());
        newCourseInstanceDTO.setYear(2021);
        String uri = courseInstanceSPARQLEndpointService.createNewCourseInstance(newCourseInstanceDTO);
        String instanceUUID = uri.substring(uri.lastIndexOf('#') + 1);

        NewExerciseSheetDTO firstNewExerciseSheet = new NewExerciseSheetDTO();
        firstNewExerciseSheet.setName("TestSheet 1");
        firstNewExerciseSheet.setDifficultyId(ETutorVocabulary.Medium.getURI());
        firstNewExerciseSheet.setLearningGoals(new ArrayList<>());

        var firstExerciseSheet = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(firstNewExerciseSheet, "admin");

        NewExerciseSheetDTO secondNewExerciseSheet = new NewExerciseSheetDTO();
        secondNewExerciseSheet.setName("TestSheet 2");
        secondNewExerciseSheet.setDifficultyId(ETutorVocabulary.Medium.getURI());
        secondNewExerciseSheet.setLearningGoals(new ArrayList<>());

        var secondExerciseSheet = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(secondNewExerciseSheet, "admin");

        courseInstanceSPARQLEndpointService.addExerciseSheetCourseInstanceAssignments(
            instanceUUID,
            Arrays.asList(firstExerciseSheet.getId(), secondExerciseSheet.getId())
        );

        var exerciseSheets = courseInstanceSPARQLEndpointService.getExerciseSheetsOfCourseInstance(instanceUUID);

        assertThat(exerciseSheets).hasSize(2);
        assertThat(StreamEx.of(exerciseSheets).map(ExerciseSheetDisplayDTO::getInternalId).toList())
            .containsExactlyInAnyOrder(firstExerciseSheet.getId(), secondExerciseSheet.getId());
    }

    /**
     * Tests the get exercise sheets of course instance method with a null value.
     */
    @Test
    public void testGetExerciseSheetsOfCourseInstanceNullValue() {
        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.getExerciseSheetsOfCourseInstance(null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the get exercise sheets of course instance method with an invalid
     * course instance uuid.
     */
    @Test
    public void testGetExerciseSheetsOfCourseInstanceWithInvalidCourseInstanceUUID() {
        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.getExerciseSheetsOfCourseInstance("test"))
            .isInstanceOf(CourseInstanceNotFoundException.class);
    }

    /**
     * Tests the remove course instance method with a null value.
     */
    @Test
    public void testRemoveCourseInstanceNullValue() {
        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.removeCourseInstance(null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the removal of a nonexistent course instance.
     */
    @Test
    public void testRemoveNonexistentCourseInstance() {
        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.removeCourseInstance("123"))
            .isInstanceOf(CourseInstanceNotFoundException.class);
    }

    /**
     * Tests the removal of a course instance.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testRemoveCourseInstance() throws Exception {
        NewCourseInstanceDTO newCourseInstanceDTO = new NewCourseInstanceDTO();
        newCourseInstanceDTO.setCourseId(course.getId());
        newCourseInstanceDTO.setTermId(ETutorVocabulary.Summer.getURI());
        newCourseInstanceDTO.setYear(2021);

        String uri = courseInstanceSPARQLEndpointService.createNewCourseInstance(newCourseInstanceDTO);

        String uuid = uri.substring(uri.lastIndexOf('#') + 1);
        courseInstanceSPARQLEndpointService.removeCourseInstance(uuid);

        var optional = courseInstanceSPARQLEndpointService.getCourseInstance(uuid);

        assertThat(optional).isEmpty();
    }

    /**
     * Tests getting all course instances of a course
     * @throws CourseNotFoundException
     */
    @Test
    public void testGetInstancesOfCourse() throws CourseNotFoundException {
        NewCourseInstanceDTO newCourseInstanceDTO = new NewCourseInstanceDTO();
        newCourseInstanceDTO.setCourseId(course.getId());
        newCourseInstanceDTO.setTermId(ETutorVocabulary.Summer.getURI());
        newCourseInstanceDTO.setYear(2021);

        var instances = courseInstanceSPARQLEndpointService.getInstancesOfCourse(course.getName());
        assertThat(instances.isEmpty());

        courseInstanceSPARQLEndpointService.createNewCourseInstance(newCourseInstanceDTO);

        NewCourseInstanceDTO newCourseInstanceDTO2 = new NewCourseInstanceDTO();
        newCourseInstanceDTO2.setCourseId(course.getId());
        newCourseInstanceDTO2.setTermId(ETutorVocabulary.Winter.getURI());
        newCourseInstanceDTO2.setYear(2021);

        courseInstanceSPARQLEndpointService.createNewCourseInstance(newCourseInstanceDTO2);

        instances = courseInstanceSPARQLEndpointService.getInstancesOfCourse(course.getName());
        assertThat(instances.size() == 2);
    }

    @Test
    public void testSetAndGetStudentsOfCourseInstance() throws CourseInstanceNotFoundException, CourseNotFoundException {
        NewCourseInstanceDTO newCourseInstanceDTO = new NewCourseInstanceDTO();
        newCourseInstanceDTO.setCourseId(course.getId());
        newCourseInstanceDTO.setTermId(ETutorVocabulary.Summer.getURI());
        newCourseInstanceDTO.setYear(2021);
        String uri = courseInstanceSPARQLEndpointService.createNewCourseInstance(newCourseInstanceDTO);
        String uuid = uri.substring(uri.lastIndexOf('#') + 1);

        assertThat(courseInstanceSPARQLEndpointService.getStudentsOfCourseInstance(uuid).isEmpty());

        String mat1 = "12345";
        String mat2 = "11234";
        var list = new ArrayList<String>();
        list.add(mat1);
        list.add(mat2);
        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(list, uri);

        assertThat(courseInstanceSPARQLEndpointService.getStudentsOfCourseInstance(uuid).size() == 2);
    }
}
