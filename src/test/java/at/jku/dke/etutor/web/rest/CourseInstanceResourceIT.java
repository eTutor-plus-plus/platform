package at.jku.dke.etutor.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.User;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.CourseInstanceSPARQLEndpointService;
import at.jku.dke.etutor.service.SPARQLEndpointService;
import at.jku.dke.etutor.service.UserService;
import at.jku.dke.etutor.service.dto.*;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import at.jku.dke.etutor.web.rest.vm.CourseInstanceStudentsVM;
import java.time.Year;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import liquibase.integration.spring.SpringLiquibase;
import one.util.streamex.StreamEx;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdfconnection.RDFConnection;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for the course instance resource endpoint.
 *
 * @author fne
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = { AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.ADMIN }, username = "admin")
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CourseInstanceResourceIT {

    private static final String OWNER = "admin";

    @Autowired
    private RDFConnectionFactory rdfConnectionFactory;

    @Autowired
    private SPARQLEndpointService sparqlEndpointService;

    @Autowired
    private CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService;

    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc restCourseInstanceMockMvc;

    @Autowired
    private SpringLiquibase springLiquibase;

    private LearningGoalDTO goal1;
    private LearningGoalDTO goal2;
    private CourseDTO course;
    private User student1;
    private User student2;

    /**
     * Init method which initializes the testing environment before all tests.
     *
     * @throws Exception must not be thrown
     */
    @BeforeAll
    public void initBeforeAllTests() throws Exception {
        springLiquibase.setDropFirst(true);
        springLiquibase.afterPropertiesSet();
        rdfConnectionFactory.clearDataset();

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
     * Tests the creation of a course instance.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(1)
    public void testCreateCourseInstance() throws Exception {
        NewCourseInstanceDTO newCourseInstanceDTO = new NewCourseInstanceDTO();
        newCourseInstanceDTO.setCourseId(course.getId());
        newCourseInstanceDTO.setTermId(ETutorVocabulary.Summer.getURI());
        newCourseInstanceDTO.setYear(Year.now().getValue());
        newCourseInstanceDTO.setDescription("Testdescription");

        var result = restCourseInstanceMockMvc
            .perform(
                post("/api/course-instance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(newCourseInstanceDTO))
            )
            .andExpect(status().isCreated())
            .andReturn();

        String newId = result.getResponse().getContentAsString();
        assertThat(newId).isNotBlank();
        String location = result.getResponse().getHeader("Location");

        result = restCourseInstanceMockMvc.perform(get(location)).andExpect(status().isOk()).andReturn();

        String jsonData = result.getResponse().getContentAsString();
        CourseInstanceDTO courseInstanceDTO = TestUtil.convertFromJSONString(jsonData, CourseInstanceDTO.class);
        assertThat(newId).isEqualTo(String.format("\"%s\"", courseInstanceDTO.getId()));
        assertThat(courseInstanceDTO.getCourseName()).isEqualTo(course.getName());
        assertThat(courseInstanceDTO.getTermId()).isEqualTo(newCourseInstanceDTO.getTermId());
        assertThat(courseInstanceDTO.getStudents()).isEmpty();

        ParameterizedSparqlString graphQry = new ParameterizedSparqlString(
            """
            ASK {
                GRAPH ?graph {
                }
            }
            """
        );
        graphQry.setIri("?graph", newId.substring(1, newId.length() - 1));
        try (RDFConnection connection = rdfConnectionFactory.getRDFConnection()) {
            assertThat(connection.queryAsk(graphQry.asQuery())).isTrue();
        }
    }

    /**
     * Tests the creation of a course instance from a nonexistent course.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(2)
    public void testCreateCourseInstanceOfNonexistentCourse() throws Exception {
        NewCourseInstanceDTO newCourseInstanceDTO = new NewCourseInstanceDTO();
        newCourseInstanceDTO.setCourseId("http://www.test.at");
        newCourseInstanceDTO.setTermId(ETutorVocabulary.Summer.getURI());
        newCourseInstanceDTO.setYear(Year.now().getValue());

        restCourseInstanceMockMvc
            .perform(
                post("/api/course-instance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(newCourseInstanceDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorKey").value("courseNotFound"))
            .andExpect(jsonPath("$.title").value("The course does not exist!"));
    }

    /**
     * Tests the get instances of course endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(3)
    public void testGetInstancesOfCourse() throws Exception {
        var result = restCourseInstanceMockMvc
            .perform(get("/api/course-instance/instances/of/{name}", course.getName()))
            .andExpect(status().isOk())
            .andReturn();
        String jsonData = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        List<CourseInstanceDTO> instances = TestUtil.convertCollectionFromJSONString(jsonData, CourseInstanceDTO.class, List.class);
        assertThat(instances).hasSize(1);
    }

    /**
     * Tests the setting of course students.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(4)
    @SuppressWarnings("unchecked")
    public void testSetStudentsOfCourseInstance() throws Exception {
        var result = restCourseInstanceMockMvc
            .perform(get("/api/course-instance/instances/of/{name}", course.getName()))
            .andExpect(status().isOk())
            .andReturn();
        String jsonData = result.getResponse().getContentAsString();
        List<CourseInstanceDTO> instances = TestUtil.convertCollectionFromJSONString(jsonData, CourseInstanceDTO.class, List.class);
        var instance = instances.get(0);

        List<String> students = Arrays.asList(student1.getLogin(), student2.getLogin());
        CourseInstanceStudentsVM courseInstanceStudentsVM = new CourseInstanceStudentsVM();
        courseInstanceStudentsVM.setCourseInstanceId(instance.getId());
        courseInstanceStudentsVM.setMatriculationNumbers(students);

        restCourseInstanceMockMvc
            .perform(
                put("/api/course-instance/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(courseInstanceStudentsVM))
            )
            .andExpect(status().isNoContent());

        result =
            restCourseInstanceMockMvc
                .perform(get("/api/course-instance/instances/of/{name}", course.getName()))
                .andExpect(status().isOk())
                .andReturn();
        jsonData = result.getResponse().getContentAsString();
        instances = TestUtil.convertCollectionFromJSONString(jsonData, CourseInstanceDTO.class, List.class);
        instance = instances.get(0);

        assertThat(instance.getStudents()).hasSize(2);
        assertThat(StreamEx.of(instance.getStudents()).map(StudentInfoDTO::getMatriculationNumber).toList())
            .containsExactlyInAnyOrder(student1.getLogin(), student2.getLogin());
    }

    /**
     * Tests the set students endpoint with an invalid course instance id.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(5)
    public void testSetStudentsOfNonexistentCourseInstance() throws Exception {
        List<String> students = Collections.singletonList(student1.getLogin());
        CourseInstanceStudentsVM courseInstanceStudentsVM = new CourseInstanceStudentsVM();
        courseInstanceStudentsVM.setCourseInstanceId("http://www.test.at");
        courseInstanceStudentsVM.setMatriculationNumbers(students);

        restCourseInstanceMockMvc
            .perform(
                put("/api/course-instance/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(courseInstanceStudentsVM))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorKey").value("courseInstanceNotFound"))
            .andExpect(jsonPath("$.title").value("The course instance can not be found!"));
    }

    /**
     * Tests the get instance of course endpoint with an invalid course.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(6)
    public void testGetInstancesOfInvalidCourse() throws Exception {
        restCourseInstanceMockMvc
            .perform(get("/api/course-instance/instances/of/Test"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorKey").value("courseNotFound"))
            .andExpect(jsonPath("$.title").value("The course does not exist!"));
    }

    /**
     * Tests the remove course instance endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(7)
    public void testRemoveCourseInstance() throws Exception {
        NewCourseInstanceDTO newCourseInstanceDTO = new NewCourseInstanceDTO();
        newCourseInstanceDTO.setCourseId(course.getId());
        newCourseInstanceDTO.setTermId(ETutorVocabulary.Summer.getURI());
        newCourseInstanceDTO.setYear(2021);

        String uri = courseInstanceSPARQLEndpointService.createNewCourseInstance(newCourseInstanceDTO);
        String uuid = uri.substring(uri.lastIndexOf('#') + 1);

        restCourseInstanceMockMvc.perform(delete("/api/course-instance/{uuid}", uuid)).andExpect(status().isNoContent());

        var optional = courseInstanceSPARQLEndpointService.getCourseInstance(uuid);

        assertThat(optional).isEmpty();
    }

    /**
     * Tests the removal of a nonexistent course instance.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(8)
    public void testRemoveNonexistentCourseInstance() throws Exception {
        restCourseInstanceMockMvc
            .perform(delete("/api/course-instance/123"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.courseInstanceNotFound"))
            .andExpect(jsonPath("$.title").value("The course instance can not be found!"));
    }
}
