package at.jku.dke.etutor.service;

import at.jku.dke.etutor.helper.RDFConnectionFactory;
import at.jku.dke.etutor.service.dto.CourseOverviewDTO;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
     * Returns the paged courses of a user who is a course instructor.
     *
     * @param login the user's login
     * @param page  the pagination information
     * @return paged courses
     */
    public Page<CourseOverviewDTO> getPagedCoursesOfUser(String login, Pageable page) {
        Objects.requireNonNull(login);

        ParameterizedSparqlString selectQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            SELECT ?courseName (STR(?course) AS ?courseInstanceId)
            WHERE {
              ?course etutor:hasCourseCreator ?creator.
              ?course a etutor:Course.
              ?courseInstance etutor:hasCourse ?course.
              ?courseInstance a etutor:CourseInstance.
              ?courseInstance rdfs:label ?courseName.
            }
            ORDER BY LCASE(?courseName)
            """);

        if (page.isPaged()) {
            selectQry.append("LIMIT ");
            selectQry.append(page.getPageSize());
            selectQry.append("\nOFFSET ");
            selectQry.append(page.getOffset());
        }

        ParameterizedSparqlString countQry = new ParameterizedSparqlString("""
            PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

            SELECT (COUNT(?courseInstance) AS ?cnt)
            WHERE {
              ?course etutor:hasCourseCreator ?creator.
              ?course a etutor:Course.
              ?courseInstance etutor:hasCourse ?course.
              ?courseInstance a etutor:CourseInstance.
            }
            """);

        selectQry.setLiteral("?creator", login);
        countQry.setLiteral("?creator", login);

        try (RDFConnection connection = getConnection()) {
            List<CourseOverviewDTO> courses = new ArrayList<>();
            long count;
            try (QueryExecution queryExecution = connection.query(countQry.asQuery())) {
                ResultSet set = queryExecution.execSelect();
                //noinspection ResultOfMethodCallIgnored
                set.hasNext();
                count = set.nextSolution().getLiteral("?cnt").getInt();
            }

            try (QueryExecution queryExecution = connection.query(selectQry.asQuery())) {
                ResultSet set = queryExecution.execSelect();

                while (set.hasNext()) {
                    QuerySolution solution = set.nextSolution();
                    String name = solution.getLiteral("?courseName").getString();
                    String id = solution.getLiteral("?courseInstanceId").getString();

                    courses.add(new CourseOverviewDTO(id, name));
                }

                return PageableExecutionUtils.getPage(courses, page, () -> count);
            }
        }
    }
}
