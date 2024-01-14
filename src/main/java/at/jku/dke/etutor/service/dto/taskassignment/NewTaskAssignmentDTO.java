package at.jku.dke.etutor.service.dto.taskassignment;

import at.jku.dke.etutor.service.dto.validation.DifficultyRankingConstraint;
import at.jku.dke.etutor.service.dto.validation.TaskAssignmentTypeConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotBlank;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for a new task assignment.
 *
 * @author fne
 */
public class NewTaskAssignmentDTO {

    private List<LearningGoalDisplayDTO> learningGoalIds = new ArrayList<>();

    @NotBlank
    private String creator;

    @NotBlank
    private String header;

    private int uploadFileId;
    private int calcSolutionFileId; // has to match with task.model.ts
    private int calcInstructionFileId; // has to match with task.model.ts
    private int writerInstructionFileId;
    // insert time

    private Timestamp startTime;
    private Timestamp endTime;

    private String taskIdForDispatcher;
    private String sqlSolution;
    private String maxPoints;
    private String diagnoseLevelWeighting;
    private String xQuerySolution;
    private String xQueryXPathSorting;
    private String datalogSolution;
    private String datalogQuery;
    private String datalogUncheckedTerms;
    // Pm task related variables
    private int maxActivity;
    private int minActivity;
    private int maxLogSize;
    private int minLogSize;
    private String configNum;

/** start apriori   */
    private String aprioriDatasetId;
/** apriori end */

    private String processingTime;

    private String bpmnTestConfig;

    // NF start
    /**
     * NF-specific variable: attributes of base relation
     */
    private String nfBaseAttributes;

    /**
     * NF-specific variable: functional dependencies of base relation
     */
    private String nfBaseDependencies;

    /**
     * NF-specific variable: the id of the NF task subtype
     */
    private String nfTaskSubtypeId;

    // Keys determination

    /**
     * NF-specific variable: Number of points deducted per missing key in a keys determination task
     */
    private int nfKeysDeterminationPenaltyPerMissingKey;

    /**
     * NF-specific variable: Number of points deducted per incorrect key in a keys determination task
     */
    private int nfKeysDeterminationPenaltyPerIncorrectKey;

    // Attribute closure

    /**
     * NF-specific variable: base attributes for closure in an attribute closure task
     */
    private String nfAttributeClosureBaseAttributes;

    /**
     * NF-specific variable: Number of points deducted per missing attribute an attribute closure task
     */
    private int nfAttributeClosurePenaltyPerMissingAttribute;

    /**
     * NF-specific variable: Number of points deducted per incorrect attribute in an attribute closure task
     */
    private int nfAttributeClosurePenaltyPerIncorrectAttribute;

    // Minimal cover

    /**
     * NF-specific variable: Number of points deducted per non-canonical functional dependency in a minimal cover task
     */
    private int nfMinimalCoverPenaltyPerNonCanonicalDependency;

    /**
     * NF-specific variable: Number of points deducted per trivial functional dependency in a minimal cover task
     */
    private int nfMinimalCoverPenaltyPerTrivialDependency;

    /**
     * NF-specific variable: Number of points deducted per extraneous attribute on the left-hand side of a functional
     * dependency in a minimal cover task
     */
    private int nfMinimalCoverPenaltyPerExtraneousAttribute;

    /**
     * NF-specific variable: Number of points deducted per redundant functional dependency in a minimal cover task
     */
    private int nfMinimalCoverPenaltyPerRedundantDependency;

    /**
     * NF-specific variable: Number of points deducted per missing functional dependency in a minimal cover task
     * (compared to the correct solution)
     */
    private int nfMinimalCoverPenaltyPerMissingDependencyVsSolution;

    /**
     * NF-specific variable: Number of points deducted per incorrect functional dependency in a minimal cover task
     * (compared to the correct solution)
     */
    private int nfMinimalCoverPenaltyPerIncorrectDependencyVsSolution;

    // Normal form determination

    /**
     * NF-specific variable: Number of points deducted for an incorrect total normal form in a normal form determination
     * task
     */
    private int nfNormalFormDeterminationPenaltyForIncorrectOverallNormalform;

