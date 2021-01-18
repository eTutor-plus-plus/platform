package at.jku.dke.etutor.service;


import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.LocalRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import one.util.streamex.StreamEx;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        assertThatThrownBy(() -> exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(new NewExerciseSheetDTO(), null))
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
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(OWNER);
        var displayGoals = StreamEx.of(goals).map(x -> new LearningGoalDisplayDTO(x.getId(), x.getName())).toList();

        newExerciseSheetDTO.setLearningGoals(displayGoals);

        ExerciseSheetDTO exerciseSheetDTO = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, OWNER);
        assertThat(exerciseSheetDTO.getName()).isEqualTo(newExerciseSheetDTO.getName());
        assertThat(exerciseSheetDTO.getInternalCreator()).isEqualTo(OWNER);
        assertThat(exerciseSheetDTO.getDifficultyId()).isEqualTo(newExerciseSheetDTO.getDifficultyId());

        String id = exerciseSheetDTO.getId().substring(exerciseSheetDTO.getId().lastIndexOf('#') + 1);

        var optionalExerciseSheetFromKG = exerciseSheetSPARQLEndpointService.getExerciseSheetById(id);
        assertThat(optionalExerciseSheetFromKG).isPresent();

        var exerciseSheetFromKG = optionalExerciseSheetFromKG.get();
        assertThat(exerciseSheetFromKG).isEqualToIgnoringGivenFields(exerciseSheetDTO, "learningGoals");
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
        assertThatThrownBy(() -> exerciseSheetSPARQLEndpointService.getExerciseSheetById(null))
            .isInstanceOf(NullPointerException.class);
    }
}
