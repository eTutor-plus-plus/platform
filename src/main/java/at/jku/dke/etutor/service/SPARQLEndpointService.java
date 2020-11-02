package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.LearningGoalDTO;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.ParseException;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Service class for SPARQL related operations.
 *
 * @author fne
 */
@Service
public class SPARQLEndpointService {

    private static final String SCHEME_PATH = "/rdf/scheme.ttl";

    private static final String QRY_GOAL_COUNT = """
        SELECT (COUNT(DISTINCT ?subject) as ?count)
        WHERE {
        	?subject ?predicate ?object.
          FILTER(?subject = <http://www.dke.uni-linz.ac.at/etutorpp/%s/Goal#%s> )
        }
        """;

    private static final String QRY_ID_GOAL_COUNT = """
        SELECT (COUNT(DISTINCT ?subject) as ?count)
        WHERE {
        	?subject ?predicate ?object.
          FILTER(?subject = <%s> )
        }
        """;

    private final Logger log = LoggerFactory.getLogger(SPARQLEndpointService.class);

    private final RDFConnectionFactory rdfConnectionFactory;

    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public SPARQLEndpointService(RDFConnectionFactory rdfConnectionFactory) {
        this.rdfConnectionFactory = rdfConnectionFactory;
    }

    /**
     * Inserts the RDF scheme into the configured fuseki instance.
     */
    public void insertScheme() {
        try (RDFConnection conn = getConnection();
             InputStream schemeStream = getClass().getResourceAsStream(SCHEME_PATH)) {

            Model model = ModelFactory.createDefaultModel();
            model.read(schemeStream, null, "TTL");

            Txn.executeWrite(conn, () -> conn.load(model));
        } catch (IOException ex) {
            log.error("Internal error! Must not occur!", ex);
        }
    }

    /**
     * Inserts a new learning goal into the RDF graph.
     *
     * @param newLearningGoalDTO the dto of the new learning goal
     * @param owner              the owner of the learning goal
     * @return the created learning goal
     * @throws LearningGoalAlreadyExistsException if the learning goal already exists
     */
    public LearningGoalDTO insertNewLearningGoal(NewLearningGoalDTO newLearningGoalDTO, String owner)
        throws LearningGoalAlreadyExistsException {

        Instant now = Instant.now();

        Model model = ModelFactory.createDefaultModel();
        Resource goal = constructLearningGoalFromDTO(newLearningGoalDTO, owner, model, now, false);

        try (RDFConnection conn = getConnection()) {
            int cnt;

            try (QueryExecution qExec = conn.query(String.format(QRY_GOAL_COUNT, owner,
                newLearningGoalDTO.getNameForRDF()))) {
                cnt = qExec.execSelect().next().getLiteral("?count").getInt();
            }

            if (cnt > 0) {
                throw new LearningGoalAlreadyExistsException();
            }

            conn.load(model);
        }

        return new LearningGoalDTO(newLearningGoalDTO, owner, now, goal.getURI());
    }

    /**
     * Updates an existing learning goal.
     *
     * @param learningGoalDTO the data of the learning goal
     * @throws LearningGoalNotExistsException if the learning goal does not exist
     */
    public void updateLearningGoal(LearningGoalDTO learningGoalDTO) throws LearningGoalNotExistsException {
        try (RDFConnection conn = getConnection()) {
            int cnt;

            try (QueryExecution qExec = conn.query(String.format(QRY_ID_GOAL_COUNT,
                learningGoalDTO.getId()))) {
                cnt = qExec.execSelect().next().getLiteral("?count").getInt();
            }

            if (cnt == 0) {
                throw new LearningGoalNotExistsException();
            }

            Instant now = Instant.now();
            String nowStr = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(Date.from(now));

            String description = ObjectUtils.firstNonNull(learningGoalDTO.getDescription(), "");

            String updateQry = String.format("""
                    PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                    PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>

                    DELETE {
                      ?subject etutor:hasChangeDate ?changeDate.
                      ?subject etutor:hasDescription ?description.
                      ?subject etutor:isPrivate ?private
                    }
                    INSERT {
                      ?subject etutor:hasChangeDate "%s"^^xsd:dateTime.
                      ?subject etutor:hasDescription "%s".
                      ?subject etutor:isPrivate %b
                    }
                    WHERE {
                      ?subject etutor:hasChangeDate ?changeDate.
                      ?subject etutor:hasDescription ?description.
                      ?subject etutor:isPrivate ?private.
                      FILTER(?subject = <%s> )
                    }
                    """, nowStr, description, learningGoalDTO.isPrivateGoal(),
                learningGoalDTO.getId());

            conn.update(updateQry);
        }
    }

