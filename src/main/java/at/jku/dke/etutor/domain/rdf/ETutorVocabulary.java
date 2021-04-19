package at.jku.dke.etutor.domain.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.util.Objects;

/**
 * RDF vocabulary class.
 */
public final class ETutorVocabulary {

    private static final String CLASS_GOAL = "Goal";
    private static final String CLASS_SUB_GOAL = "SubGoal";
    private static final String CLASS_COURSE = "Course";
    private static final String CLASS_TASK_ASSIGNMENT = "TaskAssignment";
    private static final String CLASS_DIFFICULTY_RANKING = "DifficultyRanking";
    private static final String CLASS_EXERCISE_SHEET = "ExerciseSheet";
    private static final String CLASS_COURSE_INSTANCE = "CourseInstance";
    private static final String CLASS_TERM = "Term";
    private static final String CLASS_STUDENT = "Student";
    private static final String CLASS_INDIVIDUAL_TASK_ASSIGNMENT = "IndividualTaskAssignment";

    private static final String PROP_IS_PRIVATE = "isPrivate";
    private static final String PROP_DEPENDS_ON = "dependsOn";
    private static final String PROP_HAS_DESCRIPTION = "hasDescription";
    private static final String PROP_HAS_CHANGE_DATE = "hasChangeDate";
    private static final String PROP_HAS_OWNER = "hasOwner";
    private static final String PROP_HAS_SUB_GOAL = "hasSubGoal";
    private static final String PROP_HAS_REFERENCE_CNT = "hasReferenceCnt";
    private static final String PROP_HAS_ROOT_GOAL = "hasRootGoal";
    private static final String PROP_NEEDS_VERIFICATION_BEFORE_COMPLETION = "needsVerificationBeforeCompletion";

    private static final String PROP_HAS_COURSE_DESCRIPTION = "hasCourseDescription";
    private static final String PROP_HAS_COURSE_LINK = "hasCourseLink";
    private static final String PROP_HAS_COURSE_TYPE = "hasCourseType";
    private static final String PROP_HAS_COURSE_CREATOR = "hasCourseCreator";
    private static final String PROP_HAS_GOAL = "hasGoal";

    private static final String PROP_HAS_INSTANCE_YEAR = "hasInstanceYear";
    private static final String PROP_HAS_TERM = "hasTerm";
    private static final String PROP_HAS_INSTANCE_DESCRIPTION = "hasInstanceDescription";
    private static final String PROP_HAS_INSTANCE_COUNT = "hasInstanceCount";
    private static final String PROP_HAS_COURSE = "hasCourse";
    private static final String PROP_HAS_STUDENT = "hasStudent";
    private static final String PROP_HAS_EXERCISE_SHEET = "hasExerciseSheet";
    private static final String PROP_HAS_INDIVIDUAL_TASK_ASSIGNMENT = "hasIndividualTaskAssignment";
    private static final String PROP_FROM_COURSE_INSTANCE = "fromCourseInstance";
    private static final String PROP_FROM_EXERCISE_SHEET = "fromExerciseSheet";

    private static final String PROP_HAS_TASK_ASSIGNMENT = "hasTaskAssignment";
    private static final String PROP_HAS_TASK_CREATOR = "hasTaskCreator";
    private static final String PROP_HAS_INTERNAL_TASK_CREATOR = "hasInternalTaskCreator";
    private static final String PROP_HAS_TASK_HEADER = "hasTaskHeader";
    private static final String PROP_HAS_TASK_CREATION_DATE = "hasTaskCreationDate";
    private static final String PROP_HAS_TYPICAL_PROCESSING_TIME = "hasTypicalProcessingTime";
    private static final String PROP_HAS_TASK_DIFFICULTY = "hasTaskDifficulty";
    private static final String PROP_HAS_TASK_ORGANISATION_UNIT = "hasTaskOrganisationUnit";
    private static final String PROP_HAS_TASK_URL = "hasTaskUrl";
    private static final String PROP_HAS_TASK_INSTRUCTION = "hasTaskInstruction";
    private static final String PROP_IS_PRIVATE_TASK = "isPrivateTask";
    private static final String PROP_IS_ASSIGNMENT_OF = "isAssignmentOf";

    private static final String PROP_CONTAINS_LEARNING_GOAL = "containsLearningGoal";
    private static final String PROP_HAS_EXERCISE_SHEET_DIFFICULTY = "hasExerciseSheetDifficulty";
    private static final String PROP_HAS_INTERNAL_EXERCISE_SHEET_CREATOR = "hasInternalExerciseSheetCreator";
    private static final String PROP_HAS_EXERCISE_SHEET_CREATION_TIME = "hasExerciseSheetCreationTime";
    private static final String PROP_HAS_EXERCISE_SHEET_TASK_COUNT = "hasExerciseSheetTaskCount";

