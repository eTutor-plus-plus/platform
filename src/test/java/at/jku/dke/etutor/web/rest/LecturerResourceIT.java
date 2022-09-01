package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.User;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.*;
import at.jku.dke.etutor.service.dto.AdminUserDTO;
import at.jku.dke.etutor.service.dto.CourseDTO;
import at.jku.dke.etutor.service.dto.LearningGoalDTO;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.LecturerGradingInfoDTO;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.StudentAssignmentOverviewInfoDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.LearningGoalAssignmentDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.web.rest.vm.GradingInfoVM;
import liquibase.integration.spring.SpringLiquibase;
import one.util.streamex.StreamEx;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdfconnection.RDFConnection;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the lecturer resource endpoint.
 *
 * @author fne
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = {AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.ADMIN}, username = "admin")
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("unchecked")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class LecturerResourceIT {

    private static final String OWNER = "admin";

    @Autowired
    private MockMvc restLecturerMockMvc;

    @Autowired
    private RDFConnectionFactory rdfConnectionFactory;

    @Autowired
    private CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService;

    @Autowired
    private ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService;

    @Autowired
    private SPARQLEndpointService sparqlEndpointService;

    @Autowired
    private UserService userService;

    @Autowired
    private AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;

    @Autowired
    private LecturerSPARQLEndpointService lecturerSPARQLEndpointService;

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

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setFirstName("Max");
        userDTO.setLastName("Mustermann");
        userDTO.setLogin("k11805540");
        userDTO.setEmail("k11805540@students.jku.at");
        userDTO.setAuthorities(Set.of(AuthoritiesConstants.STUDENT));
        student = userService.createUser(userDTO);

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
        newTaskAssignmentDTO.setLearningGoalIds(
            StreamEx.of(goal1, goal2).map(x -> new LearningGoalDisplayDTO(x.getId(), x.getName())).toList()
        );

        TaskAssignmentDTO taskAssignmentDTO = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        // Setup exercise sheets
        NewExerciseSheetDTO newExerciseSheetDTO = new NewExerciseSheetDTO();
        newExerciseSheetDTO.setName("Test exercise sheet");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        newExerciseSheetDTO.setLearningGoals(
            StreamEx.of(goal1, goal2).map(x -> new LearningGoalAssignmentDTO(new LearningGoalDisplayDTO(x.getId(), x.getName()), 1)).toList()
        );
        newExerciseSheetDTO.setTaskCount(1);

        exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);

        // Setup students & course instance exercise sheet assignment
        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(student.getLogin()), courseInstanceUrl);

        courseInstanceSPARQLEndpointService.addExerciseSheetCourseInstanceAssignments(
            courseInstanceUrl.substring(courseInstanceUrl.lastIndexOf('#') + 1),
            Collections.singletonList(exerciseSheetDTO.getId())
        );

        // Setup demo assignment
        ParameterizedSparqlString demoAssignmentUpdate = new ParameterizedSparqlString(
            """
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                INSERT DATA {
                  ?student etutor:hasIndividualTaskAssignment [
                  	etutor:fromExerciseSheet ?sheet ;
                    etutor:fromCourseInstance ?courseInstance;
                    etutor:hasIndividualTask [
                  		etutor:isGraded false;
                    	etutor:refersToTask ?task;
                        etutor:hasOrderNo 1;
                        etutor:isLearningGoalCompleted false;
                        etutor:isSubmitted true
                  	]
                  ]
                }
                """
        );

        demoAssignmentUpdate.setIri("?student", ETutorVocabulary.getStudentURLFromMatriculationNumber(student.getLogin()));
        demoAssignmentUpdate.setIri("?sheet", exerciseSheetDTO.getId());
        demoAssignmentUpdate.setIri("?courseInstance", courseInstanceUrl);
        demoAssignmentUpdate.setIri("?task", taskAssignmentDTO.getId());

        try (RDFConnection connection = rdfConnectionFactory.getRDFConnection()) {
            connection.update(demoAssignmentUpdate.asUpdate());
        }
    }

    /**
     * Tests the get paged lucturer overview endpoint.
     *
     * @throws Exception must not be thrown.
     */
    @Test
    @Order(1)
    public void testGetPagedLecturerOverview() throws Exception {
        String courseInstanceUUID = courseInstanceUrl.substring(courseInstanceUrl.lastIndexOf('#') + 1);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        var result = restLecturerMockMvc
            .perform(get("/api/lecturer/overview/{courseInstanceUUID}/{exerciseSheetUUID}", courseInstanceUUID, exerciseSheetUUID))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        List<StudentAssignmentOverviewInfoDTO> data = TestUtil.convertCollectionFromJSONString(
            jsonData,
            StudentAssignmentOverviewInfoDTO.class,
            List.class
        );
        assertThat(data).hasSize(1);
    }

    /**
     * Tests the get grading info endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(2)
    public void testGetGradingInfo() throws Exception {
        String courseInstanceUUID = courseInstanceUrl.substring(courseInstanceUrl.lastIndexOf('#') + 1);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);
        String matriculationNumber = student.getLogin();

        var result = restLecturerMockMvc
            .perform(
                get(
                    "/api/lecturer/grading/{courseInstanceUUID}/{exerciseSheetUUID}/{matriculationNo}",
                    courseInstanceUUID,
                    exerciseSheetUUID,
                    matriculationNumber
                )
            )
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        List<LecturerGradingInfoDTO> data = TestUtil.convertCollectionFromJSONString(jsonData, LecturerGradingInfoDTO.class, List.class);
        assertThat(data).hasSize(1);
    }

    /**
     * Tests the set grade for assignment endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(3)
    public void testSetGradeForAssignment() throws Exception {
        String courseInstanceUUID = courseInstanceUrl.substring(courseInstanceUrl.lastIndexOf('#') + 1);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);
        String matriculationNumber = student.getLogin();
        GradingInfoVM gradingInfoVM = new GradingInfoVM();

        gradingInfoVM.setCourseInstanceUUID(courseInstanceUUID);
        gradingInfoVM.setMatriculationNo(matriculationNumber);
        gradingInfoVM.setExerciseSheetUUID(exerciseSheetUUID);
        gradingInfoVM.setGoalCompleted(true);
        gradingInfoVM.setOrderNo(1);

        restLecturerMockMvc
            .perform(
                put("/api/lecturer/grading")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(gradingInfoVM))
            )
            .andExpect(status().isNoContent());

        var gradingInfoList = lecturerSPARQLEndpointService.getGradingInfo(courseInstanceUUID, exerciseSheetUUID, matriculationNumber);
        assertThat(gradingInfoList).hasSize(1);

        var gradingInfo = gradingInfoList.get(0);

        assertThat(gradingInfo.isGraded()).isTrue();
        assertThat(gradingInfo.isCompleted()).isTrue();
    }
}
