package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.SPARQLEndpointService;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import one.util.streamex.StreamEx;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(authorities = {AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.ADMIN}, username = "admin")
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExerciseSheetResourceIT {

    private static final String USERNAME = "admin";

    @Autowired
    private RDFConnectionFactory rdfConnectionFactory;

    @Autowired
    private SPARQLEndpointService sparqlEndpointService;

    @Autowired
    private MockMvc restExerciseSheetMockMvc;

    /**
     * Init method which is called before the rest run.
     *
     * @throws Exception must not be thrown
     */
    @BeforeAll
    public void initBeforeAllTests() throws Exception {
        rdfConnectionFactory.clearDataset();

        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("TestGoal1");
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, USERNAME);

        newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("TestGoal2");
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, USERNAME);
    }

    /**
     * Tests the create exercise sheet endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(1)
    public void testCreateExerciseSheet() throws Exception {
        NewExerciseSheetDTO newExerciseSheetDTO = new NewExerciseSheetDTO();
        newExerciseSheetDTO.setName("Testname");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME);
        var displayGoals = StreamEx.of(goals).map(x -> new LearningGoalDisplayDTO(x.getId(), x.getName())).toList();

        newExerciseSheetDTO.setLearningGoals(displayGoals);

        var result = restExerciseSheetMockMvc.perform(post("/api/exercise-sheet")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(newExerciseSheetDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(newExerciseSheetDTO.getName()))
            .andExpect(jsonPath("$.difficultyId").value(newExerciseSheetDTO.getDifficultyId()))
            .andExpect(jsonPath("$.internalCreator").value(USERNAME))
            .andReturn();

        ExerciseSheetDTO exerciseSheetDTO = TestUtil.convertFromJSONString(result.getResponse().getContentAsString(), ExerciseSheetDTO.class);

        String location = result.getResponse().getHeader("Location");

        assertThat(location).isNotNull();
        assertThat(location).isNotBlank();

        result = restExerciseSheetMockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andReturn();

        ExerciseSheetDTO exerciseSheetDTOFromEndpoint = TestUtil.convertFromJSONString(result.getResponse().getContentAsString(), ExerciseSheetDTO.class);

        assertThat(exerciseSheetDTOFromEndpoint).isEqualToIgnoringGivenFields(exerciseSheetDTO, "learningGoals");
    }

    /**
     * Tests the get exercise sheet by id method with an invalid id.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(2)
    public void testGetExerciseSheetWithInvalidId() throws Exception {
        restExerciseSheetMockMvc.perform(get("/api/exercise-sheet/123"))
            .andExpect(status().isNotFound());
    }
}
