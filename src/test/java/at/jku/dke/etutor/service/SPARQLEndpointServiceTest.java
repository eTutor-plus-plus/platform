package at.jku.dke.etutor.service;

import at.jku.dke.etutor.helper.LocalRDFConnectionFactory;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.CourseDTO;
import at.jku.dke.etutor.service.dto.LearningGoalDTO;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
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
    public void testUpdateLearningGoal() throws LearningGoalAlreadyExistsException,
        LearningGoalNotExistsException, InternalModelException, PrivateSuperGoalException {
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
    public void testUpdateLearningGoalWithSubGoal() throws LearningGoalAlreadyExistsException,
        LearningGoalNotExistsException, PrivateSuperGoalException, InternalModelException {
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

        var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(owner);

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
    public void testUpdatePrivateLearningGoalWithPublicSubGoal() throws LearningGoalAlreadyExistsException,
        LearningGoalNotExistsException {
        String owner = "admin";
        NewLearningGoalDTO newLearningGoalDTO = new NewLearningGoalDTO();
        newLearningGoalDTO.setName("Testziel");
        newLearningGoalDTO.setDescription(null);
        newLearningGoalDTO.setPrivateGoal(true);

        var goal = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, owner);

        NewLearningGoalDTO subGoal = new NewLearningGoalDTO();
        subGoal.setName("SubGoal");
        subGoal.setDescription(null);
        subGoal.setPrivateGoal(true);

        var insertedSubGoal = sparqlEndpointService.insertSubGoal(subGoal, owner, newLearningGoalDTO.getName());
        insertedSubGoal.setPrivateGoal(false);


        assertThatThrownBy(() -> sparqlEndpointService.updateLearningGoal(insertedSubGoal))
            .isInstanceOf(PrivateSuperGoalException.class);
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

        course = sparqlEndpointService.insertNewCourse(course, user);
        RDFTestUtil.checkThatSubjectExists("<http://www.dke.uni-linz.ac.at/etutorpp/Course#TestCourse>", rdfConnectionFactory);

        var courses = sparqlEndpointService.getAllCourses();
        assertThat(courses.size()).isEqualTo(1);
        CourseDTO courseFromService = courses.first();
        assertThat(courseFromService.getName()).isEqualTo("TestCourse");
        assertThat(courseFromService.getCourseType()).isEqualTo("LVA");
        assertThat(courseFromService.getLink().toString()).isEqualTo("https://www.dke.uni-linz.ac.at");
    }

    /**
     * Tests the insertion of null values
     */
    @Test
    public void testInsertNewCourseNull() {
        assertThatThrownBy(() -> sparqlEndpointService.insertNewCourse(null, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sparqlEndpointService.insertNewCourse(new CourseDTO(), null))
            .isInstanceOf(NullPointerException.class);
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
        sparqlEndpointService.deleteCourse(course.getId(), user);
        assertThat(RDFTestUtil.getCourseCount(rdfConnectionFactory)).isEqualTo(0);
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

        assertThatThrownBy(() -> sparqlEndpointService.deleteCourse("testid", "test"))
            .isInstanceOf(CourseNotFoundException.class);

        assertThatThrownBy(() -> sparqlEndpointService.deleteCourse(courseFromService.getId(), "test"))
            .isInstanceOf(CourseNotFoundException.class);

        assertThat(RDFTestUtil.getCourseCount(rdfConnectionFactory)).isEqualTo(1);
    }

    /**
     * Tests the removal of courses with null values.
     */
    @Test
    public void testDeleteCourseNullValues() {
        assertThatThrownBy(() -> sparqlEndpointService.deleteCourse(null, null))
            .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> sparqlEndpointService.deleteCourse("test", null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Tests the update course method with a null value.
     */
    @Test
    public void testUpdateCourseNullValue() {
        assertThatThrownBy(() -> sparqlEndpointService.updateCourse(null))
            .isInstanceOf(NullPointerException.class);
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

        assertThatThrownBy(() -> sparqlEndpointService.updateCourse(course))
            .isInstanceOf(CourseNotFoundException.class);
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
        assertThat(course).isEqualToComparingFieldByField(courseFromService);
    }

    /**
     * Tests the get course method with a null value.
     */
    @Test
    public void testGetCourseNullValue() {
        assertThatThrownBy(() -> sparqlEndpointService.getCourse(null))
            .isInstanceOf(NullPointerException.class);
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
}
