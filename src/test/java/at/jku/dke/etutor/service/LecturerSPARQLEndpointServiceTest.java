package at.jku.dke.etutor.service;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.User;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.LocalRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.dto.CourseDTO;
import at.jku.dke.etutor.service.dto.LearningGoalDTO;
import at.jku.dke.etutor.service.dto.UserDTO;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the {@link LecturerSPARQLEndpointService} class.
 *
 * @author fne
 */
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class LecturerSPARQLEndpointServiceTest {

    private static final String OWNER = "admin";

    private LecturerSPARQLEndpointService lecturerSPARQLEndpointService;

    @Autowired
    private UserService userService;

    @Autowired
    private SpringLiquibase springLiquibase;

    private User student;
    private String courseInstanceUrl;
    private ExerciseSheetDTO exerciseSheetDTO;

    /**
     * Initializes the test class.
     *
     * @throws Exception must not be thrown
     */
    @BeforeAll
    public void init() throws Exception {
        springLiquibase.setDropFirst(true);
        springLiquibase.afterPropertiesSet();

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName("Max");
        userDTO.setLastName("Mustermann");
        userDTO.setLogin("k11805541");
        userDTO.setEmail("k11805541@students.jku.at");
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.STUDENT));
        student = userService.createUser(userDTO);
    }

    /**
     * Method which initializes the dataset and endpoint service before each run.
     *
     * @throws Exception must not be thrown
     */
    @BeforeEach
    public void setup() throws Exception {
        Dataset dataset = DatasetFactory.createTxnMem();
        RDFConnectionFactory rdfConnectionFactory = new LocalRDFConnectionFactory(dataset);
        lecturerSPARQLEndpointService = new LecturerSPARQLEndpointService(rdfConnectionFactory);
        SPARQLEndpointService sparqlEndpointService = new SPARQLEndpointService(rdfConnectionFactory);
        CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService = new CourseInstanceSPARQLEndpointService(rdfConnectionFactory, userService);
        AssignmentSPARQLEndpointService assignmentSPARQLEndpointService = new AssignmentSPARQLEndpointService(rdfConnectionFactory);
        ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService = new ExerciseSheetSPARQLEndpointService(rdfConnectionFactory);

        sparqlEndpointService.insertScheme();

        // Setup learning goals
        LearningGoalDTO learningGoalDTO = new LearningGoalDTO();
        learningGoalDTO.setName("Testgoal1");

        LearningGoalDTO goal1 = sparqlEndpointService.insertNewLearningGoal(learningGoalDTO, OWNER);

        learningGoalDTO = new LearningGoalDTO();
        learningGoalDTO.setName("Testgoal2");

        LearningGoalDTO goal2 = sparqlEndpointService.insertNewLearningGoal(learningGoalDTO, OWNER);

        // Setup courses
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("Testcourse");
        courseDTO.setCourseType("LVA");
        courseDTO = sparqlEndpointService.insertNewCourse(courseDTO, OWNER);

        // Setup course instances
        NewCourseInstanceDTO newCourseInstanceDTO = new NewCourseInstanceDTO();
        newCourseInstanceDTO.setCourseId(courseDTO.getId());
        newCourseInstanceDTO.setYear(2021);
        newCourseInstanceDTO.setTermId(ETutorVocabulary.Summer.getURI());

        courseInstanceUrl = courseInstanceSPARQLEndpointService.createNewCourseInstance(newCourseInstanceDTO);

        // Setup tasks
        NewTaskAssignmentDTO newTaskAssignmentDTO = new NewTaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator(OWNER);
        newTaskAssignmentDTO.setHeader("Testassignment");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setLearningGoalIds(StreamEx.of(goal1, goal2).map(x -> new LearningGoalDisplayDTO(x.getId(), x.getName())).toList());

        TaskAssignmentDTO taskAssignmentDTO = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        // Setup exercise sheets
        NewExerciseSheetDTO newExerciseSheetDTO = new NewExerciseSheetDTO();
        newExerciseSheetDTO.setName("Test exercise sheet");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        newExerciseSheetDTO.setLearningGoals(StreamEx.of(goal1, goal2).map(x -> new LearningGoalDisplayDTO(x.getId(), x.getName())).toList());
        newExerciseSheetDTO.setTaskCount(1);

        exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);

        // Setup students & course instance exercise sheet assignment
        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(student.getLogin()), courseInstanceUrl);

        courseInstanceSPARQLEndpointService.addExerciseSheetCourseInstanceAssignments(courseInstanceUrl.substring(courseInstanceUrl.lastIndexOf('#') + 1),
            Collections.singletonList(exerciseSheetDTO.getId()));

        // Setup demo assignment
        ParameterizedSparqlString demoAssignmentUpdate = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            INSERT DATA {
              ?student etutor:hasIndividualTaskAssignment [
              	etutor:fromExerciseSheet ?sheet ;
                etutor:fromCourseInstance ?courseInstance;
                etutor:isAssignmentSubmitted true;
                etutor:hasIndividualTask [
              		etutor:isGraded false;
                	etutor:refersToTask ?task;
                    etutor:hasOrderNo 1;
                    etutor:isLearningGoalCompleted false
              	]
              ]
            }
            """);

        demoAssignmentUpdate.setIri("?student", ETutorVocabulary.getStudentURLFromMatriculationNumber(student.getLogin()));
        demoAssignmentUpdate.setIri("?sheet", exerciseSheetDTO.getId());
        demoAssignmentUpdate.setIri("?courseInstance", courseInstanceUrl);
        demoAssignmentUpdate.setIri("?task", taskAssignmentDTO.getId());

        try (RDFConnection connection = rdfConnectionFactory.getRDFConnection()) {
            connection.update(demoAssignmentUpdate.asUpdate());
        }
    }

    /**
     * Tests get paged lecturer overview method with null values.
     */
    @Test
    public void testGetPagedLecturerOverviewNullValues() {
        assertThatThrownBy(() -> lecturerSPARQLEndpointService.getPagedLecturerOverview(null, null, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> lecturerSPARQLEndpointService.getPagedLecturerOverview("test", null, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> lecturerSPARQLEndpointService.getPagedLecturerOverview("test", "test", null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the get grading info method with null values.
     */
    @Test
    public void testGetGradingInfoNullValues() {
        assertThatThrownBy(() -> lecturerSPARQLEndpointService.getGradingInfo(null, null, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> lecturerSPARQLEndpointService.getGradingInfo("test", null, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> lecturerSPARQLEndpointService.getGradingInfo("test", "test", null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the update grade for assignment method with null values.
     */
    @Test
    public void testUpdateGradeForAssignmentNullValues() {
        assertThatThrownBy(() -> lecturerSPARQLEndpointService.updateGradeForAssignment(null, null, null, 1, false))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> lecturerSPARQLEndpointService.updateGradeForAssignment("test", null, null, 1, false))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> lecturerSPARQLEndpointService.updateGradeForAssignment("test", "test", null, 1, false))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the get paged lecturer overview.
     */
    @Test
    public void testGetPagedLecturerOverview() {
        Pageable pageable = PageRequest.of(0, 5);
        String courseInstanceUUID = courseInstanceUrl.substring(courseInstanceUrl.lastIndexOf('#') + 1);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);
        var page = lecturerSPARQLEndpointService.getPagedLecturerOverview(
            courseInstanceUUID, exerciseSheetUUID, pageable);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        var overviewInfo = page.getContent().get(0);
        assertThat(overviewInfo.getMatriculationNo()).isEqualTo(student.getLogin());
        assertThat(overviewInfo.isFullyGraded()).isFalse();
        assertThat(overviewInfo.isSubmitted()).isTrue();
    }

    /**
     * Tests the get grading info method.
     */
    @Test
    public void testGetGradingInfo() {
        String courseInstanceUUID = courseInstanceUrl.substring(courseInstanceUrl.lastIndexOf('#') + 1);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        var gradingInfoList = lecturerSPARQLEndpointService.getGradingInfo(courseInstanceUUID, exerciseSheetUUID, student.getLogin());
        assertThat(gradingInfoList).hasSize(1);

        var gradingInfo = gradingInfoList.get(0);
        assertThat(gradingInfo.getOrderNo()).isEqualTo(1);
    }

    /**
     * Tests the update grade for assignment method.
     */
    @Test
    public void testUpdateGradeForAssignment() {
        String courseInstanceUUID = courseInstanceUrl.substring(courseInstanceUrl.lastIndexOf('#') + 1);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        lecturerSPARQLEndpointService.updateGradeForAssignment(courseInstanceUUID, exerciseSheetUUID, student.getLogin(), 1, true);
        var gradingInfoList = lecturerSPARQLEndpointService.getGradingInfo(courseInstanceUUID, exerciseSheetUUID, student.getLogin());
        assertThat(gradingInfoList).hasSize(1);

        var gradingInfo = gradingInfoList.get(0);

        assertThat(gradingInfo.isGraded()).isTrue();
        assertThat(gradingInfo.isCompleted()).isTrue();
    }
}
