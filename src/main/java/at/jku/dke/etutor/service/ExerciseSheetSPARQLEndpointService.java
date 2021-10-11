package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDisplayDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.LearningGoalAssignmentDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

/**
 * Service endpoint for managing exercise sheets.
 *
 * @author fne
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Service
public non-sealed class ExerciseSheetSPARQLEndpointService extends AbstractSPARQLEndpointService {

    private static final String QRY_CONSTRUCT_EXERCISE_BY_ID =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            CONSTRUCT { ?exerciseSheet ?p ?o.
            			?exerciseSheet etutor:containsLearningGoalAssignment ?assignment.
                        ?assignment etutor:containsLearningGoal ?goal.
                        ?assignment etutor:hasPriority ?priority.
            			?goal rdfs:label ?goalName.
            			?goal a etutor:Goal }
            WHERE {
              ?exerciseSheet a etutor:ExerciseSheet.
              ?exerciseSheet ?p ?o.
              OPTIONAL {
                ?exerciseSheet etutor:containsLearningGoalAssignment ?assignment.
                ?assignment etutor:containsLearningGoal ?goal.
                ?assignment etutor:hasPriority ?priority.
                ?goal rdfs:label ?goalName
              }
            }
            """;

    private static final String DELETE_EXERCISE_BY_ID =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE { ?exerciseSheet ?predicate ?object.
                     ?exerciseSheet etutor:containsLearningGoalAssignment ?assignment.
                     ?assignment etutor:containsLearningGoal ?goal.
                     ?assignment etutor:hasPriority ?priority. }
            WHERE {
              ?exerciseSheet a etutor:ExerciseSheet.
              ?exerciseSheet ?predicate ?object.
              OPTIONAL {
                ?exerciseSheet etutor:containsLearningGoalAssignment ?assignment.
                ?assignment etutor:containsLearningGoal ?goal.
                ?assignment etutor:hasPriority ?priority.
              }
            }
            """;

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

    /**
     * Updates the given exercise sheet.
     *
     * @param exerciseSheetDTO the exercise sheet data
     */
    public void updateExerciseSheet(ExerciseSheetDTO exerciseSheetDTO) {
        Objects.requireNonNull(exerciseSheetDTO);

        ParameterizedSparqlString query = new ParameterizedSparqlString(
            """
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

                DELETE {
                    ?exerciseSheet rdfs:label ?lbl.
                    ?exerciseSheet etutor:hasExerciseSheetDifficulty ?difficulty.
                    ?exerciseSheet etutor:containsLearningGoalAssignment ?goalAssignment.
                    ?goalAssignment a etutor:LearningGoalAssignment.
                    ?goalAssignment etutor:containsLearningGoal ?goal.
                    ?goalAssignment etutor:hasPriority ?priority.
                    ?exerciseSheet etutor:hasExerciseSheetTaskCount ?taskCount.
                    ?exerciseSheet etutor:isGenerateWholeExerciseSheet ?generate.
                }
                INSERT {
                    ?exerciseSheet rdfs:label ?newLbl.
                    ?exerciseSheet etutor:hasExerciseSheetDifficulty ?newDifficulty.
                    ?exerciseSheet etutor:hasExerciseSheetTaskCount ?newTaskCount.
                    ?exerciseSheet etutor:isGenerateWholeExerciseSheet ?newGenerate.
                }
                WHERE {
                  ?exerciseSheet a etutor:ExerciseSheet.
                  ?exerciseSheet rdfs:label ?lbl.
                  ?exerciseSheet etutor:hasExerciseSheetDifficulty ?difficulty.
                  ?exerciseSheet etutor:hasExerciseSheetTaskCount ?taskCount.
                  ?exerciseSheet etutor:isGenerateWholeExerciseSheet ?generate.
                  OPTIONAL {
                    ?exerciseSheet etutor:containsLearningGoalAssignment ?goalAssignment.
                    ?goalAssignment a etutor:LearningGoalAssignment.
                    ?goalAssignment etutor:containsLearningGoal ?goal.
                    ?goalAssignment etutor:hasPriority ?priority.
                  }
                }
                """
        );

        query.setIri("?exerciseSheet", exerciseSheetDTO.getId());
        query.setLiteral("?newLbl", exerciseSheetDTO.getName().trim());
        query.setIri("?newDifficulty", exerciseSheetDTO.getDifficultyId());
        query.setLiteral("?newTaskCount", exerciseSheetDTO.getTaskCount());
        query.setLiteral("?newGenerate", exerciseSheetDTO.isGenerateWholeExerciseSheet());

        ParameterizedSparqlString goalAssignmentInsertQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            INSERT {
            """);

        int i = 0;
        for (LearningGoalAssignmentDTO learningGoalAssignment : exerciseSheetDTO.getLearningGoals()) {
            goalAssignmentInsertQry.append(MessageFormat.format("""
                ?exerciseSheet etutor:containsLearningGoalAssignment _:b{0}.
                _:b{0} a etutor:LearningGoalAssignment.
                _:b{0} etutor:hasPriority\040""", i));
            goalAssignmentInsertQry.appendLiteral(String.valueOf(learningGoalAssignment.getPriority()), XSDDatatype.XSDunsignedInt);
            goalAssignmentInsertQry.append(MessageFormat.format("""
                .
                _:b{0} etutor:containsLearningGoal\040""", i));
            goalAssignmentInsertQry.appendIri(learningGoalAssignment.getLearningGoal().getId());
            goalAssignmentInsertQry.append(" .\n");

            i++;
        }

        goalAssignmentInsertQry.append("""
            } WHERE {
                ?exerciseSheet a etutor:ExerciseSheet.
            }
            """);
        goalAssignmentInsertQry.setIri("?exerciseSheet", exerciseSheetDTO.getId());

        try (RDFConnection connection = getConnection()) {
            connection.update(query.asUpdate());

            if (i > 0) {
                connection.update(goalAssignmentInsertQry.asUpdate());
            }
        }
    }

    /**
     * Returns an exercise sheet by its id.
     *
     * @param id the internal id
     * @return {@link Optional} which is either empty or contains the corresponding exercise sheet dto
     * @throws ParseException if an internal parsing error occurs
     */
    public Optional<ExerciseSheetDTO> getExerciseSheetById(String id) throws ParseException {
        Objects.requireNonNull(id);

        String exerciseURL = ETutorVocabulary.createExerciseSheetURLString(id);
        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_CONSTRUCT_EXERCISE_BY_ID);
        query.setIri("?exerciseSheet", exerciseURL);

        try (RDFConnection connection = getConnection()) {
            Model model = connection.queryConstruct(query.asQuery());
            if (model.isEmpty()) {
                return Optional.empty();
            }
            Resource exerciseSheetResource = model.getResource(exerciseURL);
            return Optional.of(new ExerciseSheetDTO(exerciseSheetResource));
        }
    }

    /**
     * Returns a paged exercise sheet display (name + id). An optional name filter can be passed to this method.
     *
     * @param nameQry the optional name filter, might be null
     * @param page    the mandatory pageable object
     * @return {@code Slice} containing the elements
     */
    public Slice<ExerciseSheetDisplayDTO> getFilteredExerciseSheetDisplayDTOs(String nameQry, Pageable page) {
        Objects.requireNonNull(nameQry);
        Objects.requireNonNull(page);

        ParameterizedSparqlString query = new ParameterizedSparqlString(
            """
                PREFIX text:   <http://jena.apache.org/text#>
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

                SELECT (STR(?exerciseSheet) as ?id) ?name (COUNT(?individualAssignment) AS ?cnt)
                WHERE {
                """
        );

        if (StringUtils.isNotBlank(nameQry)) {
            query.append(String.format("?exerciseSheet text:query (rdfs:label \"*%s*\").%n", nameQry));
        }

        query.append(
            """
                  ?exerciseSheet a etutor:ExerciseSheet.
                  ?exerciseSheet rdfs:label ?name.
                  OPTIONAL {
                    ?individualAssignment etutor:fromExerciseSheet ?exerciseSheet.
                  }
                }
                GROUP BY ?exerciseSheet ?name
                ORDER BY (LCASE(?name))
                """
        );

        if (page.isPaged()) {
            query.append("LIMIT ");
            query.append(page.getPageSize() + 1);
            query.append("\nOFFSET ");
            query.append(page.getOffset());
        }

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution queryExecution = connection.query(query.asQuery())) {
                ResultSet set = queryExecution.execSelect();
                List<ExerciseSheetDisplayDTO> resultList = new ArrayList<>();

                while (set.hasNext()) {
                    QuerySolution querySolution = set.nextSolution();

                    String id = querySolution.getLiteral("?id").getString();
                    String name = querySolution.getLiteral("?name").getString();
                    int count = querySolution.getLiteral("?cnt").getInt();
                    resultList.add(new ExerciseSheetDisplayDTO(id, name, count, false));
                }

                boolean hasNext = page.isPaged() && resultList.size() > page.getPageSize();
                return new SliceImpl<>(hasNext ? resultList.subList(0, page.getPageSize()) : resultList, page, hasNext);
            }
        }
    }

    /**
     * Returns a paged exercise sheet display (name + id). An optional name filter can be passed to this method.
     *
     * @param nameQry the optional name filter, might be null
     * @param page    the mandatory pageable object
     * @return {@code Page} containing the elements
     */
    public Page<ExerciseSheetDisplayDTO> getFilteredExerciseSheetDisplayDTOsAsPage(String nameQry, Pageable page) {
        Objects.requireNonNull(nameQry);
        Objects.requireNonNull(page);

        ParameterizedSparqlString countQry = new ParameterizedSparqlString(
            """
                PREFIX text:   <http://jena.apache.org/text#>
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

                SELECT (COUNT(DISTINCT ?exerciseSheet) as ?cnt)
                WHERE {
                """
        );
        ParameterizedSparqlString query = new ParameterizedSparqlString(
            """
                PREFIX text:   <http://jena.apache.org/text#>
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

                SELECT (STR(?exerciseSheet) as ?id) ?name (COUNT(?individualAssignment) AS ?cnt)
                WHERE {
                """
        );

        if (StringUtils.isNotBlank(nameQry)) {
            query.append(String.format("?exerciseSheet text:query (rdfs:label \"*%s*\").%n", nameQry));
            countQry.append(String.format("?exerciseSheet text:query (rdfs:label \"*%s*\").%n", nameQry));
        }

        query.append(
            """
                  ?exerciseSheet a etutor:ExerciseSheet.
                  ?exerciseSheet rdfs:label ?name.
                  OPTIONAL {
                    ?individualAssignment etutor:fromExerciseSheet ?exerciseSheet.
                  }
                }
                GROUP BY ?exerciseSheet ?name
                ORDER BY (LCASE(?name))
                """
        );

        countQry.append(
            """
                  ?exerciseSheet a etutor:ExerciseSheet.
                  ?exerciseSheet rdfs:label ?name.
                }
                """
        );

        if (page.isPaged()) {
            query.append("LIMIT ");
            query.append(page.getPageSize());
            query.append("\nOFFSET ");
            query.append(page.getOffset());
        }

        try (RDFConnection connection = getConnection()) {
            List<ExerciseSheetDisplayDTO> list = new ArrayList<>();
            int count;

            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                while (set.hasNext()) {
                    QuerySolution querySolution = set.nextSolution();

                    String id = querySolution.getLiteral("?id").getString();
                    String name = querySolution.getLiteral("?name").getString();
                    int cnt = querySolution.getLiteral("?cnt").getInt();
                    list.add(new ExerciseSheetDisplayDTO(id, name, cnt, false));
                }
            }
            try (QueryExecution execution = connection.query(countQry.asQuery())) {
                ResultSet set = execution.execSelect();
                set.hasNext();
                count = set.nextSolution().getLiteral("?cnt").getInt();
            }

            return PageableExecutionUtils.getPage(list, page, () -> count);
        }
    }

    /**
     * Deletes an exercise sheet by id.
     *
     * @param internalId the exercise sheet's id
     */
    public void deleteExerciseSheetById(String internalId) {
        Objects.requireNonNull(internalId);
        ParameterizedSparqlString query = new ParameterizedSparqlString(DELETE_EXERCISE_BY_ID);
        String exerciseURL = ETutorVocabulary.createExerciseSheetURLString(internalId);
        query.setIri("?exerciseSheet", exerciseURL);

        try (RDFConnection connection = getConnection()) {
            connection.update(query.asUpdate());
        }
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
    private Resource constructResourceFromNewExerciseSheetDTO(
        NewExerciseSheetDTO newExerciseSheetDTO,
        String user,
        Instant time,
        Model model,
        String uuid
    ) {
        Resource resource = ETutorVocabulary.createExerciseSheetOfModel(uuid, model);

        resource.addProperty(ETutorVocabulary.hasInternalExerciseSheetCreator, user);
        resource.addProperty(ETutorVocabulary.hasExerciseSheetCreationTime, instantToRDFString(time), XSDDatatype.XSDdateTime);
        resource.addProperty(RDFS.label, newExerciseSheetDTO.getName().trim());
        resource.addProperty(ETutorVocabulary.hasExerciseSheetDifficulty, model.createResource(newExerciseSheetDTO.getDifficultyId()));
        resource.addProperty(RDF.type, ETutorVocabulary.ExerciseSheet);
        resource.addProperty(
            ETutorVocabulary.hasExerciseSheetTaskCount,
            String.valueOf(newExerciseSheetDTO.getTaskCount()),
            XSDDatatype.XSDint
        );
        resource.addProperty(ETutorVocabulary.isGenerateWholeExerciseSheet, String.valueOf(newExerciseSheetDTO.isGenerateWholeExerciseSheet()), XSDDatatype.XSDboolean);

        for (LearningGoalAssignmentDTO entry : newExerciseSheetDTO.getLearningGoals()) {
            Resource assignmentResource = model.createResource();
            assignmentResource.addProperty(RDF.type, ETutorVocabulary.LearningGoalAssignment);
            assignmentResource.addProperty(ETutorVocabulary.hasPriority, String.valueOf(entry.getPriority()), XSDDatatype.XSDunsignedInt);
            assignmentResource.addProperty(ETutorVocabulary.containsLearningGoal, model.createResource(entry.getLearningGoal().getId()));

            resource.addProperty(ETutorVocabulary.containsLearningGoalAssignment, assignmentResource);
        }

        return resource;
    }
    //endregion
}
