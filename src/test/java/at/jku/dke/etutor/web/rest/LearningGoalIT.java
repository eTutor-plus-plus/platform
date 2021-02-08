package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.RDFTestUtil;
import at.jku.dke.etutor.service.SPARQLEndpointService;
import at.jku.dke.etutor.service.dto.LearningGoalDTO;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link LearningGoalResource} REST controller.
 *
 * @author fne
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = {AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.ADMIN}, username = "admin")
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LearningGoalIT {

    @Autowired
    private RDFConnectionFactory rdfConnectionFactory;

    @Autowired
    private MockMvc restLearningGoalMockMvc;

    @Autowired
    private SPARQLEndpointService sparqlEndpointService;

    /**
     * Init method which is called before the test run.
     */
    @BeforeAll
    public void initBeforeAllTests() {
        rdfConnectionFactory.clearDataset();
    }

    /**
     * Tests the successful creation of a learning goal.
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(1)
    public void testCreateLearningGoalSuccessful() throws Exception {
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setPrivateGoal(true);

        restLearningGoalMockMvc.perform(post("/api/learninggoals")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(newLearningGoalDTO)))
            .andExpect(status().isCreated());

        RDFTestUtil.checkThatSubjectExists("<http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Testziel>", rdfConnectionFactory);
    }

    /**
     * Tests the creation of a duplicate learning which
     * is not allowed.
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(2)
    public void testCreateLearningGoalDuplicate() throws Exception {
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");

        restLearningGoalMockMvc.perform(post("/api/learninggoals")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(newLearningGoalDTO)))
            .andExpect(status().isBadRequest());

        assertThat(RDFTestUtil.getGoalCount(rdfConnectionFactory)).isEqualTo(1);
    }

    /**
     * Tests the successful creation of a sub goal.
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(3)
    public void testCreateSubGoalSuccessful() throws Exception {
        NewLearningGoalDTO subGoal = new NewLearningGoalDTO();
        subGoal.setName("Subgoal");
        subGoal.setPrivateGoal(true);

        restLearningGoalMockMvc.perform(post("/api/learninggoals/admin/Testziel/subGoal")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(subGoal)))
            .andExpect(status().isCreated());

        RDFTestUtil.checkThatSubjectExists("<http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Subgoal>", rdfConnectionFactory);
    }

    /**
     * Tests the creation of sub goal for another user's goal.
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(4)
    public void testCreateSubGoalWrongUser() throws Exception {
        NewLearningGoalDTO subGoal = new NewLearningGoalDTO();
        subGoal.setName("Subgoal1");

        restLearningGoalMockMvc.perform(post("/api/learninggoals/admin1/Testziel/subGoal")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(subGoal)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the creation of a sub goal for a nonexistent goal.
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(5)
    public void testCreateSubGoalForNonexistentGoal() throws Exception {
        NewLearningGoalDTO subGoal = new NewLearningGoalDTO();
        subGoal.setName("Subgoal12");

        restLearningGoalMockMvc.perform(post("/api/learninggoals/admin/Testziel123/subGoal")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(subGoal)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the creation of a duplicate sub goal.
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(6)
    public void testCreateDuplicateSubGoal() throws Exception {
        NewLearningGoalDTO subGoal = new NewLearningGoalDTO();
        subGoal.setName("Subgoal");

        restLearningGoalMockMvc.perform(post("/api/learninggoals/admin/Testziel123/subGoal")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(subGoal)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the successful creation of a goal with another user.
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(7)
    @WithMockUser(authorities = {AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.ADMIN}, username = "admin1")
    public void testCreateGoalWithOtherUser() throws Exception {
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");

        restLearningGoalMockMvc.perform(post("/api/learninggoals")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(newLearningGoalDTO)))
            .andExpect(status().isCreated());

        RDFTestUtil.checkThatSubjectExists("<http://www.dke.uni-linz.ac.at/etutorpp/admin1/Goal#Testziel>", rdfConnectionFactory);
    }

    /**
     * Tests the successful getting of visible learning goals.
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(8)
    @WithMockUser(authorities = {AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.ADMIN}, username = "admin1")
    public void testGetVisibleGoals() throws Exception {
        var mvcResult = restLearningGoalMockMvc.perform(get("/api/learninggoals"))
            .andReturn();

        String jsonData = mvcResult.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        SortedSet<LearningGoalDTO> goals = TestUtil.convertCollectionFromJSONString(jsonData, LearningGoalDTO.class, TreeSet.class);

        assertThat(goals.size()).isEqualTo(1);

        var goal = goals.first();
        assertThat(goal.getSubGoals().size()).isEqualTo(0);
        assertThat(goal.getName()).isEqualTo("Testziel");
    }

    /**
     * Tests the successful update of an existing learning goal.
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(9)
    @WithMockUser(authorities = {AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.ADMIN}, username = "admin1")
    public void testUpdateLearningGoalSuccess() throws Exception {
        var mvcResult = restLearningGoalMockMvc.perform(get("/api/learninggoals"))
            .andReturn();

        String jsonData = mvcResult.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        SortedSet<LearningGoalDTO> goals = TestUtil.convertCollectionFromJSONString(jsonData, LearningGoalDTO.class, TreeSet.class);

        assertThat(goals.size()).isEqualTo(1);

        var goal = goals.first();
        goal.setDescription("NewDescription");
        goal.setPrivateGoal(true);

        restLearningGoalMockMvc.perform(put("/api/learninggoals")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(goal)))
            .andExpect(status().isNoContent())
            .andReturn();

        mvcResult = restLearningGoalMockMvc.perform(get("/api/learninggoals"))
            .andReturn();

        jsonData = mvcResult.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        SortedSet<LearningGoalDTO> secondGoals = TestUtil.convertCollectionFromJSONString(jsonData, LearningGoalDTO.class, TreeSet.class);

        assertThat(secondGoals.size()).isEqualTo(1);

        goal = secondGoals.first();

        assertThat(goal.getDescription()).isEqualTo("NewDescription");
        assertThat(goal.isPrivateGoal()).isTrue();
    }

    /**
     * Tests the update of a nonexistent learning goal.
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(10)
    public void testUpdateLearningGoalNotExist() throws Exception {
        LearningGoalDTO goal = new LearningGoalDTO();
        goal.setId("http://www.test.at");
        goal.setName("Testname");
        goal.setOwner("admin");

        restLearningGoalMockMvc.perform(put("/api/learninggoals")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(goal)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the update of a sub goal whose super is private.
     * The sub goal is set to public; therefore, an exception has to
     * be thrown.
     *
     * @throws Exception must not happen
     */
    @Test
    @Order(11)
    public void testUpdateLearningGoalWithPrivateSuperGoal() throws Exception {
        var mvcResult = restLearningGoalMockMvc.perform(get("/api/learninggoals"))
            .andReturn();

        String jsonData = mvcResult.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        SortedSet<LearningGoalDTO> goals = TestUtil.convertCollectionFromJSONString(jsonData, LearningGoalDTO.class, TreeSet.class);

        var goal = goals.first().getSubGoals().first();
        goal.setPrivateGoal(false);

        restLearningGoalMockMvc.perform(put("/api/learninggoals")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(goal)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the update process of a goal with an user who is not the creator.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Order(12)
    @WithMockUser(authorities = {AuthoritiesConstants.INSTRUCTOR, AuthoritiesConstants.ADMIN}, username = "admin1")
    public void testUpdateLearningGoalWithOtherUser() throws Exception {
        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser("admin", false);
        var goal = goals.first();

        restLearningGoalMockMvc.perform(put("/api/learninggoals")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(goal)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorKey").value("learningGoalNotOwner"));
    }
}
