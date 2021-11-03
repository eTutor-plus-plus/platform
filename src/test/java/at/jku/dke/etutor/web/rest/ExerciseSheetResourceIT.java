package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.ExerciseSheetSPARQLEndpointService;
import at.jku.dke.etutor.service.SPARQLEndpointService;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDisplayDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.LearningGoalAssignmentDTO;
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

import java.text.ParseException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the exercise sheet resource endpoint.
 *
 * @author fne
 */
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
    private ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService;

    @Autowired
    private MockMvc restExerciseSheetMockMvc;

    private int fullTextCount;

    /**
     * Init method which is called before the rest run.
     *
     * @throws Exception must not be thrown
     */
    @BeforeAll
    public void initBeforeAllTests() throws Exception {
        rdfConnectionFactory.clearDataset();

        sparqlEndpointService.insertScheme();

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
        newExerciseSheetDTO.setTaskCount(1);
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(USERNAME, false);
        var displayGoals = StreamEx.of(goals).map(x -> new LearningGoalAssignmentDTO(new LearningGoalDisplayDTO(x.getId(), x.getName()), 1)).toList();

        newExerciseSheetDTO.setLearningGoals(displayGoals);

        var result = restExerciseSheetMockMvc
            .perform(
                post("/api/exercise-sheet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(newExerciseSheetDTO))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(newExerciseSheetDTO.getName()))
            .andExpect(jsonPath("$.difficultyId").value(newExerciseSheetDTO.getDifficultyId()))
            .andExpect(jsonPath("$.internalCreator").value(USERNAME))
            .andReturn();

        ExerciseSheetDTO exerciseSheetDTO = TestUtil.convertFromJSONString(
            result.getResponse().getContentAsString(),
            ExerciseSheetDTO.class
        );

        String location = result.getResponse().getHeader("Location");

        assertThat(location).isNotNull().isNotBlank();

        result = restExerciseSheetMockMvc.perform(get(location)).andExpect(status().isOk()).andReturn();

        ExerciseSheetDTO exerciseSheetDTOFromEndpoint = TestUtil.convertFromJSONString(
            result.getResponse().getContentAsString(),
            ExerciseSheetDTO.class
        );

        assertThat(exerciseSheetDTOFromEndpoint)
            .usingRecursiveComparison()
            .ignoringFields("learningGoals")
            .isEqualTo(exerciseSheetDTO);
    }

    /**
     * Tests the get exercise sheet by id method with an invalid id.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(2)
    public void testGetExerciseSheetWithInvalidId() throws Exception {
        restExerciseSheetMockMvc.perform(get("/api/exercise-sheet/123")).andExpect(status().isNotFound());
    }

    /**
     * Tests the update exercise sheet method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(3)
    public void testUpdateExerciseSheet() throws Exception {
        NewExerciseSheetDTO newExerciseSheetDTO = new NewExerciseSheetDTO();
        newExerciseSheetDTO.setName("Testname");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        newExerciseSheetDTO.setTaskCount(2);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, USERNAME);
        exerciseSheetDTO.setName("Newname");
        exerciseSheetDTO.setTaskCount(1);

        restExerciseSheetMockMvc
            .perform(
                put("/api/exercise-sheet")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(exerciseSheetDTO))
            )
            .andExpect(status().isNoContent());

        String id = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        var result = restExerciseSheetMockMvc.perform(get("/api/exercise-sheet/{id}", id)).andExpect(status().isOk()).andReturn();

        ExerciseSheetDTO exerciseSheetDTOFromEndpoint = TestUtil.convertFromJSONString(
            result.getResponse().getContentAsString(),
            ExerciseSheetDTO.class
        );

        assertThat(exerciseSheetDTOFromEndpoint)
            .usingRecursiveComparison()
            .isEqualTo(exerciseSheetDTO);
    }

    /**
     * Tests the removal of an exercise sheet.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(4)
    public void testRemoveExerciseSheet() throws Exception {
        NewExerciseSheetDTO newExerciseSheetDTO = new NewExerciseSheetDTO();
        newExerciseSheetDTO.setName("Testname");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, USERNAME);
        String id = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        restExerciseSheetMockMvc.perform(delete("/api/exercise-sheet/{id}", id)).andExpect(status().isNoContent());

        restExerciseSheetMockMvc.perform(get("/api/exercise-sheet/{id}", id)).andExpect(status().isNotFound());
    }

    /**
     * Tests the get exercise display list endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(5)
    @SuppressWarnings("unchecked")
    public void testGetExerciseDisplayList() throws Exception {
        fullTextCount = insertExerciseSheetsForFulltextSearch();
        int page = 0;
        int size = fullTextCount - 1;
        String nameFilter = "for";

        var result = restExerciseSheetMockMvc
            .perform(get("/api/exercise-sheet/display/sliced?page={page}&size={size}&name={nameFilter}", page, size, nameFilter))
            .andExpect(status().isOk())
            .andReturn();
        String jsonData = result.getResponse().getContentAsString();

        List<ExerciseSheetDisplayDTO> displayList = TestUtil.convertCollectionFromJSONString(
            jsonData,
            ExerciseSheetDisplayDTO.class,
            List.class
        );
        assertThat(displayList).hasSize(1);
        assertThat(result.getResponse().getHeader("X-Has-Next-Page")).isEqualTo("false");

        result =
            restExerciseSheetMockMvc
                .perform(get("/api/exercise-sheet/display/sliced?page={page}&size={size}", page, size))
                .andExpect(status().isOk())
                .andReturn();
        jsonData = result.getResponse().getContentAsString();
        displayList = TestUtil.convertCollectionFromJSONString(jsonData, ExerciseSheetDisplayDTO.class, List.class);

        assertThat(displayList).hasSize(size);
        assertThat(result.getResponse().getHeader("X-Has-Next-Page")).isEqualTo("true");
    }

    /**
     * Tests the get paged exercise display list endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(6)
    @SuppressWarnings("unchecked")
    public void testGetPagedExerciseDisplayList() throws Exception {
        int page = 0;
        int size = fullTextCount - 1;
        String nameFilter = "for";

        var result = restExerciseSheetMockMvc
            .perform(get("/api/exercise-sheet/display/paged?page={page}&size={size}&name={nameFilter}", page, size, nameFilter))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();

        List<ExerciseSheetDisplayDTO> list = TestUtil.convertCollectionFromJSONString(jsonData, ExerciseSheetDisplayDTO.class, List.class);
        assertThat(result.getResponse().getHeader("X-Total-Count")).isEqualTo("1");

        assertThat(list).hasSize(1);

        result =
            restExerciseSheetMockMvc
                .perform(get("/api/exercise-sheet/display/paged?page={page}&size={size}", page, size))
                .andExpect(status().isOk())
                .andReturn();

        jsonData = result.getResponse().getContentAsString();
        list = TestUtil.convertCollectionFromJSONString(jsonData, ExerciseSheetDisplayDTO.class, List.class);

        assertThat(result.getResponse().getHeader("X-Total-Count")).isEqualTo(String.valueOf(fullTextCount + 2));
        assertThat(list).hasSize(size);
    }

    //region Private helper methods

    /**
     * Inserts the test exercises for the fulltext search.
     *
     * @return amount of inserted exercise sheets.
     * @throws Exception must not be thrown
     */
    private int insertExerciseSheetsForFulltextSearch() throws Exception {
        insertExerciseSheetForFulltextSearch("Testheader");
        insertExerciseSheetForFulltextSearch("New header for");
        insertExerciseSheetForFulltextSearch("Beispielaufgabe");
        insertExerciseSheetForFulltextSearch("Aufgabe1");
        insertExerciseSheetForFulltextSearch("Test123");

        return 5;
    }

    /**
     * Inserts a new exercise sheet for fulltext search.
     *
     * @param name the name of the exercise sheet
     * @throws ParseException must not be thrown
     */
    private void insertExerciseSheetForFulltextSearch(String name) throws ParseException {
        NewExerciseSheetDTO newExerciseSheetDTO = new NewExerciseSheetDTO();
        newExerciseSheetDTO.setName(name);
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());

        exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, USERNAME);
    }
    //endregion
}
