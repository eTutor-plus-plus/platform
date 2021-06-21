package at.jku.dke.etutor.service.dto.taskassignment;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.text.ParseException;
import java.time.Instant;

/**
 * DTO class which represents an already existent task group.
 *
 * @author fne
 */
public class TaskGroupDTO extends NewTaskGroupDTO {
    private String id;
    private String creator;
    private Instant changeDate;

    /**
     * Constructor.
     *
     * @param name        the mandatory name
     * @param description the optional description
     * @param id          the internal id
     * @param creator     the creator
     * @param changeDate  the change date
     */
    public TaskGroupDTO(String name, String description, String id, String creator, Instant changeDate) {
        super(name, description);
        this.id = id;
        this.creator = creator;
        this.changeDate = changeDate;
    }

    /**
     * Empty constructor.
     */
    public TaskGroupDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param resource the RDF resource
     */
    public TaskGroupDTO(Resource resource) {
        setId(resource.getURI());
        setName(resource.getProperty(ETutorVocabulary.hasTaskGroupName).getString());

        Statement descriptionStatement = resource.getProperty(ETutorVocabulary.hasTaskGroupDescription);

        if (descriptionStatement != null) {
            setDescription(descriptionStatement.getString());
        }
        setCreator(resource.getProperty(ETutorVocabulary.hasTaskGroupCreator).getString());
        try {
            setChangeDate((DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(
                resource.getProperty(ETutorVocabulary.hasTaskGroupChangeDate).getString()).toInstant()));
        } catch (ParseException e) {
            setChangeDate(null);
        }
    }

    /**
     * Returns the internal id.
     *
     * @return the internal id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the internal id.
     *
     * @param id the internal id to set
     */
    public void setId(String id) {
        this.id = id;
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
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Returns the change date.
     *
     * @return the change date
     */
    public Instant getChangeDate() {
        return changeDate;
    }

    /**
     * Sets the change date.
     *
     * @param changeDate the change date to set
     */
    public void setChangeDate(Instant changeDate) {
        this.changeDate = changeDate;
    }
}