    private static final String INSTANCE_EASY = "Easy";
    private static final String INSTANCE_MEDIUM = "Medium";
    private static final String INSTANCE_HARD = "Hard";
    private static final String INSTANCE_VERY_HARD = "VeryHard";

    private static final String INSTANCE_WINTER = "Winter";
    private static final String INSTANCE_SUMMER = "Summer";

    /**
     * The namespace of the vocabulary.
     */
    public static final String URI = "http://www.dke.uni-linz.ac.at/etutorpp/";
    /**
     * The namespace of the difficulty types.
     */
    public static final String DIFFICULTY_URI = "http://www.dke.uni-linz.ac.at/etutorpp/DifficultyRanking#";
    /**
     * The namespace of the terms.
     */
    public static final String TERM_URI = "http://www.dke.uni-linz.ac.at/etutorpp/Term#";

    private static final Model m = ModelFactory.createDefaultModel();

    /**
     * The hasDescription property.
     */
    public static final Property hasDescription = m.createProperty(URI + PROP_HAS_DESCRIPTION);
    /**
     * The hasChangeDate property.
     */
    public static final Property hasChangeDate = m.createProperty(URI + PROP_HAS_CHANGE_DATE);
    /**
     * The hasOwner property.
     */
    public static final Property hasOwner = m.createProperty(URI + PROP_HAS_OWNER);
    /**
     * The isPrivate property.
     */
    public static final Property isPrivate = m.createProperty(URI + PROP_IS_PRIVATE);
    /**
     * The dependsOn property.
     */
    public static final Property dependsOn = m.createProperty(URI + PROP_DEPENDS_ON);
    /**
     * The hasSubGoal property.
     */
    public static final Property hasSubGoal = m.createProperty(URI + PROP_HAS_SUB_GOAL);
    /**
     * The hasReferenceCnt property.
     */
    public static final Property hasReferenceCnt = m.createProperty(URI + PROP_HAS_REFERENCE_CNT);
    /**
     * The hasRootGoal property.
     */
    public static final Property hasRootGoal = m.createProperty(URI + PROP_HAS_ROOT_GOAL);
    /**
     * The needsVerificationBeforeCompletion property.
     */
    public static final Property needsVerificationBeforeCompletion = m.createProperty(URI + PROP_NEEDS_VERIFICATION_BEFORE_COMPLETION);
    /**
     * The hasCourseDescription property.
     */
    public static final Property hasCourseDescription = m.createProperty(URI + PROP_HAS_COURSE_DESCRIPTION);
    /**
     * The hasCourseLink property.
     */
    public static final Property hasCourseLink = m.createProperty(URI + PROP_HAS_COURSE_LINK);
    /**
     * The hasCourseType property.
     */
    public static final Property hasCourseType = m.createProperty(URI + PROP_HAS_COURSE_TYPE);
    /**
     * The hasCourseCreator property.
     */
    public static final Property hasCourseCreator = m.createProperty(URI + PROP_HAS_COURSE_CREATOR);
    /**
     * The hasGoal property.
     */
    public static final Property hasGoal = m.createProperty(URI + PROP_HAS_GOAL);
    /**
     * The hasInstanceYear property.
     */
    public static final Property hasInstanceYear = m.createProperty(URI + PROP_HAS_INSTANCE_YEAR);
    /**
     * The hasTerm property.
     */
    public static final Property hasTerm = m.createProperty(URI + PROP_HAS_TERM);
    /**
     * The hasInstanceDescription property.
     */
    public static final Property hasInstanceDescription = m.createProperty(URI + PROP_HAS_INSTANCE_DESCRIPTION);
    /**
     * The hasInstanceCount property.
     */
    public static final Property hasInstanceCount = m.createProperty(URI + PROP_HAS_INSTANCE_COUNT);
    /**
     * The hasCourse property.
     */
    public static final Property hasCourse = m.createProperty(URI + PROP_HAS_COURSE);
    /**
     * The hasStudent property.
     */
    public static final Property hasStudent = m.createProperty(URI + PROP_HAS_STUDENT);
    /**
     * The hasExerciseSheet property.
     */
    public static final Property hasExerciseSheet = m.createProperty(URI + PROP_HAS_EXERCISE_SHEET);
    /**
     * The hasIndividualTaskAssignment property.
     */
    public static final Property hasIndividualTaskAssignment = m.createProperty(URI + PROP_HAS_INDIVIDUAL_TASK_ASSIGNMENT);
    /**
     * The fromCourseInstance property.
     */
    public static final Property fromCourseInstance = m.createProperty(URI + PROP_FROM_COURSE_INSTANCE);
    /**
     * The fromExerciseSheet property.
     */
    public static final Property fromExerciseSheet = m.createProperty(URI + PROP_FROM_EXERCISE_SHEET);
    /**
     * The hasTaskAssignment property.
     */
    public static final Property hasTaskAssignment = m.createProperty(URI + PROP_HAS_TASK_ASSIGNMENT);
    /**
     * The hasTaskCreator property.
     */
    public static final Property hasTaskCreator = m.createProperty(URI + PROP_HAS_TASK_CREATOR);
    /**
     * The hasInternalTaskCreator property.
     */
    public static final Property hasInternalTaskCreator = m.createProperty(URI + PROP_HAS_INTERNAL_TASK_CREATOR);
    /**
     * The hasTaskHeader property.
     */
    public static final Property hasTaskHeader = m.createProperty(URI + PROP_HAS_TASK_HEADER);
    /**
     * The hasTaskCreationDate property.
     */
    public static final Property hasTaskCreationDate = m.createProperty(URI + PROP_HAS_TASK_CREATION_DATE);
    /**
     * The hasTypicalProcessingTime property.
     */
    public static final Property hasTypicalProcessingTime = m.createProperty(URI + PROP_HAS_TYPICAL_PROCESSING_TIME);
    /**
     * The hasTaskDifficulty property.
     */
    public static final Property hasTaskDifficulty = m.createProperty(URI + PROP_HAS_TASK_DIFFICULTY);
    /**
     * The hasTaskOrganisationUnit property.
     */
    public static final Property hasTaskOrganisationUnit = m.createProperty(URI + PROP_HAS_TASK_ORGANISATION_UNIT);
    /**
     * The hasTaskUrl property.
     */
    public static final Property hasTaskUrl = m.createProperty(URI + PROP_HAS_TASK_URL);
    /**
     * The hasTaskInstruction property.
     */
    public static final Property hasTaskInstruction = m.createProperty(URI + PROP_HAS_TASK_INSTRUCTION);
    /**
     * The isPrivateTask property.
     */
    public static final Property isPrivateTask = m.createProperty(URI + PROP_IS_PRIVATE_TASK);
    /**
     * The isAssignmentOf property.
     */
    public static final Property isAssignmentOf = m.createProperty(URI + PROP_IS_ASSIGNMENT_OF);
    /**
     * The containsLearningGoal property.
     */
    public static final Property containsLearningGoal = m.createProperty(URI + PROP_CONTAINS_LEARNING_GOAL);
    /**
     * The hasExerciseSheetDifficulty property.
     */
    public static final Property hasExerciseSheetDifficulty = m.createProperty(URI + PROP_HAS_EXERCISE_SHEET_DIFFICULTY);
    /**
     * The hasInternalExerciseSheetCreator property.
     */
    public static final Property hasInternalExerciseSheetCreator = m.createProperty(URI + PROP_HAS_INTERNAL_EXERCISE_SHEET_CREATOR);
    /**
     * The hasExerciseSheetCreationTime property.
     */
    public static final Property hasExerciseSheetCreationTime = m.createProperty(URI + PROP_HAS_EXERCISE_SHEET_CREATION_TIME);
    /**
     * The hasExerciseSheetTaskCount property.
     */
    public static final Property hasExerciseSheetTaskCount = m.createProperty(URI + PROP_HAS_EXERCISE_SHEET_TASK_COUNT);

