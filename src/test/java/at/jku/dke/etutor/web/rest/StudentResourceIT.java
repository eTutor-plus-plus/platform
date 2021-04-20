package at.jku.dke.etutor.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.CourseInstanceSPARQLEndpointService;
import at.jku.dke.etutor.service.ExerciseSheetSPARQLEndpointService;
import at.jku.dke.etutor.service.SPARQLEndpointService;
import at.jku.dke.etutor.service.StudentService;
import at.jku.dke.etutor.service.dto.CourseDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceInformationDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceProgressOverviewDTO;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Test class for the student resource endpoint.
 *
 * @author fne
 */
@AutoConfigureMockMvc
@WithMockUser(
    authorities = { AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.ADMIN, AuthoritiesConstants.STUDENT },
    username = "admin"
)
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StudentResourceIT {

    @Autowired
    private RDFConnectionFactory rdfConnectionFactory;

    @Autowired
    private SPARQLEndpointService sparqlEndpointService;

    @Autowired
    private SpringLiquibase springLiquibase;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService;

    @Autowired
    private ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService;

    @Autowired
    private MockMvc restMockMvc;

    private String courseInstanceUUID;

    /**
     * Init method which initializes the testing envirnment before all tests.
     *
     * @throws Exception must not be thrown
     */
    @BeforeAll
    public void initBeforeAllTests() throws Exception {
        springLiquibase.setDropFirst(true);
        springLiquibase.afterPropertiesSet();
        rdfConnectionFactory.clearDataset();

        sparqlEndpointService.insertScheme();

        MultipartFile file = new MockMultipartFile(
            "file.csv",
            "file.csv",
            "text/csv",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("/at/jku/dke/etutor/service/test_students.csv"))
        );

        var importedStudents = studentService.importStudentsFromFile(file);

        String mNr = importedStudents.get(0).getMatriculationNumber();

        CourseDTO newCourseDTO = new CourseDTO();
        newCourseDTO.setName("Testcourse1");
        newCourseDTO.setCourseType("LVA");

        newCourseDTO = sparqlEndpointService.insertNewCourse(newCourseDTO, "admin");

        NewCourseInstanceDTO firstInstance = new NewCourseInstanceDTO();
        firstInstance.setYear(2021);
        firstInstance.setCourseId(newCourseDTO.getId());
        firstInstance.setTermId(ETutorVocabulary.Summer.getURI());

        String firstInstanceId = courseInstanceSPARQLEndpointService.createNewCourseInstance(firstInstance);

        courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(Collections.singletonList(mNr), firstInstanceId);

        NewExerciseSheetDTO firstNewExerciseSheet = new NewExerciseSheetDTO();
        firstNewExerciseSheet.setName("TestSheet 1");
        firstNewExerciseSheet.setDifficultyId(ETutorVocabulary.Medium.getURI());
        firstNewExerciseSheet.setLearningGoals(new ArrayList<>());

        var firstExerciseSheet = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(firstNewExerciseSheet, "admin");

        NewExerciseSheetDTO secondNewExerciseSheet = new NewExerciseSheetDTO();
        secondNewExerciseSheet.setName("TestSheet 2");
        secondNewExerciseSheet.setDifficultyId(ETutorVocabulary.Medium.getURI());
        secondNewExerciseSheet.setLearningGoals(new ArrayList<>());

        var secondExerciseSheet = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(secondNewExerciseSheet, "admin");

        courseInstanceUUID = firstInstanceId.substring(firstInstanceId.lastIndexOf('#') + 1);

        courseInstanceSPARQLEndpointService.addExerciseSheetCourseInstanceAssignments(
            courseInstanceUUID,
            Arrays.asList(firstExerciseSheet.getId(), secondExerciseSheet.getId())
        );
    }

    /**
     * Tests the get students endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetStudents() throws Exception {
        var result = restMockMvc.perform(get("/api/student")).andExpect(status().isOk()).andReturn();

        String jsonData = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        List<StudentInfoDTO> students = TestUtil.convertCollectionFromJSONString(jsonData, StudentInfoDTO.class, List.class);

        assertThat(students).hasSize(2);
    }

    /**
     * Tests the get student's courses endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @WithMockUser(value = "k11804012", authorities = { AuthoritiesConstants.USER, AuthoritiesConstants.STUDENT })
    public void testGetStudentsCourses() throws Exception {
        var result = restMockMvc.perform(get("/api/student/courses")).andExpect(status().isOk()).andReturn();

        String jsonData = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        List<CourseInstanceInformationDTO> courseInfos = TestUtil.convertCollectionFromJSONString(
            jsonData,
            CourseInstanceInformationDTO.class,
            List.class
        );
        assertThat(courseInfos).hasSize(1);
    }

    /**
     * Tests the get progress information endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @WithMockUser(value = "k11804012", authorities = { AuthoritiesConstants.USER, AuthoritiesConstants.STUDENT })
    public void testGetProgressInformation() throws Exception {
        var result = restMockMvc
            .perform(get("/api/student/courses/{uuid}/progress", courseInstanceUUID))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        List<CourseInstanceProgressOverviewDTO> entries = TestUtil.convertCollectionFromJSONString(
            jsonData,
            CourseInstanceProgressOverviewDTO.class,
            List.class
        );
        assertThat(entries).hasSize(2);
    }
}
