package at.jku.dke.etutor.service;

import at.jku.dke.etutor.helper.LocalRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.*;
import at.jku.dke.etutor.service.exception.*;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the {@code SPARQLEndpointService} class.
 *
 * @author fne
 */
public class SPARQLEndpointServiceTest {

    private SPARQLEndpointService sparqlEndpointService;

    private RDFConnectionFactory rdfConnectionFactory;

    /**
     * Method which initializes the dataset and endpoint service before each run.
     */
    @BeforeEach
    public void setup() {
        Dataset dataset = DatasetFactory.createTxnMem();
        rdfConnectionFactory = new LocalRDFConnectionFactory(dataset);
        sparqlEndpointService = new SPARQLEndpointService(rdfConnectionFactory);

        sparqlEndpointService.insertScheme();
    }

    /**
     * Tests the insertScheme method.
     */
    @Test
    public void testInsertScheme() {
        RDFTestUtil.checkThatSubjectExists("etutor:hasOwner", rdfConnectionFactory);
        RDFTestUtil.checkThatSubjectExists("etutor:SubGoal", rdfConnectionFactory);

        RDFTestUtil.checkThatSubjectExists("etutor:hasSubGoal", rdfConnectionFactory);
        RDFTestUtil.checkThatSubjectExists("etutor:dependsOn", rdfConnectionFactory);
        RDFTestUtil.checkThatSubjectExists("etutor:hasDescription", rdfConnectionFactory);
        RDFTestUtil.checkThatSubjectExists("etutor:isPrivate", rdfConnectionFactory);
        RDFTestUtil.checkThatSubjectExists("etutor:isPrivate", rdfConnectionFactory);
        RDFTestUtil.checkThatSubjectExists("etutor:hasChangeDate", rdfConnectionFactory);
        RDFTestUtil.checkThatSubjectExists("etutor:hasOwner", rdfConnectionFactory);
    }

    //region Learning goals

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

        SortedSet<LearningGoalDTO> goals = sparqlEndpointService.getVisibleLearningGoalsForUser("admin", false);

        assertThat(goals.size()).isEqualTo(1);

        LearningGoalDTO goal = goals.first();

        assertThat(goal.getOwner()).isEqualTo("admin");
        assertThat(goal.getReferencedFromCount()).isZero();
        assertThat(goal.getSubGoals().size()).isZero();
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

        assertThat(RDFTestUtil.getGoalCount(rdfConnectionFactory)).isEqualTo(2);
        RDFTestUtil.checkThatSubjectExists("<http://www.dke.uni-linz.ac.at/etutorpp/admin2/Goal#Testziel>", rdfConnectionFactory);
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
        assertThat(RDFTestUtil.getGoalCount(rdfConnectionFactory)).isEqualTo(2);

        RDFTestUtil.checkThatSubjectExists("<http://www.dke.uni-linz.ac.at/etutorpp/admin/Goal#Teilziel>", rdfConnectionFactory);
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
     * @throws PrivateSuperGoalException          must not happen
     */
    @Test
    public void testUpdateLearningGoal()
        throws LearningGoalAlreadyExistsException, LearningGoalNotExistsException, InternalModelException, PrivateSuperGoalException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(true);

        LearningGoalDTO learningGoalDTO = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        learningGoalDTO.setDescription("Testbeschreibung");
        learningGoalDTO.setPrivateGoal(false);

        sparqlEndpointService.updateLearningGoal(learningGoalDTO);

        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(owner, false);

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

    /**
     * Tests the update goal method with a goal which has a sub goal. The goal
     * will be set to a private goal; therefore, the sub goal has also to become
     * a private goal.
     *
     * @throws LearningGoalAlreadyExistsException must not happen
     * @throws LearningGoalNotExistsException     must not happen
     * @throws PrivateSuperGoalException          must not happen
     * @throws InternalModelException             must not happen
     */
    @Test
    public void testUpdateLearningGoalWithSubGoal()
        throws LearningGoalAlreadyExistsException, LearningGoalNotExistsException, PrivateSuperGoalException, InternalModelException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        var goal = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        NewLearningGoalDTO subGoal = new NewLearningGoalDTO();
        subGoal.setName("SubGoal");
        subGoal.setDescription(null);
        subGoal.setPrivateGoal(false);