    /**
     * The goal resource.
     */
    public static final Resource Goal = m.createResource(URI + CLASS_GOAL);
    /**
     * The sub goal resource.
     */
    public static final Resource SubGoal = m.createResource(URI + CLASS_SUB_GOAL);
    /**
     * The course resource.
     */
    public static final Resource Course = m.createResource(URI + CLASS_COURSE);
    /**
     * The task assignment resource.
     */
    public static final Resource TaskAssignment = m.createResource(URI + CLASS_TASK_ASSIGNMENT);
    /**
     * The difficulty ranking resource.
     */
    public static final Resource DifficultyRanking = m.createResource(URI + CLASS_DIFFICULTY_RANKING);
    /**
     * The exercise sheet resource.
     */
    public static final Resource ExerciseSheet = m.createResource(URI + CLASS_EXERCISE_SHEET);
    /**
     * The course instance resource.
     */
    public static final Resource CourseInstance = m.createResource(URI + CLASS_COURSE_INSTANCE);
    /**
     * The term resource.
     */
    public static final Resource Term = m.createResource(URI + CLASS_TERM);
    /**
     * The student resource.
     */
    public static final Resource Student = m.createResource(URI + CLASS_STUDENT);
    /**
     * The individual task assignment resource.
     */
    public static final Resource IndividualTaskAssignment = m.createResource(URI + CLASS_INDIVIDUAL_TASK_ASSIGNMENT);

