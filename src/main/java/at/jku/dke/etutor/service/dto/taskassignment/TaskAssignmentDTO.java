package at.jku.dke.etutor.service.dto.taskassignment;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;

/**
 * Class for a task assignment.
 *
 * @author fne
 */
public class TaskAssignmentDTO extends NewTaskAssignmentDTO implements Comparable<TaskAssignmentDTO> {

    private String id;
    private Instant creationDate;

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
     */
    public TaskAssignmentDTO(NewTaskAssignmentDTO newTaskAssignmentDTO, String id, Instant creationDate) {
        super();

        setLearningGoalId(newTaskAssignmentDTO.getLearningGoalId());
        setCreator(newTaskAssignmentDTO.getCreator());
        setHeader(newTaskAssignmentDTO.getHeader());
        setProcessingTime(newTaskAssignmentDTO.getProcessingTime());
        setTaskDifficultyId(newTaskAssignmentDTO.getTaskDifficultyId());
        setOrganisationUnit(newTaskAssignmentDTO.getOrganisationUnit());
        setUrl(newTaskAssignmentDTO.getUrl());
        setInstruction(newTaskAssignmentDTO.getInstruction());
        setPrivateTask(newTaskAssignmentDTO.isPrivateTask());

        setId(id);
        setCreationDate(creationDate);
    }

    /**
     * Constructor.
     *
     * @param resource       the rdf resource which contains this assignment
     * @param learningGoalId the learning goal which is associated with this assignment
     * @throws MalformedURLException if the url can not be parsed
     * @throws ParseException        if a date can not be parsed
     */
    public TaskAssignmentDTO(Resource resource, String learningGoalId) throws MalformedURLException, ParseException {
        this();

        setId(resource.getURI());
        setLearningGoalId(learningGoalId);
        setCreator(resource.getProperty(ETutorVocabulary.hasTaskCreator).getString());
        setHeader(resource.getProperty(ETutorVocabulary.hasTaskHeader).getString());

        Statement processingTimeStatement = resource.getProperty(ETutorVocabulary.hasTypicalProcessingTime);
        if (processingTimeStatement != null) {
            setProcessingTime(processingTimeStatement.getString());
        }
        setTaskDifficultyId(resource.getProperty(ETutorVocabulary.hasTaskDifficulty).getObject().asResource().getURI());
        setOrganisationUnit(resource.getProperty(ETutorVocabulary.hasTaskOrganisationUnit).getString());

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
