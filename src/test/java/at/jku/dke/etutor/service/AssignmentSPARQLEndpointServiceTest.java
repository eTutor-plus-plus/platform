package at.jku.dke.etutor.service;


import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.LocalRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.InternalTaskAssignmentNonexistentException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the {@code AssignmentSPARQLEndpointService} class.
 *
 * @author fne
 */
public class AssignmentSPARQLEndpointServiceTest {

    private static final String OWNER = "admin";

    private AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private SPARQLEndpointService sparqlEndpointService;
    private RDFConnectionFactory rdfConnectionFactory;

    /**
     * Method which initializes the dataset and endpoint service before each run.
     *
     * @throws LearningGoalAlreadyExistsException must not be thrown
     */
    @BeforeEach
    public void setup() throws LearningGoalAlreadyExistsException {
        Dataset dataset = DatasetFactory.createTxnMem();
        rdfConnectionFactory = new LocalRDFConnectionFactory(dataset);
        sparqlEndpointService = new SPARQLEndpointService(rdfConnectionFactory);
        assignmentSPARQLEndpointService = new AssignmentSPARQLEndpointService(rdfConnectionFactory);

        sparqlEndpointService.insertScheme();

        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("TestGoal1");
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, OWNER);

        newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("TestGoal2");
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, OWNER);
    }

    /**
     * Tests the insert new task assignment method with a null value.
     */
    @Test
    public void testInsertNewAssignmentNullValues() {
        assertThatThrownBy(() -> assignmentSPARQLEndpointService.insertNewTaskAssignment(null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the get assignments of goal method with null values.
     */
    @Test
    public void testGetTaskAssignmentsOfGoalNullValues() {
        assertThatThrownBy(() -> assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal("Test", null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(null, null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the insertion of a new task assignment and the retrieval of
     * task assignments from the corresponding course.
     *
     * @throws InternalModelException must not be thrown
     */
    @Test
    public void testInsertNewTaskAssignmentAndGetAssignmentsOfGoal() throws InternalModelException {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testassignment");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setLearningGoalId(testGoal1.getId());
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());

        var insertedAssignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO);

        RDFTestUtil.checkThatSubjectExists(String.format("<%s>", insertedAssignment.getId()), rdfConnectionFactory);

        var assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(testGoal1.getName(), testGoal1.getOwner());
        assertThat(assignments).isNotEmpty();
        assertThat(assignments.first()).isEqualByComparingTo(insertedAssignment);
    }

    /**
     * Tests the insertion of a task assignment where all fields are set.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testInsertNewTaskAssignmentWithoutBlankFields() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian1");
        newTaskAssignmentDTO.setHeader("Test");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setLearningGoalId(testGoal1.getId());
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setInstruction("<b>Test</b>");
        newTaskAssignmentDTO.setProcessingTime("1 h");
        newTaskAssignmentDTO.setUrl(new URL("http://www.test.at"));

        var insertedAssignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO);

        RDFTestUtil.checkThatSubjectExists(String.format("<%s>", insertedAssignment.getId()), rdfConnectionFactory);

        var assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(testGoal1.getName(), testGoal1.getOwner());
        assertThat(assignments).isNotEmpty();
        assertThat(assignments.first()).isEqualByComparingTo(insertedAssignment);
    }

    /**
     * Tests the task assignment removal method with a null value.
     */
    @Test
    public void testRemoveTaskAssignmentWithNullValue() {
        assertThatThrownBy(() -> assignmentSPARQLEndpointService.removeTaskAssignment(null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the removal of a task assignment.
     *
     * @throws InternalModelException must not happen
     */
    @Test
    public void testRemoveTaskAssignment() throws InternalModelException {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testassignment");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setLearningGoalId(testGoal1.getId());
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());

        var insertedAssignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO);

        var assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(testGoal1.getName(), testGoal1.getOwner());
        assertThat(assignments).isNotEmpty();

        String id = insertedAssignment.getId().substring(insertedAssignment.getId().lastIndexOf('#') + 1);

        assignmentSPARQLEndpointService.removeTaskAssignment(id);
        assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(testGoal1.getName(), testGoal1.getOwner());

        assertThat(assignments).isEmpty();
    }

    /**
     * Tests the update task assignment method with a null value.
     */
    @Test
    public void testUpdateTaskAssignmentWithNullValue() {
        assertThatThrownBy(() -> assignmentSPARQLEndpointService.updateTaskAssignment(null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the update task assignment method.
     *
     * @throws Exception must not happen
     */
    @Test
    public void testUpdateTaskAssignment() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testassignment");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setLearningGoalId(testGoal1.getId());
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());

        var insertedAssignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO);

        insertedAssignment.setHeader("Newheader");

        assignmentSPARQLEndpointService.updateTaskAssignment(insertedAssignment);
    }

    /**
     * Tests the update task assignment method with all fields.
     *
     * @throws Exception must not happen
     */
    @Test
    public void testUpdateTaskAssignmentWithAllFields() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testassignment");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setLearningGoalId(testGoal1.getId());
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());

        var assignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO);

        assignment.setProcessingTime("1h");
        assignment.setUrl(new URL("http://www.test.at"));
        assignment.setInstruction("<b>Testinstructions</b>");

        assignmentSPARQLEndpointService.updateTaskAssignment(assignment);
    }

    /**
     * Tests the update method with a nonexistent assignment.
     */
    @Test
    public void testUpdateTaskAssignmentWithNonexistentAssignment() {
        TaskAssignmentDTO taskAssignmentDTO = new TaskAssignmentDTO();
        taskAssignmentDTO.setId("http://www.testid.at");

        assertThatThrownBy(() -> assignmentSPARQLEndpointService.updateTaskAssignment(taskAssignmentDTO))
            .isInstanceOf(InternalTaskAssignmentNonexistentException.class);
    }
}