    /**
     * Inserts a new sub goal.
     *
     * @param newLearningGoalDTO the dto of the new learning goal
     * @param owner              the owner of the learning goal
     * @param parentGoalName     the name of the parent goal
     * @return the created learning goal
     * @throws LearningGoalAlreadyExistsException if the learning goal already exists
     * @throws LearningGoalNotExistsException     if the parent goal could not be found
     */
    public LearningGoalDTO insertSubGoal(NewLearningGoalDTO newLearningGoalDTO, String owner, String parentGoalName)
        throws LearningGoalAlreadyExistsException, LearningGoalNotExistsException {

        Instant now = Instant.now();
        Model model = ModelFactory.createDefaultModel();

        try (RDFConnection conn = getConnection()) {
            int cnt;

            try (QueryExecution qExec = conn.query(String.format(QRY_GOAL_COUNT, owner,
                newLearningGoalDTO.getNameForRDF()))) {
                cnt = qExec.execSelect().next().getLiteral("?count").getInt();
            }

            if (cnt > 0) {
                throw new LearningGoalAlreadyExistsException();
            }

            String escapedParentGoalName = URLEncoder.encode(parentGoalName.replace(' ', '_'), Charsets.UTF_8);

            Boolean superGoalPrivate = isLearningGoalPrivate(conn, owner, escapedParentGoalName);

            if (superGoalPrivate == null) {
                throw new LearningGoalNotExistsException();
            }

            Resource newGoal = constructLearningGoalFromDTO(newLearningGoalDTO, owner, model, now, superGoalPrivate);


            Resource parentGoalResource = ETutorVocabulary.createUserGoalResourceOfModel(owner, escapedParentGoalName, model);
            parentGoalResource.addProperty(ETutorVocabulary.hasSubGoal, newGoal);

            conn.load(model);

            return new LearningGoalDTO(newLearningGoalDTO, owner, now, newGoal.getURI());
        }
    }

