package at.jku.dke.etutor.service.dto.taskassignment;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for a task assignment.
 *
 * @author fne
 */
public class TaskAssignmentDTO extends NewTaskAssignmentDTO implements Comparable<TaskAssignmentDTO> {

    @NotEmpty
    private String id;

    @NotNull
    private Instant creationDate;

    @NotEmpty
    private String internalCreator;

    /**
     * Default constructor.
     */
    public TaskAssignmentDTO() {
        super();
    }

    /**
     * Constructor.
     *
     * @param newTaskAssignmentDTO the new task assignment
     * @param id                   the created id
     * @param creationDate         the creation date
     * @param internalCreator      the internal creator
     */
    public TaskAssignmentDTO(NewTaskAssignmentDTO newTaskAssignmentDTO, String id, Instant creationDate, String internalCreator) {
        super();
        setLearningGoalIds(newTaskAssignmentDTO.getLearningGoalIds());
        setCreator(newTaskAssignmentDTO.getCreator());
        setHeader(newTaskAssignmentDTO.getHeader());
        setTaskIdForDispatcher(newTaskAssignmentDTO.getTaskIdForDispatcher());
        setSqlSolution(newTaskAssignmentDTO.getSqlSolution());
        setMaxPoints(newTaskAssignmentDTO.getMaxPoints());
        setDiagnoseLevelWeighting(newTaskAssignmentDTO.getDiagnoseLevelWeighting());
        setProcessingTime(newTaskAssignmentDTO.getProcessingTime());
        setTaskDifficultyId(newTaskAssignmentDTO.getTaskDifficultyId());
        setOrganisationUnit(newTaskAssignmentDTO.getOrganisationUnit());
        setUrl(newTaskAssignmentDTO.getUrl());
        setInstruction(newTaskAssignmentDTO.getInstruction());
        setPrivateTask(newTaskAssignmentDTO.isPrivateTask());
        setTaskAssignmentTypeId(newTaskAssignmentDTO.getTaskAssignmentTypeId());
        setTaskGroupId(newTaskAssignmentDTO.getTaskGroupId());
        setxQuerySolution(newTaskAssignmentDTO.getxQuerySolution());
        setxQueryXPathSorting(newTaskAssignmentDTO.getxQueryXPathSorting());
        setDatalogSolution(newTaskAssignmentDTO.getDatalogSolution());
        setDatalogQuery(newTaskAssignmentDTO.getDatalogQuery());
        setDatalogUncheckedTerms(newTaskAssignmentDTO.getDatalogUncheckedTerms());
        setUploadFileId(newTaskAssignmentDTO.getUploadFileId());
        setCalcSolutionFileId(newTaskAssignmentDTO.getCalcSolutionFileId());
        setCalcInstructionFileId(newTaskAssignmentDTO.getCalcInstructionFileId());
        setWriterInstructionFileId(newTaskAssignmentDTO.getWriterInstructionFileId());
        setStartTime(newTaskAssignmentDTO.getStartTime());
        setEndTime(newTaskAssignmentDTO.getEndTime());


        setId(id);
        setCreationDate(creationDate);
        setInternalCreator(internalCreator);
    }

    /**
     * Constructor.
     *
     * @param resource the rdf resource which contains this assignment
     * @throws MalformedURLException if the url can not be parsed
     * @throws ParseException        if a date can not be parsed
     */
    public TaskAssignmentDTO(Resource resource) throws MalformedURLException, ParseException {
        this();
        setId(resource.getURI());
        List<LearningGoalDisplayDTO> learningGoalIds = new ArrayList<>();

        StmtIterator stmtIterator = resource.listProperties(ETutorVocabulary.isAssignmentOf);
        try {
            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.nextStatement();
                Resource goalResource = statement.getObject().asResource();

                String goalId = goalResource.getURI();
                String name = goalResource.getProperty(RDFS.label).getString();

                learningGoalIds.add(new LearningGoalDisplayDTO(goalId, name));
            }
        } finally {
            stmtIterator.close();
        }

        setLearningGoalIds(learningGoalIds);
        setCreator(resource.getProperty(ETutorVocabulary.hasTaskCreator).getString());
        setHeader(resource.getProperty(ETutorVocabulary.hasTaskHeader).getString());

        Statement uploadFileIdStatement = resource.getProperty(ETutorVocabulary.hasUploadFileId);
        if(uploadFileIdStatement != null){
            setUploadFileId(uploadFileIdStatement.getInt());
        }else{
            setUploadFileId(-1);
        }

        Statement calcSolutionFileIdStatement = resource.getProperty(ETutorVocabulary.hasUploadCalcSolutionFileId);
        if(calcSolutionFileIdStatement != null){
            setCalcSolutionFileId(calcSolutionFileIdStatement.getInt());
        }else{
            setCalcSolutionFileId(-1);
        }

        Statement calcInstructionFileIdStatement = resource.getProperty(ETutorVocabulary.hasUploadCalcInstructionFileId);
        if(calcInstructionFileIdStatement != null){
            setCalcInstructionFileId(calcInstructionFileIdStatement.getInt());
        }else{
            setCalcInstructionFileId(-1);
        }

