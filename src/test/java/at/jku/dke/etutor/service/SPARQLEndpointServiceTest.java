package at.jku.dke.etutor.service;

import at.jku.dke.etutor.helper.LocalRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.LearningGoalDTO;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import org.apache.jena.query.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@code SPARQLEndpointService} class.
 *
 * @author fne
 */
public class SPARQLEndpointServiceTest {

    private SPARQLEndpointService sparqlEndpointService;

    private Dataset dataset;
    private RDFConnectionFactory rdfConnectionFactory;

    /**
     * Method which initializes the dataset and endpoint service before each run.
     */
    @BeforeEach
    public void setup() {
        dataset = DatasetFactory.createTxnMem();
        rdfConnectionFactory = new LocalRDFConnectionFactory(dataset);
        sparqlEndpointService = new SPARQLEndpointService(rdfConnectionFactory);

        sparqlEndpointService.insertScheme();
    }

    /**
     * Tests the insertScheme method.
     */
    @Test
    public void testInsertScheme() {
        checkThatExists("etutor:hasOwner");
        checkThatExists("etutor:SubGoal");

        checkThatExists("etutor:hasSubGoal");
        checkThatExists("etutor:dependsOn");
        checkThatExists("etutor:hasDescription");
        checkThatExists("etutor:isPrivate");
        checkThatExists("etutor:isPrivate");
        checkThatExists("etutor:hasChangeDate");
        checkThatExists("etutor:hasOwner");
    }

    /**
     * Tests the successful insertion of a new learning goal
     *
     * @throws LearningGoalAlreadyExistsException must not happen
     * @throws InternalModelException             must not happen
     */
    @Test
    public void testInsertNewLearningGoalSuccess() throws LearningGoalAlreadyExistsException, InternalModelException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        SortedSet<LearningGoalDTO> goals = sparqlEndpointService.getVisibleLearningGoalsForUser("admin");

        assertThat(goals.size()).isEqualTo(1);

        LearningGoalDTO goal = goals.first();