    /**
     * NF-specific variable: Number of points deducted per incorrectly determined normal form of a functional dependency
     * in a normal form determination task
     */
    private int nfNormalFormDeterminationPenaltyPerIncorrectDependencyNormalform;

    // Normalization

    /**
     * NF-specific variable: The minimum normal form level which the resulting relations must have in a normalization
     * task
     */
    private String nfNormalizationTargetLevel;

    /**
     * NF-specific variable: The maximum number of functional dependencies that is permitted to be lost in the
     * decomposition process before points are deducted in a normalization task
     */
    private int nfNormalizationMaxLostDependencies;

    /**
     * NF-specific variable: Points deducted for every attribute of the base relation that is not present in any of the
     * resulting relations in a normalization task
     */
    private int nfNormalizationPenaltyPerLostAttribute;

    /**
     * NF-specific variable: Points deducted if the resulting relations cannot be re-combined into the base relation in
     * a normalization task
     */
    private int nfNormalizationPenaltyForLossyDecomposition;

    /**
     * NF-specific variable: Points deducted for every non-canonical functional dependency in a resulting relation in
     * a normalization task
     */
    private int nfNormalizationPenaltyPerNonCanonicalDependency;

    /**
     * NF-specific variable: Points deducted for every trivial functional dependency in a resulting relation in
     * a normalization task
     */
    private int nfNormalizationPenaltyPerTrivialDependency;

    /**
     * NF-specific variable: Points deducted for every extraneous attribute on the left-hand side of a functional
     * dependency in a resulting relation in a normalization task
     */
    private int nfNormalizationPenaltyPerExtraneousAttributeInDependencies;

    /**
     * NF-specific variable: Points deducted for every redundant functional dependency in a resulting relation in
     * a normalization task
     */
    private int nfNormalizationPenaltyPerRedundantDependency;

    /**
     * NF-specific variable: Points deducted for every functional dependency that was lost during the decomposition
     * process and exceeds the maximum permitted number of lost functional dependencies in a normalization task
     */
    private int nfNormalizationPenaltyPerExcessiveLostDependency;

    /**
     * NF-specific variable: Points deducted for every functional dependency that would have to exist in a resulting
     * relation due to the decomposition process but does not in a normalization task
     */
    private int nfNormalizationPenaltyPerMissingNewDependency;

    /**
     * NF-specific variable: Points deducted for every functional dependency that exists in a resulting relation, even
     * though it is not supposed to (due to the decomposition process, more specifically the RBR algorithm) in a
     * normalization task
     */
    private int nfNormalizationPenaltyPerIncorrectNewDependency;

    /**
     * NF-specific variable: Points deducted for every missing key in a resulting relation in a normalization task
     */
    private int nfNormalizationPenaltyPerMissingKey;

    /**
     * NF-specific variable: Points deducted for every incorrect key in a resulting relation in a normalization task
     */
    private int nfNormalizationPenaltyPerIncorrectKey;

    /**
     * NF-specific variable: Points deducted for every resulting relation that does not match or exceed the required
     * normal form in a normalization task
     */
    private int nfNormalizationPenaltyPerIncorrectNFRelation;
    // NF end

    @NotBlank
    @DifficultyRankingConstraint
    private String taskDifficultyId;

    @NotBlank
    private String organisationUnit;

    private URL url;
    private String instruction;
    private boolean privateTask;

    @NotBlank
    @TaskAssignmentTypeConstraint
    private String taskAssignmentTypeId;

    private String taskGroupId;

    /**
     * Returns the associated learning goals.
     *
     * @return the associated learning goals
     */
    public List<LearningGoalDisplayDTO> getLearningGoalIds() {
        return learningGoalIds;
    }

    /**
     * Sets the associated learning goals.
     *
     * @param learningGoalIds the associated learning goals to set
     */
    public void setLearningGoalIds(List<LearningGoalDisplayDTO> learningGoalIds) {
        this.learningGoalIds = learningGoalIds;
    }