    /**
     * Returns all learning goals which are visible for the given owner.
     *
     * @param owner the owner of requested learning goals
     * @return a list of all {@link LearningGoalDTO} which are visible for the given owner
     * @throws InternalModelException if the internal date format is not valid
     */
    public SortedSet<LearningGoalDTO> getVisibleLearningGoalsForUser(String owner) throws InternalModelException {
        String queryStr = String.format("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

            CONSTRUCT { ?subject ?predicate ?object }
            WHERE {
              {
                  ?subject ?predicate ?object.
                  ?subject rdfs:label ?lbl
                  {
                    ?subject etutor:isPrivate false.
                  }
                  UNION
                  {
                    ?subject etutor:isPrivate true.
                    ?subject etutor:hasOwner "%s".
                  }
              } UNION {
                  BIND(rdf:type AS ?predicate)
                  BIND(etutor:SubGoal AS ?object)
                  ?goal etutor:hasSubGoal ?subject .
              }
            }
            """, owner);

        SortedSet<LearningGoalDTO> goalList = new TreeSet<>(Comparator.comparing(NewLearningGoalDTO::getName));

        try (RDFConnection conn = getConnection()) {
            Model resultModel = conn.queryConstruct(queryStr);
            ResIterator iterator = null;

            try {
                iterator = resultModel.listSubjects();
                while (iterator.hasNext()) {
                    Resource resource = iterator.next();
                    if (!resource.hasProperty(RDF.type, ETutorVocabulary.SubGoal)) {
                        goalList.add(new LearningGoalDTO(resource));
                    }
                }
            } catch (ParseException ex) {
                log.error("Parsing exception", ex);
                throw new InternalModelException(ex);
            } finally {
                if (iterator != null) {
                    iterator.close();
                }
            }

            return goalList;
        }
    }

    /**
     * Returns whether a learning goal is private or not. If the learning goal can't be found, {@code null}
     * will be returned.
     *
     * @param owner            the owner of the learning goal
     * @param learningGoalName the rdf encoded name of the learning goal
     * @return {@code null} if the goal has not been found, otherwise the coresponding {@code boolean} value
     */
    public Boolean isLearningGoalPrivate(String owner, String learningGoalName) {
        try (RDFConnection conn = getConnection()) {
            return isLearningGoalPrivate(conn, owner, learningGoalName);
        }
    }

    //region Private Methods

    /**
     * Returns whether a learning goal is private or not. If the learning goal can't be found, {@code null}
     * will be returned.
     *
     * @param conn             the rdf connection
     * @param owner            the owner of the learning goal
     * @param learningGoalName the rdf encoded name of the learning goal
     * @return {@code null} if the goal has not been found, otherwise the corresponding {@code boolean} value
     */
    private Boolean isLearningGoalPrivate(RDFConnection conn, String owner, String learningGoalName) {
        String query = String.format("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT  ?privateGoal
            WHERE {
              <http://www.dke.uni-linz.ac.at/etutorpp/%s/Goal#%s> etutor:isPrivate ?privateGoal
            }
            """, owner, learningGoalName);
        try (QueryExecution exec = conn.query(query)) {
            ResultSet set = exec.execSelect();

            if (!set.hasNext()) {
                return null;
            }
            QuerySolution solution = set.nextSolution();
            return solution.getLiteral("?privateGoal").getBoolean();
        }
    }

    /**
     * Creates a new rdf connection to the configured fuseki server.
     *
     * @return new rdf connection
     */
    private RDFConnection getConnection() {
        return rdfConnectionFactory.getRDFConnection();
    }

    /**
     * Creates a new learning goal from the given parameters.
     *
     * @param newLearningGoalDTO the dto of the new learning goal
     * @param owner              the owner of the learning goal
     * @param model              the rdf model which should be used
     * @param creationTime       the creation time of the learning goal
     * @param superGoalPrivate   {code true} if the super goal is already private, otherwise {@code false}
     * @return {@link Resource} which represents the new learning goal
     */
    private Resource constructLearningGoalFromDTO(NewLearningGoalDTO newLearningGoalDTO, String owner, Model model,
                                                  Instant creationTime, boolean superGoalPrivate) {
        String newResourceName = newLearningGoalDTO.getNameForRDF();

        Resource newGoal = ETutorVocabulary.createUserGoalResourceOfModel(owner, newResourceName, model);
        String creationTimeStr = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(Date.from(creationTime));

        if (newLearningGoalDTO.getDescription() != null && newLearningGoalDTO.getDescription().trim().length() > 0) {
            newGoal.addProperty(ETutorVocabulary.hasDescription, newLearningGoalDTO.getDescription().trim());
        } else {
            newGoal.addProperty(ETutorVocabulary.hasDescription, "");
        }

        newGoal.addProperty(RDFS.label, newLearningGoalDTO.getName().trim());
        newGoal.addProperty(ETutorVocabulary.hasChangeDate, creationTimeStr, XSDDatatype.XSDdateTime);
        newGoal.addProperty(ETutorVocabulary.hasOwner, owner);

        String privateStr = superGoalPrivate ? String.valueOf(true)
            : String.valueOf(newLearningGoalDTO.isPrivateGoal());

        newGoal.addProperty(ETutorVocabulary.isPrivate, privateStr, XSDDatatype.XSDboolean);
        newGoal.addProperty(RDF.type, ETutorVocabulary.Goal);

        return newGoal;
    }
    //endregion
}