        Statement writerInstructionFileIdStatement = resource.getProperty(ETutorVocabulary.hasUploadWriterInstructionFileId);
        if (writerInstructionFileIdStatement != null) {
            setWriterInstructionFileId(writerInstructionFileIdStatement.getInt());
        } else {
            setWriterInstructionFileId(-1);
        }

        // TODO: rdf time format
        Statement startTimeStatement = resource.getProperty(ETutorVocabulary.hasStartTime);
        if(startTimeStatement != null){
            setStartTime(Timestamp.valueOf(startTimeStatement.getString()));
        }else{
            setStartTime(null);
        }

        // TODO: rdf time format
        Statement endTimeStatement = resource.getProperty(ETutorVocabulary.hasEndTime);
        if(endTimeStatement != null){
            setStartTime(Timestamp.valueOf(endTimeStatement.getString()));
        }else{
            setEndTime(null);
        }



        Statement taskIdForDispatcherStatement = resource.getProperty(ETutorVocabulary.hasTaskIdForDispatcher);
        if (taskIdForDispatcherStatement != null) {
            setTaskIdForDispatcher(taskIdForDispatcherStatement.getString());
        }
        Statement sqlSolutionStatement = resource.getProperty(ETutorVocabulary.hasSQLSolution);
        if(sqlSolutionStatement != null){
            setSqlSolution(sqlSolutionStatement.getString());
        }
        Statement maxPointsStatement = resource.getProperty(ETutorVocabulary.hasMaxPoints);
        if(maxPointsStatement != null){
            setMaxPoints(maxPointsStatement.getString());
        }
        Statement diagnoseLevelWeightingStatement = resource.getProperty(ETutorVocabulary.hasDiagnoseLevelWeighting);
        if(diagnoseLevelWeightingStatement != null){
            setDiagnoseLevelWeighting(diagnoseLevelWeightingStatement.getString());
        }
        Statement processingTimeStatement = resource.getProperty(ETutorVocabulary.hasTypicalProcessingTime);
        if (processingTimeStatement != null) {
            setProcessingTime(processingTimeStatement.getString());
        }
        Statement xQuerySolutionStatement = resource.getProperty(ETutorVocabulary.hasXQuerySolution);
        if(xQuerySolutionStatement != null){
            setxQuerySolution(xQuerySolutionStatement.getString());
        }
        Statement xPathSortingStatement = resource.getProperty(ETutorVocabulary.hasXQueryXPathSorting);
        if(xPathSortingStatement != null){
            setxQueryXPathSorting(xPathSortingStatement.getString());
        }

        Statement datalogSolutionStatement = resource.getProperty(ETutorVocabulary.hasDLGSolution);
        if(datalogSolutionStatement != null){
            setDatalogSolution(datalogSolutionStatement.getString());
        }

        Statement datalogQueryStatment = resource.getProperty(ETutorVocabulary.hasDLGQuery);
        if(datalogQueryStatment != null){
            setDatalogQuery(datalogQueryStatment.getString());
        }

        Statement datalogUncheckedTermsStatement = resource.getProperty(ETutorVocabulary.hasUncheckedDLGTerm);
        if(datalogUncheckedTermsStatement != null){
            setDatalogUncheckedTerms(datalogUncheckedTermsStatement.getString());
        }


        setTaskDifficultyId(resource.getProperty(ETutorVocabulary.hasTaskDifficulty).getObject().asResource().getURI());
        setTaskAssignmentTypeId(resource.getProperty(ETutorVocabulary.hasTaskAssignmentType).getObject().asResource().getURI());
        setOrganisationUnit(resource.getProperty(ETutorVocabulary.hasTaskOrganisationUnit).getString());
        setInternalCreator(resource.getProperty(ETutorVocabulary.hasInternalTaskCreator).getString());

        Statement urlStatement = resource.getProperty(ETutorVocabulary.hasTaskUrl);
        if (urlStatement != null) {
            setUrl(new URL(urlStatement.getString()));
        }

        Statement taskInstructionStatement = resource.getProperty(ETutorVocabulary.hasTaskInstruction);
        if (taskInstructionStatement != null) {
            setInstruction(taskInstructionStatement.getString());
        }
        setPrivateTask(resource.getProperty(ETutorVocabulary.isPrivateTask).getBoolean());

        String creationDateAsString = resource.getProperty(ETutorVocabulary.hasTaskCreationDate).getString();
        setCreationDate(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(creationDateAsString).toInstant());

        Statement taskGroupStatement = resource.getProperty(ETutorVocabulary.hasTaskGroup);
        if (taskGroupStatement != null) {
            setTaskGroupId(taskGroupStatement.getObject().asResource().getURI());
        }
    }

    /**
     * Returns the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the creation date.
     *
     * @return the creation date
     */
    public Instant getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creation date.
     *
     * @param creationDate the creation date to set
     */
    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Returns the internal creator.
     *
     * @return the internal creator
     */
    public String getInternalCreator() {
        return internalCreator;
    }

    /**
     * Sets the internal creator.
     *
     * @param internalCreator the internal creator to set
     */
    public void setInternalCreator(String internalCreator) {
        this.internalCreator = internalCreator;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param obj the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(TaskAssignmentDTO obj) {
        return getHeader().compareTo(obj.getHeader());
    }
}
