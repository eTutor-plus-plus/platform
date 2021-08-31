package at.jku.dke.etutor.service;

import at.jku.dke.etutor.helper.RDFConnectionFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Utility class for testing RDF related controllers.
 *
 * @author fne
 */
public final class RDFTestUtil {

    /**
     * Returns the number of goals.
     *
     * @param rdfConnectionFactory the rdf connection factory
     * @return the number of goals
     */
    public static int getGoalCount(RDFConnectionFactory rdfConnectionFactory) {
        String query =
            """
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                SELECT (COUNT(DISTINCT ?subject) as ?cnt)
                WHERE
                {
                  ?subject a etutor:Goal.
                }
                """;
        return getCount(query, rdfConnectionFactory);
    }

    /**
     * Returns the number of courses.
     *
     * @param rdfConnectionFactory the rdf connection factory
     * @return the number of courses
     */
    public static int getCourseCount(RDFConnectionFactory rdfConnectionFactory) {
        String query =
            """
                PREFIX etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                SELECT (COUNT(DISTINCT ?subject) as ?cnt)
                WHERE
                {
                  ?subject a etutor:Course.
                }
                """;
        return getCount(query, rdfConnectionFactory);
    }

    /**
     * Returns the number which will be retrieved by the given query
     *
     * @param query                the query to execute
     * @param rdfConnectionFactory the rdf connection factory
     * @return the determined number
     */
    private static int getCount(String query, RDFConnectionFactory rdfConnectionFactory) {
        try (RDFConnection connection = rdfConnectionFactory.getRDFConnection()) {
            try (QueryExecution execution = connection.query(query)) {
                ResultSet set = execution.execSelect();

                QuerySolution solution = set.nextSolution();
                return solution.get("?cnt").asLiteral().getInt();
            }
        }
    }

    /**
     * Checks that the given subject exists.
     *
     * @param subject              the subject to check
     * @param rdfConnectionFactory the rdf connection factory
     */
    public static void checkThatSubjectExists(String subject, RDFConnectionFactory rdfConnectionFactory) {
        String query = String.format(
            """
                prefix etutor: <http://www.dke.uni-linz.ac.at/etutorpp/>

                ASK {
                  ?subject ?predicate ?object.
                  FILTER (?subject = %s)
                }
                """,
            subject
        );

        try (RDFConnection connection = rdfConnectionFactory.getRDFConnection()) {
            boolean res = connection.queryAsk(query);
            assertThat(res).isTrue();
        }
    }

    /**
     * Loads a given learning goal hierarchy into the RDF dataset.
     *
     * @param rdfConnectionFactory the dataset's RDF connection factory
     * @param ttlFileUrl           the URL to the turtle file which contains the learning goal hierarchy
     * @return the imported RDF model
     */
    public static Model uploadLearningGoalHierarchy(RDFConnectionFactory rdfConnectionFactory, URL ttlFileUrl) {
        Model model = RDFDataMgr.loadModel(ttlFileUrl.toString(), Lang.TTL);

        try (RDFConnection connection = rdfConnectionFactory.getRDFConnection()) {
            connection.load(model);
        }
        return model;
    }

    private RDFTestUtil() {
    }
}
