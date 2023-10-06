package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.client.dke.DatalogClient;
import at.jku.dke.etutor.service.client.dke.DkeSubmissionClient;
import at.jku.dke.etutor.service.client.dke.SqlClient;
import at.jku.dke.etutor.service.client.dke.XQueryClient;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Data endpoints providing things like sql-tables, datalog facts to the client.
 */
@RestController
@RequestMapping("/api/dispatcher")
public class DispatcherDataResource {
        private final SqlClient sqlCLient;
        private final DatalogClient datalogClient;
        private final XQueryClient xQueryClient;
        public DispatcherDataResource(SqlClient sqlCLient,
                                      DatalogClient datalogClient,
                                      XQueryClient xQueryClient){
            this.sqlCLient = sqlCLient;
            this.datalogClient = datalogClient;
            this.xQueryClient = xQueryClient;
        }
        @GetMapping(value="/sql/table/{tableName}")
        public ResponseEntity<String> getHTMLTableForSQL(@PathVariable String tableName, @RequestParam(defaultValue = "-1") int connId, @RequestParam(defaultValue="-1") int exerciseId, @RequestParam(defaultValue = "") String taskGroup) throws DispatcherRequestFailedException {
            return sqlCLient.getHTMLTableForSQL(tableName, connId, exerciseId, taskGroup);
        }

        /**
         * Requests the facts for a datalog task group in HTML format
         * @param id the id of the facts
         * @return the facts
         */
        @GetMapping("/datalog/facts/id/{id}")
        public ResponseEntity<String> getDLGFacts(@PathVariable int id) throws DispatcherRequestFailedException {
            return datalogClient.getDLGFacts(id);
        }

        /**
         * Requests the facts for a datalog task group as raw string
         * @param id the id of the facts
         * @return the facts
         */
        @GetMapping("/datalog/facts/id/{id}/asinputstream")
        public ResponseEntity<Resource> getDLGFactsAsInputStream(@PathVariable int id) throws DispatcherRequestFailedException {
            return datalogClient.getDLGFactsAsInputStream(id);
        }

        /**
         * Returns the xml  for an xquery taskgroup
         * @param id the file id of the xml
         * @return a ResponseEntity
         */
        @GetMapping("/xquery/xml/fileid/{id}")
        public ResponseEntity<String> getXMLForXQByFileId(@PathVariable int id) throws DispatcherRequestFailedException {
            return xQueryClient.getXMLForXQByFileId(id);
        }

        /**
         * Returns the xml  for an xquery taskgroup as inputstream
         * @param id the file id of the xml
         * @return a ResponseEntity containing the InputStreamResource
         */
        @GetMapping("/xquery/xml/fileid/{id}/asinputstream")
        public ResponseEntity<Resource> getXMLForXQByFileIdAsInputStream(@PathVariable int id) throws DispatcherRequestFailedException {
            return xQueryClient.getXMLForXQByFileIdAsInputStream(id);
        }
}
