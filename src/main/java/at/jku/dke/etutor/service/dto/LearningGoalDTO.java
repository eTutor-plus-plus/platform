package at.jku.dke.etutor.service.dto;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import java.text.ParseException;
import java.time.Instant;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;

/**
 * DTO class for a learning goal which extends the new learning goal dto.
 *
 * @author fne
 */
public class LearningGoalDTO extends NewLearningGoalDTO implements Comparable<LearningGoalDTO> {

    private Instant lastModifiedDate;
    private String owner;
    private SortedSet<LearningGoalDTO> subGoals;
    private int referencedFromCount = 0;
    private String id;

    /**
     * Constructor.
     */
    public LearningGoalDTO() {
        // Needed for serialization
        subGoals = new TreeSet<>();
    }

    /**
     * Constructor.
     *
     * @param newLearningGoalDTO the new learning goal dto
     * @param owner              the owner of the learning goal
     * @param lastModifiedDate   the last modification date of the learning goal
     * @param id                 the id of the learning goal
     */
    public LearningGoalDTO(NewLearningGoalDTO newLearningGoalDTO, String owner, Instant lastModifiedDate, String id) {
        this();
        setName(newLearningGoalDTO.getName());
        setDescription(newLearningGoalDTO.getDescription());
        setPrivateGoal(newLearningGoalDTO.isPrivateGoal());
        setNeedVerification(newLearningGoalDTO.isNeedVerification());

        this.owner = owner;
        this.lastModifiedDate = lastModifiedDate;
        this.id = id;
    }

    /**
     * Constructor.
     *
     * @param rdfResource the rdf representation of the learning goal
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
        this.lastModifiedDate = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.parse(lastModifiedDateStr).toInstant();

        Statement referenceCntStatement = rdfResource.getProperty(ETutorVocabulary.hasReferenceCnt);
        if (referenceCntStatement != null) {
            setReferencedFromCount(referenceCntStatement.getInt());
        }

        Statement verificationStmt = rdfResource.getProperty(ETutorVocabulary.needsVerificationBeforeCompletion);
        if (verificationStmt != null) {
            setNeedVerification(verificationStmt.getBoolean());
        }

        StmtIterator subGoalIterator = rdfResource.listProperties(ETutorVocabulary.hasSubGoal);
        try {
            while (subGoalIterator.hasNext()) {
                Statement stmt = subGoalIterator.nextStatement();
                Resource subGoalResource = stmt.getResource();
                subGoals.add(getSubGoalForConstructor(subGoalResource));
            }
        } finally {
            subGoalIterator.close();
        }
    }

    /**
     * Method which creates a new sub goal and is designed to be used only
     * in the constructor.
     *
     * @param subGoalResource the resource for the sub goal
     * @return a new learning goal dto which represents a sub goal
     * @throws ParseException is thrown if the modification date is stored in the wrong format
     */
    protected LearningGoalDTO getSubGoalForConstructor(Resource subGoalResource) throws ParseException {
        return new LearningGoalDTO(subGoalResource);
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
     * Returns the set of sub goals.
     *
     * @return the set of sub goals
     */
    public SortedSet<LearningGoalDTO> getSubGoals() {
        return subGoals;
    }

    /**
     * Sets the set of sub goals.
     *
     * @param subGoals the set of sub goals to set
     */
    public void setSubGoals(SortedSet<LearningGoalDTO> subGoals) {
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
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(LearningGoalDTO o) {
        int res = getName().compareToIgnoreCase(o.getName());
        if (res == 0) {
            return getOwner().compareToIgnoreCase(o.getOwner());
        }
        return res;
    }
}
