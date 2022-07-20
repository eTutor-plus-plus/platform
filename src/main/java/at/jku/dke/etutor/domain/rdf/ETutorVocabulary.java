package at.jku.dke.etutor.domain.rdf;

import liquibase.pro.packaged.S;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private static final String CLASS_TASK_ASSIGNMENT_TYPE = "TaskAssignmentType";
    private static final String CLASS_EXERCISE_SHEET = "ExerciseSheet";
    private static final String CLASS_COURSE_INSTANCE = "CourseInstance";
    private static final String CLASS_TERM = "Term";
    private static final String CLASS_STUDENT = "Student";
    private static final String CLASS_INDIVIDUAL_TASK_ASSIGNMENT = "IndividualTaskAssignment";
    private static final String CLASS_INDIVIDUAL_TASK = "IndividualTask";
    private static final String CLASS_INDIVIDUAL_TASK_SUBMISSION = "IndividualTaskSubmission";
    private static final String CLASS_TASK_GROUP = "TaskGroup";
    private static final String CLASS_LEARNING_GOAL_ASSIGNMENT = "LearningGoalAssignment";

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
    private static final String PROP_HAS_INDIVIDUAL_TASK = "hasIndividualTask";
    private static final String PROP_HAS_INDIVIDUAL_TASK_SUBMISSION = "hasIndividualTaskSubmission";
    private static final String PROP_IS_CLOSED = "isClosed";
    private static final String PROP_IS_GRADED = "isGraded";
    private static final String PROP_IS_SUBMITTED = "isSubmitted";
    private static final String PROP_REFERS_TO_TASK = "refersToTask";
    private static final String PROP_HAS_ORDER_NO = "hasOrderNo";
    private static final String PROP_IS_LEARNING_GOAL_COMPLETED = "isLearningGoalCompleted";
    private static final String PROP_IS_INITIAL_TEST_COMPLETED = "isInitialTestCompleted";
    private static final String PROP_HAS_FILE_ATTACHMENT_ID = "hasFileAttachmentId";
    private static final String PROP_HAS_SUBMISSION = "hasSubmission";
    private static final String PROP_HAS_DISPATCHER_POINTS = "hasDispatcherPoints";
    private static final String PROP_HAS_DIAGNOSE_LEVEL = "hasDiagnoseLevel";

    private static final String PROP_HAS_TASK_ASSIGNMENT = "hasTaskAssignment";
    private static final String PROP_HAS_TASK_CREATOR = "hasTaskCreator";
    private static final String PROP_HAS_INTERNAL_TASK_CREATOR = "hasInternalTaskCreator";
    private static final String PROP_HAS_TASK_HEADER = "hasTaskHeader";
    private static final String PROP_HAS_TASK_CREATION_DATE = "hasTaskCreationDate";
    private static final String PROP_HAS_UPLOAD_FILE_ID = "hasUploadFileId";
    private static final String PROP_HAS_UPLOAD_CALC_SOLUTION_FILE_ID = "hasCalcSolutionFileId";// Beziehung im RDF Graphen
    private static final String PROP_HAS_UPLOAD_CALC_INSTRUCTION_FILE_ID = "hasCalcInstructionFileId";// Beziehung im RDF Graphen
    private static final String PROP_HAS_UPLOAD_WRITER_INSTRUCTION_FILE_ID = "hasWriterInstructionFileId";
    // TODO: i change now the hasCalcAssignmentFileId to hasCalcInstructionAssignmentFileId and add new ids because of the new implementation