    /**
     * Adds a learning goal.
     *
     * @param learningGoal the learning goal to add, not null
     */
    @JsonIgnore
    public void addLearningGoal(LearningGoalDisplayDTO learningGoal) {
        if (learningGoalIds == null) {
            learningGoalIds = new ArrayList<>();
        }
        if (learningGoal != null) {
            learningGoalIds.add(learningGoal);
        }
    }

    /**
     * Removes a learning goal.
     *
     * @param learningGoal the learning goal to remove, not null
     */
    @JsonIgnore
    public void removeLearningGoal(LearningGoalDisplayDTO learningGoal) {
        if (learningGoalIds != null && learningGoal != null) {
            learningGoalIds.remove(learningGoal);
        }
    }

    /**
     * Returns the creator.
     *
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Sets the creator.
     *
     * @param creator the creator
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Returns the header.
     *
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * Sets the header.
     *
     * @param header the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Returns the task id for dispatcher.
     *
     * @return the task id for dispatcher
     */
    public String getTaskIdForDispatcher() {
        return taskIdForDispatcher;
    }

    /**
     * Sets the task id for dispatcher.
     *
     * @param taskIdForDispatcher task id for the dispatcher to set
     */
    public void setTaskIdForDispatcher(String taskIdForDispatcher) {
        this.taskIdForDispatcher = taskIdForDispatcher;
    }

    /**
     * Returns the solution for an SQL exercise
     * @return
     */
    public String getSqlSolution() {
        return sqlSolution;
    }

    /**
     * Sets the solution for an SQL exercise
     * @param sqlSolution
     */
    public void setSqlSolution(String sqlSolution) {
        this.sqlSolution = sqlSolution;
    }

    /**
     * Returns the maxPoints
     * @return the maxPoints
     */
    public String getMaxPoints() {
        return maxPoints;
    }

    /**
     * Sets the maxPoints
     * @param maxPoints the maxPoints
     */
    public void setMaxPoints(String maxPoints) {
        this.maxPoints = maxPoints;
    }
    /**
     * Returns the weighting for the diagnose level
     * @return the weighting
     */
    public String getDiagnoseLevelWeighting() {
        return diagnoseLevelWeighting;
    }
    /**
     * Sets the weighting for the diagnose level
     * @param diagnoseLevelWeighting
     */
    public void setDiagnoseLevelWeighting(String diagnoseLevelWeighting) {
        this.diagnoseLevelWeighting = diagnoseLevelWeighting;
    }

    /**
     * Returns the processing time.
     *
     * @return the processing time
     */
    public String getProcessingTime() {
        return processingTime;
    }


