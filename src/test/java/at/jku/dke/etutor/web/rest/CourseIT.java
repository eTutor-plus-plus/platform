package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.RDFTestUtil;
import at.jku.dke.etutor.service.SPARQLEndpointService;
import at.jku.dke.etutor.service.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CourseResource} REST controller.
 *
 * @author fne
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = {AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.ADMIN}, username = "admin")
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CourseIT {

    @Autowired
    private RDFConnectionFactory rdfConnectionFactory;

    @Autowired
    private MockMvc restCourseMockMvc;

    @Autowired
    private SPARQLEndpointService sparqlEndpointService;

    /**
     * Init method which is called before the test run.
     */
    @BeforeAll
    public void initBeforeAllTests() {
        rdfConnectionFactory.clearDataset();
    }

    /**
     * Tests the successful creation of a course.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(1)
    public void testCreateCourseSuccess() throws Exception {
        CourseDTO course = new CourseDTO();
        course.setName("Testcourse");
        course.setCourseType("LVA");

        restCourseMockMvc
            .perform(post("/api/course").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(course)))
            .andExpect(status().isCreated());

        RDFTestUtil.checkThatSubjectExists("<http://www.dke.uni-linz.ac.at/etutorpp/Course#Testcourse>", rdfConnectionFactory);
    }

    /**
     * Tests the creation of a duplicate course.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(2)
    public void testCreateDuplicateCourse() throws Exception {
        CourseDTO course = new CourseDTO();
        course.setName("Testcourse");
        course.setCourseType("LVA");

        restCourseMockMvc
            .perform(post("/api/course").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(course)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.courseAlreadyExists"));
    }

    /**
     * Tests the retrieval of all courses
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(3)
    public void testGetCourseList() throws Exception {
        var mvcResult = restCourseMockMvc.perform(get("/api/course")).andReturn();

        String jsonData = mvcResult.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        SortedSet<CourseDTO> courses = TestUtil.convertCollectionFromJSONString(jsonData, CourseDTO.class, TreeSet.class);
        assertThat(courses.size()).isEqualTo(1);
        CourseDTO course = courses.first();

        assertThat(course.getName()).isEqualTo("Testcourse");
        assertThat(course.getCourseType()).isEqualTo("LVA");
        assertThat(course.getCreator()).isEqualTo("admin");
    }

    /**
     * Tests the retrieval of a course which does not exist.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(4)
    public void testGetCourseNotFound() throws Exception {
        restCourseMockMvc.perform(get("/api/course/Test")).andExpect(status().isNotFound());
    }

    /**
     * Tests the retrieval of a course which does exist.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(5)
    public void testGetCourseSuccess() throws Exception {
        var mvcResult = restCourseMockMvc.perform(get("/api/course/Testcourse")).andReturn();

        String jsonData = mvcResult.getResponse().getContentAsString();
        CourseDTO course = TestUtil.convertFromJSONString(jsonData, CourseDTO.class);

        assertThat(course.getName()).isEqualTo("Testcourse");
        assertThat(course.getCreator()).isEqualTo("admin");
        assertThat(course.getLink()).isNull();
        assertThat(course.getId()).isEqualTo("http://www.dke.uni-linz.ac.at/etutorpp/Course#Testcourse");
    }

    /**
     * Tests the removal of a nonexistent course.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(6)
    public void testRemoveNonexistentCourse() throws Exception {
        restCourseMockMvc
            .perform(delete("/api/course/Test123"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorKey").value("courseNotFound"));
    }

    /**
     * Tests the successful removal of an existing course.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(7)
    public void testRemoveCourseSuccess() throws Exception {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("Testcourse1");
        courseDTO.setCourseType("LVA");

        sparqlEndpointService.insertNewCourse(courseDTO, "admin");

        restCourseMockMvc.perform(delete("/api/course/Testcourse1")).andExpect(status().isNoContent());
    }

    /**
     * Tests the update course rest endpoint with a nonexistent course.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(8)
    public void testUpdateNonexistentCourse() throws Exception {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId("http://www.test.at");
        courseDTO.setName("Testname");
        courseDTO.setCourseType("Modul");

        restCourseMockMvc
            .perform(put("/api/course").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(courseDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorKey").value("courseNotFound"));
    }

    /**
     * Tests the update course endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(9)
    public void testUpdateCourseSuccess() throws Exception {
        CourseDTO oldCourse = sparqlEndpointService.getCourse("Testcourse").orElseThrow();
        oldCourse.setDescription("Testdescription");
        oldCourse.setLink(new URL("http://www.dke.uni-linz.ac.at"));
        oldCourse.setCourseType("Modul");

        restCourseMockMvc
            .perform(put("/api/course").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(oldCourse)))
            .andExpect(status().isNoContent());

        CourseDTO newCourse = sparqlEndpointService.getCourse("Testcourse").orElseThrow();

        assertThat(newCourse).usingRecursiveComparison().isEqualTo(oldCourse);
    }

    /**
     * Tests the creation of a goal assignment.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(10)
    public void testAddGoalAssignment() throws Exception {
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        var goal = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, "admin");
        var course = sparqlEndpointService.getCourse("Testcourse").orElseThrow();

        var assignment = new LearningGoalAssignmentDTO();
        assignment.setLearningGoalId(goal.getId());
        assignment.setCourseId(course.getId());

        restCourseMockMvc
            .perform(
                post("/api/course/goal").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(assignment))
            )
            .andExpect(status().isNoContent());

        var associatedGoals = sparqlEndpointService.getLearningGoalsForCourse(course.getName());
        assertThat(associatedGoals).isNotEmpty();
        assertThat(associatedGoals.size()).isEqualTo(1);
    }

    /**
     * Tests the creation of an already existing goal assignment.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(11)
    public void testAddAlreadyExistentGoalAssignment() throws Exception {
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        var course = sparqlEndpointService.getCourse("Testcourse").orElseThrow();
        var goal = sparqlEndpointService.getLearningGoalsForCourse(course.getName()).first();

        var assignment = new LearningGoalAssignmentDTO();
        assignment.setLearningGoalId(goal.getId());
        assignment.setCourseId(course.getId());

        restCourseMockMvc
            .perform(
                post("/api/course/goal").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(assignment))
            )
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the getting of learning goals from a specific course.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(12)
    public void testGetLearningGoalsFromCourse() throws Exception {
        var result = restCourseMockMvc.perform(get("/api/course/Testcourse/goals")).andReturn();

        String jsonData = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        SortedSet<DisplayLearningGoalAssignmentDTO> goals = TestUtil.convertCollectionFromJSONString(
            jsonData,
            DisplayLearningGoalAssignmentDTO.class,
            TreeSet.class
        );

        assertThat(goals).isNotEmpty();
        assertThat(goals.size()).isEqualTo(1);
    }

    /**
     * Tests the getting of learning goals from a nonexistent course.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(13)
    public void testGetLearningGoalFromNonExistentCourse() throws Exception {
        restCourseMockMvc.perform(get("/api/course/Testcourse56789/goals")).andExpect(status().isBadRequest());
    }

    /**
     * Tests the removal of a nonexistent learning goal assignment.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(14)
    public void testRemoveGoalAssignmentOfANonExistentAssignment() throws Exception {
        LearningGoalAssignmentDTO learningGoalAssignmentDTO = new LearningGoalAssignmentDTO();
        learningGoalAssignmentDTO.setCourseId("http://www.test.at/nonexistent");
        learningGoalAssignmentDTO.setLearningGoalId("http://www.test123.at/nonexistent");

        restCourseMockMvc
            .perform(
                delete("/api/course/goal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(learningGoalAssignmentDTO))
            )
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the set assignment endpoint.
     *
     * @throws Exception mut not be thrown
     */
    @Test
    @Order(15)
    public void testSetGoalAssignment() throws Exception {
        NewLearningGoalDTO learningGoalDTO = new NewLearningGoalDTO();
        learningGoalDTO.setName("AssignmentTestGoal");
        var secondGoal = sparqlEndpointService.insertNewLearningGoal(learningGoalDTO, "admin");

        var course = sparqlEndpointService.getCourse("Testcourse").orElseThrow();
        var goal = sparqlEndpointService.getLearningGoalsForCourse(course.getName()).first();

        LearningGoalUpdateAssignmentDTO learningGoalUpdateAssignmentDTO = new LearningGoalUpdateAssignmentDTO();
        learningGoalUpdateAssignmentDTO.setCourseId(course.getId());
        learningGoalUpdateAssignmentDTO.getLearningGoalIds().add(goal.getId());
        learningGoalUpdateAssignmentDTO.getLearningGoalIds().add(secondGoal.getId());

        restCourseMockMvc
            .perform(
                put("/api/course/goal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(learningGoalUpdateAssignmentDTO))
            )
            .andExpect(status().isNoContent());

        var goals = sparqlEndpointService.getLearningGoalsForCourse(course.getName());
        assertThat(goals).hasSize(2);

        learningGoalUpdateAssignmentDTO.getLearningGoalIds().clear();
        learningGoalUpdateAssignmentDTO.getLearningGoalIds().add(goal.getId());

        restCourseMockMvc
            .perform(
                put("/api/course/goal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(learningGoalUpdateAssignmentDTO))
            )
            .andExpect(status().isNoContent());

        goals = sparqlEndpointService.getLearningGoalsForCourse(course.getName());
        assertThat(goals).hasSize(1);
    }

    /**
     * Tests the removal of an existing goal assignment
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(16)
    public void testRemoveGoalAssignment() throws Exception {
        var course = sparqlEndpointService.getCourse("Testcourse").orElseThrow();
        var goal = sparqlEndpointService.getLearningGoalsForCourse(course.getName()).first();

        var assignment = new LearningGoalAssignmentDTO();
        assignment.setLearningGoalId(goal.getId());
        assignment.setCourseId(course.getId());

        restCourseMockMvc
            .perform(
                delete("/api/course/goal").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(assignment))
            )
            .andExpect(status().isNoContent());

        var associatedGoals = sparqlEndpointService.getLearningGoalsForCourse(course.getName());
        assertThat(associatedGoals).isEmpty();
    }
}
