package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.SPARQLEndpointService;
import at.jku.dke.etutor.service.dto.LearningGoalDTO;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import at.jku.dke.etutor.service.dto.TaskDisplayDTO;
import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDisplayDTO;
import at.jku.dke.etutor.service.exception.LearningGoalAlreadyExistsException;
import one.util.streamex.StreamEx;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME, false);
        var firstGoal = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testheader");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Easy.getURI());
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.NoType.getURI());
        newTaskAssignmentDTO.addLearningGoal(new LearningGoalDisplayDTO(firstGoal.getId(), firstGoal.getName()));

        var result = restTaskAssignmentMockMvc
            .perform(
                post("/api/tasks/assignments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(newTaskAssignmentDTO))
            )
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        TaskAssignmentDTO taskAssignmentDTO = TestUtil.convertFromJSONString(jsonData, TaskAssignmentDTO.class);

        assertThat(taskAssignmentDTO.getCreator()).isEqualTo(newTaskAssignmentDTO.getCreator());
        assertThat(taskAssignmentDTO.getHeader()).isEqualTo(newTaskAssignmentDTO.getHeader());
        assertThat(taskAssignmentDTO.getOrganisationUnit()).isEqualTo(newTaskAssignmentDTO.getOrganisationUnit());
        assertThat(taskAssignmentDTO.getTaskDifficultyId()).isEqualTo(newTaskAssignmentDTO.getTaskDifficultyId());
        assertThat(taskAssignmentDTO.getLearningGoalIds()).containsExactlyInAnyOrderElementsOf(newTaskAssignmentDTO.getLearningGoalIds());
    }

    /**
     * Tests the retrieval of task assignments
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(2)
    public void testGetTaskAssignmentsOfGoal() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME, false);
        var firstGoal = goals.first();

        var result = restTaskAssignmentMockMvc
            .perform(get(String.format("/api/tasks/assignments/%s/goal/%s", firstGoal.getOwner(), firstGoal.getName())))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        SortedSet<TaskAssignmentDTO> assignments = TestUtil.convertCollectionFromJSONString(
            jsonData,
            TaskAssignmentDTO.class,
            SortedSet.class
        );

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
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME, false);
        var secondGoal = goals.last();

        var result = restTaskAssignmentMockMvc
            .perform(get(String.format("/api/tasks/assignments/%s/goal/%s", secondGoal.getOwner(), secondGoal.getName())))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        SortedSet<TaskAssignmentDTO> assignments = TestUtil.convertCollectionFromJSONString(
            jsonData,
            TaskAssignmentDTO.class,
            SortedSet.class
        );

        assertThat(assignments).isEmpty();
    }

    /**
     * Tests the removal of a task assignment.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(4)
    public void testRemoveTaskAssignment() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME, false);
        var firstGoal = goals.first();
        var assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(firstGoal.getName(), firstGoal.getOwner());
        assertThat(assignments).isNotEmpty();
        var assignment = assignments.first();

        String id = assignment.getId().substring(assignment.getId().lastIndexOf('#') + 1);

        restTaskAssignmentMockMvc.perform(delete(String.format("/api/tasks/assignments/%s", id))).andExpect(status().isNoContent());

        assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(firstGoal.getName(), firstGoal.getOwner());
        assertThat(assignments).isEmpty();
    }

    /**
     * Tests the update method with a nonexistent task assignment.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(5)
    public void testUpdateNonexistentTaskAssignment() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME, false);
        var testGoal1 = goals.first();

        TaskAssignmentDTO taskAssignmentDTO = new TaskAssignmentDTO();
        taskAssignmentDTO.setId("http://www.test.at");
        taskAssignmentDTO.setCreationDate(Instant.now());
        taskAssignmentDTO.addLearningGoal(new LearningGoalDisplayDTO(testGoal1.getId(), testGoal1.getName()));
        taskAssignmentDTO.setCreator("Florian");
        taskAssignmentDTO.setHeader("Test assignment");
        taskAssignmentDTO.setProcessingTime("1h");
        taskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        taskAssignmentDTO.setOrganisationUnit("DKE");
        taskAssignmentDTO.setInternalCreator("admin");
        taskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.NoType.getURI());

        restTaskAssignmentMockMvc
            .perform(
                put("/api/tasks/assignments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(taskAssignmentDTO))
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.taskAssignmentNotFound"))
            .andExpect(jsonPath("$.title").value("The task assignment does not exist!"));
    }

    /**
     * Tests the update task assignment REST endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(6)
    public void testUpdateTaskAssignment() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME, false);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testassignment");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.addLearningGoal(new LearningGoalDisplayDTO(testGoal1.getId(), testGoal1.getName()));
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.NoType.getURI());

        var assignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, USERNAME);

        assignment.setProcessingTime("2h");
        assignment.setUrl(new URL("http://www.test.at"));
        assignment.setTaskDifficultyId(ETutorVocabulary.VeryHard.getURI());
        assignment.setOrganisationUnit("JKU SE");
        assignment.setTaskAssignmentTypeId(ETutorVocabulary.UploadTask.getURI());

        restTaskAssignmentMockMvc
            .perform(
                put("/api/tasks/assignments").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(assignment))
            )
            .andExpect(status().isNoContent());

        var assignmentsFromDB = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(testGoal1.getName(), testGoal1.getOwner());
        assertThat(assignmentsFromDB).isNotEmpty().hasSize(1);

        var assignmentFromDB = assignmentsFromDB.first();
        assertThat(assignmentFromDB)
            .usingRecursiveComparison()
            .ignoringFields("creationDate")
            .isEqualTo(assignment);
    }

    /**
     * Tests the set assignment endpoint with a nonexistent assignment.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(7)
    public void testSetAssignmentWithNonexistentAssignment() throws Exception {
        List<String> ids = new ArrayList<>();
        String assignmentId = "12i345-789";

        restTaskAssignmentMockMvc
            .perform(
                put("/api/tasks/assignments/{assignmentId}", assignmentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(ids))
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.taskAssignmentNotFound"))
            .andExpect(jsonPath("$.title").value("The task assignment does not exist!"));
    }

    /**
     * Tests the set assignment endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(8)
    public void testSetAssignment() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME, false);
        var testGoal1 = goals.first();

        List<LearningGoalDisplayDTO> displayGoals = StreamEx
            .of(goals)
            .map(x -> new LearningGoalDisplayDTO(x.getId(), x.getName()))
            .toList();

        var assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(testGoal1.getName(), testGoal1.getOwner());
        var assignment = assignments.first();

        String assignmentId = assignment.getId().substring(assignment.getId().lastIndexOf('#') + 1);

        List<String> displayGoalsToSerialize = StreamEx.of(displayGoals).map(LearningGoalDisplayDTO::getId).toList();

        restTaskAssignmentMockMvc
            .perform(
                put("/api/tasks/assignments/{assignmentId}", assignmentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(displayGoalsToSerialize))
            )
            .andExpect(status().isNoContent());

        assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(testGoal1.getName(), testGoal1.getOwner());
        assignment = assignments.first();

        assertThat(assignment.getLearningGoalIds()).containsExactlyInAnyOrderElementsOf(displayGoals);
    }

    /**
     * Tests the get assignment endpoint with the query parameter.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(9)
    public void testGetAssignmentsWithQueryParameter() throws Exception {
        var result = restTaskAssignmentMockMvc
            .perform(get("/api/tasks/assignments?taskHeader=test123"))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        List<TaskAssignmentDTO> assignments = TestUtil.convertCollectionFromJSONString(jsonData, TaskAssignmentDTO.class, List.class);

        assertThat(assignments).isEmpty();
    }

    /**
     * Tests the get assignment endpoint without the query parameter.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(10)
    public void testGetAssignmentsWithoutQueryParameter() throws Exception {
        var result = restTaskAssignmentMockMvc.perform(get("/api/tasks/assignments")).andExpect(status().isOk()).andReturn();

        String jsonData = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        List<TaskAssignmentDTO> assignments = TestUtil.convertCollectionFromJSONString(jsonData, TaskAssignmentDTO.class, List.class);

        assertThat(assignments).hasSize(1);
    }

    /**
     * Tests the get display task endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(11)
    @SuppressWarnings("unchecked")
    public void testGetAllTaskDisplayList() throws Exception {
        insertTestAssignmentsForFulltextSearch();

        int page = 0;
        int size = 5;

        var result = restTaskAssignmentMockMvc
            .perform(get("/api/tasks/display?page={page}&size={size}", page, size))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        List<TaskDisplayDTO> displayList = TestUtil.convertCollectionFromJSONString(jsonData, TaskDisplayDTO.class, List.class);
        assertThat(result.getResponse().getHeader("X-Has-Next-Page")).isEqualTo("true");
        assertThat(displayList).hasSize(5);

        page++;
        result =
            restTaskAssignmentMockMvc
                .perform(get("/api/tasks/display?page={page}&size={size}", page, size))
                .andExpect(status().isOk())
                .andReturn();
        jsonData = result.getResponse().getContentAsString();
        displayList = TestUtil.convertCollectionFromJSONString(jsonData, TaskDisplayDTO.class, List.class);
        assertThat(result.getResponse().getHeader("X-Has-Next-Page")).isEqualTo("false");
        assertThat(displayList).hasSize(1);
    }

    /**
     * Tests the get task assignment by internal id method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(12)
    public void testGetTaskAssignmentByInternalId() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME, false);
        var testGoal1 = goals.first();

        List<LearningGoalDisplayDTO> displayGoals = StreamEx
            .of(goals)
            .map(x -> new LearningGoalDisplayDTO(x.getId(), x.getName()))
            .toList();

        var assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(testGoal1.getName(), testGoal1.getOwner());
        var assignment = assignments.first();

        String assignmentId = assignment.getId().substring(assignment.getId().lastIndexOf('#') + 1);

        var result = restTaskAssignmentMockMvc
            .perform(get("/api/tasks/assignments/{assignmentId}", assignmentId))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();

        TaskAssignmentDTO taskAssignmentDTO = TestUtil.convertFromJSONString(jsonData, TaskAssignmentDTO.class);
        assertThat(taskAssignmentDTO)
            .usingRecursiveComparison()
            .isEqualTo(assignment);
    }

    /**
     * Tests the get task assignment by internal id method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(13)
    public void testGetTaskAssignmentByInternalIdEmpty() throws Exception {
        restTaskAssignmentMockMvc.perform(get("/api/tasks/assignments/5")).andExpect(status().isNotFound());
    }

    /**
     * Tests the get assigned learning goals of assignment endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(14)
    @SuppressWarnings("unchecked")
    public void testGetAssignedLearningGoalsOfAssignment() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME, false);
        insertTestAssignmentsForFulltextSearch();
        List<TaskAssignmentDTO> tasks = assignmentSPARQLEndpointService.getTaskAssignments("for", USERNAME);
        TaskAssignmentDTO task = tasks.get(0);

        String id = task.getId().substring(task.getId().lastIndexOf('#') + 1);
        List<String> goalIds = StreamEx.of(goals).map(LearningGoalDTO::getId).toList();

        assignmentSPARQLEndpointService.setTaskAssignment(id, goalIds);

        var result = restTaskAssignmentMockMvc
            .perform(get("/api/tasks/assignments/{id}/learninggoals", id))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        List<String> goalIdsFromWeb = TestUtil.convertCollectionFromJSONString(jsonData, String.class, List.class);
        assertThat(goalIdsFromWeb).containsExactlyInAnyOrder(goalIds.toArray(new String[0]));
    }

    /**
     * Tests the get tasks of learning goal endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(15)
    @SuppressWarnings("unchecked")
    public void testGetTasksOfLearningGoal() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME, false);
        var firstGoal = goals.first();

        var result = restTaskAssignmentMockMvc
            .perform(get("/api/tasks/of/{owner}/{name}", firstGoal.getOwner(), firstGoal.getName()))
            .andExpect(status().isOk())
            .andReturn();
        String jsonData = result.getResponse().getContentAsString();
        List<TaskAssignmentDisplayDTO> list = TestUtil.convertCollectionFromJSONString(
            jsonData,
            TaskAssignmentDisplayDTO.class,
            List.class
        );
        assertThat(list).hasSize(2);

        result =
            restTaskAssignmentMockMvc
                .perform(get("/api/tasks/of/{owner}/{name}", firstGoal.getOwner(), "Test"))
                .andExpect(status().isOk())
                .andReturn();
        jsonData = result.getResponse().getContentAsString();
        list = TestUtil.convertCollectionFromJSONString(jsonData, TaskAssignmentDisplayDTO.class, List.class);
        assertThat(list).isEmpty();
    }

    //region Private methods

    /**
     * Inserts the test assignments for the fulltext search.
     *
     * @return count of inserted assignments
     */
    private int insertTestAssignmentsForFulltextSearch() {
        insertTaskAssignmentForFulltextSearch("Testheader");
        insertTaskAssignmentForFulltextSearch("New header for");
        insertTaskAssignmentForFulltextSearch("Beispielaufgabe");
        insertTaskAssignmentForFulltextSearch("Aufgabe1");
        insertTaskAssignmentForFulltextSearch("Test123");

        return 5;
    }

    /**
     * Inserts a new task assignment for the fulltext search.
     *
     * @param header the header of the assignment (will be indexed)
     */
    private void insertTaskAssignmentForFulltextSearch(String header) {
        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setCreator("TestCreator");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setHeader(header);

        assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, USERNAME);
    }
    //endregion
}
