package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.taskassignment.LearningGoalDisplayDTO;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Service endpoint for managing exercise sheets.
 *
 * @author fne
 */
@Service
public class ExerciseSheetSPARQLEndpointService extends AbstractSPARQLEndpointService {
    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public ExerciseSheetSPARQLEndpointService(RDFConnectionFactory rdfConnectionFactory) {
        super(rdfConnectionFactory);
    }

    /**
     * Inserts a new exercise sheet into the knowledge graph.
     *
     * @param newExerciseSheetDTO the new exercise sheet dto
     * @param user                the currently logged-in user
     * @return the created exercise sheet
     * @throws ParseException if an internal parsing error occurs
     */
    public ExerciseSheetDTO insertNewExerciseSheet(NewExerciseSheetDTO newExerciseSheetDTO, String user) throws ParseException {
        Objects.requireNonNull(newExerciseSheetDTO);
        Objects.requireNonNull(user);

        Instant now = Instant.now();
        String uuid = UUID.randomUUID().toString();
        Model model = ModelFactory.createDefaultModel();

        Resource resource = constructResourceFromNewExerciseSheetDTO(newExerciseSheetDTO, user, now, model, uuid);

        try (RDFConnection connection = getConnection()) {
            connection.load(model);
        }

        return new ExerciseSheetDTO(newExerciseSheetDTO, resource.getURI(), instantFromRDFString(instantToRDFString(now)), user);
    }

    //region Private helper methods

    /**
     * Constructs a exercise sheet resource.
     *
     * @param newExerciseSheetDTO the new exercise sheet dto
     * @param user                the creator
     * @param time                the creation time
     * @param model               the base model
     * @param uuid                the generated uuid
     * @return {@link Resource} which represents the new exercise sheet.
     */
    private Resource constructResourceFromNewExerciseSheetDTO(NewExerciseSheetDTO newExerciseSheetDTO, String user, Instant time, Model model, String uuid) {
        Resource resource = ETutorVocabulary.createExerciseSheetOfModel(uuid, model);

        resource.addProperty(ETutorVocabulary.hasInternalExerciseSheetCreator, user);
        resource.addProperty(ETutorVocabulary.hasExerciseSheetCreationTime, instantToRDFString(time), XSDDatatype.XSDdateTime);
        resource.addProperty(RDFS.label, newExerciseSheetDTO.getName().trim());
        resource.addProperty(ETutorVocabulary.hasExerciseSheetDifficulty, model.createResource(newExerciseSheetDTO.getDifficultyId()));
        resource.addProperty(RDF.type, ETutorVocabulary.ExerciseSheet);

        for (LearningGoalDisplayDTO entry : newExerciseSheetDTO.getLearningGoals()) {
            resource.addProperty(ETutorVocabulary.containsLearningGoal, model.createResource(entry.getId()));
        }

        return resource;
    }
    //endregion
}