    /**
     * Sets the processing time.
     *
     * @param processingTime the processing time to set
     */
    public void setProcessingTime(String processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * Returns the corresponding task difficulty id.
     *
     * @return the corresponding task difficulty id
     */
    public String getTaskDifficultyId() {
        return taskDifficultyId;
    }

    /**
     * Sets the task difficulty id.
     *
     * @param taskDifficultyId the task difficulty id to set
     */
    public void setTaskDifficultyId(String taskDifficultyId) {
        this.taskDifficultyId = taskDifficultyId;
    }

    /**
     * Returns the organisation unit.
     *
     * @return the organisation unit
     */
    public String getOrganisationUnit() {
        return organisationUnit;
    }

    /**
     * Sets the organisation unit.
     *
     * @param organisationUnit the organisation unit
     */
    public void setOrganisationUnit(String organisationUnit) {
        this.organisationUnit = organisationUnit;
    }

    /**
     * Returns the task's url.
     *
     * @return the task's url
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets the task's url.
     *
     * @param url the task's url to set
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Returns the instructions.
     *
     * @return the instructions
     */
    public String getInstruction() {
        return instruction;
    }

    /**
     * Sets the instructions.
     *
     * @param instruction the instruction to set
     */
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    /**
     * Returns whether the task is a private task or not.
     *
     * @return {@code} true if the task is a private task, otherwise {@code false}
     */
    public boolean isPrivateTask() {
        return privateTask;
    }

    /**
     * Sets whether the task is a private task or not.
     *
     * @param privateTask {@code} true if the task is a private task, otherwise {@code false}
     */
    public void setPrivateTask(boolean privateTask) {
        this.privateTask = privateTask;
    }

    /**
     * Returns the task assignment type id.
     *
     * @return the task assignment type id
     */
    public String getTaskAssignmentTypeId() {
        return taskAssignmentTypeId;
    }

    /**
     * Sets the task assignment type id.
     *
     * @param taskAssignmentTypeId the task assignment type id to seto
     */
    public void setTaskAssignmentTypeId(String taskAssignmentTypeId) {
        this.taskAssignmentTypeId = taskAssignmentTypeId;
    }

    /**
     * Returns the task group id.
     *
     * @return the task group id
     */
    public String getTaskGroupId() {
        return taskGroupId;
    }

    /**
     * Sets the task group id.
     *
     * @param taskGroupId the task group id to set
     */
    public void setTaskGroupId(String taskGroupId) {
        this.taskGroupId = taskGroupId;
    }

    public String getxQuerySolution() {
        return xQuerySolution;
    }

    public void setxQuerySolution(String xQuerySolution) {
        this.xQuerySolution = xQuerySolution;
    }

    public String getxQueryXPathSorting() {
        return xQueryXPathSorting;
    }

    public void setxQueryXPathSorting(String xQueryXPathSorting) {
        this.xQueryXPathSorting = xQueryXPathSorting;
    }

    public String getDatalogSolution() {
        return datalogSolution;
    }

    public void setDatalogSolution(String datalogSolution) {
        this.datalogSolution = datalogSolution;
    }

    public String getDatalogQuery() {
        return datalogQuery;
    }

    public void setDatalogQuery(String datalogQuery) {
        this.datalogQuery = datalogQuery;
    }

    public String getDatalogUncheckedTerms() {
        return datalogUncheckedTerms;
    }

    public void setDatalogUncheckedTerms(String datalogUncheckedTerms) {
        this.datalogUncheckedTerms = datalogUncheckedTerms;
    }

    // Pm related getter/setter:
    public int getMaxActivity(){return maxActivity;}

    public void setMaxActivity(int maxActivity){
        this.maxActivity = maxActivity;
    }

    public int getMinActivity() {
        return minActivity;
    }

    public void setMinActivity(int minActivity) {
        this.minActivity = minActivity;
    }

    public int getMaxLogSize() {
        return maxLogSize;
    }

    public void setMaxLogSize(int maxLogSize) {
        this.maxLogSize = maxLogSize;
    }

    public int getMinLogSize() {
        return minLogSize;
    }

    public void setMinLogSize(int minLogSize) {
        this.minLogSize = minLogSize;
    }

    public String getConfigNum() {
        return configNum;
    }

    public void setConfigNum(String configNum) {
        this.configNum = configNum;
    }

    public int getUploadFileId() {
        return uploadFileId;
    }

    public void setUploadFileId(int uploadFileId) {
        this.uploadFileId = uploadFileId;
    }

    public String getBpmnTestConfig() {
        return bpmnTestConfig;
    }

    public void setBpmnTestConfig(String bpmnTestConfig) {
        this.bpmnTestConfig = bpmnTestConfig;
    }

    public int getCalcSolutionFileId() {
        return calcSolutionFileId;
    }

    public void setCalcSolutionFileId(int calcSolutionFileId) {
        this.calcSolutionFileId = calcSolutionFileId;
    }

    public int getCalcInstructionFileId() {
        return calcInstructionFileId;
    }

    public void setCalcInstructionFileId(int calcInstructionFileId) {
        this.calcInstructionFileId = calcInstructionFileId;
    }

    public int getWriterInstructionFileId() {
        return writerInstructionFileId;
    }

    public void setWriterInstructionFileId(int writerInstructionFileId) {
        this.writerInstructionFileId = writerInstructionFileId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

/** start apriori   */

    public String getAprioriDatasetId() {
        return aprioriDatasetId;
    }

    public void setAprioriDatasetId(String aprioriDatasetId) {
        this.aprioriDatasetId = aprioriDatasetId;
    }



/** apriori end */

    // NF start
    public String getNfBaseAttributes() {
        return nfBaseAttributes;
    }

    public void setNfBaseAttributes(String nfBaseAttributes) {
        this.nfBaseAttributes = nfBaseAttributes;
    }

    public String getNfBaseDependencies() {
        return nfBaseDependencies;
    }

    public void setNfBaseDependencies(String nfBaseDependencies) {
        this.nfBaseDependencies = nfBaseDependencies;
    }

    public String getNfTaskSubtypeId() {
        return nfTaskSubtypeId;
    }

    public void setNfTaskSubtypeId(String nfTaskSubtypeId) {
        this.nfTaskSubtypeId = nfTaskSubtypeId;
    }

    public int getNfKeysDeterminationPenaltyPerMissingKey() {
        return nfKeysDeterminationPenaltyPerMissingKey;
    }

    public void setNfKeysDeterminationPenaltyPerMissingKey(int nfKeysDeterminationPenaltyPerMissingKey) {
        this.nfKeysDeterminationPenaltyPerMissingKey = nfKeysDeterminationPenaltyPerMissingKey;
    }

    public int getNfKeysDeterminationPenaltyPerIncorrectKey() {
        return nfKeysDeterminationPenaltyPerIncorrectKey;
    }

    public void setNfKeysDeterminationPenaltyPerIncorrectKey(int nfKeysDeterminationPenaltyPerIncorrectKey) {
        this.nfKeysDeterminationPenaltyPerIncorrectKey = nfKeysDeterminationPenaltyPerIncorrectKey;
    }

    public String getNfAttributeClosureBaseAttributes() {
        return nfAttributeClosureBaseAttributes;
    }

    public void setNfAttributeClosureBaseAttributes(String nfAttributeClosureBaseAttributes) {
        this.nfAttributeClosureBaseAttributes = nfAttributeClosureBaseAttributes;
    }

    public int getNfAttributeClosurePenaltyPerMissingAttribute() {
        return nfAttributeClosurePenaltyPerMissingAttribute;
    }

    public void setNfAttributeClosurePenaltyPerMissingAttribute(int nfAttributeClosurePenaltyPerMissingAttribute) {
        this.nfAttributeClosurePenaltyPerMissingAttribute = nfAttributeClosurePenaltyPerMissingAttribute;
    }

    public int getNfAttributeClosurePenaltyPerIncorrectAttribute() {
        return nfAttributeClosurePenaltyPerIncorrectAttribute;
    }

    public void setNfAttributeClosurePenaltyPerIncorrectAttribute(int nfAttributeClosurePenaltyPerIncorrectAttribute) {
        this.nfAttributeClosurePenaltyPerIncorrectAttribute = nfAttributeClosurePenaltyPerIncorrectAttribute;
    }

    public int getNfMinimalCoverPenaltyPerNonCanonicalDependency() {
        return nfMinimalCoverPenaltyPerNonCanonicalDependency;
    }

    public void setNfMinimalCoverPenaltyPerNonCanonicalDependency(int nfMinimalCoverPenaltyPerNonCanonicalDependency) {
        this.nfMinimalCoverPenaltyPerNonCanonicalDependency = nfMinimalCoverPenaltyPerNonCanonicalDependency;
    }

    public int getNfMinimalCoverPenaltyPerTrivialDependency() {
        return nfMinimalCoverPenaltyPerTrivialDependency;
    }

    public void setNfMinimalCoverPenaltyPerTrivialDependency(int nfMinimalCoverPenaltyPerTrivialDependency) {
        this.nfMinimalCoverPenaltyPerTrivialDependency = nfMinimalCoverPenaltyPerTrivialDependency;
    }

    public int getNfMinimalCoverPenaltyPerExtraneousAttribute() {
        return nfMinimalCoverPenaltyPerExtraneousAttribute;
    }

    public void setNfMinimalCoverPenaltyPerExtraneousAttribute(int nfMinimalCoverPenaltyPerExtraneousAttribute) {
        this.nfMinimalCoverPenaltyPerExtraneousAttribute = nfMinimalCoverPenaltyPerExtraneousAttribute;
    }

    public int getNfMinimalCoverPenaltyPerRedundantDependency() {
        return nfMinimalCoverPenaltyPerRedundantDependency;
    }

    public void setNfMinimalCoverPenaltyPerRedundantDependency(int nfMinimalCoverPenaltyPerRedundantDependency) {
        this.nfMinimalCoverPenaltyPerRedundantDependency = nfMinimalCoverPenaltyPerRedundantDependency;
    }

    public int getNfMinimalCoverPenaltyPerMissingDependencyVsSolution() {
        return nfMinimalCoverPenaltyPerMissingDependencyVsSolution;
    }

    public void setNfMinimalCoverPenaltyPerMissingDependencyVsSolution(int nfMinimalCoverPenaltyPerMissingDependencyVsSolution) {
        this.nfMinimalCoverPenaltyPerMissingDependencyVsSolution = nfMinimalCoverPenaltyPerMissingDependencyVsSolution;
    }

    public int getNfMinimalCoverPenaltyPerIncorrectDependencyVsSolution() {
        return nfMinimalCoverPenaltyPerIncorrectDependencyVsSolution;
    }

    public void setNfMinimalCoverPenaltyPerIncorrectDependencyVsSolution(int nfMinimalCoverPenaltyPerIncorrectDependencyVsSolution) {
        this.nfMinimalCoverPenaltyPerIncorrectDependencyVsSolution = nfMinimalCoverPenaltyPerIncorrectDependencyVsSolution;
    }

    public int getNfNormalFormDeterminationPenaltyForIncorrectOverallNormalform() {
        return nfNormalFormDeterminationPenaltyForIncorrectOverallNormalform;
    }

    public void setNfNormalFormDeterminationPenaltyForIncorrectOverallNormalform(int nfNormalFormDeterminationPenaltyForIncorrectOverallNormalform) {
        this.nfNormalFormDeterminationPenaltyForIncorrectOverallNormalform = nfNormalFormDeterminationPenaltyForIncorrectOverallNormalform;
    }

    public int getNfNormalFormDeterminationPenaltyPerIncorrectDependencyNormalform() {
        return nfNormalFormDeterminationPenaltyPerIncorrectDependencyNormalform;
    }

    public void setNfNormalFormDeterminationPenaltyPerIncorrectDependencyNormalform(int nfNormalFormDeterminationPenaltyPerIncorrectDependencyNormalform) {
        this.nfNormalFormDeterminationPenaltyPerIncorrectDependencyNormalform = nfNormalFormDeterminationPenaltyPerIncorrectDependencyNormalform;
    }

    public String getNfNormalizationTargetLevel() {
        return nfNormalizationTargetLevel;
    }

    public void setNfNormalizationTargetLevel(String nfNormalizationTargetLevel) {
        this.nfNormalizationTargetLevel = nfNormalizationTargetLevel;
    }

    public int getNfNormalizationMaxLostDependencies() {
        return nfNormalizationMaxLostDependencies;
    }

    public void setNfNormalizationMaxLostDependencies(int nfNormalizationMaxLostDependencies) {
        this.nfNormalizationMaxLostDependencies = nfNormalizationMaxLostDependencies;
    }

    public int getNfNormalizationPenaltyPerLostAttribute() {
        return nfNormalizationPenaltyPerLostAttribute;
    }

    public void setNfNormalizationPenaltyPerLostAttribute(int nfNormalizationPenaltyPerLostAttribute) {
        this.nfNormalizationPenaltyPerLostAttribute = nfNormalizationPenaltyPerLostAttribute;
    }

    public int getNfNormalizationPenaltyForLossyDecomposition() {
        return nfNormalizationPenaltyForLossyDecomposition;
    }

    public void setNfNormalizationPenaltyForLossyDecomposition(int nfNormalizationPenaltyForLossyDecomposition) {
        this.nfNormalizationPenaltyForLossyDecomposition = nfNormalizationPenaltyForLossyDecomposition;
    }

    public int getNfNormalizationPenaltyPerNonCanonicalDependency() {
        return nfNormalizationPenaltyPerNonCanonicalDependency;
    }

    public void setNfNormalizationPenaltyPerNonCanonicalDependency(int nfNormalizationPenaltyPerNonCanonicalDependency) {
        this.nfNormalizationPenaltyPerNonCanonicalDependency = nfNormalizationPenaltyPerNonCanonicalDependency;
    }

    public int getNfNormalizationPenaltyPerTrivialDependency() {
        return nfNormalizationPenaltyPerTrivialDependency;
    }

    public void setNfNormalizationPenaltyPerTrivialDependency(int nfNormalizationPenaltyPerTrivialDependency) {
        this.nfNormalizationPenaltyPerTrivialDependency = nfNormalizationPenaltyPerTrivialDependency;
    }

    public int getNfNormalizationPenaltyPerExtraneousAttributeInDependencies() {
        return nfNormalizationPenaltyPerExtraneousAttributeInDependencies;
    }

    public void setNfNormalizationPenaltyPerExtraneousAttributeInDependencies(int nfNormalizationPenaltyPerExtraneousAttributeInDependencies) {
        this.nfNormalizationPenaltyPerExtraneousAttributeInDependencies = nfNormalizationPenaltyPerExtraneousAttributeInDependencies;
    }

    public int getNfNormalizationPenaltyPerRedundantDependency() {
        return nfNormalizationPenaltyPerRedundantDependency;
    }

    public void setNfNormalizationPenaltyPerRedundantDependency(int nfNormalizationPenaltyPerRedundantDependency) {
        this.nfNormalizationPenaltyPerRedundantDependency = nfNormalizationPenaltyPerRedundantDependency;
    }

    public int getNfNormalizationPenaltyPerExcessiveLostDependency() {
        return nfNormalizationPenaltyPerExcessiveLostDependency;
    }

    public void setNfNormalizationPenaltyPerExcessiveLostDependency(int nfNormalizationPenaltyPerExcessiveLostDependency) {
        this.nfNormalizationPenaltyPerExcessiveLostDependency = nfNormalizationPenaltyPerExcessiveLostDependency;
    }

    public int getNfNormalizationPenaltyPerMissingNewDependency() {
        return nfNormalizationPenaltyPerMissingNewDependency;
    }

    public void setNfNormalizationPenaltyPerMissingNewDependency(int nfNormalizationPenaltyPerMissingNewDependency) {
        this.nfNormalizationPenaltyPerMissingNewDependency = nfNormalizationPenaltyPerMissingNewDependency;
    }

    public int getNfNormalizationPenaltyPerIncorrectNewDependency() {
        return nfNormalizationPenaltyPerIncorrectNewDependency;
    }

    public void setNfNormalizationPenaltyPerIncorrectNewDependency(int nfNormalizationPenaltyPerIncorrectNewDependency) {
        this.nfNormalizationPenaltyPerIncorrectNewDependency = nfNormalizationPenaltyPerIncorrectNewDependency;
    }

    public int getNfNormalizationPenaltyPerMissingKey() {
        return nfNormalizationPenaltyPerMissingKey;
    }

    public void setNfNormalizationPenaltyPerMissingKey(int nfNormalizationPenaltyPerMissingKey) {
        this.nfNormalizationPenaltyPerMissingKey = nfNormalizationPenaltyPerMissingKey;
    }

    public int getNfNormalizationPenaltyPerIncorrectKey() {
        return nfNormalizationPenaltyPerIncorrectKey;
    }

    public void setNfNormalizationPenaltyPerIncorrectKey(int nfNormalizationPenaltyPerIncorrectKey) {
        this.nfNormalizationPenaltyPerIncorrectKey = nfNormalizationPenaltyPerIncorrectKey;
    }

    public int getNfNormalizationPenaltyPerIncorrectNFRelation() {
        return nfNormalizationPenaltyPerIncorrectNFRelation;
    }

    public void setNfNormalizationPenaltyPerIncorrectNFRelation(int nfNormalizationPenaltyPerIncorrectNFRelation) {
        this.nfNormalizationPenaltyPerIncorrectNFRelation = nfNormalizationPenaltyPerIncorrectNFRelation;
    }
    // NF end
}
