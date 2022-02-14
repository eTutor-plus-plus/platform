package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.*;
import at.jku.dke.etutor.service.exception.*;
import at.jku.dke.etutor.web.rest.errors.BadRequestAlertException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.*;
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
import org.springframework.web.servlet.tags.Param;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

/**
 * Service class for SPARQL related operations.
 *
 * @author fne
 */
@Service
public non-sealed class SPARQLEndpointService extends AbstractSPARQLEndpointService {

    private static final String SCHEME_PATH = "/rdf/scheme.ttl";

    //region Queries
    private static final String QRY_GOAL_COUNT =
        """
            SELECT (COUNT(DISTINCT ?subject) as ?count)
            WHERE {
            	?subject ?predicate ?object.
              FILTER(?subject = <http://www.dke.uni-linz.ac.at/etutorpp/%s/Goal#%s> )
            }
            """;

    private static final String QRY_ID_SUBJECT_COUNT =
        """
            SELECT (COUNT(DISTINCT ?subject) as ?count)
            WHERE {
            	?subject ?predicate ?object.
              FILTER(?subject = <%s> )
            }
            """;

    private static final String QRY_ASK_COURSE_EXIST =
        """
            ASK {
            	?subject ?predicate ?object.
              FILTER(?subject = <http://www.dke.uni-linz.ac.at/etutorpp/Course#%s> )
            }
            """;

    private static final String QRY_ASK_COURSE_WITH_OWNER_EXIST =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            ASK {
            	?subject ?predicate ?object.
            	?subject etutor:hasCourseCreator ?creator
                FILTER(?subject = ?uri )
            }
            """;

    private static final String QRY_DELETE_ALL_FROM_SUBJECT =
        """
            DELETE { ?subject ?predicate ?object }
            WHERE {
                ?subject ?predicate ?object.
                FILTER(?subject = ?uri)
            }
            """;

    private static final String QRY_GOAL_ASSIGNMENT_EXISTS =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            ASK {
            	?course etutor:hasGoal ?goal
            }
            """;

    private static final String QRY_GOAL_DEPENDENCIES =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT (STR(?otherGoal) AS ?goalId)
            WHERE {
              ?goal a etutor:Goal.
              ?goal etutor:dependsOn ?otherGoal.
              ?otherGoal rdfs:label ?otherName
            }
            ORDER BY (LCASE(?otherName))
            """;

    private static final String QRY_GOAL_DEPENDENCIES_TEXT =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT ?goalName
            WHERE {
              ?goal a etutor:Goal.
              ?goal etutor:dependsOn ?otherGoal.
              ?otherGoal rdfs:label ?goalName
            }
            ORDER BY (LCASE(?goalName))
            """;

    private static final String DELETE_GOAL_WITH_SUBGOALS =
        """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE {
              ?goal ?predicate ?object.
              ?secondGoal ?secondPredicate ?secondObject.
              ?otherGoal etutor:dependsOn ?goal.
              ?otherGoal1 etutor:dependsOn ?secondGoal.
              ?parent etutor:hasSubGoal ?goal.
            } WHERE {
              ?goal a etutor:Goal.
              ?goal ?predicate ?object.
              OPTIONAL {
                ?goal etutor:hasSubGoal* ?secondGoal.
                ?secondGoal ?secondPredicate ?secondObject.
                OPTIONAL {
                  ?otherGoal1 etutor:dependsOn ?secondGoal.
                }
              }
              OPTIONAL {
                ?otherGoal etutor:dependsOn ?goal.
              }
              OPTIONAL {
                ?parent etutor:hasSubGoal ?goal.
              }
            }
            """;

    private final String ASK_IS_SUBGOAL_TRANS =
            """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            ASK {
              %s etutor:hasSubGoal* %s.
            }
            """;
    //endregion

    private final Logger log = LoggerFactory.getLogger(SPARQLEndpointService.class);

    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public SPARQLEndpointService(RDFConnectionFactory rdfConnectionFactory) {
        super(rdfConnectionFactory);
    }

    /**
     * Inserts the RDF scheme into the configured fuseki instance.
     */
    public void insertScheme() {
        try (RDFConnection conn = getConnection(); InputStream schemeStream = getClass().getResourceAsStream(SCHEME_PATH)) {
            Model model = ModelFactory.createDefaultModel();
            model.read(schemeStream, null, "TTL");

            Txn.executeWrite(conn, () -> conn.load(model));
        } catch (IOException ex) {
            log.error("Internal error! Must not occur!", ex);
        }
    }

    //region Learning goals

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

            try (QueryExecution qExec = conn.query(String.format(QRY_GOAL_COUNT, owner, newLearningGoalDTO.getNameForRDF()))) {
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
     * Removes a learning goal with its sub goals.
     *
     * @param owner    the learning goal's owner
     * @param goalName the learning goal's name
     * @throws LearningGoalNotExistsException if the learning goal can not be found
     */
    public void removeLearningGoalAndSubGoals(String owner, String goalName) throws LearningGoalNotExistsException {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(goalName);

        String escapedGoalName = URLEncoder.encode(goalName.replace(' ', '_'), StandardCharsets.UTF_8);
        String goalUri = String.format("http://www.dke.uni-linz.ac.at/etutorpp/%s/Goal#%s", owner, escapedGoalName);

        ParameterizedSparqlString query = new ParameterizedSparqlString(DELETE_GOAL_WITH_SUBGOALS);
        query.setIri("?goal", goalUri);

        try (RDFConnection conn = getConnection()) {
            int cnt;

            try (QueryExecution qExec = conn.query(String.format(QRY_ID_SUBJECT_COUNT, goalUri))) {
                cnt = qExec.execSelect().next().getLiteral("?count").getInt();
            }

            if (cnt == 0) {
                throw new LearningGoalNotExistsException();
            }

            conn.update(query.asUpdate());
        }
    }

    /**
     * Updates an existing learning goal.
     *
     * @param learningGoalDTO the data of the learning goal
     * @throws LearningGoalNotExistsException if the learning goal does not exist
     * @throws PrivateSuperGoalException      if the learning goal should be public and has a private super goal
     */
    public void updateLearningGoal(LearningGoalDTO learningGoalDTO) throws LearningGoalNotExistsException, PrivateSuperGoalException {
        try (RDFConnection conn = getConnection()) {
            int cnt;

            try (QueryExecution qExec = conn.query(String.format(QRY_ID_SUBJECT_COUNT, learningGoalDTO.getId()))) {
                cnt = qExec.execSelect().next().getLiteral("?count").getInt();
            }

            if (cnt == 0) {
                throw new LearningGoalNotExistsException();
            }

            if (!learningGoalDTO.isPrivateGoal()) {
                ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString(
                    """
                        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                        ASK {
                          ?subject ^etutor:hasSubGoal+ ?goal.
                          ?goal etutor:isPrivate true.
                          FILTER(?subject = ?startSubject)
                        }
                        """
                );
                parameterizedSparqlString.setIri("?startSubject", learningGoalDTO.getId());

                boolean containsPrivateSuperGoal = conn.queryAsk(parameterizedSparqlString.toString());

                if (containsPrivateSuperGoal) {
                    throw new PrivateSuperGoalException();
                }
            }

            Instant now = Instant.now();
            String nowStr = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(Date.from(now));

            String description = ObjectUtils.firstNonNull(learningGoalDTO.getDescription(), "");

            ParameterizedSparqlString updateQry = new ParameterizedSparqlString(
                """
                    PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                    PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>

                    DELETE {
                      ?subject etutor:hasChangeDate ?changeDate.
                      ?subject etutor:hasDescription ?description.
                      ?subject etutor:isPrivate ?private.
                      ?subject etutor:needsVerificationBeforeCompletion ?needsVerification.
                    }
                    INSERT {
                      ?subject etutor:hasChangeDate ?newChangeDate.
                      ?subject etutor:hasDescription ?newDescription.
                      ?subject etutor:isPrivate ?newPrivate.
                      ?subject etutor:needsVerificationBeforeCompletion ?newNeedsVerification.
                    }
                    WHERE {
                      ?subject etutor:hasChangeDate ?changeDate.
                      ?subject etutor:hasDescription ?description.
                      ?subject etutor:isPrivate ?private.
                      ?subject etutor:needsVerificationBeforeCompletion ?needsVerification.
                    }
                    """
            );
            updateQry.setIri("?subject", learningGoalDTO.getId());
            updateQry.setLiteral("?newChangeDate", nowStr, XSDDatatype.XSDdateTime);
            updateQry.setLiteral("?newDescription", description);
            updateQry.setLiteral("?newPrivate", learningGoalDTO.isPrivateGoal());
            updateQry.setLiteral("?newNeedsVerification", learningGoalDTO.isNeedVerification());

            conn.update(updateQry.asUpdate());

            if (learningGoalDTO.isPrivateGoal()) {
                // Update 'privateGoal' of all sub goals
                String transitiveUpdateQry = String.format(
                    """
                        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                        DELETE {
                          ?goal etutor:isPrivate ?private
                        }
                        INSERT {
                          ?goal etutor:isPrivate true
                        }
                        WHERE {
                          ?subject etutor:hasSubGoal+ ?goal.
                          ?goal etutor:isPrivate ?private.
                          FILTER(?subject = <%s>)
                        }
                        """,
                    learningGoalDTO.getId()
                );

                conn.update(transitiveUpdateQry);
            }
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

            try (QueryExecution qExec = conn.query(String.format(QRY_GOAL_COUNT, owner, newLearningGoalDTO.getNameForRDF()))) {
                cnt = qExec.execSelect().next().getLiteral("?count").getInt();
            }

            if (cnt > 0) {
                throw new LearningGoalAlreadyExistsException();
            }

            String escapedParentGoalName = URLEncoder.encode(parentGoalName.replace(' ', '_'), StandardCharsets.UTF_8);

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
     * Adds an existing goal as sub-goal of another existing goal
     * @param subGoalName the name of the sub-goal
     * @param parentGoalName the name of the parent-goal
     * @throws LearningGoalNotExistsException     if one of the goals could not be found
     */
    public void insertExistingGoalAsSubgoal(String owner, String subGoalName, String parentGoalName) throws LearningGoalNotExistsException, IllegalArgumentException {
        Model model = ModelFactory.createDefaultModel();

        try (RDFConnection conn = getConnection()) {
            String escapedParentGoalName = URLEncoder.encode(parentGoalName.replace(' ', '_'), StandardCharsets.UTF_8);
            String escapedSubGoalName = URLEncoder.encode(subGoalName.replace(' ', '_'), StandardCharsets.UTF_8);


            Boolean superGoalPrivate = isLearningGoalPrivate(conn, owner, escapedParentGoalName);
            Boolean subGoalPrivate = isLearningGoalPrivate(conn, owner, escapedSubGoalName);

            if (superGoalPrivate == null || subGoalPrivate == null) {
                throw new LearningGoalNotExistsException();
            }
            String parentGoalURL = ETutorVocabulary.createGoalUrl(owner, escapedParentGoalName);
            String parentGoalIRI = "<" + parentGoalURL + ">";
            String subGoalURL = ETutorVocabulary.createGoalUrl(owner, escapedSubGoalName);
            String subGoalIRI = "<" + subGoalURL + ">";

            String query = String.format(ASK_IS_SUBGOAL_TRANS, subGoalIRI, parentGoalIRI);
            if(!conn.queryAsk(query)){
                ParameterizedSparqlString updateQry = new ParameterizedSparqlString(
                    """
                        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                        DELETE {
                          ?goal etutor:hasSubGoal ?subGoal .
                        }
                        WHERE{
                          ?goal a etutor:Goal.
                          OPTIONAL {
                            ?goal etutor:hasSubGoal ?subGoal .
                          }
                        }
                        """
                );
                updateQry.setIri("?subGoal", subGoalURL);
                conn.update(updateQry.asUpdate());

                Resource parentGoalResource = ETutorVocabulary.createUserGoalResourceOfModel(owner, escapedParentGoalName, model);
                Resource subGoalResource = ETutorVocabulary.createUserGoalResourceOfModel(owner, escapedSubGoalName, model);

                parentGoalResource.addProperty(ETutorVocabulary.hasSubGoal, subGoalResource);

                conn.load(model);
            }else{
                throw new IllegalArgumentException();
            }
        }
    }
    /**
     * Returns all learning goals which are visible for the given owner.
     *
     * @param owner            the owner of requested learning goals
     * @param showOnlyOwnGoals {@code} true, if only the user's own goals should be displayed, otherwise {@code false}
     * @return a list of all {@link LearningGoalDTO} which are visible for the given owner
     * @throws InternalModelException if the internal date format is not valid
     */
    public SortedSet<LearningGoalDTO> getVisibleLearningGoalsForUser(String owner, boolean showOnlyOwnGoals) throws InternalModelException {
        String queryStr;

        if (showOnlyOwnGoals) {
            queryStr =
                """
                    PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

                    CONSTRUCT {
                      ?subject ?predicate ?object.
                      ?subject etutor:hasReferenceCnt ?cnt.
                      ?subject etutor:hasRoot ?root
                    } WHERE {
                      {
                        ?subject ?predicate ?object.
                        ?subject a etutor:Goal.
                        ?subject rdfs:label ?lbl
                        {
                          ?subject etutor:hasOwner ?currentUser.
                        }
                      } UNION {
                        BIND(rdf:type AS ?predicate)
                        BIND(etutor:SubGoal AS ?object)
                        ?goal etutor:hasSubGoal ?subject .
                        ?goal etutor:hasOwner ?currentUser.
                      } {
                        SELECT (COUNT(?course) as ?cnt) ?subject WHERE {
                          ?subject a etutor:Goal.
                          OPTIONAL { ?course etutor:hasGoal ?subject }
                        }
                        GROUP BY ?subject
                      } {
                        SELECT ?subject ?root WHERE {
                          ?root etutor:hasSubGoal* ?subject.
                          FILTER (
                            !EXISTS {
                              ?otherGoal etutor:hasSubGoal ?root.
                            }
                          )
                        }
                      }
                    }
                    """;
        } else {
            queryStr =
                """
                        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

                        CONSTRUCT {
                          ?subject ?predicate ?object.
                          ?subject etutor:hasReferenceCnt ?cnt.
                          ?subject etutor:hasRoot ?root.
                          ?subject etutor:hasSubGoal ?subGoalFromSubject.
                        } WHERE {
                          {
                            ?subject a etutor:Goal.
                            ?subject ?predicate ?object.
                            ?subject !etutor:hasSubGoal ?object.
                            ?subject rdfs:label ?lbl
                            {
                              ?subject etutor:isPrivate false.
                            }
                            UNION
                            {
                              ?subject etutor:isPrivate true.
                              ?subject etutor:hasOwner ?currentUser.
                            }
                            OPTIONAL {
                              ?subject etutor:hasSubGoal ?subGoalFromSubject.
                              {
                                ?subGoalFromSubject etutor:isPrivate false.
                              }
                              UNION
                              {
                                ?subGoalFromSubject etutor:isPrivate true.
                                ?subGoalFromSubject etutor:hasOwner ?currentUser.
                              }
                            }
                          } UNION {
                            BIND(rdf:type AS ?predicate)
                            BIND(etutor:SubGoal AS ?object)
                            ?goal etutor:hasSubGoal ?subject .
                            {
                              ?subject etutor:isPrivate false.
                            }
                            UNION
                            {
                              ?subject etutor:isPrivate true.
                              ?subject etutor:hasOwner "admin".
                            }
                          } {
                            SELECT (COUNT(?course) as ?cnt) ?subject WHERE {
                              ?subject a etutor:Goal.
                              {
                              	?subject etutor:isPrivate false.
                              }
                              UNION
                              {
                                ?subject etutor:isPrivate true.
                                ?subject etutor:hasOwner ?currentUser.
                              }
                              OPTIONAL { ?course etutor:hasGoal ?subject }
                            }
                            GROUP BY ?subject
                          } {
                            SELECT ?subject ?root WHERE {
                              ?root etutor:hasSubGoal* ?subject.
                              {
                                ?subject etutor:isPrivate false.
                              }
                              UNION
                              {
                                ?subject etutor:isPrivate true.
                                ?subject etutor:hasOwner ?currentUser.
                              }
                              FILTER (
                                !EXISTS {
                                  ?otherGoal etutor:hasSubGoal ?root.
                                }
                              )
                            }
                          }
                        }


                    """;
        }

        ParameterizedSparqlString qry = new ParameterizedSparqlString(queryStr);
        qry.setLiteral("?currentUser", owner);

        SortedSet<LearningGoalDTO> goalList = new TreeSet<>();

        try (RDFConnection conn = getConnection()) {
            Model resultModel = conn.queryConstruct(qry.asQuery());
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

    /**
     * Sets the dependencies of a specific goal.
     *
     * @param owner    the goal's owner
     * @param goalName the goal's name
     * @param goalIds  the list of dependency ids
     */
    public void setDependencies(String owner, String goalName, List<String> goalIds) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(goalName);
        Objects.requireNonNull(goalIds);

        String escapedGoalName = URLEncoder.encode(goalName.replace(' ', '_'), StandardCharsets.UTF_8);
        String goalUri = String.format("http://www.dke.uni-linz.ac.at/etutorpp/%s/Goal#%s", owner, escapedGoalName);

        ParameterizedSparqlString updateQry = new ParameterizedSparqlString(
            """
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                DELETE {
                  ?goal etutor:dependsOn ?otherGoal
                }
                INSERT {
                """
        );

        for (String goalId : goalIds) {
            updateQry.append("?goal etutor:dependsOn ");
            updateQry.appendIri(goalId);
            updateQry.append(".\n");
        }

        updateQry.append(
            """
                }
                WHERE {
                  ?goal a etutor:Goal
                  OPTIONAL {
                    ?goal etutor:dependsOn ?otherGoal
                  }
                }
                """
        );

        updateQry.setIri("?goal", goalUri);

        try (RDFConnection connection = getConnection()) {
            connection.update(updateQry.asUpdate());
        }
    }

    /**
     * Returns the list of a given goal's dependencies.
     *
     * @param owner    the goal's owner
     * @param goalName the goal's name
     * @return list of dependency ids
     */
    public List<String> getDependencies(String owner, String goalName) {
        String escapedGoalName = URLEncoder.encode(goalName.replace(' ', '_'), StandardCharsets.UTF_8);
        String goalUri = String.format("http://www.dke.uni-linz.ac.at/etutorpp/%s/Goal#%s", owner, escapedGoalName);
        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_GOAL_DEPENDENCIES);
        qry.setIri("?goal", goalUri);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(qry.asQuery())) {
                ResultSet set = execution.execSelect();

                List<String> resultIds = new ArrayList<>();
                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    resultIds.add(solution.getLiteral("?goalId").getString());
                }
                return resultIds;
            }
        }
    }

    /**
     * Returns the displayable dependencies (dependency names)
     * of a given goal.
     *
     * @param owner    the goal's owner
     * @param goalName the goal's name
     * @return list of dependency names
     */
    public List<String> getDisplayableDependencies(String owner, String goalName) {
        String escapedGoalName = URLEncoder.encode(goalName.replace(' ', '_'), StandardCharsets.UTF_8);
        String goalUri = String.format("http://www.dke.uni-linz.ac.at/etutorpp/%s/Goal#%s", owner, escapedGoalName);

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_GOAL_DEPENDENCIES_TEXT);
        qry.setIri("?goal", goalUri);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution queryExecution = connection.query(qry.asQuery())) {
                ResultSet set = queryExecution.execSelect();

                List<String> retList = new ArrayList<>();
                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    retList.add(solution.getLiteral("?goalName").getString());
                }
                return retList;
            }
        } catch (QueryParseException queryParseException) {
            log.warn("Faulty query - Owner: {}, goal name: {}, query: {}", owner, goalName, qry);

            throw queryParseException;
        }
    }

    //endregion

    //region Courses

    /**
     * Creates a new course.
     *
     * @param courseDTO the dto of the course to create
     * @param user      the current logged in user
     * @return the newly created course dto
     * @throws CourseAlreadyExistsException if a course already exists
     */
    public CourseDTO insertNewCourse(CourseDTO courseDTO, String user) throws CourseAlreadyExistsException {
        Objects.requireNonNull(courseDTO);
        Objects.requireNonNull(user);

        String query = String.format(QRY_ASK_COURSE_EXIST, courseDTO.getNameForRDF());
        Model model = ModelFactory.createDefaultModel();

        try (RDFConnection connection = getConnection()) {
            boolean courseAlreadyExist = connection.queryAsk(query);

            if (courseAlreadyExist) {
                throw new CourseAlreadyExistsException();
            }

            Resource newCourse = constructCourseFromDTO(courseDTO, user, model);
            connection.load(model);
            courseDTO.setId(newCourse.getURI());
            courseDTO.setCreator(user);
            return courseDTO;
        }
    }

    /**
     * Updates the given course
     *
     * @param courseDTO the course dto which contains the update information
     * @throws CourseNotFoundException if the course does not exist
     */
    public void updateCourse(CourseDTO courseDTO) throws CourseNotFoundException {
        Objects.requireNonNull(courseDTO);

        String courseExistQry = String.format(QRY_ASK_COURSE_EXIST, courseDTO.getNameForRDF());

        try (RDFConnection conn = getConnection()) {
            boolean courseExist = conn.queryAsk(courseExistQry);

            if (!courseExist) {
                throw new CourseNotFoundException();
            }

            ParameterizedSparqlString updateQry = new ParameterizedSparqlString(
                """
                    PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                    DELETE {
                        ?subject etutor:hasCourseDescription ?description.
                        ?subject etutor:hasCourseLink ?link.
                        ?subject etutor:hasCourseType ?type.
                    }
                    INSERT {
                        ?subject etutor:hasCourseDescription ?newDescription.
                        ?subject etutor:hasCourseLink ?newLink.
                        ?subject etutor:hasCourseType ?newType.
                    }
                    WHERE {
                        ?subject etutor:hasCourseDescription ?description.
                        ?subject etutor:hasCourseLink ?link.
                        ?subject etutor:hasCourseType ?type.
                        FILTER(?subject = ?courseUri)
                    }
                    """
            );

            updateQry.setIri("?courseUri", courseDTO.getId());

            String description = ObjectUtils.firstNonNull(courseDTO.getDescription(), "");
            description = description.trim();
            updateQry.setLiteral("?newDescription", description);

            String link = "";

            if (courseDTO.getLink() != null) {
                link = courseDTO.getLink().toString();
            }

            updateQry.setLiteral("?newLink", link);
            updateQry.setLiteral("?newType", courseDTO.getCourseType());

            conn.update(updateQry.asUpdate());
        }
    }

    /**
     * Returns the course by it's name.
     *
     * @param name the rdf encoded name of the course which should be returned
     * @return An empty optional, if the course does not exist, otherwise the optional containing the course
     */
    public Optional<CourseDTO> getCourse(String name) {
        Objects.requireNonNull(name);

        ParameterizedSparqlString query = new ParameterizedSparqlString(
            """
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                CONSTRUCT { ?course ?predicate ?object.
                            ?course etutor:hasInstanceCount ?instanceCnt }
                WHERE {
                  {
                    ?course a etutor:Course.
                    FILTER(?course = ?courseUri)
                  	?course ?predicate ?object.
                  } UNION {
                    SELECT ?course (COUNT(?courseInstance) AS ?instanceCnt)
                    WHERE {
                      ?courseInstance etutor:hasCourse ?course.
                      FILTER(?course = ?courseUri)
                    }
                    GROUP BY ?course
                  }
                }
                """
        );
        query.setIri("?courseUri", ETutorVocabulary.createCourseURL(name));

        try (RDFConnection conn = getConnection()) {
            Model model = conn.queryConstruct(query.asQuery());
            ResIterator iterator = null;

            try {
                iterator = model.listSubjects();
                if (iterator.hasNext()) {
                    Resource resource = iterator.nextResource();
                    return Optional.of(new CourseDTO(resource));
                } else {
                    return Optional.empty();
                }
            } finally {
                if (iterator != null) {
                    iterator.close();
                }
            }
        }
    }

    /**
     * Deletes the given course.
     *
     * @param name    the rdf encoded course name
     * @param creator the creator of the course
     * @throws CourseNotFoundException if the course does not exist or the given creator is not the course's creator
     */
    public void deleteCourse(String name, String creator) throws CourseNotFoundException {
        Objects.requireNonNull(name);
        Objects.requireNonNull(creator);

        String id = ETutorVocabulary.Course.getURI() + "#" + URLEncoder.encode(name.replace(' ', '_').trim(), StandardCharsets.UTF_8);

        ParameterizedSparqlString courseExistQry = new ParameterizedSparqlString(QRY_ASK_COURSE_WITH_OWNER_EXIST);
        courseExistQry.setIri("?uri", id);
        courseExistQry.setLiteral("?creator", creator);

        try (RDFConnection conn = getConnection()) {
            boolean courseExist = conn.queryAsk(courseExistQry.toString());

            if (!courseExist) {
                throw new CourseNotFoundException();
            }

            ParameterizedSparqlString courseDeleteQry = new ParameterizedSparqlString(QRY_DELETE_ALL_FROM_SUBJECT);
            courseDeleteQry.setIri("?uri", id);

            conn.update(courseDeleteQry.asUpdate());
        }
    }

    /**
     * Returns a sorted set of all available courses.
     *
     * @return a sorted set of all available courses
     */
    public SortedSet<CourseDTO> getAllCourses() {
        String query =
            """
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                CONSTRUCT { ?course ?predicate ?object.
                            ?course etutor:hasInstanceCount ?instanceCnt }
                WHERE {
                  {
                    ?course a etutor:Course.
                  	?course ?predicate ?object.
                  } UNION {
                    SELECT ?course (COUNT(?courseInstance) AS ?instanceCnt)
                    WHERE {
                      ?courseInstance etutor:hasCourse ?course.
                    }
                    GROUP BY ?course
                  }
                }
                """;

        try (RDFConnection conn = getConnection()) {
            SortedSet<CourseDTO> set = new TreeSet<>();

            try (QueryExecution exec = conn.query(query)) {
                Model model = exec.execConstruct();
                ResIterator iterator = null;

                try {
                    iterator = model.listSubjects();

                    while (iterator.hasNext()) {
                        Resource resource = iterator.nextResource();
                        set.add(new CourseDTO(resource));
                    }

                    return set;
                } finally {
                    if (iterator != null) {
                        iterator.close();
                    }
                }
            }
        }
    }

    //endregion

    //region Learning Goal Assignment

    /**
     * Returns the associated learning goals of a given course.
     *
     * @param course the course
     * @return the associated learning goals
     * @throws CourseNotFoundException if the course does not exist
     * @throws InternalModelException  if an internal model exception occurs
     */
    public SortedSet<DisplayLearningGoalAssignmentDTO> getLearningGoalsForCourse(String course)
        throws CourseNotFoundException, InternalModelException {
        Objects.requireNonNull(course);

        String qry = String.format(QRY_ASK_COURSE_EXIST, URLEncoder.encode(course.replace(" ", "_"), StandardCharsets.UTF_8));

        ParameterizedSparqlString constructQry = new ParameterizedSparqlString(
            """
                  PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

                  CONSTRUCT {
                    ?s ?p ?o.
                    ?s etutor:hasReferenceCnt ?cnt
                  }
                  WHERE {
                    {
                      SELECT ?s ?p ?o WHERE {
                        ?course etutor:hasGoal+/etutor:hasSubGoal* ?s.
                        BIND(etutor:hasRootGoal AS ?p).
                        ?o etutor:hasSubGoal* ?s.
                        FILTER (
                          !EXISTS {
                            ?otherGoal etutor:hasSubGoal ?o.
                          }
                        )
                      }
                    } UNION {
                      SELECT ?s ?p ?o WHERE {
                        ?s ?p ?o .
                        ?course etutor:hasGoal ?s .
                      }
                    } UNION {
                      SELECT ?s ?p ?o WHERE {
                        ?s ?p ?o .
                        ?course etutor:hasGoal ?goal .
                        ?goal etutor:hasSubGoal* ?s .
                      }
                    } UNION {
                      SELECT ?s ?p ?o WHERE {
                        ?course etutor:hasGoal ?goal .
                        ?goal etutor:hasSubGoal+ ?s .
                        BIND(rdf:type AS ?p)
                        BIND(etutor:SubGoal AS ?o)
                      }
                    } {
                      SELECT (COUNT(?course1) as ?cnt) ?s WHERE {
                        ?s a etutor:Goal.
                        OPTIONAL { ?course1 etutor:hasGoal ?s. }
                      }
                      GROUP BY ?s
                    }
                  }
                """
        );

        constructQry.setIri("?course", "http://www.dke.uni-linz.ac.at/etutorpp/Course#" + URLEncoder.encode(course.replace(" ", "_"), StandardCharsets.UTF_8));

        try (RDFConnection connection = getConnection()) {
            boolean exist = connection.queryAsk(qry);

            if (!exist) {
                throw new CourseNotFoundException();
            }

            SortedSet<DisplayLearningGoalAssignmentDTO> goalList = new TreeSet<>();
            Model resultModel = connection.queryConstruct(constructQry.asQuery());
            ResIterator iterator = null;

            try {
                iterator = resultModel.listSubjects();
                while (iterator.hasNext()) {
                    Resource resource = iterator.next();
                    if (!resource.hasProperty(RDF.type, ETutorVocabulary.SubGoal)) {
                        goalList.add(new DisplayLearningGoalAssignmentDTO(resource));
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
     * Adds a new learning goal assignment.
     *
     * @param learningGoalAssignmentDTO the learning goal assignment dto to add
     * @throws LearningGoalAssignmentAlreadyExistsException if a learning goal assignment already exists
     */
    public void addGoalAssignment(LearningGoalAssignmentDTO learningGoalAssignmentDTO) throws LearningGoalAssignmentAlreadyExistsException {
        Objects.requireNonNull(learningGoalAssignmentDTO);
        Objects.requireNonNull(learningGoalAssignmentDTO.getCourseId());
        Objects.requireNonNull(learningGoalAssignmentDTO.getLearningGoalId());

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_GOAL_ASSIGNMENT_EXISTS);
        qry.setIri("?course", learningGoalAssignmentDTO.getCourseId());
        qry.setIri("?goal", learningGoalAssignmentDTO.getLearningGoalId());

        try (RDFConnection connection = getConnection()) {
            boolean exists = connection.queryAsk(qry.asQuery());

            if (exists) {
                throw new LearningGoalAssignmentAlreadyExistsException();
            }

            Model model = ModelFactory.createDefaultModel();
            Resource courseResource = model.createResource(learningGoalAssignmentDTO.getCourseId());
            Resource goalResource = model.createResource(learningGoalAssignmentDTO.getLearningGoalId());
            courseResource.addProperty(ETutorVocabulary.hasGoal, goalResource);

            connection.load(model);
        }
    }

    /**
     * Removes a given learning goal assignment.
     *
     * @param learningGoalAssignmentDTO the learning goal assignment to remove
     * @throws LearningGoalAssignmentNonExistentException if the learning goal does not exist
     */
    public void removeGoalAssignment(LearningGoalAssignmentDTO learningGoalAssignmentDTO)
        throws LearningGoalAssignmentNonExistentException {
        Objects.requireNonNull(learningGoalAssignmentDTO);
        Objects.requireNonNull(learningGoalAssignmentDTO.getCourseId());
        Objects.requireNonNull(learningGoalAssignmentDTO.getLearningGoalId());

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_GOAL_ASSIGNMENT_EXISTS);
        qry.setIri("?course", learningGoalAssignmentDTO.getCourseId());
        qry.setIri("?goal", learningGoalAssignmentDTO.getLearningGoalId());

        try (RDFConnection connection = getConnection()) {
            boolean exists = connection.queryAsk(qry.asQuery());

            if (!exists) {
                throw new LearningGoalAssignmentNonExistentException();
            }

            qry =
                new ParameterizedSparqlString(
                    """
                        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                        DELETE { ?course etutor:hasGoal ?goal }
                        WHERE {
                            ?course etutor:hasGoal ?goal
                        }
                        """
                );
            qry.setIri("?course", learningGoalAssignmentDTO.getCourseId());
            qry.setIri("?goal", learningGoalAssignmentDTO.getLearningGoalId());

            connection.update(qry.asUpdate());
        }
    }

    /**
     * Sets the given learning goal assignment.
     *
     * @param learningGoalUpdateAssignment the assignment to set
     */
    public void setGoalAssignment(LearningGoalUpdateAssignmentDTO learningGoalUpdateAssignment) {
        Objects.requireNonNull(learningGoalUpdateAssignment);
        Objects.requireNonNull(learningGoalUpdateAssignment.getCourseId());
        Objects.requireNonNull(learningGoalUpdateAssignment.getLearningGoalIds());

        StringBuilder builder = new StringBuilder();
        builder.append(
            """
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                DELETE { ?subject etutor:hasGoal ?goal }
                INSERT {
                """
        );

        for (String goal : learningGoalUpdateAssignment.getLearningGoalIds()) {
            builder.append(String.format("?subject etutor:hasGoal <%s> .%n", goal));
        }

        builder.append(
            """
                }
                WHERE {
                  ?subject a etutor:Course.
                  OPTIONAL {
                    ?subject etutor:hasGoal ?goal.
                  }
                  FILTER(?subject = ?course)
                }
                """
        );

        ParameterizedSparqlString updateQry = new ParameterizedSparqlString(builder.toString());
        updateQry.setIri("?course", learningGoalUpdateAssignment.getCourseId());

        try (RDFConnection conn = getConnection()) {
            conn.update(updateQry.asUpdate());
        }
    }

    //endregion

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
        String query = String.format(
            """
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                SELECT  ?privateGoal
                WHERE {
                  <http://www.dke.uni-linz.ac.at/etutorpp/%s/Goal#%s> etutor:isPrivate ?privateGoal
                }
                """,
            owner,
            learningGoalName
        );
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
     * Creates a new learning goal from the given parameters.
     *
     * @param newLearningGoalDTO the dto of the new learning goal
     * @param owner              the owner of the learning goal
     * @param model              the rdf model which should be used
     * @param creationTime       the creation time of the learning goal
     * @param superGoalPrivate   {code true} if the super goal is already private, otherwise {@code false}
     * @return {@link Resource} which represents the new learning goal
     */
    private Resource constructLearningGoalFromDTO(
        NewLearningGoalDTO newLearningGoalDTO,
        String owner,
        Model model,
        Instant creationTime,
        boolean superGoalPrivate
    ) {
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
        newGoal.addProperty(
            ETutorVocabulary.needsVerificationBeforeCompletion,
            String.valueOf(newLearningGoalDTO.isNeedVerification()),
            XSDDatatype.XSDboolean
        );

        String privateStr = superGoalPrivate ? String.valueOf(true) : String.valueOf(newLearningGoalDTO.isPrivateGoal());

        newGoal.addProperty(ETutorVocabulary.isPrivate, privateStr, XSDDatatype.XSDboolean);
        newGoal.addProperty(RDF.type, ETutorVocabulary.Goal);

        return newGoal;
    }

    /**
     * Creates a new course from the given parameters.
     *
     * @param courseDTO the course dto of the new course
     * @param owner     the owner of the new course
     * @param model     the rdf model which should be used
     * @return {@link Resource} which represents the new course
     */
    private Resource constructCourseFromDTO(CourseDTO courseDTO, String owner, Model model) {
        String resourceName = courseDTO.getNameForRDF();

        Resource newCourse = ETutorVocabulary.createCourseResourceOfModel(resourceName, model);

        String courseDescription = ObjectUtils.firstNonNull(courseDTO.getDescription(), "");
        courseDescription = courseDescription.trim();

        newCourse.addProperty(ETutorVocabulary.hasCourseDescription, courseDescription);
        newCourse.addProperty(RDFS.label, courseDTO.getName().trim());
        newCourse.addProperty(ETutorVocabulary.hasCourseCreator, owner);

        URL url = courseDTO.getLink();
        String urlAsString = "";

        if (url != null) {
            urlAsString = url.toString();
        }
        newCourse.addProperty(ETutorVocabulary.hasCourseLink, urlAsString);
        newCourse.addProperty(ETutorVocabulary.hasCourseType, courseDTO.getCourseType());
        newCourse.addProperty(RDF.type, ETutorVocabulary.Course);

        return newCourse;
    }
    //endregion
}
