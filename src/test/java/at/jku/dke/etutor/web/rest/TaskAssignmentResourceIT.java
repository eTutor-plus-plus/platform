package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.LearningGoalAlreadyExistsException;
import at.jku.dke.etutor.service.SPARQLEndpointService;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Integration tests for the {@link TaskAssignmentResource} REST controller.
 *
 * @author fne
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = {AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.ADMIN}, username = "admin")
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskAssignmentResourceIT {

    private static final String USERNAME = "admin";

    @Autowired
    private RDFConnectionFactory rdfConnectionFactory;

    @Autowired
    private SPARQLEndpointService sparqlEndpointService;

    @Autowired
    private AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;

    @Autowired
    private MockMvc restTaskAssignmentMockMvc;

    /**
     * Init method which is called before the test run
     *
     * @throws LearningGoalAlreadyExistsException must not happen
     */
    @BeforeAll
    public void initBeforeAllTests() throws LearningGoalAlreadyExistsException {
        rdfConnectionFactory.clearDataset();

        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("TestGoal1");
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, USERNAME);

        newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("TestGoal2");
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, USERNAME);
    }

    /**
     * Tests the creation of a new task assignment
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(1)
    public void testAddNewTaskAssignment() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME);
        var firstGoal = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testheader");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Easy.getURI());
        newTaskAssignmentDTO.setLearningGoalId(firstGoal.getId());

        var result = restTaskAssignmentMockMvc.perform(post("/api/tasks/assignments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(newTaskAssignmentDTO)))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        TaskAssignmentDTO taskAssignmentDTO = TestUtil.convertFromJSONString(jsonData, TaskAssignmentDTO.class);

        assertThat(taskAssignmentDTO.getCreator()).isEqualTo(newTaskAssignmentDTO.getCreator());
        assertThat(taskAssignmentDTO.getHeader()).isEqualTo(newTaskAssignmentDTO.getHeader());
        assertThat(taskAssignmentDTO.getOrganisationUnit()).isEqualTo(newTaskAssignmentDTO.getOrganisationUnit());
        assertThat(taskAssignmentDTO.getTaskDifficultyId()).isEqualTo(newTaskAssignmentDTO.getTaskDifficultyId());
        assertThat(taskAssignmentDTO.getLearningGoalId()).isEqualTo(newTaskAssignmentDTO.getLearningGoalId());
    }

    /**
     * Tests the retrieval of task assignments
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(2)
    public void testGetTaskAssignmentsOfGoal() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME);
        var firstGoal = goals.first();

        var result = restTaskAssignmentMockMvc.perform(get(String.format("/api/tasks/assignments/%s/goal/%s", firstGoal.getOwner(), firstGoal.getName())))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        SortedSet<TaskAssignmentDTO> assignments = TestUtil.convertCollectionFromJSONString(jsonData, TaskAssignmentDTO.class, SortedSet.class);

        assertThat(assignments).isNotEmpty();
    }

    /**
     * Tests the retrieval of task assignments from a goal which does not
     * contain any task assignments.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(3)
    public void testGetTaskAssignmentsOfEmptyGoal() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME);
        var secondGoal = goals.last();

        var result = restTaskAssignmentMockMvc.perform(get(String.format("/api/tasks/assignments/%s/goal/%s", secondGoal.getOwner(), secondGoal.getName())))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        SortedSet<TaskAssignmentDTO> assignments = TestUtil.convertCollectionFromJSONString(jsonData, TaskAssignmentDTO.class, SortedSet.class);

        assertThat(assignments).isEmpty();
    }

    /**
     * Tests the removal of a task assignment.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(4)
    public void testRemoveTaskAssignment() throws  Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME);
        var firstGoal = goals.first();
        var assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(firstGoal.getName(), firstGoal.getOwner());
        assertThat(assignments).isNotEmpty();
        var assignment = assignments.first();

        String id = assignment.getId().substring(assignment.getId().lastIndexOf('#') + 1);

        restTaskAssignmentMockMvc.perform(delete(String.format("/api/tasks/assignments/%s", id)))
            .andExpect(status().isNoContent());

        assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(firstGoal.getName(), firstGoal.getOwner());
        assertThat(assignments).isEmpty();
    }
}
