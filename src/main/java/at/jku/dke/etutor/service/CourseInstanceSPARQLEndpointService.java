package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.DisplayableCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * SPARQL endpoint service for managing course instances.
 */
@Service
public class CourseInstanceSPARQLEndpointService extends AbstractSPARQLEndpointService {
    private static final String QRY_ASK_COURSE_INSTANCE_EXISTS = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        ASK {
          ?courseInstance a etutor:CourseInstance
        }
        """;

    private static final String QRY_ASK_COURSE_EXISTS = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        ASK {
          ?course a etutor:Course
        }
        """;

    private static final String QRY_CONSTRUCT_COURSE_INSTANCES_FROM_COURSE = """
         PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
         PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

         CONSTRUCT {
           ?courseInstance ?predicate ?object.
           ?course rdfs:label ?courseName.
           ?course a etutor:Course.
           ?courseInstance etutor:hasStudent ?student.
           ?student rdfs:label ?matriklNr.
           ?student a etutor:Student.
         }
         WHERE {
           ?courseInstance etutor:hasCourse ?course.
           ?course rdfs:label ?courseName.
           ?courseInstance a etutor:CourseInstance.
           ?courseInstance ?predicate ?object.
           OPTIONAL {
             ?courseInstance etutor:hasStudent ?student.
             ?student rdfs:label ?matriklNr.
           }
         }
        """;

    public static final String QRY_CONSTRUCT_STUDENTS_OF_INSTANCE = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

        CONSTRUCT {
          ?student rdfs:label ?matriklNr.
          ?student a etutor:Student.
        } WHERE {
          ?courseInstance a etutor:CourseInstance.
          OPTIONAL {
            ?courseInstance etutor:hasStudent ?student.
            ?student rdfs:label ?matriklNr.
          }
        }
        """;

    private final UserService userService;

    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     * @param userService          the injected user service
     */
    public CourseInstanceSPARQLEndpointService(RDFConnectionFactory rdfConnectionFactory, UserService userService) {
        super(rdfConnectionFactory);

        this.userService = userService;
    }

    /**
     * Creates a new course instance.
     *
     * @param newCourseInstanceDTO the new course dto
     * @return uri of the newly created course
     * @throws CourseNotFoundException if the course can not be found
     */
    public String createNewCourseInstance(NewCourseInstanceDTO newCourseInstanceDTO) throws CourseNotFoundException {
        Model model = ModelFactory.createDefaultModel();
        String uuid = UUID.randomUUID().toString();

        try (RDFConnection connection = getConnection()) {
            Resource resource = constructNewCourseInstanceFromDTO(newCourseInstanceDTO, model, uuid, connection);
            connection.load(model);

            copyLearningGoalsIntoNamedCourseInstanceGraph(resource.getURI(), connection);

            return resource.getURI();
        }
    }

    /**
     * Sets the students for this course instance.
     *
     * @param matriculationNumbers the list of the students' matriculation numbers.
     * @param courseInstanceId     the internal id of the course instance
     * @throws CourseInstanceNotFoundException if the course instance cannot be found.
     */
    public void setStudentsOfCourseInstance(List<String> matriculationNumbers, String courseInstanceId) throws CourseInstanceNotFoundException {
        Objects.requireNonNull(matriculationNumbers);
        Objects.requireNonNull(courseInstanceId);

        ParameterizedSparqlString courseInstanceExistsQry = new ParameterizedSparqlString(QRY_ASK_COURSE_INSTANCE_EXISTS);
        courseInstanceExistsQry.setIri("?courseInstance", courseInstanceId);

        try (RDFConnection connection = getConnection()) {
            boolean courseInstanceExists = connection.queryAsk(courseInstanceExistsQry.asQuery());

            if (!courseInstanceExists) {
                throw new CourseInstanceNotFoundException();
            }

            ParameterizedSparqlString updateQry = new ParameterizedSparqlString("""
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

