package at.jku.dke.etutor.service.dto.exercisesheet;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
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
 * DTO class for an existing exercise sheet.
 *
 * @author fne
 */
public class ExerciseSheetDTO extends NewExerciseSheetDTO {

    private String id;
    private Instant creationDate;
    private String internalCreator;

    /**
     * Empty constructor.
     */
    public ExerciseSheetDTO() {
        // For serialization
    }

    /**
     * Constructor.
     *
     * @param baseDTO         the base dto
     * @param id              the generated id
     * @param creationDate    the creation date
     * @param internalCreator the internal creator
     */
    public ExerciseSheetDTO(NewExerciseSheetDTO baseDTO, String id, Instant creationDate, String internalCreator) {
        setName(baseDTO.getName().trim());
        setDifficultyId(baseDTO.getDifficultyId());
        setLearningGoals(baseDTO.getLearningGoals());
        setTaskCount(baseDTO.getTaskCount());
        setGenerateWholeExerciseSheet(baseDTO.isGenerateWholeExerciseSheet());

        this.id = id;
        this.creationDate = creationDate;
        this.internalCreator = internalCreator;
    }

    /**
     * Constructor
     *
     * @param resource the rdf resource from the knowledge graph
     * @throws ParseException if a date can not be parsed
     */
    public ExerciseSheetDTO(Resource resource) throws ParseException {
        super();
        setName(resource.getProperty(RDFS.label).getString());
        setDifficultyId(resource.getProperty(ETutorVocabulary.hasExerciseSheetDifficulty).getResource().getURI());
        setId(resource.getURI());
        setInternalCreator(resource.getProperty(ETutorVocabulary.hasInternalExerciseSheetCreator).getString());
        setCreationDate(
            DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT
                .parse(resource.getProperty(ETutorVocabulary.hasExerciseSheetCreationTime).getString())
                .toInstant()
        );
        setGenerateWholeExerciseSheet(resource.getProperty(ETutorVocabulary.isGenerateWholeExerciseSheet).getBoolean());
        setTaskCount(resource.getProperty(ETutorVocabulary.hasExerciseSheetTaskCount).getInt());
        List<LearningGoalAssignmentDTO> goals = new ArrayList<>();
        StmtIterator stmtIterator = resource.listProperties(ETutorVocabulary.containsLearningGoalAssignment);
        try {
            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.nextStatement();
                Resource goalAssignmentResource = statement.getObject().asResource();
                int priority = goalAssignmentResource.getProperty(ETutorVocabulary.hasPriority).getInt();

                Resource goalResource = goalAssignmentResource.getProperty(ETutorVocabulary.containsLearningGoal).getResource();
                String goalName = goalResource.getProperty(RDFS.label).getString();
                goals.add(new LearningGoalAssignmentDTO(new LearningGoalDisplayDTO(goalResource.getURI(), goalName), priority));
            }
        } finally {
            stmtIterator.close();
        }
        setLearningGoals(goals);
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
     * Sets the creation date
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
}
