package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.CSVHelper;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.repository.StudentRepository;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.dto.UserDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceInformationDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentImportDTO;
import at.jku.dke.etutor.service.exception.StudentCSVImportException;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Service class for managing students.
 *
 * @author fne
 */
@Service
public class StudentService extends AbstractSPARQLEndpointService {

    private static final String QRY_STUDENTS_COURSES = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

        SELECT DISTINCT (STR(?term) AS ?termId) ?courseName ?instructor (STR(?instance) AS ?instanceId) ?year
        WHERE {
          ?instance etutor:hasStudent ?student.
          ?instance etutor:hasTerm ?term.
          ?instance etutor:hasCourse ?course.
          ?instance etutor:hasInstanceYear ?year.
          ?course rdfs:label ?courseName.
          ?course etutor:hasCourseCreator ?instructor.
        }
        ORDER BY(?courseName)
        """;

    private final UserService userService;
    private final StudentRepository studentRepository;

    /**
     * Constructor.
     *
     * @param userService          the injected user service
     * @param studentRepository    the injected student repository
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public StudentService(UserService userService, StudentRepository studentRepository, RDFConnectionFactory rdfConnectionFactory) {
        super(rdfConnectionFactory);
        this.userService = userService;
        this.studentRepository = studentRepository;
    }

    /**
     * Reads the students from the given CSV file and imports the student into the database if
     * they do not exist in the database.
     *
     * @param file the multipart csv file containing the students
     * @return list of loaded students
     * @throws StudentCSVImportException if an import error occurs
     */
    @Transactional
    public List<StudentImportDTO> importStudentsFromFile(MultipartFile file) throws StudentCSVImportException {
        if (!CSVHelper.hasCSVFileFormat(file)) {
            throw new StudentCSVImportException();
        }
        List<StudentImportDTO> students = CSVHelper.getStudentsFromCSVFile(file);

        for (StudentImportDTO student : students) {
            if (!studentRepository.studentExists(student.getMatriculationNumber())) {
                UserDTO userDTO = new UserDTO();
                userDTO.setFirstName(student.getFirstName());
                userDTO.setLastName(student.getLastName());
                userDTO.setEmail(student.getEmail());
                userDTO.setAuthorities(Set.of(AuthoritiesConstants.STUDENT));
                userDTO.setLogin(student.getMatriculationNumber());
                userService.createUser(userDTO);
            }
        }

        return students;
    }

    /**
     * Returns the courses of a given student.
     *
     * @param matriculationNumber the student's matriculation number - must not be null
     * @return list of courses
     */
    public List<CourseInstanceInformationDTO> getCoursesFromStudent(String matriculationNumber) {
        Objects.requireNonNull(matriculationNumber);

        String studentURI = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNumber);
        ParameterizedSparqlString query = new ParameterizedSparqlString(QRY_STUDENTS_COURSES);
        query.setIri("?student", studentURI);

        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(query.asQuery())) {
                ResultSet set = execution.execSelect();
                List<CourseInstanceInformationDTO> retList = new ArrayList<>();
                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    String termId = solution.getLiteral("?termId").getString();
                    String courseName = solution.getLiteral("?courseName").getString();
                    String instructor = solution.getLiteral("?instructor").getString();
                    String instanceId = solution.getLiteral("?instanceId").getString();
                    int year = solution.getLiteral("?year").getInt();

                    retList.add(new CourseInstanceInformationDTO(courseName, termId, instructor, instanceId, year));
                }
                return retList;
            }
        }
    }
}