        sparqlEndpointService.insertSubGoal(subGoal, owner, newLearningGoalDTO.getName());

        goal.setPrivateGoal(true);
        sparqlEndpointService.updateLearningGoal(goal);

        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(owner, false);

        assertThat(goals.size()).isEqualTo(1);
        goal = goals.first();
        assertThat(goal.getSubGoals().size()).isEqualTo(1);
        assertThat(goal.isPrivateGoal()).isTrue();
        assertThat(goal.getSubGoals().first().isPrivateGoal()).isTrue();
    }

    /**
     * Tests the update of a sub goal whose super is private.
     * The sub goal is set to public; therefore, an exception has to
     * be thrown.
     *
     * @throws LearningGoalAlreadyExistsException must not happen
     * @throws LearningGoalNotExistsException     must not happen
     */
    @Test
    public void testUpdatePrivateLearningGoalWithPublicSubGoal() throws LearningGoalAlreadyExistsException, LearningGoalNotExistsException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(true);

        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        NewLearningGoalDTO subGoal = new NewLearningGoalDTO();
        subGoal.setName("SubGoal");
        subGoal.setDescription(null);
        subGoal.setPrivateGoal(true);

        var insertedSubGoal = sparqlEndpointService.insertSubGoal(subGoal, owner, newLearningGoalDTO.getName());
        insertedSubGoal.setPrivateGoal(false);

        assertThatThrownBy(() -> sparqlEndpointService.updateLearningGoal(insertedSubGoal)).isInstanceOf(PrivateSuperGoalException.class);
    }

    /**
     * Tests the get visible learning goals for user method with the option
     * which only shows the learning goal of the currently logged-in user.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetVisibleLearningGoalsForUser() throws Exception {
        String newOwner = "test123";
        String owner = "admin";

        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, newOwner);
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        newLearningGoalDTO.setName("Testziel1");
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, newOwner);
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        newLearningGoalDTO.setName("Testziel2");
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, newOwner);
        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        var resultList = sparqlEndpointService.getVisibleLearningGoalsForUser(newOwner, true);
        assertThat(resultList).hasSize(3);

        resultList = sparqlEndpointService.getVisibleLearningGoalsForUser(newOwner, false);
        assertThat(resultList).hasSize(6);
    }

    /**
     * Tests the set dependency method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testSetDependencies() throws Exception {
        String owner = "admin";

        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        List<String> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();

        String mainGoalName = "Testziel";

        sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        for (int i = 1; i <= 5; i++) {
            String name = "Test " + i;
            names.add(name);
            newLearningGoalDTO.setName(name);
            ids.add(sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner).getId());
        }

        sparqlEndpointService.setDependencies(owner, mainGoalName, ids);

        List<String> dependenciesFromService = sparqlEndpointService.getDependencies(owner, mainGoalName);
        assertThat(dependenciesFromService).containsExactlyInAnyOrderElementsOf(ids);

        List<String> dependencyNames = sparqlEndpointService.getDisplayableDependencies(owner, mainGoalName);
        assertThat(dependencyNames).containsExactlyInAnyOrderElementsOf(names);

        sparqlEndpointService.setDependencies(owner, mainGoalName, new ArrayList<>());
        dependenciesFromService = sparqlEndpointService.getDependencies(owner, mainGoalName);
        assertThat(dependenciesFromService).isEmpty();

        dependencyNames = sparqlEndpointService.getDisplayableDependencies(owner, mainGoalName);
        assertThat(dependencyNames).isEmpty();
    }

    /**
     * Tests the remove learning goal and sub goals method with null values.
     */
    @Test
    public void testRemoveLearningGoalAndSubGoalsNullValues() {
        assertThatThrownBy(() -> sparqlEndpointService.removeLearningGoalAndSubGoals(null, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> sparqlEndpointService.removeLearningGoalAndSubGoals("admin", null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the remove learning goal and sub goals method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testRemoveLearningGoalAndSubGoals() throws Exception {
        var newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testgoal1");
        var firstGoal = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, "admin");

        newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testgoal2");
        var secondGoal = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, "admin");

        newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Subgoal1");

        var subGoal = sparqlEndpointService.insertSubGoal(newLearningGoalDTO, "admin", firstGoal.getName());

        sparqlEndpointService.removeLearningGoalAndSubGoals("admin", firstGoal.getName());

        var visibleGoals = sparqlEndpointService.getVisibleLearningGoalsForUser("admin", true);
        assertThat(visibleGoals).hasSize(1);
        assertThat(visibleGoals.first().getId()).isEqualTo(secondGoal.getId());
    }

    /**
     * Tests the removal of a nonexistent learning goal.
     */
    @Test
    public void testRemoveNonexistentLearningGoal() {
        assertThatThrownBy(() -> sparqlEndpointService.removeLearningGoalAndSubGoals("admin", "NonexistentGoal"))
            .isInstanceOf(LearningGoalNotExistsException.class);
    }

    //endregion

    //region Courses

    /**
     * Tests the successful insertion of a course.
     *
     * @throws CourseAlreadyExistsException must not be thrown
     * @throws MalformedURLException        must not be thrown
     */
    @Test
    public void testInsertNewCourseSuccess() throws CourseAlreadyExistsException, MalformedURLException {
        String user = "admin";
        CourseDTO course = new CourseDTO();
        course.setName("TestCourse");
        course.setCourseType("LVA");
        course.setLink(new URL("https://www.dke.uni-linz.ac.at"));

        sparqlEndpointService.insertNewCourse(course, user);
        RDFTestUtil.checkThatSubjectExists("<http://www.dke.uni-linz.ac.at/etutorpp/Course#TestCourse>", rdfConnectionFactory);

        var courses = sparqlEndpointService.getAllCourses();
        assertThat(courses.size()).isEqualTo(1);
        CourseDTO courseFromService = courses.first();
        assertThat(courseFromService.getName()).isEqualTo("TestCourse");
        assertThat(courseFromService.getCourseType()).isEqualTo("LVA");
        assertThat(courseFromService.getLink()).hasToString("https://www.dke.uni-linz.ac.at");
    }

    /**
     * Tests the insertion of null values
     */
    @Test
    public void testInsertNewCourseNull() {
        assertThatThrownBy(() -> sparqlEndpointService.insertNewCourse(null, null)).isInstanceOf(NullPointerException.class);

        var courseDTO = new CourseDTO();
        assertThatThrownBy(() -> sparqlEndpointService.insertNewCourse(courseDTO, null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the insertion of an already existing course.
     *
     * @throws CourseAlreadyExistsException must not be thrown
     */
    @Test
    public void testInsertAlreadyExistingCourse() throws CourseAlreadyExistsException {
        String user = "admin";
        CourseDTO course = new CourseDTO();
        course.setName("TestCourse");
        course.setCourseType("LVA");

        sparqlEndpointService.insertNewCourse(course, user);

        assertThatThrownBy(() -> sparqlEndpointService.insertNewCourse(course, "testuser"))
            .isInstanceOf(CourseAlreadyExistsException.class);
    }

    /**
     * Tests the successful removal of an existing course.
     *
     * @throws CourseAlreadyExistsException must not be thrown
     * @throws CourseNotFoundException      must not be thrown
     */
    @Test
    public void testDeleteCourseSuccess() throws CourseAlreadyExistsException, CourseNotFoundException {
        String user = "admin";
        CourseDTO course = new CourseDTO();
        course.setName("TestCourse");
        course.setCourseType("LVA");

        course = sparqlEndpointService.insertNewCourse(course, user);
        assertThat(RDFTestUtil.getCourseCount(rdfConnectionFactory)).isEqualTo(1);
        sparqlEndpointService.deleteCourse(course.getNameForRDF(), user);
        assertThat(RDFTestUtil.getCourseCount(rdfConnectionFactory)).isZero();
    }

    /**
     * Tests the removal of a nonexistent course.
     *
     * @throws CourseAlreadyExistsException must not be thrown
     */
    @Test
    public void testDeleteNonexistentCourse() throws CourseAlreadyExistsException {
        String user = "admin";
        CourseDTO course = new CourseDTO();
        course.setName("TestCourse");
        course.setCourseType("LVA");

        var courseFromService = sparqlEndpointService.insertNewCourse(course, user);

        assertThatThrownBy(() -> sparqlEndpointService.deleteCourse("testid", "test")).isInstanceOf(CourseNotFoundException.class);

        assertThatThrownBy(() -> sparqlEndpointService.deleteCourse(courseFromService.getNameForRDF(), "test"))
            .isInstanceOf(CourseNotFoundException.class);

        assertThat(RDFTestUtil.getCourseCount(rdfConnectionFactory)).isEqualTo(1);
    }

    /**
     * Tests the removal of courses with null values.
     */
    @Test
    public void testDeleteCourseNullValues() {
        assertThatThrownBy(() -> sparqlEndpointService.deleteCourse(null, null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sparqlEndpointService.deleteCourse("test", null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the update course method with a null value.
     */
    @Test
    public void testUpdateCourseNullValue() {
        assertThatThrownBy(() -> sparqlEndpointService.updateCourse(null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the modification of a nonexistent course.
     */
    @Test
    public void testUpdateOfNonexistentCourse() {
        CourseDTO course = new CourseDTO();
        course.setName("TestCourse");
        course.setCourseType("LVA");
        course.setId("https://www.test.at");

        assertThatThrownBy(() -> sparqlEndpointService.updateCourse(course)).isInstanceOf(CourseNotFoundException.class);
    }

    /**
     * Tests the successful modification of a course.
     *
     * @throws CourseAlreadyExistsException must not be thrown
     * @throws CourseNotFoundException      must not be thrown
     * @throws MalformedURLException        must not be thrown
     */
    @Test
    public void testUpdateCourseSuccess() throws CourseAlreadyExistsException, CourseNotFoundException, MalformedURLException {
        String user = "admin";
        CourseDTO course = new CourseDTO();
        course.setName("TestCourse");
        course.setCourseType("LVA");

        var courseFromService = sparqlEndpointService.insertNewCourse(course, user);

        courseFromService.setDescription("Testbeschreibung");
        courseFromService.setCourseType("Kurs");
        courseFromService.setLink(new URL("https://www.dke.uni-linz.ac.at"));

        sparqlEndpointService.updateCourse(courseFromService);

        assertThat(RDFTestUtil.getCourseCount(rdfConnectionFactory)).isEqualTo(1);

        var courses = sparqlEndpointService.getAllCourses();
        course = courses.first();
        assertThat(course)
            .usingRecursiveComparison()
            .isEqualTo(courseFromService);
    }

    /**
     * Tests the get course method with a null value.
     */
    @Test
    public void testGetCourseNullValue() {
        assertThatThrownBy(() -> sparqlEndpointService.getCourse(null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the get course method with a nonexistent course.
     */
    @Test
    public void testGetCourseWithNonexistentCourse() {
        var course = sparqlEndpointService.getCourse("Testcourse");

        assertThat(course).isEmpty();
    }

    /**
     * Tests the get course method with an existing course.
     *
     * @throws CourseAlreadyExistsException must not be thrown
     */
    @Test
    public void testGetCourseWithExistingCourse() throws CourseAlreadyExistsException {
        String user = "admin";
        CourseDTO course = new CourseDTO();
        course.setName("TestCourse");
        course.setCourseType("LVA");

        var courseFromService = sparqlEndpointService.insertNewCourse(course, user);

        Optional<CourseDTO> optionalCourse = sparqlEndpointService.getCourse(course.getNameForRDF());
        assertThat(optionalCourse).isPresent();
        course = optionalCourse.get();
        assertThat(course.getName()).isEqualTo(courseFromService.getName());
        assertThat(course.getCourseType()).isEqualTo(courseFromService.getCourseType());
        assertThat(course.getId()).isEqualTo(courseFromService.getId());
    }

    //endregion

    //region Learning goal assignment

    /**
     * Tests the add goal assignment method.
     *
     * @throws LearningGoalAlreadyExistsException           must not be thrown
     * @throws CourseAlreadyExistsException                 must not be thrown
     * @throws LearningGoalAssignmentAlreadyExistsException must not be thrown
     * @throws CourseNotFoundException                      must not be thrown
     */
    @Test
    public void testAddGoalAssignment()
        throws LearningGoalAlreadyExistsException, CourseAlreadyExistsException, LearningGoalAssignmentAlreadyExistsException, CourseNotFoundException, InternalModelException {
        String owner = "admin";
        CourseDTO course = new CourseDTO();
        course.setName("TestCourse");
        course.setCourseType("LVA");

        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        var goal = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);
        course = sparqlEndpointService.insertNewCourse(course, owner);

        LearningGoalAssignmentDTO learningGoalAssignmentDTO = new LearningGoalAssignmentDTO();
        learningGoalAssignmentDTO.setCourseId(course.getId());
        learningGoalAssignmentDTO.setLearningGoalId(goal.getId());

        sparqlEndpointService.addGoalAssignment(learningGoalAssignmentDTO);

        var associatedGoals = sparqlEndpointService.getLearningGoalsForCourse(course.getName());
        assertThat(associatedGoals).isNotEmpty();
        assertThat(associatedGoals.size()).isEqualTo(1);
    }

    /**
     * Tests the insertion of an already existing goal assignment.
     *
     * @throws LearningGoalAlreadyExistsException           must not be thrown
     * @throws CourseAlreadyExistsException                 must not be thrown
     * @throws LearningGoalAssignmentAlreadyExistsException must not be thrown
     */
    @Test
    public void testAddGoalAssignmentWithAnAlreadyExistentAssignment()
        throws LearningGoalAlreadyExistsException, CourseAlreadyExistsException, LearningGoalAssignmentAlreadyExistsException {
        String owner = "admin";
        CourseDTO course = new CourseDTO();
        course.setName("TestCourse");
        course.setCourseType("LVA");

        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        var goal = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);
        course = sparqlEndpointService.insertNewCourse(course, owner);

        LearningGoalAssignmentDTO learningGoalAssignmentDTO = new LearningGoalAssignmentDTO();
        learningGoalAssignmentDTO.setCourseId(course.getId());
        learningGoalAssignmentDTO.setLearningGoalId(goal.getId());

        sparqlEndpointService.addGoalAssignment(learningGoalAssignmentDTO);

        assertThatThrownBy(() -> sparqlEndpointService.addGoalAssignment(learningGoalAssignmentDTO))
            .isInstanceOf(LearningGoalAssignmentAlreadyExistsException.class);
    }

    /**
     * Tests the getting of learning goals from a nonexistent course.
     */
    @Test
    public void testGetLearningGoalsForANonExistentCourse() {
        assertThatThrownBy(() -> sparqlEndpointService.getLearningGoalsForCourse("NonExistentCourse"))
            .isInstanceOf(CourseNotFoundException.class);
    }

    /**
     * Tests the removal of a nonexistent learning goal assignment.
     */
    @Test
    public void testRemoveNonExistentLearningGoalAssignment() {
        LearningGoalAssignmentDTO learningGoalAssignmentDTO = new LearningGoalAssignmentDTO();
        learningGoalAssignmentDTO.setCourseId("http://www.test.at/nonexistent");
        learningGoalAssignmentDTO.setLearningGoalId("http://www.test123.at/nonexistent");

        assertThatThrownBy(() -> sparqlEndpointService.removeGoalAssignment(learningGoalAssignmentDTO))
            .isInstanceOf(LearningGoalAssignmentNonExistentException.class);
    }

    /**
     * Tests the removal of a goal assignment.
     *
     * @throws LearningGoalAlreadyExistsException           must not be thrown
     * @throws CourseAlreadyExistsException                 must not be thrown
     * @throws LearningGoalAssignmentAlreadyExistsException must not be thrown
     * @throws CourseNotFoundException                      must not be thrown
     * @throws InternalModelException                       must not be thrown
     * @throws LearningGoalAssignmentNonExistentException   must not be thrown
     */
    @Test
    public void testRemoveGoalAssignment()
        throws LearningGoalAlreadyExistsException, CourseAlreadyExistsException, LearningGoalAssignmentAlreadyExistsException, CourseNotFoundException, InternalModelException, LearningGoalAssignmentNonExistentException {
        String owner = "admin";
        CourseDTO course = new CourseDTO();
        course.setName("TestCourse");
        course.setCourseType("LVA");

        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        var goal = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);
        course = sparqlEndpointService.insertNewCourse(course, owner);

        LearningGoalAssignmentDTO learningGoalAssignmentDTO = new LearningGoalAssignmentDTO();
        learningGoalAssignmentDTO.setCourseId(course.getId());
        learningGoalAssignmentDTO.setLearningGoalId(goal.getId());

        sparqlEndpointService.addGoalAssignment(learningGoalAssignmentDTO);

        var associatedGoals = sparqlEndpointService.getLearningGoalsForCourse(course.getName());
        assertThat(associatedGoals).isNotEmpty();
        assertThat(associatedGoals.size()).isEqualTo(1);

        sparqlEndpointService.removeGoalAssignment(learningGoalAssignmentDTO);
        associatedGoals = sparqlEndpointService.getLearningGoalsForCourse(course.getName());
        assertThat(associatedGoals).isEmpty();
    }

    /**
     * Tests the set goal assignment method with null and empty values.
     */
    @Test
    public void testSetGoalAssignmentException() {
        LearningGoalUpdateAssignmentDTO learningGoalUpdateAssignmentDTO = new LearningGoalUpdateAssignmentDTO();

        assertThatThrownBy(() -> sparqlEndpointService.setGoalAssignment(null)).isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sparqlEndpointService.setGoalAssignment(learningGoalUpdateAssignmentDTO))
            .isInstanceOf(NullPointerException.class);
        learningGoalUpdateAssignmentDTO.setCourseId("http://www.test.at");
    }

    /**
     * Tests the set goal assignment method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testSetGoalAssignment() throws Exception {
        String owner = "admin";
        CourseDTO course = new CourseDTO();
        course.setName("TestCourse");
        course.setCourseType("LVA");

        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(false);

        var goal = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);
        course = sparqlEndpointService.insertNewCourse(course, owner);

        LearningGoalUpdateAssignmentDTO learningGoalUpdateAssignmentDTO = new LearningGoalUpdateAssignmentDTO();
        learningGoalUpdateAssignmentDTO.setCourseId(course.getId());
        learningGoalUpdateAssignmentDTO.getLearningGoalIds().add(goal.getId());

        sparqlEndpointService.setGoalAssignment(learningGoalUpdateAssignmentDTO);

        var assignedGoals = sparqlEndpointService.getLearningGoalsForCourse(course.getName());
        assertThat(assignedGoals).isNotEmpty().hasSize(1);

        var assignedGoal = assignedGoals.first();
        assertThat(assignedGoal.getId()).isEqualTo(goal.getId());
    }
    //endregion
}