//    private static final String PROP_HAS_CALC_ASSIGNMENT_FILE_ID = "hasCalcAssignmentFileId";
    private static final String PROP_HAS_CALC_INSTRUCTION_ASSIGNMENT_FILE_ID = "hasCalcInstructionAssignmentFileId";
    private static final String PROP_HAS_CALC_SOLUTION_ASSIGNMENT_FILE_ID = "hasCalcSolutionAssignmentFileId";
    private static final String PROP_HAS_WRITER_INSTRUCTION_ASSIGNMENT_FILE_ID = "hasWriterInstructionAssignmentFileId";
    private static final String PROP_HAS_START_TIME = "hasStartTime";
    private static final String PROP_HAS_END_TIME = "hasEndTime";


    private static final String PROP_HAS_TASK_ID_FOR_DISPATCHER = "hasTaskIdForDispatcher";
    private static final String PROP_HAS_SQL_SCHEMA_NAME = "hasSQLSchemaName";
    private static final String PROP_HAS_SQL_CREATE_STATEMENTS = "hasSQLCreateStatements";
    private static final String PROP_HAS_SQL_INSERT_STATEMENTS_SUBMISSION = "hasSQLInsertStatementsSubmission";
    private static final String PROP_HAS_SQL_INSERT_STATEMENTS_DIAGNOSE = "hasSQLInsertStatementsDiagnose";
    private static final String PROP_HAS_DIAGNOSE_XML_FILE = "hasDiagnoseXMLFile";
    private static final String PROP_HAS_SUBMISSION_XML_FILE = "hasSubmissionXMLFile";
    private static final String PROP_HAS_SQL_SOLUTION = "hasSQLSolution";
    private static final String PROP_HAS_MAX_POINTS = "hasMaxPoints";
    private static final String PROP_HAS_DIAGNOSE_LEVEL_WEIGHTING = "hasDiagnoseLevelWeighting";
    private static final String PROP_HAS_XQUERY_SOLUTION = "hasXQuerySolution";
    private static final String PROP_HAS_XPATH_SORTING = "hasXPathSorting";
    private static final String PROP_HAS_DLG_SOLUTION = "hasDLGSolution";
    private static final String PROP_HAS_DLG_QUERY = "hasDLGQuery";
    private static final String PROP_HAS_UNCHECKED_DLG_TERM = "hasUncheckedDLGTerms";

    private static final String PROP_HAS_TYPICAL_PROCESSING_TIME = "hasTypicalProcessingTime";
    private static final String PROP_HAS_TASK_DIFFICULTY = "hasTaskDifficulty";
    private static final String PROP_HAS_TASK_ORGANISATION_UNIT = "hasTaskOrganisationUnit";
    private static final String PROP_HAS_TASK_URL = "hasTaskUrl";
    private static final String PROP_HAS_TASK_INSTRUCTION = "hasTaskInstruction";
    private static final String PROP_IS_PRIVATE_TASK = "isPrivateTask";
    private static final String PROP_HAS_TASK_ASSIGNMENT_TYPE = "hasTaskAssignmentType";
    private static final String PROP_IS_ASSIGNMENT_OF = "isAssignmentOf";

    private static final String PROP_IS_COMPLETED_FROM = "isCompletedFrom";
    private static final String PROP_HAS_FAILED_COUNT = "hasFailedCount";

    private static final String PROP_CONTAINS_LEARNING_GOAL = "containsLearningGoal";
    private static final String PROP_HAS_EXERCISE_SHEET_DIFFICULTY = "hasExerciseSheetDifficulty";
    private static final String PROP_HAS_INTERNAL_EXERCISE_SHEET_CREATOR = "hasInternalExerciseSheetCreator";
    private static final String PROP_HAS_EXERCISE_SHEET_CREATION_TIME = "hasExerciseSheetCreationTime";
    private static final String PROP_HAS_EXERCISE_SHEET_TASK_COUNT = "hasExerciseSheetTaskCount";
    private static final String PROP_IS_GENERATE_WHOLE_EXERCISE_SHEET = "isGenerateWholeExerciseSheet";

    private static final String PROP_CONTAINS_LEARNING_GOAL_ASSIGNMENT = "containsLearningGoalAssignment";
    private static final String PROP_HAS_PRIORITY = "hasPriority";

    private static final String PROP_HAS_TASK_GROUP_NAME = "hasTaskGroupName";
    private static final String PROP_HAS_TASK_GROUP_DESCRIPTION = "hasTaskGroupDescription";
    private static final String PROP_HAS_TASK_GROUP_TYPE = "hasTaskGroupType";
    private static final String PROP_HAS_TASK = "hasTask";
    private static final String PROP_HAS_TASK_GROUP_CREATOR = "hasTaskGroupCreator";
    private static final String PROP_HAS_TASK_GROUP_CHANGE_DATE = "hasTaskGroupChangeDate";
    private static final String PROP_HAS_TASK_GROUP = "hasTaskGroup";
    private static final String PROP_HAS_FILE_URL = "hasFileURL";
    private static final String PROP_HAS_DATALOG_FACTS = "hasDatalogFacts";

    private static final String INSTANCE_UPLOAD_TASK = "UploadTask";
    private static final String INSTANCE_NO_TYPE = "NoType";
    private static final String INSTANCE_SQL_TASK = "SQLTask";
    private static final String INSTANCE_RA_TASK = "RATask";
    private static final String INSTANCE_XQ_TASK = "XQTask";
    private static final String INSTANCE_DLG_TASK = "DLGTask";
    private static final String INSTANCE_CALC_TASK = "CalcTask";


    private static final String INSTANCE_SQL_TYPE_TASK_GROUP = "SQLType";
    private static final String INSTANCE_NO_TYPE_TASK_GROUP ="NoType";
    private static final String INSTANCE_XQUERY_TASK_GROUP = "XQueryType";
    private static final String INSTANCE_DATALOG_TASK_GROUP = "DatalogType";


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
    /**
     * The namespace for task assignment types.
     */
    public static final String TASK_ASSIGNMENT_TYPE_URI = "http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#";
    /**
     * The namespace for taskg group types.
     */
    public static final String TASK_GROUP_TYPE_TURI = "http://www.dke.uni-linz.ac.at/etutorpp/TaskGroupType#";

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
     * The hasTaskGroup property.
     */
    public static final Property hasTaskGroup = m.createProperty(URI + PROP_HAS_TASK_GROUP);
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
    public static final Property needsVerificationBeforeCompletion =
        m.createProperty(URI + PROP_NEEDS_VERIFICATION_BEFORE_COMPLETION);
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
    public static final Property hasIndividualTaskAssignment =
        m.createProperty(URI + PROP_HAS_INDIVIDUAL_TASK_ASSIGNMENT);
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
     * the hasUploadFileId property-
     */
    public static final Property hasUploadFileId = m.createProperty(URI + PROP_HAS_UPLOAD_FILE_ID);

    public static final Property hasUploadCalcSolutionFileId = m.createProperty(URI + PROP_HAS_UPLOAD_CALC_SOLUTION_FILE_ID);

    public static final Property hasUploadCalcInstructionFileId = m.createProperty(URI + PROP_HAS_UPLOAD_CALC_INSTRUCTION_FILE_ID);

    public static final Property hasUploadWriterInstructionFileId = m.createProperty(URI + PROP_HAS_UPLOAD_WRITER_INSTRUCTION_FILE_ID);

    public static final Property hasCalcInstructionAssignmentFileId = m.createProperty(URI + PROP_HAS_CALC_INSTRUCTION_ASSIGNMENT_FILE_ID);

    public static final Property hasCalcSolutionAssignmentFileId = m.createProperty(URI + PROP_HAS_CALC_SOLUTION_ASSIGNMENT_FILE_ID);

    public static final Property hasWriterInstructionAssignmentFileId = m.createProperty(URI + PROP_HAS_WRITER_INSTRUCTION_ASSIGNMENT_FILE_ID);

    public static final Property hasStartTime = m.createProperty(URI + PROP_HAS_START_TIME);

    public static final Property hasEndTime = m.createProperty(URI + PROP_HAS_END_TIME);


    /**
     * The hasTypicalProcessingTime property.
     */
    public static final Property hasTypicalProcessingTime = m.createProperty(URI + PROP_HAS_TYPICAL_PROCESSING_TIME);
    /**
     * The hasTaskIdForDispatcher property.
     */
    public static final Property hasTaskIdForDispatcher = m.createProperty(URI + PROP_HAS_TASK_ID_FOR_DISPATCHER);
    /**
     * The hasSQLSchemaName property.
     */
    public static final Property hasSQLSchemaName = m.createProperty(URI + PROP_HAS_SQL_SCHEMA_NAME);
    /**
     * The hasSQLCreateStatements property.
     */
    public static final Property hasSQLCreateStatements = m.createProperty(URI + PROP_HAS_SQL_CREATE_STATEMENTS);
    /**
     * The hasSQLInsertStatementsSubmission property.
     */
    public static final Property hasSQLInsertStatementsSubmission = m.createProperty(URI + PROP_HAS_SQL_INSERT_STATEMENTS_SUBMISSION);
    /**
     * The hasSQLInsertStatementsDiagnose property.
     */
    public static final Property hasSQLInsertStatementsDiagnose = m.createProperty(URI + PROP_HAS_SQL_INSERT_STATEMENTS_DIAGNOSE);
    /**
     * The hasDiagnoseXMLFile property
     */
    public static final Property hasDiagnoseXMLFile = m.createProperty(URI+PROP_HAS_DIAGNOSE_XML_FILE);
    /**
     * The hasSubmissionXMLFile property
     */
    public static final Property hasSubmissionXMLFile = m.createProperty(URI+PROP_HAS_SUBMISSION_XML_FILE);
    /**
     * The hasSQLSolution property.
     */
    public static final Property hasSQLSolution = m.createProperty(URI + PROP_HAS_SQL_SOLUTION);
    /**
     * The hasXQuerySolution property.
     */
    public static final Property hasXQuerySolution = m.createProperty(URI+PROP_HAS_XQUERY_SOLUTION);
    /**
     * The hasXqueryXPathSorting property.
     */
    public static final Property hasXQueryXPathSorting = m.createProperty(URI+PROP_HAS_XPATH_SORTING);
    /**
     * The hasDLGSolution property.
     */
    public static final Property hasDLGSolution = m.createProperty(URI+PROP_HAS_DLG_SOLUTION);
    /**
     * The hasDLGQuery property.
     */
    public static final Property hasDLGQuery = m.createProperty(URI+PROP_HAS_DLG_QUERY);
    /**
     * The hasUncheckedDLGTerm property.
     */
    public static final Property hasUncheckedDLGTerm = m.createProperty(URI+PROP_HAS_UNCHECKED_DLG_TERM);
     /* *
     *The hasMaxPoints property
     */
    public static final Property hasMaxPoints = m.createProperty(URI+PROP_HAS_MAX_POINTS);
    /**
     * The hasDiagnoseLevelWeighting property
     */
    public static final Property hasDiagnoseLevelWeighting = m.createProperty(URI+PROP_HAS_DIAGNOSE_LEVEL_WEIGHTING);
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
     * The hasTaskAssignmentType property
     */
    public static final Property hasTaskAssignmentType = m.createProperty(URI + PROP_HAS_TASK_ASSIGNMENT_TYPE);
    /**
     * The isAssignmentOf property.
     */
    public static final Property isAssignmentOf = m.createProperty(URI + PROP_IS_ASSIGNMENT_OF);

    /**
     * The isCompletedFrom property.
     */
    public static final Property isCompletedFrom = m.createProperty(URI + PROP_IS_COMPLETED_FROM);
    /**
     * The hasFailedCount property.
     */
    public static final Property hasFailedCount = m.createProperty(URI + PROP_HAS_FAILED_COUNT);

    /**
     * The containsLearningGoal property.
     */
    public static final Property containsLearningGoal = m.createProperty(URI + PROP_CONTAINS_LEARNING_GOAL);
    /**
     * The hasExerciseSheetDifficulty property.
     */
    public static final Property hasExerciseSheetDifficulty =
        m.createProperty(URI + PROP_HAS_EXERCISE_SHEET_DIFFICULTY);
    /**
     * The hasInternalExerciseSheetCreator property.
     */
    public static final Property hasInternalExerciseSheetCreator =
        m.createProperty(URI + PROP_HAS_INTERNAL_EXERCISE_SHEET_CREATOR);
    /**
     * The hasExerciseSheetCreationTime property.
     */
    public static final Property hasExerciseSheetCreationTime =
        m.createProperty(URI + PROP_HAS_EXERCISE_SHEET_CREATION_TIME);
    /**
     * The hasExerciseSheetTaskCount property.
     */
    public static final Property hasExerciseSheetTaskCount = m.createProperty(URI + PROP_HAS_EXERCISE_SHEET_TASK_COUNT);
    /**
     * The isGenerateWholeExerciseSheet property.
     */
    public static final Property isGenerateWholeExerciseSheet = m.createProperty(URI + PROP_IS_GENERATE_WHOLE_EXERCISE_SHEET);
    /**
     * The containsLearningGoalAssignment property.
     */
    public static final Property containsLearningGoalAssignment = m.createProperty(URI + PROP_CONTAINS_LEARNING_GOAL_ASSIGNMENT);
    /**
     * The hasPriority property.
     */
    public static final Property hasPriority = m.createProperty(URI + PROP_HAS_PRIORITY);
    /**
     * The hasIndividualTask property.
     */
    public static final Property hasIndividualTask = m.createProperty(URI + PROP_HAS_INDIVIDUAL_TASK);
    /**
     * The hasIndividualTaskSubmission property
     */
    public static final Property hasIndividualTaskSubmission = m.createProperty(URI+PROP_HAS_INDIVIDUAL_TASK_SUBMISSION);
    /**
     * The isClosed property.
     */
    public static final Property isClosed = m.createProperty(URI + PROP_IS_CLOSED);
    /**
     * The isGraded property.
     */
    public static final Property isGraded = m.createProperty(URI + PROP_IS_GRADED);
    /**
     * The isSubmitted property.
     */
    public static final Property isSubmitted = m.createProperty(URI + PROP_IS_SUBMITTED);
    /**
     * The refersToTask property.
     */
    public static final Property refersToTask = m.createProperty(URI + PROP_REFERS_TO_TASK);
    /**
     * The hasOrderNo property.
     */
    public static final Property hasOrderNo = m.createProperty(URI + PROP_HAS_ORDER_NO);
    /**
     * The isLearningGoalCompleted property.
     */
    public static final Property isLearningGoalCompleted = m.createProperty(URI + PROP_IS_LEARNING_GOAL_COMPLETED);
    /**
     * The isInitialTestCompleted property.
     */
    public static final Property isInitialTestCompleted = m.createProperty(URI + PROP_IS_INITIAL_TEST_COMPLETED);
    /**
     * The hasFileAttachmentId property.
     */
    public static final Property hasFileAttachmentId = m.createProperty(URI + PROP_HAS_FILE_ATTACHMENT_ID);
    /**
     * The hasSubmission property
     */
    public static final Property hasSubmission = m.createProperty(URI+ PROP_HAS_SUBMISSION);
    /**
     * The hasDispatcherPoints property
     */
    public static final Property hasDispatcherPoints = m.createProperty(URI+PROP_HAS_DISPATCHER_POINTS);
    /**
     * The hasDiagnoseLevel property
     */
    public static final Property hasDiagnoseLevel = m.createProperty(URI+PROP_HAS_DIAGNOSE_LEVEL);
    /**
     * The hasTaskGroupName property.
     */
    public static final Property hasTaskGroupName = m.createProperty(URI + PROP_HAS_TASK_GROUP_NAME);
    /**
     * The hasTaskGroupDescription property.
     */
    public static final Property hasTaskGroupDescription = m.createProperty(URI + PROP_HAS_TASK_GROUP_DESCRIPTION);
    /**
     * The hasTaskGroupType property.
     */
    public static final Property hasTaskGroupType = m.createProperty(URI+PROP_HAS_TASK_GROUP_TYPE);
    /**
     * The hasFileUrl property
     */
    public static final Property hasFileUrl = m.createProperty(URI+PROP_HAS_FILE_URL);
    /**
     * The hasDatalogFacts property
     */
    public static final Property hasDatalogFacts = m.createProperty(URI+PROP_HAS_DATALOG_FACTS);

    /**
     * The hasTask property.
     */
    public static final Property hasTask = m.createProperty(URI + PROP_HAS_TASK);
    /**
     * The hasTaskGroupCreator property.
     */
    public static final Property hasTaskGroupCreator = m.createProperty(URI + PROP_HAS_TASK_GROUP_CREATOR);
    /**
     * The hasTaskGroupChangeDate property.
     */
    public static final Property hasTaskGroupChangeDate = m.createProperty(URI + PROP_HAS_TASK_GROUP_CHANGE_DATE);

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
     * The task assignment type resource.
     */
    public static final Resource TaskAssignmentType = m.createResource(URI + CLASS_TASK_ASSIGNMENT_TYPE);
    /**
     * The exercise sheet resource.
     */
    public static final Resource ExerciseSheet = m.createResource(URI + CLASS_EXERCISE_SHEET);
    /**
     * The learning goal assignment resource.
     */
    public static final Resource LearningGoalAssignment = m.createResource(URI + CLASS_LEARNING_GOAL_ASSIGNMENT);
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
     * The individual task resource.
     */
    public static final Resource IndividualTask = m.createResource(URI + CLASS_INDIVIDUAL_TASK);
    /**
     * The individual task submission resource.
     */
    public static final Resource IndividualTaskSubmission = m.createResource(URI + CLASS_INDIVIDUAL_TASK_SUBMISSION);
    /**
     * The task group resource.
     */
    public static final Resource TaskGroup = m.createResource(URI + CLASS_TASK_GROUP);

    /**
     * The upload task assignment type instance.
     */
    public static final Resource UploadTask = m.createResource(TASK_ASSIGNMENT_TYPE_URI + INSTANCE_UPLOAD_TASK);
    /**
     * The not type task assignment type instance.
     */
    public static final Resource NoType = m.createResource(TASK_ASSIGNMENT_TYPE_URI + INSTANCE_NO_TYPE);
    /**
     * The SQL task assignment type instance.
     */
    public static final Resource SQLTask = m.createResource(TASK_ASSIGNMENT_TYPE_URI + INSTANCE_SQL_TASK);
    /***
     * The relational algebra type instance
     */
    public static final Resource RATask = m.createResource(TASK_ASSIGNMENT_TYPE_URI + INSTANCE_RA_TASK);
    /**
     * The XQuery type instance
     */
    public static final Resource XQueryTask = m.createResource(TASK_ASSIGNMENT_TYPE_URI + INSTANCE_XQ_TASK);
    /**
     * The Datalog type instance
     */
    public static final Resource DatalogTask = m.createResource(TASK_ASSIGNMENT_TYPE_URI + INSTANCE_DLG_TASK);

    /**
     * The Calc type instance
     */
    public static final Resource CalcTask = m.createResource(TASK_ASSIGNMENT_TYPE_URI + INSTANCE_CALC_TASK);

    /**
     * The no type task group type
     */
    public static final Resource NoTypeTaskGroup = m.createResource(TASK_GROUP_TYPE_TURI+INSTANCE_NO_TYPE_TASK_GROUP);
    /**
     * The SQL task group type
     */
    public static final Resource SQLTypeTaskGroup = m.createResource(TASK_GROUP_TYPE_TURI+INSTANCE_SQL_TYPE_TASK_GROUP);
    /**
     * The XQuery task group type
     */
    public static final Resource XQueryTypeTaskGroup = m.createResource(TASK_GROUP_TYPE_TURI+INSTANCE_XQUERY_TASK_GROUP);
    /**
     * The Datalog task group type
     */
    public static final Resource DatalogTypeTaskGroup = m.createResource(TASK_GROUP_TYPE_TURI+INSTANCE_DATALOG_TASK_GROUP);

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
     * Creates the URL of an individual user goal.
     *
     * @param userLogin the goal's creator
     * @param goalName  the goal's name
     * @return unique goal URL
     */
    public static String createGoalUrl(String userLogin, String goalName) {
        return URI + userLogin + "/" + CLASS_GOAL + "#" + goalName;
    }

    /**
     * Creates an individual goal resource from a given model.
     *
     * @param userLogin the login of the user
     * @param goalName  the rdf encoded goal name
     * @param model     the base model of the resource
     * @return the individual goal resource
     */
    public static Resource createUserGoalResourceOfModel(String userLogin, String goalName, Model model) {
        return model.createResource(createGoalUrl(userLogin, goalName));
    }

    /**
     * Creates an individual course resource from a given model.
     *
     * @param courseName the  rdf encoded name of the course
     * @param model      the base model of the resource
     * @return the individual course resource
     */
    public static Resource createCourseResourceOfModel(String courseName, Model model) {
        return model.createResource(createCourseURL(courseName));
    }

    /**
     * Returns the course url from a given course name
     *
     * @param courseName the course's name
     * @return the course url
     */
    public static String createCourseURL(String courseName) {
        return URI + CLASS_COURSE + "#" + courseName;
    }

    /**
     * Creates an individual task assignment from a given model.
     *
     * @param uuid  the uuid
     * @param model the base model
     * @return the individual task assignment resource
     */
    public static Resource createTaskAssignmentResourceOfModel(String uuid, Model model) {
        return model.createResource(URI + CLASS_TASK_ASSIGNMENT + "#" + uuid);
    }

    /**
     * Creates an individual exercise sheet from a given model.
     *
     * @param uuid  the uuid
     * @param model the base model
     * @return the individual exercise sheet resource
     */
    public static Resource createExerciseSheetOfModel(String uuid, Model model) {
        return model.createResource(createExerciseSheetURLString(uuid));
    }

    /**
     * Creates the url of an exercise sheet.
     *
     * @param uuid the uuid
     * @return the internal url of an individual exercise sheet
     */
    public static String createExerciseSheetURLString(String uuid) {
        return URI + CLASS_EXERCISE_SHEET + "#" + uuid;
    }

    /**
     * Creates the url of a course instance.
     *
     * @param uuid the uuid
     * @return the internal url of a course instance
     */
    public static String createCourseInstanceURLString(String uuid) {
        return URI + CLASS_COURSE_INSTANCE + "#" + uuid;
    }

    /**
     * Creates an individual course instance from a given model.
     *
     * @param uuid  the uuid
     * @param model the base model
     * @return the individual course instance
     */
    public static Resource createCourseInstanceOfModel(String uuid, Model model) {
        return model.createResource(createCourseInstanceURLString(uuid));
    }

    /**
     * Returns the term text from a given uri. If the uri does not
     * represent a term, {@code null} will be returned.
     *
     * @param termUri the term uri, must not be null
     * @return string representation, or {@code null}
     */
    public static String getTermTextFromUri(String termUri) {
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
    public static String getStudentURLFromMatriculationNumber(String matriculationNumber) {
        return URI + CLASS_STUDENT + "#" + matriculationNumber;
    }

    /**
     * Returns the task id for a task group entity.
     *
     * @param name the task group's name
     * @return id the entity's id
     */
    public static String getTaskGroupIdFromName(String name) {
        return URI + CLASS_TASK_GROUP + "#" + URLEncoder.encode(name.replace(' ', '_').trim(), StandardCharsets.UTF_8);
    }

    /**
     * Constructor.
     */
    private ETutorVocabulary() {
        throw new IllegalStateException("Utility class");
    }
}
