package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.*;
import at.jku.dke.etutor.service.exception.LearningGoalAssignmentAlreadyExistsException;
import at.jku.dke.etutor.service.exception.LearningGoalAssignmentNonExistentException;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
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
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

/**
 * Service class for SPARQL related operations.
 *
 * @author fne
 */
@Service
public class SPARQLEndpointService {

    private static final String SCHEME_PATH = "/rdf/scheme.ttl";

    //region Queries
    private static final String QRY_GOAL_COUNT = """
        SELECT (COUNT(DISTINCT ?subject) as ?count)
        WHERE {
        	?subject ?predicate ?object.
          FILTER(?subject = <http://www.dke.uni-linz.ac.at/etutorpp/%s/Goal#%s> )
        }
        """;

    private static final String QRY_ID_SUBJECT_COUNT = """
        SELECT (COUNT(DISTINCT ?subject) as ?count)
        WHERE {
        	?subject ?predicate ?object.
          FILTER(?subject = <%s> )
        }
        """;

    private static final String QRY_ASK_COURSE_EXIST = """
        ASK {
        	?subject ?predicate ?object.
          FILTER(?subject = <http://www.dke.uni-linz.ac.at/etutorpp/Course#%s> )
        }
        """;

    private static final String QRY_ASK_COURSE_WITH_OWNER_EXIST = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        ASK {
        	?subject ?predicate ?object.
        	?subject etutor:hasCourseCreator ?creator
            FILTER(?subject = ?uri )
        }
        """;

    private static final String QRY_DELETE_ALL_FROM_SUBJECT = """
        DELETE { ?subject ?predicate ?object }
        WHERE {
            ?subject ?predicate ?object.
            FILTER(?subject = ?uri)
        }
        """;

    private static final String QRY_GOAL_ASSIGNMENT_EXISTS = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        ASK {
        	?course etutor:hasGoal ?goal
        }
        """;
    //endregion

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
     * @throws PrivateSuperGoalException      if the learning goal should be public and has a private super goal
     */
    public void updateLearningGoal(LearningGoalDTO learningGoalDTO) throws LearningGoalNotExistsException,
        PrivateSuperGoalException {
        try (RDFConnection conn = getConnection()) {
            int cnt;

            try (QueryExecution qExec = conn.query(String.format(QRY_ID_SUBJECT_COUNT,
                learningGoalDTO.getId()))) {
                cnt = qExec.execSelect().next().getLiteral("?count").getInt();
            }

            if (cnt == 0) {
                throw new LearningGoalNotExistsException();
            }

            if (!learningGoalDTO.isPrivateGoal()) {
                ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString("""
                    PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                    ASK {
                      ?subject ^etutor:hasSubGoal+ ?goal.
                      ?goal etutor:isPrivate true.
                      FILTER(?subject = ?startSubject)
                    }
                    """);
                parameterizedSparqlString.setIri("?startSubject", learningGoalDTO.getId());

                boolean containsPrivateSuperGoal = conn.queryAsk(parameterizedSparqlString.toString());

                if (containsPrivateSuperGoal) {
                    throw new PrivateSuperGoalException();
                }
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

            if (learningGoalDTO.isPrivateGoal()) {
                // Update 'privateGoal' of all sub goals
                String transitiveUpdateQry = String.format("""
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
                    """, learningGoalDTO.getId());

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

            CONSTRUCT {
             	?subject ?predicate ?object.
             	?subject etutor:hasReferenceCnt ?cnt
             } WHERE {
               {
                 ?subject ?predicate ?object.
                 ?subject a etutor:Goal.
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
               } {
               	SELECT (COUNT(?course) as ?cnt) ?subject WHERE {
                   ?subject a etutor:Goal.
                   OPTIONAL { ?course etutor:hasGoal ?subject }
               	}
               	GROUP BY ?subject
               }
             }
            """, owner);

        SortedSet<LearningGoalDTO> goalList = new TreeSet<>();

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

            ParameterizedSparqlString updateQry = new ParameterizedSparqlString("""
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
                """);

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

        String query = String.format("""
            CONSTRUCT { ?subject ?predicate ?object }
            WHERE {
                ?subject ?predicate ?object.
                FILTER(?subject = <http://www.dke.uni-linz.ac.at/etutorpp/Course#%s>)
            }
            """, name);

        try (RDFConnection conn = getConnection()) {

            Model model = conn.queryConstruct(query);
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

        String id = ETutorVocabulary.Course.getURI() + "#" + URLEncoder.encode(name.replace(' ', '_').trim(), Charsets.UTF_8);

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
        String query = """
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            CONSTRUCT { ?subject ?predicate ?object }
            WHERE {
                ?subject ?predicate ?object.
                ?subject a etutor:Course
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
    public SortedSet<LearningGoalDTO> getLearningGoalsForCourse(String course) throws CourseNotFoundException, InternalModelException {
        Objects.requireNonNull(course);

        String qry = String.format(QRY_ASK_COURSE_EXIST, course);

        ParameterizedSparqlString constructQry = new ParameterizedSparqlString("""
             PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
             PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

             CONSTRUCT {
             	?s ?p ?o.
               	?s etutor:hasReferenceCnt ?cnt
             }
              WHERE {
               {
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

            """);

        constructQry.setIri("?course", "http://www.dke.uni-linz.ac.at/etutorpp/Course#" + course);

        try (RDFConnection connection = getConnection()) {
            boolean exist = connection.queryAsk(qry);

            if (!exist) {
                throw new CourseNotFoundException();
            }

            SortedSet<LearningGoalDTO> goalList = new TreeSet<>();
            Model resultModel = connection.queryConstruct(constructQry.asQuery());
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
    public void removeGoalAssignment(LearningGoalAssignmentDTO learningGoalAssignmentDTO) throws LearningGoalAssignmentNonExistentException {
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

            qry = new ParameterizedSparqlString("""
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                DELETE { ?course etutor:hasGoal ?goal }
                WHERE {
                    ?course etutor:hasGoal ?goal
                }
                """);
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
        StringBuilder builder = new StringBuilder();
        builder.append("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            DELETE { ?subject etutor:hasGoal ?goal }
            INSERT {
            """);

        for(String goal : learningGoalUpdateAssignment.getLearningGoalIds()) {
            builder.append(String.format("?subject etutor:hasGoal <%s> .%n", goal));
        }

        builder.append("""
            }
            WHERE {
              ?subject a etutor:Course.
              OPTIONAL {
                ?subject etutor:hasGoal ?goal.
              }
              FILTER(?subject = ?course)
            }
            """);

        ParameterizedSparqlString updateQry = new ParameterizedSparqlString(builder.toString());
        updateQry.setIri("?course", learningGoalUpdateAssignment.getCourseId());

        try(RDFConnection conn = getConnection()) {
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
