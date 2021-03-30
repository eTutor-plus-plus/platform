package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.LecturerGradingInfoDTO;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.StudentAssignmentOverviewInfoDTO;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service endpoint for managing lecturer related data.
 *
 * @author fne
 */
@Service
public class LecturerSPARQLEndpointService extends AbstractSPARQLEndpointService {

    private static final String QRY_SELECT_LECTURER_OVERVIEW = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        SELECT ?student ?submitted ?fullyGraded
        WHERE {
          {
            ?student etutor:hasIndividualTaskAssignment [
              etutor:fromExerciseSheet ?sheet;
              etutor:fromCourseInstance ?courseInstance;
              etutor:isAssignmentSubmitted ?submitted;
            ].
            OPTIONAL {
              {
                SELECT (COUNT(*) AS ?cnt) ?student
                WHERE {
                  ?student etutor:hasIndividualTaskAssignment ?individualAssignment.
                  ?individualAssignment etutor:fromExerciseSheet ?sheet;
                                        etutor:fromCourseInstance ?courseInstance;
                                        etutor:isAssignmentSubmitted ?submitted.
                  FILTER(NOT EXISTS{
                      ?individualAssignment etutor:hasIndividualTask [
                        etutor:isGraded false
                      ]
                  })
                }
                GROUP BY ?student
              }
            }
            BIND(bound(?cnt) AS ?fullyGraded)
          }
        }
        ORDER BY (?student)
        """;

    private static final String QRY_COUNT_LECTURER_OVERVIEW = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        SELECT (COUNT(*) as ?cnt)
        WHERE {
          {
            ?student etutor:hasIndividualTaskAssignment [
              etutor:fromExerciseSheet ?sheet;
              etutor:fromCourseInstance ?courseInstance;
            ]
          }
        }
        """;

    private static final String QRY_GRADING_OVERVIEW = """
        PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

        SELECT ?task ?taskTitle ?completed ?graded ?orderNo
        WHERE {
          ?student etutor:hasIndividualTaskAssignment [
            etutor:fromExerciseSheet ?sheet;
            etutor:fromCourseInstance ?courseInstance;
            etutor:hasIndividualTask [
              etutor:isGraded ?graded;
              etutor:refersToTask ?task;
              etutor:hasOrderNo ?orderNo;
              etutor:isLearningGoalCompleted ?completed
            ]
          ].
          ?task etutor:hasTaskHeader ?taskTitle.
        }
        ORDER BY (?orderNo)
        """;

    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public LecturerSPARQLEndpointService(RDFConnectionFactory rdfConnectionFactory) {
        super(rdfConnectionFactory);
    }

    /**
     * Returns the page of the lecturer overview.
     *
     * @param courseInstanceUUID the course instance uuid
     * @param exerciseSheetUUID  the exercise sheet uuid
     * @param page               the pageing information
     * @return page of the lecturer overview
     */
    public Page<StudentAssignmentOverviewInfoDTO> getPagedLecturerOverview(String courseInstanceUUID, String exerciseSheetUUID, Pageable page) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(page);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_SELECT_LECTURER_OVERVIEW);
        ParameterizedSparqlString countQry = new ParameterizedSparqlString(QRY_COUNT_LECTURER_OVERVIEW);

        countQry.setIri("?courseInstance", courseInstanceId);
        countQry.setIri("?sheet", exerciseSheetId);

        if (page.isPaged()) {
            qry.append("LIMIT ");
            qry.append(page.getPageSize());
            qry.append("\nOFFSET ");
            qry.append(page.getOffset());
        }
        qry.setIri("?courseInstance", courseInstanceId);
        qry.setIri("?sheet", exerciseSheetId);

        List<StudentAssignmentOverviewInfoDTO> entries = new ArrayList<>();
        long count;
        try (RDFConnection connection = getConnection()) {
            try (QueryExecution execution = connection.query(qry.asQuery())) {
                ResultSet set = execution.execSelect();

                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();

                    String studentURI = solution.getResource("?student").getURI();
                    String matriculationNo = studentURI.substring(studentURI.lastIndexOf('#') + 1);

                    boolean submitted = solution.getLiteral("?submitted").getBoolean();
                    boolean fullyGraded = solution.getLiteral("?fullyGraded").getBoolean();

                    entries.add(new StudentAssignmentOverviewInfoDTO(matriculationNo, submitted, fullyGraded));
                }
            }
            try (QueryExecution execution = connection.query(countQry.asQuery())) {
                ResultSet set = execution.execSelect();
                //noinspection ResultOfMethodCallIgnored
                set.hasNext();
                QuerySolution solution = set.nextSolution();
                count = solution.getLiteral("?cnt").getInt();
            }
        }
        return PageableExecutionUtils.getPage(entries, page, () -> count);
    }

    /**
     * Retrieves the grading info for a specific exercise sheet from a given student from a course instance.
     *
     * @param courseInstanceUUID the course instance uuid
     * @param exerciseSheetUUID  the exercise sheet uuid
     * @param matriculationNo    the student's matriculation number
     * @return the list of grading infos
     */
    public List<LecturerGradingInfoDTO> getGradingInfo(String courseInstanceUUID, String exerciseSheetUUID, String matriculationNo) {
        Objects.requireNonNull(courseInstanceUUID);
        Objects.requireNonNull(exerciseSheetUUID);
        Objects.requireNonNull(matriculationNo);

        String courseInstanceId = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        String exerciseSheetId = ETutorVocabulary.createExerciseSheetURLString(exerciseSheetUUID);
        String studentId = ETutorVocabulary.getStudentURLFromMatriculationNumber(matriculationNo);

        ParameterizedSparqlString qry = new ParameterizedSparqlString(QRY_GRADING_OVERVIEW);
        qry.setIri("?student", studentId);
        qry.setIri("?sheet", exerciseSheetId);
        qry.setIri("?courseInstance", courseInstanceId);

        try (RDFConnection connection = getConnection()) {
            List<LecturerGradingInfoDTO> list = new ArrayList<>();
            try (QueryExecution execution = connection.query(qry.asQuery())) {
                ResultSet set = execution.execSelect();

                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    String taskUrl = solution.getResource("?task").getURI();
                    String taskTitle = solution.getLiteral("?taskTitle").getString();
                    boolean completed = solution.getLiteral("?completed").getBoolean();
                    boolean graded = solution.getLiteral("?graded").getBoolean();
                    int orderNo = solution.getLiteral("?orderNo").getInt();

                    list.add(new LecturerGradingInfoDTO(taskUrl, taskTitle, completed, graded, orderNo));
                }
            }
            return list;
        }
    }
}