                DELETE {
                  ?courseInstance etutor:hasStudent ?student.
                  ?student a etutor:Student.
                  ?student rdfs:label ?matriculationNr.
                } INSERT {
                """);

            for (String matriculationNumber : matriculationNumbers) {
                String studentUri = String.format("http://www.dke.uni-linz.ac.at/etutorpp/Student#%s", matriculationNumber);
                updateQry.append("?courseInstance etutor:hasStudent ");
                updateQry.appendIri(studentUri);
                updateQry.append(".\n");
                updateQry.appendIri(studentUri);
                updateQry.append(" a etutor:Student.\n");
                updateQry.appendIri(studentUri);
                updateQry.append(" rdfs:label ");
                updateQry.appendLiteral(matriculationNumber);
                updateQry.append(".\n");
            }

            updateQry.append("""
                } WHERE {
                  ?courseInstance a etutor:CourseInstance.
                  OPTIONAL {
                    ?courseInstance etutor:hasStudent ?student.
                    ?student rdfs:label ?matriculationNr.
                  }
                }
                """);
            updateQry.setIri("?courseInstance", courseInstanceId);
            connection.update(updateQry.asUpdate());
        }
    }

    /**
     * Returns the instances of the given course.
     *
     * @param courseName the name of the course
     * @return sorted set of course instances
     * @throws CourseNotFoundException if the given course can not be found
     */
    public SortedSet<CourseInstanceDTO> getInstancesOfCourse(String courseName) throws CourseNotFoundException {
        Objects.requireNonNull(courseName);

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_CONSTRUCT_COURSE_INSTANCES_FROM_COURSE);
        String courseId = String.format("http://www.dke.uni-linz.ac.at/etutorpp/Course#%s", courseName.replace(' ', '_'));
        qry.setIri("?course", courseId);

        ParameterizedSparqlString courseExistsQry = new ParameterizedSparqlString(QRY_ASK_COURSE_EXISTS);
        courseExistsQry.setIri("?course", courseId);

        try (RDFConnection connection = getConnection()) {
            boolean courseExists = connection.queryAsk(courseExistsQry.asQuery());

            if (!courseExists) {
                throw new CourseNotFoundException();
            }

            Model model = connection.queryConstruct(qry.asQuery());

            SortedSet<CourseInstanceDTO> courseInstances = new TreeSet<>(Comparator.comparing(CourseInstanceDTO::getInstanceName)
                .thenComparing(CourseInstanceDTO::getId));

            ResIterator iterator = model.listSubjectsWithProperty(RDF.type, ETutorVocabulary.Student);
            Map<String, StudentInfoDTO> studentCache = getStudentCache(iterator);

            iterator = model.listSubjectsWithProperty(RDF.type, ETutorVocabulary.CourseInstance);
            try {
                while (iterator.hasNext()) {
                    Resource courseInstanceResource = iterator.nextResource();
                    courseInstances.add(constructCourseInstanceDTOFromResource(courseInstanceResource, studentCache));
                }
            } finally {
                iterator.close();
            }

            return courseInstances;
        }
    }

    /**
     * Returns the assigned students of a given course instance id.
     *
     * @param uuid the uuid of the course instance
     * @return collection containing the assigned students
     */
    public Collection<StudentInfoDTO> getStudentsOfCourseInstance(String uuid) {
        Objects.requireNonNull(uuid);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(uuid);
        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_CONSTRUCT_STUDENTS_OF_INSTANCE);
        query.setIri("?courseInstance", courseInstanceId);

        try (RDFConnection connection = getConnection()) {
            Model model = connection.queryConstruct(query.asQuery());

            if (model.isEmpty()) {
                return Collections.emptyList();
            }

            ResIterator iterator = model.listSubjectsWithProperty(RDF.type, ETutorVocabulary.Student);
            Map<String, StudentInfoDTO> studentCache = getStudentCache(iterator);
            return StreamEx.of(studentCache.values())
                .sorted(Comparator.comparing(StudentInfoDTO::getLastName)
                    .thenComparing(StudentInfoDTO::getFirstName))
                .toList();
        }
    }

    /**
     * Returns a specific course instance.
     *
     * @param uuid the internal uuid of the course instance
     * @return {@link Optional} which is either empty or contains the specific course instance object
     */
    public Optional<CourseInstanceDTO> getCourseInstance(String uuid) {
        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(uuid);
        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_CONSTRUCT_COURSE_INSTANCES_FROM_COURSE);
        qry.setIri("?courseInstance", courseInstanceId);

        try (RDFConnection connection = getConnection()) {
            Model model = connection.queryConstruct(qry.asQuery());

            if (model.isEmpty()) {
                return Optional.empty();
            }

            Resource courseInstanceResource = model.getResource(courseInstanceId);
            ResIterator iterator = model.listSubjectsWithProperty(RDF.type, ETutorVocabulary.Student);
            Map<String, StudentInfoDTO> studentCache = getStudentCache(iterator);

            return Optional.of(constructCourseInstanceDTOFromResource(courseInstanceResource, studentCache));
        }
    }

    /**
     * Returns the page of displayable course instances.
     *
     * @param courseName the course name
     * @param page       the paging information
     * @return page of the course instance
     */
    public Page<DisplayableCourseInstanceDTO> getDisplayableCourseInstancesOfCourse(String courseName, Pageable page) {
        Objects.requireNonNull(courseName);
        Objects.requireNonNull(page);

        String courseId = String.format("http://www.dke.uni-linz.ac.at/etutorpp/Course#%s", courseName.replace(' ', '_'));
        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT (str(?instance) as ?instanceId) ?year (str(?term) as ?termId) ?instanceName (COUNT(?student) as ?studentCnt)
            WHERE {
              ?instance a etutor:CourseInstance.
              ?instance etutor:hasCourse ?course.
              ?instance rdfs:label ?instanceName.
              ?instance etutor:hasInstanceYear ?year.
              ?instance etutor:hasTerm ?term.
              OPTIONAL {
                ?instance etutor:hasStudent ?student
              }
            }
            GROUP BY ?instance ?year ?term ?instanceName
            ORDER BY ?year ?term ?instanceName
            """);

        if (page.isPaged()) {
            query.append("LIMIT ");
            query.append(page.getPageSize() + 1);
            query.append("\nOFFSET ");
            query.append(page.getOffset());
        }
        query.setIri("?course", courseId);

        ParameterizedSparqlString countQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT (COUNT(DISTINCT ?instance) as ?cnt)
            WHERE {
              ?instance a etutor:CourseInstance.
              ?instance etutor:hasCourse ?course.
            }
            """);
        countQry.setIri("?course", courseId);

        try (RDFConnection connection = getConnection()) {
            List<DisplayableCourseInstanceDTO> list = new ArrayList<>();
            int count;
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();

                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    String id = solution.getLiteral("?instanceId").getString();
                    int year = solution.getLiteral("?year").getInt();
                    String termId = solution.getLiteral("?termId").getString();
                    int studentCount = solution.getLiteral("?studentCnt").getInt();
                    String name = solution.getLiteral("?instanceName").getString();
                    list.add(new DisplayableCourseInstanceDTO(id, name, studentCount, year, termId));
                }
            }
            try (QueryExecution execution = connection.query(countQry.asQuery())) {
                ResultSet set = execution.execSelect();
                //noinspection ResultOfMethodCallIgnored
                set.hasNext();
                QuerySolution solution = set.nextSolution();
                count = solution.getLiteral("?cnt").getInt();
            }
            return PageableExecutionUtils.getPage(list, page, () -> count);
        }
    }

    /**
     * Returns the student info cache from a given student iterator.
     *
     * @param studentIterator the student iterator
     * @return map of student infos where the key is the matriculation number
     */
    private Map<String, StudentInfoDTO> getStudentCache(ResIterator studentIterator) {
        List<String> matriculationNumbers = new ArrayList<>();
        try {
            while (studentIterator.hasNext()) {
                Resource resource = studentIterator.nextResource();
                String matriculationNumber = resource.getProperty(RDFS.label).getLiteral().getString();
                matriculationNumbers.add(matriculationNumber);
            }
        } finally {
            studentIterator.close();
        }
        return userService.getStudentInfoAsMap(matriculationNumbers);
    }

    /**
     * Creates a new course instance from the given dto.
     *
     * @param newCourseInstanceDTO the dto
     * @param model                the base model
     * @param uuid                 the generated uuid
     * @return the new course instance resource
     * @throws CourseNotFoundException if the course does not exist
     */
    private Resource constructNewCourseInstanceFromDTO(NewCourseInstanceDTO newCourseInstanceDTO, Model model, String uuid, RDFConnection connection)
        throws CourseNotFoundException {
        Resource resource = ETutorVocabulary.createCourseInstanceOfModel(uuid, model);

        ParameterizedSparqlString courseQuery = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT ?courseName
            WHERE {
              ?course a etutor:Course.
              ?course rdfs:label ?courseName.
            }
            """);
        courseQuery.setIri("?course", newCourseInstanceDTO.getCourseId());

        String courseName;

        try (QueryExecution queryExecution = connection.query(courseQuery.asQuery())) {
            ResultSet set = queryExecution.execSelect();

            if (!set.hasNext()) {
                throw new CourseNotFoundException();
            }
            QuerySolution solution = set.nextSolution();
            courseName = solution.getLiteral("?courseName").getString();
        }

        resource.addProperty(ETutorVocabulary.hasCourse, model.createResource(newCourseInstanceDTO.getCourseId()));
        resource.addProperty(ETutorVocabulary.hasInstanceYear, String.valueOf(newCourseInstanceDTO.getYear()), XSDDatatype.XSDint);
        resource.addProperty(ETutorVocabulary.hasTerm, model.createResource(newCourseInstanceDTO.getTermId()));
        resource.addProperty(RDFS.label, String.format("%s %s %d", courseName, ETutorVocabulary.getTermTextFromUri(newCourseInstanceDTO.getTermId()),
            newCourseInstanceDTO.getYear()));
        resource.addProperty(RDF.type, ETutorVocabulary.CourseInstance);

        if (StringUtils.isNotBlank(newCourseInstanceDTO.getDescription())) {
            resource.addProperty(ETutorVocabulary.hasInstanceDescription, newCourseInstanceDTO.getDescription().trim());
        }

        return resource;
    }

    /**
     * Copies the learning goal structure into the named graph of the corresponding
     * course instance.
     *
     * @param courseInstanceId the internal course instance uri
     * @param connection       the rdf connection
     */
    private void copyLearningGoalsIntoNamedCourseInstanceGraph(String courseInstanceId, RDFConnection connection) {
        ParameterizedSparqlString query = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            INSERT {
              GRAPH ?instance {
              	?goal a etutor:Goal.
              }
            }
            WHERE {
              ?instance a etutor:CourseInstance.
              ?instance etutor:hasCourse ?course.
              ?course etutor:hasGoal/etutor:hasSubGoal* ?goal.
            }
            """);
        query.setIri("?instance", courseInstanceId);

        connection.update(query.asUpdate());
    }

    /**
     * Constructs a course instance DTO from the given resource and student cache.
     *
     * @param resource     the resource from the rdf graph
     * @param studentCache the student cache (the key represents the matriculation number)
     * @return course instance DTO
     */
    private CourseInstanceDTO constructCourseInstanceDTOFromResource(Resource resource, Map<String, StudentInfoDTO> studentCache) {
        int year = resource.getProperty(ETutorVocabulary.hasInstanceYear).getInt();
        String termId = resource.getProperty(ETutorVocabulary.hasTerm).getObject().asResource().getURI();

        Statement descriptionStatement = resource.getProperty(ETutorVocabulary.hasInstanceDescription);
        String description = null;
        if (descriptionStatement != null) {
            description = descriptionStatement.getLiteral().getString();
        }
        String id = resource.getURI();
        String courseName = resource.getProperty(ETutorVocabulary.hasCourse).getProperty(RDFS.label).getString();
        String instanceName = resource.getProperty(RDFS.label).getString();
        List<StudentInfoDTO> students = new ArrayList<>();

        StmtIterator studentIterator = resource.listProperties(ETutorVocabulary.hasStudent);
        try {
            while (studentIterator.hasNext()) {
                Statement statement = studentIterator.nextStatement();
                String matriculationNumber = statement.getObject().asResource().getProperty(RDFS.label).getString();
                students.add(studentCache.get(matriculationNumber));
            }
        } finally {
            studentIterator.close();
        }

        return new CourseInstanceDTO(year, termId, description, id, students, courseName, instanceName);
    }
}