        assertThat(goal.getOwner()).isEqualTo("admin");
        assertThat(goal.getReferencedFromCount()).isEqualTo(0);
        assertThat(goal.getSubGoals().size()).isEqualTo(0);
        assertThat(goal.getId()).isEqualTo("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Testziel");
    }

    /**
     * Tests the insertion of duplicate learning goals which must throw an exception.
     *
     * @throws LearningGoalAlreadyExistsException must not happen
     */
    @Test
    public void testInsertNewLearningGoalDuplicate() throws LearningGoalAlreadyExistsException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        assertThatThrownBy(() -> sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner))
            .isInstanceOf(LearningGoalAlreadyExistsException.class);
    }

    /**
     * Tests the insertion of duplicate learning goals but with different owners.
     * This method has to succeed.
     *
     * @throws LearningGoalAlreadyExistsException must not happen
     */
    @Test
    public void testInsertNewDuplicateLearningGoalWithDifferentOwner() throws LearningGoalAlreadyExistsException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        owner = "admin2";
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        assertThat(getGoalCount()).isEqualTo(2);
        checkThatExists("<http://www.dke.uni-linz.ac.at/etutorpp/admin2/Goal#Testziel>");
    }

    /**
     * Tests the successful insertion of a sub goal.
     *
     * @throws LearningGoalAlreadyExistsException must not happen
     * @throws LearningGoalNotExistsException     must not happen
     */
    @Test
    public void testInsertSubGoalSuccess() throws LearningGoalAlreadyExistsException, LearningGoalNotExistsException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        NewLearningGoalDTO newSubGoalDTO = new NewLearningGoalDTO();
        newSubGoalDTO.setName("Teilziel");
        newSubGoalDTO.setDescription("Test");
        newSubGoalDTO.setPrivateGoal(false);

        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        sparqlEndpointService.insertSubGoal(newSubGoalDTO, owner, newLearningGoalDTO.getName());
        assertThat(getGoalCount()).isEqualTo(2);

        checkThatExists("<http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Teilziel>");
    }

    /**
     * Tests the insertion of a sub goal whose parent goal does not exist.
     */
    @Test
    public void testInsertSubGoalWithParentGoalNotExists() {
        String owner = "admin";
        NewLearningGoalDTO newSubGoalDTO = new NewLearningGoalDTO();
        newSubGoalDTO.setName("Teilziel");
        newSubGoalDTO.setDescription("Test");
        newSubGoalDTO.setPrivateGoal(false);

        assertThatThrownBy(() -> sparqlEndpointService.insertSubGoal(newSubGoalDTO, owner, "Testziel"))
            .isInstanceOf(LearningGoalNotExistsException.class);
    }

    /**
     * Tests the insertion of a sub goal with an already existing goal name.
     *
     * @throws LearningGoalAlreadyExistsException must not be thrown
     */
    @Test
    public void testInsertSubGoalDuplicateGoal() throws LearningGoalAlreadyExistsException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        NewLearningGoalDTO newSubGoalDTO = new NewLearningGoalDTO();
        newSubGoalDTO.setName("Testziel");
        newSubGoalDTO.setDescription("Test");
        newSubGoalDTO.setPrivateGoal(false);

        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        assertThatThrownBy(() -> sparqlEndpointService.insertSubGoal(newSubGoalDTO, owner, newLearningGoalDTO.getName()))
            .isInstanceOf(LearningGoalAlreadyExistsException.class);
    }

    /**
     * Tests the is learning goal private method.
     *
     * @throws LearningGoalAlreadyExistsException must not be thrown
     */
    @Test
    public void testIsLearningGoalPrivate() throws LearningGoalAlreadyExistsException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(true);

        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        assertThat(sparqlEndpointService.isLearningGoalPrivate(owner, newLearningGoalDTO.getName())).isTrue();

        newLearningGoalDTO.setName("Test");
        newLearningGoalDTO.setPrivateGoal(false);

        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        assertThat(sparqlEndpointService.isLearningGoalPrivate(owner, newLearningGoalDTO.getName())).isFalse();
    }

    /**
     * Tests the update learning goal method.
     *
     * @throws LearningGoalAlreadyExistsException must not happen
     * @throws LearningGoalNotExistsException     must not happen
     * @throws InternalModelException             must not happen
     */
    @Test
    public void testUpdateLearningGoal() throws LearningGoalAlreadyExistsException, LearningGoalNotExistsException, InternalModelException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(true);

        LearningGoalDTO learningGoalDTO = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        learningGoalDTO.setDescription("Testbeschreibung");
        learningGoalDTO.setPrivateGoal(false);

        sparqlEndpointService.updateLearningGoal(learningGoalDTO);

        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(owner);

        assertThat(goals.size()).isEqualTo(1);

        var goal = goals.first();

        assertThat(goal.getName()).isEqualTo(learningGoalDTO.getName());
        assertThat(goal.getDescription()).isEqualTo(learningGoalDTO.getDescription());
        assertThat(goal.isPrivateGoal()).isEqualTo(learningGoalDTO.isPrivateGoal());
    }

    /**
     * Tests the update learning goal method with a nonexistent learning goal.
     *
     * @throws LearningGoalAlreadyExistsException must not happen
     */
    @Test
    public void testUpdateNonexistentLearningGoal() throws LearningGoalAlreadyExistsException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(true);

        LearningGoalDTO learningGoalDTO = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        learningGoalDTO.setId("http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#TestzielNonexistant");
        learningGoalDTO.setDescription("Testbeschreibung");
        learningGoalDTO.setPrivateGoal(false);

        assertThatThrownBy(() -> sparqlEndpointService.updateLearningGoal(learningGoalDTO))
            .isInstanceOf(LearningGoalNotExistsException.class);
    }

    //region Private helper methods

    /**
     * Checks that the given subject exists.
     *
     * @param subject the subject to check
     */
    private void checkThatExists(String subject) {
        String query = String.format("""
            prefix etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            ASK {
              ?subject ?predicate ?object.
              FILTER (?subject = %s)
            }
            """, subject);

        try (RDFConnection connection = rdfConnectionFactory.getRDFConnection()) {
            boolean res = connection.queryAsk(query);
            assertThat(res).isTrue();
        }
    }

    /**
     * Returns the number of goals.
     *
     * @return the number of goals
     */
    private int getGoalCount() {
        String query = """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT (COUNT(DISTINCT ?subject) as ?cnt)
            WHERE
            {
              ?subject a etutor:Goal.
            }
            """;
        try (RDFConnection connection = rdfConnectionFactory.getRDFConnection()) {
            try (QueryExecution execution = connection.query(query)) {
                ResultSet set = execution.execSelect();

                QuerySolution solution = set.nextSolution();
                return solution.get("?cnt").asLiteral().getInt();
            }
        }
    }
    //endregion
}
