package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.LocalRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.LearningGoalDTO;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import at.jku.dke.etutor.service.dto.TaskDisplayDTO;
import at.jku.dke.etutor.service.dto.taskassignment.*;
import at.jku.dke.etutor.service.exception.InternalTaskAssignmentNonexistentException;
import at.jku.dke.etutor.service.exception.LearningGoalAlreadyExistsException;
import at.jku.dke.etutor.service.exception.TaskGroupAlreadyExistentException;
import one.util.streamex.StreamEx;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

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
        assertThatThrownBy(() -> assignmentSPARQLEndpointService.insertNewTaskAssignment(null, null))
            .isInstanceOf(NullPointerException.class);

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        assertThatThrownBy(() -> assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, null))
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
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER, false);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testassignment");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.addLearningGoal(new LearningGoalDisplayDTO(testGoal1.getId(), testGoal1.getName()));
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());

        var insertedAssignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

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
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER, false);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian1");
        newTaskAssignmentDTO.setHeader("Test");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.addLearningGoal(new LearningGoalDisplayDTO(testGoal1.getId(), testGoal1.getName()));
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setInstruction("<b>Test</b>");
        newTaskAssignmentDTO.setProcessingTime("1 h");
        newTaskAssignmentDTO.setUrl(new URL("http://www.test.at"));

        var insertedAssignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

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
        assertThatThrownBy(() -> assignmentSPARQLEndpointService.removeTaskAssignment(null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the removal of a task assignment.
     *
     * @throws InternalModelException must not happen
     */
    @Test
    public void testRemoveTaskAssignment() throws InternalModelException {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER, false);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testassignment");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.addLearningGoal(new LearningGoalDisplayDTO(testGoal1.getId(), testGoal1.getName()));
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());

        var insertedAssignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

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
        assertThatThrownBy(() -> assignmentSPARQLEndpointService.updateTaskAssignment(null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the update task assignment method.
     *
     * @throws Exception must not happen
     */
    @Test
    public void testUpdateTaskAssignment() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER, false);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testassignment");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.addLearningGoal(new LearningGoalDisplayDTO(testGoal1.getId(), testGoal1.getName()));
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.NoType.getURI());

        var insertedAssignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        insertedAssignment.setHeader("Newheader");

        assertThatCode(() -> assignmentSPARQLEndpointService.updateTaskAssignment(insertedAssignment)).doesNotThrowAnyException();
    }

    /**
     * Tests the update task assignment method with all fields.
     *
     * @throws Exception must not happen
     */
    @Test
    public void testUpdateTaskAssignmentWithAllFields() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER, false);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testassignment");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.addLearningGoal(new LearningGoalDisplayDTO(testGoal1.getId(), testGoal1.getName()));
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setTaskAssignmentTypeId(ETutorVocabulary.NoType.getURI());

        var assignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        assignment.setProcessingTime("1h");
        assignment.setUrl(new URL("http://www.test.at"));
        assignment.setInstruction("<b>Testinstructions</b>");

        assertThatCode(() -> assignmentSPARQLEndpointService.updateTaskAssignment(assignment)).doesNotThrowAnyException();
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

    /**
     * Tests the set assignment method with null values.
     */
    @Test
    public void testSetAssignmentWithNullValues() {
        assertThatThrownBy(() -> assignmentSPARQLEndpointService.setTaskAssignment(null, null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> assignmentSPARQLEndpointService.setTaskAssignment("testid", null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the set assignment method with a nonexistent task.
     */
    @Test
    public void testSetAssignmentWithNonexistentTaskAssignment() {
        assertThatThrownBy(() -> assignmentSPARQLEndpointService.setTaskAssignment("12345", new ArrayList<>()))
            .isInstanceOf(InternalTaskAssignmentNonexistentException.class);
    }

    /**
     * Tests the set assignment method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testSetAssignment() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER, false);
        var testGoal1 = goals.first();

        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator("Florian");
        newTaskAssignmentDTO.setHeader("Testassignment");
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.addLearningGoal(new LearningGoalDisplayDTO(testGoal1.getId(), testGoal1.getName()));
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());

        var assignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        List<String> testGoalIds = StreamEx.of(goals).map(LearningGoalDTO::getId).toList();

        String taskId = assignment.getId().substring(assignment.getId().lastIndexOf('#') + 1);

        assignmentSPARQLEndpointService.setTaskAssignment(taskId, testGoalIds);

        try (RDFConnection connection = rdfConnectionFactory.getRDFConnection()) {
            String cntQuery =
                """
                    PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                    SELECT (COUNT(?goal) as ?cnt)
                    WHERE {
                    	?goal etutor:hasTaskAssignment ?assignment
                    }
                    """;

            try (QueryExecution queryExecution = connection.query(cntQuery)) {
                ResultSet set = queryExecution.execSelect();
                int cnt = set.nextSolution().getLiteral("?cnt").getInt();

                assertThat(cnt).isEqualTo(testGoalIds.size());
            }
        }
    }

    /**
     * Tests the getTaskAssignment method without a query string.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetTaskAssignmentsWithoutQueryString() throws Exception {
        int expectedCnt = insertTestAssignmentsForFulltextSearch();

        List<TaskAssignmentDTO> tasks = assignmentSPARQLEndpointService.getTaskAssignments(null, OWNER);
        assertThat(tasks).hasSize(expectedCnt);

        tasks = assignmentSPARQLEndpointService.getTaskAssignments("", OWNER);
        assertThat(tasks).hasSize(expectedCnt);

        tasks = assignmentSPARQLEndpointService.getTaskAssignments("     ", OWNER);
        assertThat(tasks).hasSize(expectedCnt);
    }

    /**
     * Tests the getTaskAssignment method with a query string.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetTaskAssignmentWithQueryString() throws Exception {
        insertTestAssignmentsForFulltextSearch();

        List<TaskAssignmentDTO> tasks = assignmentSPARQLEndpointService.getTaskAssignments("test", OWNER);
        assertThat(tasks).hasSize(2);

        tasks = assignmentSPARQLEndpointService.getTaskAssignments("for", OWNER);
        assertThat(tasks).hasSize(1);

        tasks = assignmentSPARQLEndpointService.getTaskAssignments("1", OWNER);
        assertThat(tasks).hasSize(2);
    }

    /**
     * Tests the get task assignment by internal id method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetTaskAssignmentByInternalId() throws Exception {
        insertTestAssignmentsForFulltextSearch();

        List<TaskAssignmentDTO> tasks = assignmentSPARQLEndpointService.getTaskAssignments("for", OWNER);
        TaskAssignmentDTO task = tasks.get(0);

        String id = task.getId().substring(task.getId().lastIndexOf('#') + 1);
        Optional<TaskAssignmentDTO> optionalTaskFromDb = assignmentSPARQLEndpointService.getTaskAssignmentByInternalId(id);

        assertThat(optionalTaskFromDb).isPresent();
        TaskAssignmentDTO taskFromDb = optionalTaskFromDb.get();

        assertThat(taskFromDb)
            .usingRecursiveComparison()
            .isEqualTo(task);

        optionalTaskFromDb = assignmentSPARQLEndpointService.getTaskAssignmentByInternalId("123");
        assertThat(optionalTaskFromDb).isEmpty();
    }

    /**
     * Tests the find all tasks method.‚
     */
    @Test
    public void testFindAllTasks() {
        int cnt = insertTestAssignmentsForFulltextSearch();
        PageRequest pageRequest = PageRequest.of(0, cnt - 1);

        Slice<TaskDisplayDTO> slice = assignmentSPARQLEndpointService.findAllTasks("for", pageRequest, OWNER, null);
        assertThat(slice.hasNext()).isFalse();
        assertThat(slice.getContent()).hasSize(1);

        slice = assignmentSPARQLEndpointService.findAllTasks("", pageRequest, OWNER, null);

        assertThat(slice.getContent()).hasSize(cnt - 1);
        assertThat(slice.hasNext()).isTrue();
    }

    /**
     * Tests the get assigned learning goal ids of task assignment method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetLearningGoalIdsOfTaskAssignment() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER, false);
        insertTestAssignmentsForFulltextSearch();
        List<TaskAssignmentDTO> tasks = assignmentSPARQLEndpointService.getTaskAssignments("for", OWNER);
        TaskAssignmentDTO task = tasks.get(0);

        String id = task.getId().substring(task.getId().lastIndexOf('#') + 1);
        List<String> goalIds = StreamEx.of(goals).map(LearningGoalDTO::getId).toList();

        assignmentSPARQLEndpointService.setTaskAssignment(id, goalIds);

        List<String> goalIdsFromDb = assignmentSPARQLEndpointService.getAssignedLearningGoalIdsOfTaskAssignment(id);

        assertThat(goalIdsFromDb).containsExactlyInAnyOrder(goalIds.toArray(new String[0]));
    }

    /**
     * Tests the get tasks of learning goal method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetTasksOfLearningGoal() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER, false);
        insertTestAssignmentsForFulltextSearch();
        List<TaskAssignmentDTO> tasks = assignmentSPARQLEndpointService.getTaskAssignments("for", OWNER);
        TaskAssignmentDTO task = tasks.get(0);

        String id = task.getId().substring(task.getId().lastIndexOf('#') + 1);
        List<String> goalIds = StreamEx.of(goals).map(LearningGoalDTO::getId).toList();
        var firstGoal = goals.first();

        assignmentSPARQLEndpointService.setTaskAssignment(id, goalIds);

        List<TaskAssignmentDisplayDTO> assignmentHeaders = assignmentSPARQLEndpointService.getTasksOfLearningGoal(
            firstGoal.getName(),
            firstGoal.getOwner()
        );
        assertThat(assignmentHeaders).hasSize(1);
        TaskAssignmentDisplayDTO assignmentHeader = assignmentHeaders.get(0);
        assertThat(assignmentHeader.getHeader()).isEqualTo(task.getHeader());
        assertThat(assignmentHeader.getId()).isEqualTo(task.getId());

        assignmentHeaders = assignmentSPARQLEndpointService.getTasksOfLearningGoal("test", firstGoal.getOwner());
        assertThat(assignmentHeaders).isEmpty();
    }

    /**
     * Tests the creation of a task group
     */
    @Test
    public void testCreateTaskGroupWithAllFields() throws TaskGroupAlreadyExistentException {
        var name = "TestGroup1";
        var description = "TestDescription";
        var fileUrl = "TestURL";
        var createSt = "TestCreate";
        var insertSub = "TestInsertSubmission";
        var insertDia = "TestInsertDiagnose";
        var diagnoseXML = "TestDiagnoseXML";
        var subXML = "TestSubmissionXML";
        var newTaskGroupDTO = new NewTaskGroupDTO();
        newTaskGroupDTO.setName(name);
        newTaskGroupDTO.setTaskGroupTypeId(ETutorVocabulary.XQueryTypeTaskGroup.toString());
        newTaskGroupDTO.setDescription(description);
        newTaskGroupDTO.setSqlCreateStatements(createSt);
        newTaskGroupDTO.setSqlInsertStatementsDiagnose(insertDia);
        newTaskGroupDTO.setSqlInsertStatementsSubmission(insertSub);
        newTaskGroupDTO.setxQueryDiagnoseXML(diagnoseXML);
        newTaskGroupDTO.setxQuerySubmissionXML(subXML);


        var taskGroupDTO = assignmentSPARQLEndpointService.createNewTaskGroup(newTaskGroupDTO, "admin");
        assignmentSPARQLEndpointService.addXMLFileURL(taskGroupDTO, fileUrl);

        assertThat(assignmentSPARQLEndpointService.getTaskGroupByName(name).isPresent());


        var fetchedGroup = assignmentSPARQLEndpointService.getTaskGroupByName(name).get();
        assertThat(fetchedGroup.getName().equals(name));
        assertThat(fetchedGroup.getDescription().equals(description));
        assertThat(fetchedGroup.getSqlCreateStatements().equals(createSt));
        assertThat(fetchedGroup.getSqlInsertStatementsDiagnose().equals(insertDia));
        assertThat(fetchedGroup.getSqlInsertStatementsSubmission().equals(insertSub));
        assertThat(fetchedGroup.getxQueryDiagnoseXML().equals(diagnoseXML));
        assertThat(fetchedGroup.getxQuerySubmissionXML().equals(subXML));
        assertThat(fetchedGroup.getFileUrl().equals(fileUrl));
        assertThat(fetchedGroup.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString()));
    }

    /**
     * Tests the deletion of a task group
     * @throws TaskGroupAlreadyExistentException
     */
    @Test
    public void testDeleteTaskGroup() throws TaskGroupAlreadyExistentException {
        var name = "TestGroup";
        var newTaskGroupDTO = new NewTaskGroupDTO();
        newTaskGroupDTO.setName(name);
        newTaskGroupDTO.setTaskGroupTypeId(ETutorVocabulary.SQLTypeTaskGroup.toString());

        assignmentSPARQLEndpointService.createNewTaskGroup(newTaskGroupDTO, "admin");
        assignmentSPARQLEndpointService.deleteTaskGroup(name);
        assertThat(assignmentSPARQLEndpointService.getTaskGroupByName(name).isEmpty());
    }

    /**
     * Tests modifying the task-groups description
     * @throws TaskGroupAlreadyExistentException if task group already exists
     */
    @Test
    public void testModifyTaskGroup() throws TaskGroupAlreadyExistentException {
        var name = "TestGroup";
        var newTaskGroupDTO = new NewTaskGroupDTO();
        newTaskGroupDTO.setName(name);
        newTaskGroupDTO.setTaskGroupTypeId(ETutorVocabulary.NoTypeTaskGroup.toString());
        newTaskGroupDTO.setDescription("TestDescription1");

        var taskGroupDTO = assignmentSPARQLEndpointService.createNewTaskGroup(newTaskGroupDTO, "admin");

        taskGroupDTO.setDescription("TestDescription2");
        assignmentSPARQLEndpointService.modifyTaskGroup(taskGroupDTO);
        assertThat(assignmentSPARQLEndpointService.getTaskGroupByName(name).isPresent());
        assertThat(assignmentSPARQLEndpointService.getTaskGroupByName(name).get().getDescription().equals("TestDescription2"));
    }

    @Test
    public void testModifySQLTaskGroup() throws TaskGroupAlreadyExistentException {
        var name = "TestGroup";
        var create = "TestCreate";
        var insert1 = "TestInsert1";
        var insert2 = "TestInsert2";
        var newTaskGroupDTO = new NewTaskGroupDTO();
        newTaskGroupDTO.setName(name);
        newTaskGroupDTO.setTaskGroupTypeId(ETutorVocabulary.SQLTypeTaskGroup.toString());

        var taskGroupDTO = assignmentSPARQLEndpointService.createNewTaskGroup(newTaskGroupDTO, "admin");
        taskGroupDTO.setSqlCreateStatements(create);
        taskGroupDTO.setSqlInsertStatementsDiagnose(insert1);
        taskGroupDTO.setSqlInsertStatementsSubmission(insert2);

        assignmentSPARQLEndpointService.modifySQLTaskGroup(taskGroupDTO);

        var fetchedGroup = assignmentSPARQLEndpointService.getTaskGroupByName(name).get();

        assertThat(fetchedGroup.getSqlCreateStatements().equals(create));
        assertThat(fetchedGroup.getSqlInsertStatementsDiagnose().equals(insert1));
        assertThat(fetchedGroup.getSqlInsertStatementsSubmission().equals(insert2));
        assertThat(fetchedGroup.getSqlInsertStatementsSubmission().equals(insert2));
    }

    @Test
    public void testModifyXQTaskGroup() throws TaskGroupAlreadyExistentException {
        var name = "TestGroup";
        var xml1 = "TestXML1";
        var xml2 = "TestXML2";
        var newTaskGroupDTO = new NewTaskGroupDTO();
        newTaskGroupDTO.setName(name);
        newTaskGroupDTO.setTaskGroupTypeId(ETutorVocabulary.XQueryTypeTaskGroup.toString());

        var taskGroupDTO = assignmentSPARQLEndpointService.createNewTaskGroup(newTaskGroupDTO, "admin");
        taskGroupDTO.setxQueryDiagnoseXML(xml1);
        taskGroupDTO.setxQuerySubmissionXML(xml2);

        assignmentSPARQLEndpointService.modifyXQueryTaskGroup(taskGroupDTO);

        var fetchedGroup = assignmentSPARQLEndpointService.getTaskGroupByName(name).get();

        assertThat(fetchedGroup.getxQueryDiagnoseXML().equals(xml1));
        assertThat(fetchedGroup.getxQuerySubmissionXML().equals(xml2));
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

        assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);
    }


    //endregion
}
