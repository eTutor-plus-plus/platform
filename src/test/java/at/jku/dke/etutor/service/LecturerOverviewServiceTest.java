package at.jku.dke.etutor.service;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.CourseDTO;
import at.jku.dke.etutor.service.dto.LearningGoalUpdateAssignmentDTO;
import at.jku.dke.etutor.service.dto.StudentSelfEvaluationLearningGoalDTO;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.LearningGoalAssignmentDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.lectureroverview.LearningGoalProgressDTO;
import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import liquibase.integration.spring.SpringLiquibase;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.javatuples.Quintet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withMarginOf;

/**
 * Test class for the {@code LecturerOverviewService} class.
 *
 * @author fne
 */
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class LecturerOverviewServiceTest {

    private static final String OWNER = "admin";

    @Autowired
    private RDFConnectionFactory rdfConnectionFactory;

    @Autowired
    private SPARQLEndpointService sparqlEndpointService;

    @Autowired
    private CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService;

    @Autowired
    private ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService;

    @Autowired
    private AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;

    @Autowired
    private LecturerOverviewService lecturerOverviewService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private LecturerSPARQLEndpointService lecturerSPARQLEndpointService;

    @Autowired
    private SpringLiquibase springLiquibase;

    /**
     * Setup before all test cases.
     *
     * @throws Exception must not be thrown
     */
    @BeforeAll
    public void init() throws Exception {
        springLiquibase.setDropFirst(true);
        springLiquibase.afterPropertiesSet();
    }

    /**
     * Setup before each test case.
     *
     * @throws Exception must not be thrown
     */
    @BeforeEach
    public void setup() throws Exception {
        rdfConnectionFactory.clearDataset();
        sparqlEndpointService.insertScheme();
    }


    /**
     * Tests the course instance overview statistics method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testCourseInstanceOverviewStatistics() throws Exception {
        //Prepare data.
        final String joinGoalUrl = ETutorVocabulary.createGoalUrl(OWNER, "Join");
        var data = prepareGoalsAndCourseData();

        Model model = data.getValue0();
        CourseDTO course = data.getValue1();
        String courseInstanceId = data.getValue2();
        String courseInstanceUUID = data.getValue3();
        List<String> matriculationNumbers = data.getValue4();

        assertThat(matriculationNumbers).hasSize(2);

        var statistics = lecturerOverviewService.getCourseInstanceOverviewStatistics(courseInstanceUUID);
        var achievementOverviewList = statistics.getLearningGoalAchievementOverview();

        assertThat(statistics.getStudentCount()).isEqualTo(matriculationNumbers.size());
        assertThat(statistics.getFailedGoalView()).isEmpty();
        assertThat(achievementOverviewList).extracting(LearningGoalProgressDTO::getAbsoluteCount).containsOnly(0);

        // Actualize data

        var studentEvaluations = getStudentSelfEvaluations(model, "^(?!Basic SQL|outerjoin|join).*$");

        studentService.saveSelfEvaluation(courseInstanceUUID, matriculationNumbers.get(0), studentEvaluations);
        studentService.saveSelfEvaluation(courseInstanceUUID, matriculationNumbers.get(1), studentEvaluations);

        // "Add" failed goals
        // Add task assignments and exercise sheets

        NewExerciseSheetDTO newExerciseSheetDTO = new ExerciseSheetDTO();
        newExerciseSheetDTO.setName("Join exercise sheet");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        newExerciseSheetDTO.setLearningGoals(Collections.singletonList(new LearningGoalAssignmentDTO(new LearningGoalDisplayDTO(joinGoalUrl, "Join"), 1)));
        newExerciseSheetDTO.setTaskCount(1);

        ExerciseSheetDTO exerciseSheetDTO =
            exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        String exerciseSheetUUID = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        NewTaskAssignmentDTO newTaskAssignmentDTO = new TaskAssignmentDTO();
        newTaskAssignmentDTO.setCreator(OWNER);
        newTaskAssignmentDTO.setHeader("Join assignment");
        newTaskAssignmentDTO.setTaskDifficultyId(ETutorVocabulary.Medium.getURI());
        newTaskAssignmentDTO.setOrganisationUnit("DKE");
        newTaskAssignmentDTO.setLearningGoalIds(Collections.singletonList(new LearningGoalDisplayDTO(joinGoalUrl, "Join")));

        assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, OWNER);

        courseInstanceSPARQLEndpointService.addExerciseSheetCourseInstanceAssignments(
            courseInstanceUUID,
            Collections.singletonList(exerciseSheetDTO.getId()));

        for (String matriculationNumber : matriculationNumbers) {
            studentService.openExerciseSheetForStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID);

            studentService.markTaskAssignmentAsSubmitted(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1);
            lecturerSPARQLEndpointService.updateGradeForAssignment(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1, true);
            lecturerSPARQLEndpointService.updateGradeForAssignment(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, 1, false);
        }

        // Test again
        statistics = lecturerOverviewService.getCourseInstanceOverviewStatistics(courseInstanceUUID);
        achievementOverviewList = statistics.getLearningGoalAchievementOverview();

        assertThat(statistics.getStudentCount()).isEqualTo(matriculationNumbers.size());
        assertThat(statistics.getFailedGoalView()).hasSize(1);
        var failedGoal = statistics.getFailedGoalView().get(0);
        assertThat(failedGoal.getId()).isEqualTo(joinGoalUrl);
        assertThat(failedGoal.getFailureCount()).isEqualTo(matriculationNumbers.size());
        assertThat(achievementOverviewList).extracting(LearningGoalProgressDTO::getAbsoluteCount).filteredOn(x -> x > 0).isNotEmpty();
    }

    /**
     * Returns the self evaluations, based on the learning goals from the model.
     * The status (completed / not completed) is inferred from the given regex
     * pattern which is matched with the goal's name.
     *
     * @param model           the model
     * @param completePattern the completion pattern
     * @return list of evaluations
     */
    private static List<StudentSelfEvaluationLearningGoalDTO> getStudentSelfEvaluations(Model model,
                                                                                        String completePattern) {
        List<StudentSelfEvaluationLearningGoalDTO> selfEvaluations = new ArrayList<>();
        ResIterator resIterator = model.listSubjectsWithProperty(RDF.type, ETutorVocabulary.Goal);

        Pattern pattern = Pattern.compile(StringUtils.isBlank(completePattern) ? ".*" : completePattern,
            Pattern.CASE_INSENSITIVE);

        while (resIterator.hasNext()) {
            Resource resource = resIterator.nextResource();
            String goalId = resource.getURI();
            String goalName = resource.getProperty(RDFS.label).getString();

            var selfEvaluation = new StudentSelfEvaluationLearningGoalDTO();
            selfEvaluation.setId(goalId);
            selfEvaluation.setText(goalName);
            selfEvaluation.setCompleted(pattern.matcher(goalName).matches());

            selfEvaluations.add(selfEvaluation);
        }

        return selfEvaluations;
    }

    /**
     * Inserts the SQL goal hierarchy, creates a course and course instance and assigns students to this course.
     *
     * @return {@link Quintet} containing the RDF model (learning goals), the course dto, the course instance url
     * (id),the course instance uuid and the students' matriculation numbers
     * @throws Exception must not be thrown
     */
    private Quintet<Model, CourseDTO, String, String, List<String>> prepareGoalsAndCourseData() throws Exception {
        //noinspection ConstantConditions
        Model model = RDFTestUtil.uploadLearningGoalHierarchy(rdfConnectionFactory, getClass().getResource(
            "goal_hierarchy.ttl"));

        // Insert course
        CourseDTO dmCourse = new CourseDTO();
        dmCourse.setName("Datenmodellierung");
        dmCourse.setCourseType("Modul");
        dmCourse = sparqlEndpointService.insertNewCourse(dmCourse, OWNER);

        // Set learning goal assignment
        var goalAssignment = new LearningGoalUpdateAssignmentDTO();
        goalAssignment.setCourseId(dmCourse.getId());
        goalAssignment.setLearningGoalIds(Collections.singletonList(ETutorVocabulary.createGoalUrl(OWNER,
            "Basic_SQL")));

        sparqlEndpointService.setGoalAssignment(goalAssignment);

        // Create course instance
        NewCourseInstanceDTO dmCourseInstance = new NewCourseInstanceDTO();
        dmCourseInstance.setCourseId(dmCourse.getId());
        dmCourseInstance.setYear(2021);
        dmCourseInstance.setTermId(ETutorVocabulary.Winter.getURI());

        String dmCourseInstanceUrl = courseInstanceSPARQLEndpointService.createNewCourseInstance(dmCourseInstance);
        String dmCourseInstanceUUID = dmCourseInstanceUrl.substring(dmCourseInstanceUrl.lastIndexOf('#') + 1);

        MultipartFile file = new MockMultipartFile(
            "file.csv",
            "file.csv",
            "text/csv",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("test_students.csv"))
        );

        var importedStudents = studentService.importStudentsFromFile(file);
        var matriculationNumbers = StreamEx.of(importedStudents).map(StudentInfoDTO::getMatriculationNumber).toList();

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(matriculationNumbers, dmCourseInstanceUrl);

        return Quintet.with(model, dmCourse, dmCourseInstanceUrl, dmCourseInstanceUUID, matriculationNumbers);
    }
}