    /**
     * The easy difficulty instance.
     */
    public static final Resource Easy = m.createResource(DIFFICULTY_URI + INSTANCE_EASY);
    /**
     * The medium difficulty instance.
     */
    public static final Resource Medium = m.createResource(DIFFICULTY_URI + INSTANCE_MEDIUM);
    /**
     * The hard difficulty instance.
     */
    public static final Resource Hard = m.createResource(DIFFICULTY_URI + INSTANCE_HARD);
    /**
     * The very hard difficulty instance.
     */
    public static final Resource VeryHard = m.createProperty(DIFFICULTY_URI + INSTANCE_VERY_HARD);


    private static final String TERM_WINTER_URI = TERM_URI + INSTANCE_WINTER;
    private static final String TERM_SUMMER_URI = TERM_URI + INSTANCE_SUMMER;

    /**
     * The winter term instance.
     */
    public static final Resource Winter = m.createResource(TERM_WINTER_URI);
    /**
     * The summer term instance.
     */
    public static final Resource Summer = m.createResource(TERM_SUMMER_URI);

    /**
     * Creates an individual goal resource from a given model.
     *
     * @param userLogin the login of the user
     * @param goalName  the rdf encoded goal name
     * @param model     the base model of the resource
     * @return the individual goal resource
     */
    public static final Resource createUserGoalResourceOfModel(String userLogin, String goalName, Model model) {
        return model.createResource(URI + userLogin + "/" + CLASS_GOAL + "#" + goalName);
    }

    /**
     * Creates an individual course resource from a given model.
     *
     * @param courseName the  rdf encoded name of the course
     * @param model      the base model of the resource
     * @return the individual course resource
     */
    public static final Resource createCourseResourceOfModel(String courseName, Model model) {
        return model.createResource(createCourseURL(courseName));
    }

    /**
     * Returns the course url from a given course name
     *
     * @param courseName the course's name
     * @return the course url
     */
    public static final String createCourseURL(String courseName) {
        return URI + CLASS_COURSE + "#" + courseName;
    }

    /**
     * Creates an individual task assignment from a given model.
     *
     * @param uuid  the uuid
     * @param model the base model
     * @return the individual task assignment resource
     */
    public static final Resource createTaskAssignmentResourceOfModel(String uuid, Model model) {
        return model.createResource(URI + CLASS_TASK_ASSIGNMENT + "#" + uuid);
    }

    /**
     * Creates an individual exercise sheet from a given model.
     *
     * @param uuid  the uuid
     * @param model the base model
     * @return the individual exercise sheet resource
     */
    public static final Resource createExerciseSheetOfModel(String uuid, Model model) {
        return model.createResource(createExerciseSheetURLString(uuid));
    }

    /**
     * Creates the url of an exercise sheet.
     *
     * @param uuid the uuid
     * @return the internal url of an individual exercise sheet
     */
    public static final String createExerciseSheetURLString(String uuid) {
        return URI + CLASS_EXERCISE_SHEET + "#" + uuid;
    }

    /**
     * Creates the url of a course instance.
     *
     * @param uuid the uuid
     * @return the internal url of a course instance
     */
    public static final String createCourseInstanceURLString(String uuid) {
        return URI + CLASS_COURSE_INSTANCE + "#" + uuid;
    }

    /**
     * Creates an individual course instance from a given model.
     *
     * @param uuid  the uuid
     * @param model the base model
     * @return the individual course instance
     */
    public static final Resource createCourseInstanceOfModel(String uuid, Model model) {
        return model.createResource(createCourseInstanceURLString(uuid));
    }

    /**
     * Returns the term text from a given uri. If the uri does not
     * represent a term, {@code null} will be returned.
     *
     * @param termUri the term uri, must not be null
     * @return string representation, or {@code null}
     */
    public static final String getTermTextFromUri(String termUri) {
        Objects.requireNonNull(termUri);

        return switch (termUri) {
            case TERM_WINTER_URI -> "Wintersemester";
            case TERM_SUMMER_URI -> "Sommersemester";
            default -> null;
        };
    }

    /**
     * Returns the student url from a given matriculation number.
     *
     * @param matriculationNumber the student's matriculation number
     * @return student url based on the given matriculation number
     */
    public static final String getStudentURLFromMatriculationNumber(String matriculationNumber) {
        return URI + CLASS_STUDENT + "#" + matriculationNumber;
    }

    /**
     * Constructor.
     */
    private ETutorVocabulary() {
        throw new IllegalStateException("Utility class");
    }
}
