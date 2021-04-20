package at.jku.dke.etutor.service.dto;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import java.text.ParseException;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

/**
 * DTO class for a displayable learning goal assignment.
 * This class is an advanced {@link LearningGoalDTO} with
 * an optional root id reference.
 *
 * @author fne
 */
public class DisplayLearningGoalAssignmentDTO extends LearningGoalDTO {

    private String rootId;

    /**
     * Constructor for serialization.
     */
    public DisplayLearningGoalAssignmentDTO() {
        super();
    }

    /**
     * Constructor.
     *
     * @param resource the rdf representation of the learning goal
     * @throws ParseException is thrown if the modification date is stored in the wrong format
     */
    public DisplayLearningGoalAssignmentDTO(Resource resource) throws ParseException {
        super(resource);
        Statement rootIdStatement = resource.getProperty(ETutorVocabulary.hasRootGoal);
        if (rootIdStatement != null) {
            setRootId(rootIdStatement.getResource().getURI());
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
    @Override
    protected LearningGoalDTO getSubGoalForConstructor(Resource subGoalResource) throws ParseException {
        return new DisplayLearningGoalAssignmentDTO(subGoalResource);
    }

    /**
     * Returns the optional root id.
     *
     * @return the optional root id
     */
    public String getRootId() {
        return rootId;
    }

    /**
     * Sets the optional root id.
     *
     * @param rootId the optional root id to set
     */
    public void setRootId(String rootId) {
        this.rootId = rootId;
    }
}
