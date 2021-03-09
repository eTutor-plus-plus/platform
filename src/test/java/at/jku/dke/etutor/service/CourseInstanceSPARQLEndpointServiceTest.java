package at.jku.dke.etutor.service;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.User;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.LocalRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.dto.*;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import liquibase.integration.spring.SpringLiquibase;
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
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Max");
        userDTO.setLastName("Mustermann");
        userDTO.setLogin("k11805540");
        userDTO.setEmail("k11805540@students.jku.at");
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.STUDENT));
        student1 = userService.createUser(userDTO);

        userDTO = new UserDTO();
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

        ParameterizedSparqlString graphQry = new ParameterizedSparqlString("""
            ASK {
                GRAPH ?graph {
                }
            }
            """);
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

        assertThatThrownBy(() -> courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.emptyList(), null))
            .isInstanceOf(NullPointerException.class);
    }
}
