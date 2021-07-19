package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.lectureroverview.LearningGoalProgressDTO;
import at.jku.dke.etutor.service.dto.lectureroverview.StatisticsOverviewModelDTO;
import at.jku.dke.etutor.service.exception.CourseInstanceNotFoundException;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Service for managing lecturer overview related data.
 *
 * @author fne
 */
@Service
public class LecturerOverviewService extends AbstractSPARQLEndpointService {

    /**
     * Constructor.
     *
     * @param rdfConnectionFactory the injected rdf connection factory
     */
    public LecturerOverviewService(RDFConnectionFactory rdfConnectionFactory) {
        super(rdfConnectionFactory);
    }

    /**
     * Returns the statistics overview of a given course instance.
     *
     * @param courseInstanceUUID the course instance's UUID
     * @return the {@link StatisticsOverviewModelDTO} containing the info
     * @throws CourseInstanceNotFoundException if a course instance can not be found
     */
    public StatisticsOverviewModelDTO getCourseInstanceOverviewStatistics(String courseInstanceUUID) throws CourseInstanceNotFoundException {
        Objects.requireNonNull(courseInstanceUUID);

        String courseInstanceUrl = ETutorVocabulary.createCourseInstanceURLString(courseInstanceUUID);
        StatisticsOverviewModelDTO statisticsOverviewModel = new StatisticsOverviewModelDTO();

        ParameterizedSparqlString courseInstanceNameAndStudCntQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT ?courseInstanceName (COUNT(?student) AS ?studentCnt)
            WHERE {
              ?courseInstance a etutor:CourseInstance.
              ?courseInstance rdfs:label ?courseInstanceName.
              ?courseInstance etutor:hasStudent ?student.
            }
            GROUP by ?courseInstanceName
            """);

        ParameterizedSparqlString studentsProgressQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT (STR(?goal) AS ?goalId) ?goalName (COUNT(?student) AS ?absoluteCnt)
            WHERE {
              GRAPH ?courseInstance {
                ?goal a etutor:Goal.
                OPTIONAL {
                  ?goal etutor:isCompletedFrom ?student
                }
              }
              ?goal rdfs:label ?goalName.
            }
            GROUP BY ?goal ?goalName
            ORDER BY LCASE(?goalName)
            """);

        courseInstanceNameAndStudCntQry.setIri("?courseInstance", courseInstanceUrl);
        studentsProgressQry.setIri("?courseInstance", courseInstanceUrl);

        try (RDFConnection connection = getConnection()) {
            int studentCount;

            try (QueryExecution queryExecution = connection.query(courseInstanceNameAndStudCntQry.asQuery())) {
                ResultSet set = queryExecution.execSelect();

                if (set.hasNext()) {
                    var solution = set.nextSolution();
                    statisticsOverviewModel.setCourseInstanceName(
                        solution.getLiteral("?courseInstanceName").getString());

                    studentCount = solution.getLiteral("?studentCnt").getInt();
                    statisticsOverviewModel.setStudentCount(studentCount);
                } else {
                    throw new CourseInstanceNotFoundException();
                }
            }

            try (QueryExecution queryExecution = connection.query(studentsProgressQry.asQuery())) {
                ResultSet set = queryExecution.execSelect();

                while (set.hasNext()) {
                    var solution = set.nextSolution();

                    String goalId = solution.getLiteral("?goalId").getString();
                    String goalName = solution.getLiteral("?goalName").getString();
                    int absoluteCount = solution.getLiteral("?absoluteCnt").getInt();
                    double relativeCount = Math.round((absoluteCount / ((double) studentCount)) * 100);

                    BigDecimal bigDecimalRelativeCount = new BigDecimal(relativeCount).setScale(2, RoundingMode.HALF_UP);
                    relativeCount = bigDecimalRelativeCount.doubleValue();

                    statisticsOverviewModel.addStatisticOverviewModel(new LearningGoalProgressDTO(goalId, goalName, absoluteCount, relativeCount));
                }
            }
        }

        return statisticsOverviewModel;
    }
}
