package at.jku.dke.etutor.service.dto;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO class for a learning goal which extends the new learning goal dto.
 *
 * @author fne
 */
public class LearningGoalDTO extends NewLearningGoalDTO {

    private Instant lastModifiedDate;
    private String owner;
    private List<LearningGoalDTO> subGoals;
    private int referencedFromCount = 0;
    private String id;

    /**
     * Constructor.
     */
    public LearningGoalDTO() {
        // Needed for serialization
        subGoals = new ArrayList<>();
    }

    /**
     * Constructor.
     *
     * @param newLearningGoalDTO the new learning goal dto
     * @param owner              the owner of the learning goal
     * @param lastModifiedDate   the last modification date of the learning goal
     * @param id the id of the learning goal
     */
    public LearningGoalDTO(NewLearningGoalDTO newLearningGoalDTO, String owner, Instant lastModifiedDate, String id) {
        this();
        setName(newLearningGoalDTO.getName());
        setDescription(newLearningGoalDTO.getDescription());
        setPrivateGoal(newLearningGoalDTO.isPrivateGoal());

        this.owner = owner;
        this.lastModifiedDate = lastModifiedDate;
        this.id = id;
    }

    /**
     * Constructor.
     *
     * @param rdfResource the rdf representation of the learning goal.
     * @throws ParseException is thrown if the modification date is stored in the wrong format
     */
    public LearningGoalDTO(Resource rdfResource) throws ParseException {
        this();
        setName(rdfResource.getProperty(RDFS.label).getString());
        setId(rdfResource.getURI());
        Statement descriptionStatement = rdfResource.getProperty(ETutorVocabulary.hasDescription);
        if (descriptionStatement != null) {
            setDescription(descriptionStatement.getString());
        }
        setPrivateGoal(rdfResource.getProperty(ETutorVocabulary.isPrivate).getBoolean());

        this.owner = rdfResource.getProperty(ETutorVocabulary.hasOwner).getString();
        String lastModifiedDateStr = rdfResource.getProperty(ETutorVocabulary.hasChangeDate).getString();
        this.lastModifiedDate = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT
            .parse(lastModifiedDateStr).toInstant();

        StmtIterator subGoalIterator = rdfResource.listProperties(ETutorVocabulary.hasSubGoal);
        try {
            while (subGoalIterator.hasNext()) {
                Statement stmt = subGoalIterator.nextStatement();
                Resource subGoalResource = stmt.getResource();
                subGoals.add(new LearningGoalDTO(subGoalResource));
            }
        } finally {
            subGoalIterator.close();
        }
    }

    /**
     * Returns the last modification date.
     *
     * @return the last modification date
     */
    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * Sets the last modification date.
     *
     * @param lastModifiedDate the last modification date to set
     */
    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the owner.
     *
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Returns the list of sub goals.
     *
     * @return the list of sub goals
     */
    public List<LearningGoalDTO> getSubGoals() {
        return subGoals;
    }

    /**
     * Sets the list of sub goals.
     *
     * @param subGoals the list of sub goals to set
     */
    public void setSubGoals(List<LearningGoalDTO> subGoals) {
        this.subGoals = subGoals;
    }

    /**
     * Returns the count of lectures which hold a reference to this learning goal.
     *
     * @return the count of lectures which have a reference to this learning goal
     */
    public int getReferencedFromCount() {
        return referencedFromCount;
    }

    /**
     * Sets the count of lectures which hold a reference to this learning goal.
     *
     * @param referencedFromCount the reference count to set
     */
    public void setReferencedFromCount(int referencedFromCount) {
        this.referencedFromCount = referencedFromCount;
    }

    /**
     * Returns the id of the learning goal.
     *
     * @return the id of the learning goal
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the learning goal.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
}
