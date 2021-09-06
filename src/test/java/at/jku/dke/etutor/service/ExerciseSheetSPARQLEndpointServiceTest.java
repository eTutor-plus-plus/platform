package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.LocalRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDisplayDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.LearningGoalAssignmentDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import one.util.streamex.StreamEx;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdfconnection.RDFConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.text.ParseException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the {@code ExerciseSheetSPARQLEndpointService} class.
 *
 * @author fne
 */
public class ExerciseSheetSPARQLEndpointServiceTest {

    private static final String OWNER = "admin";
    private ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService;
    private SPARQLEndpointService sparqlEndpointService;
    private RDFConnectionFactory rdfConnectionFactory;

    /**
     * Method which initializes the dataset and endpoint service before each run.
     *
     * @throws Exception must not be thrown
     */
    @BeforeEach
    public void setup() throws Exception {
        Dataset dataset = DatasetFactory.createTxnMem();
        rdfConnectionFactory = new LocalRDFConnectionFactory(dataset);
        sparqlEndpointService = new SPARQLEndpointService(rdfConnectionFactory);
        exerciseSheetSPARQLEndpointService = new ExerciseSheetSPARQLEndpointService(rdfConnectionFactory);

        sparqlEndpointService.insertScheme();

        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("TestGoal1");
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, OWNER);

        newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("TestGoal2");
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, OWNER);
    }

    /**
     * Tests the insert new exercise sheet method with null values.
     */
    @Test
    public void testInsertNewExerciseSheetNullValues() {
        assertThatThrownBy(() -> exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(null, null))
            .isInstanceOf(NullPointerException.class);

        var newExerciseSheetDTO = new NewExerciseSheetDTO();
        assertThatThrownBy(() -> exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the insert new exercise sheet method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testInsertNewExerciseSheet() throws Exception {
        NewExerciseSheetDTO newExerciseSheetDTO = new NewExerciseSheetDTO();
        newExerciseSheetDTO.setName("Testname");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER, false);
        var displayGoals = StreamEx.of(goals)
            .map(x -> new LearningGoalDisplayDTO(x.getId(), x.getName()))
            .map(x -> new LearningGoalAssignmentDTO(x, 1))
            .toList();

        newExerciseSheetDTO.setLearningGoals(displayGoals);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        assertThat(exerciseSheetDTO.getName()).isEqualTo(newExerciseSheetDTO.getName());
        assertThat(exerciseSheetDTO.getInternalCreator()).isEqualTo(OWNER);
        assertThat(exerciseSheetDTO.getDifficultyId()).isEqualTo(newExerciseSheetDTO.getDifficultyId());

        String id = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        var optionalExerciseSheetFromKG = exerciseSheetSPARQLEndpointService.getExerciseSheetById(id);
        assertThat(optionalExerciseSheetFromKG).isPresent();

        var exerciseSheetFromKG = optionalExerciseSheetFromKG.get();
        assertThat(exerciseSheetFromKG)
            .usingRecursiveComparison()
            .ignoringFields("learningGoals")
            .isEqualTo(exerciseSheetDTO);
        assertThat(exerciseSheetFromKG.getLearningGoals()).hasSize(2);
    }

    /**
     * Tests the get exercise sheet method with an invalid id.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetNonexistentExerciseSheet() throws Exception {
        Optional<ExerciseSheetDTO> optional = exerciseSheetSPARQLEndpointService.getExerciseSheetById("123");

        assertThat(optional).isEmpty();
    }

    /**
     * Tests the get exercise sheet method with a null parameter.
     */
    @Test
    public void testGetExerciseSheetNull() {
        assertThatThrownBy(() -> exerciseSheetSPARQLEndpointService.getExerciseSheetById(null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the update exercise sheet method with a null parameter.
     */
    @Test
    public void testUpdateExerciseSheetException() {
        assertThatThrownBy(() -> exerciseSheetSPARQLEndpointService.updateExerciseSheet(null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the update exercise sheet method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testUpdateExerciseSheet() throws Exception {
        NewExerciseSheetDTO newExerciseSheetDTO = new NewExerciseSheetDTO();
        newExerciseSheetDTO.setName("Testname");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER, false);
        var displayGoals = StreamEx.of(goals)
            .map(x -> new LearningGoalDisplayDTO(x.getId(), x.getName()))
            .map(x -> new LearningGoalAssignmentDTO(x, 1))
            .toList();

        newExerciseSheetDTO.setLearningGoals(displayGoals);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        exerciseSheetDTO.setName("Newtest");
        exerciseSheetDTO.setLearningGoals(StreamEx.of(exerciseSheetDTO.getLearningGoals())
            .map(x -> new LearningGoalAssignmentDTO(x.getLearningGoal(), 2))
            .toList());

        exerciseSheetSPARQLEndpointService.updateExerciseSheet(exerciseSheetDTO);

        String id = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);
        Optional<ExerciseSheetDTO> optionalExerciseSheetFromDb = exerciseSheetSPARQLEndpointService.getExerciseSheetById(id);
        assertThat(optionalExerciseSheetFromDb).isPresent();
        ExerciseSheetDTO exerciseSheetFromDb = optionalExerciseSheetFromDb.get();
        assertThat(exerciseSheetFromDb)
            .usingRecursiveComparison()
            .ignoringFields("learningGoals")
            .isEqualTo(exerciseSheetDTO);
        assertThat(exerciseSheetFromDb.getLearningGoals()).containsExactlyInAnyOrderElementsOf(exerciseSheetDTO.getLearningGoals());
    }

    /**
     * Tests the delete exercise sheet method with a null value.
     */
    @Test
    public void testDeleteExerciseSheetNull() {
        assertThatThrownBy(() -> exerciseSheetSPARQLEndpointService.deleteExerciseSheetById(null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the delete exercise sheet method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testDeleteExerciseSheet() throws Exception {
        NewExerciseSheetDTO newExerciseSheetDTO = new NewExerciseSheetDTO();
        newExerciseSheetDTO.setName("Testname");
        newExerciseSheetDTO.setDifficultyId(ETutorVocabulary.Medium.getURI());
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER, false);
        var displayGoals = StreamEx.of(goals)
            .map(x -> new LearningGoalDisplayDTO(x.getId(), x.getName()))
            .map(x -> new LearningGoalAssignmentDTO(x, 1))
            .toList();

        newExerciseSheetDTO.setLearningGoals(displayGoals);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        String id = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);
        exerciseSheetSPARQLEndpointService.deleteExerciseSheetById(id);

        final String askQry =
            """
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                ASK {
                  ?exerciseSheet a etutor:ExerciseSheet.
                }
                """;

        ParameterizedSparqlString qry = new ParameterizedSparqlString(askQry);
        qry.setIri("?exerciseSheet", exerciseSheetDTO.getId());

        try (RDFConnection connection = rdfConnectionFactory.getRDFConnection()) {
            boolean result = connection.queryAsk(qry.asQuery());
            assertThat(result).isFalse();
        }
    }

    /**
     * Tests the get filtered exercise sheet display dto method with null parameters.
     */
    @Test
    public void testGetFilteredExerciseSheetDisplayDTONull() {
        assertThatThrownBy(() -> exerciseSheetSPARQLEndpointService.getFilteredExerciseSheetDisplayDTOs(null, null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> exerciseSheetSPARQLEndpointService.getFilteredExerciseSheetDisplayDTOs("", null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the get filtered exercise sheet display dto method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetFilteredExerciseSheetDisplayDTO() throws Exception {
        int cnt = insertExerciseSheetsForFulltextSearch();
        PageRequest pageRequest = PageRequest.of(0, cnt - 1);

        Slice<ExerciseSheetDisplayDTO> slice = exerciseSheetSPARQLEndpointService.getFilteredExerciseSheetDisplayDTOs("for", pageRequest);
        assertThat(slice.hasNext()).isFalse();
        assertThat(slice.getContent()).hasSize(1);

        slice = exerciseSheetSPARQLEndpointService.getFilteredExerciseSheetDisplayDTOs("", pageRequest);

        assertThat(slice.hasNext()).isTrue();
        assertThat(slice.getContent()).hasSize(cnt - 1);
    }

    /**
     * Tests the get filtered exercise sheet display DTO as page method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetFilteredExerciseSheetDisplayDTOAsPage() throws Exception {
        int cnt = insertExerciseSheetsForFulltextSearch();
        PageRequest pageRequest = PageRequest.of(0, cnt - 1);

        var page = exerciseSheetSPARQLEndpointService.getFilteredExerciseSheetDisplayDTOsAsPage("for", pageRequest);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);

        page = exerciseSheetSPARQLEndpointService.getFilteredExerciseSheetDisplayDTOsAsPage("", pageRequest);

        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(cnt - 1);
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

        exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
    }
    //endregion
}
