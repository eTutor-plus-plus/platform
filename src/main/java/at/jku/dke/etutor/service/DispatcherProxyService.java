package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.dto.dispatcher.*;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.web.rest.DispatcherProxyResource;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
//TODO: logging
//TODO: documentation
@Service
public class DispatcherProxyService {
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final DispatcherProxyResource proxyResource;
    private final ObjectMapper mapper;
    private final Logger LOGGER;

    public DispatcherProxyService(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService, DispatcherProxyResource proxyResource){
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.proxyResource = proxyResource;
        this.mapper = new ObjectMapper();
        LOGGER = (Logger) LoggerFactory.getLogger(DispatcherProxyService.class);
    }

    /**
     * Returns the DispatcherSubmission for a given id
     * @param UUID the UUID
     * @return the submission
     * @throws JsonProcessingException if the returned value cannot be deserialized
     */
    public DispatcherSubmissionDTO getSubmission(String UUID) throws JsonProcessingException {
        return  mapper.readValue(proxyResource.getSubmission(UUID).getBody(), DispatcherSubmissionDTO.class);
    }

    /**
     * Returns the grading for a given id
     * @param UUID the UUID
     * @return the grading
     * @throws JsonProcessingException if the returned value cannot be parsed
     */
    public DispatcherGradingDTO getGrading(String UUID) throws JsonProcessingException {
        return mapper.readValue(proxyResource.getGrading(UUID).getBody(), DispatcherGradingDTO.class);
    }
    /**
     * Sends the request to add the xml-files for an xquery task group to the DispatcherProxyResource
     * @param taskGroupDTO the task group
     * @param request the http request to retrieve the base url
     * @param token the authorization token
     */
    public void proxyXMLtoDispatcher(TaskGroupDTO taskGroupDTO, HttpServletRequest request, String token) {
        Objects.requireNonNull(taskGroupDTO.getId());
        Objects.requireNonNull(taskGroupDTO.getxQueryDiagnoseXML());
        Objects.requireNonNull(taskGroupDTO.getxQuerySubmissionXML());

        String diagnoseXML = taskGroupDTO.getxQueryDiagnoseXML();
        String submisisonXML = taskGroupDTO.getxQuerySubmissionXML();
        DispatcherXMLDTO body = new DispatcherXMLDTO(diagnoseXML,submisisonXML);
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }
        var response = proxyResource.addXMLForXQTaskGroup(taskGroupDTO.getName(), jsonBody);
        /*
        token = token.substring(7);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
        baseUrl += "/api/dispatcher/";

        String url = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        url = baseUrl + "xquery/xml/taskGroup/" + taskGroupDTO.getName();
        var fileURL = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
         */
        var fileURL = response.getBody();
        assignmentSPARQLEndpointService.addXMLFileURL(taskGroupDTO, fileURL);
    }

    /**
     * Utility method that triggers deletion of task-group related resources by the dispatcher
     * @param taskGroupDTO  the task group
     * @param request  the request
     * @param token the auth token
     */
    public void deleteDispatcherResourcesForTaskGroup(TaskGroupDTO taskGroupDTO, HttpServletRequest request, String token) {
        Objects.requireNonNull(taskGroupDTO);
        Objects.requireNonNull(taskGroupDTO.getTaskGroupTypeId());
        Objects.requireNonNull(taskGroupDTO.getName());

        if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())){
            proxyResource.deleteXMLofXQTaskGroup(taskGroupDTO.getName());
            /*
            token = token.substring(7);

            String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
            baseUrl += "/api/dispatcher/";

            String url = "";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(null, headers);

            url = baseUrl + "xquery/xml/taskGroup/" + taskGroupDTO.getName();
            var response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class).getBody();
             */
        }
    }

    public int createXQueryTask(NewTaskAssignmentDTO newTaskAssignmentDTO, String token, HttpServletRequest request) {
        Objects.requireNonNull(newTaskAssignmentDTO.getxQuerySolution());

        List<String> sortings = new ArrayList<>();
        String dtoSorting = newTaskAssignmentDTO.getxQueryXPathSorting();
        if(dtoSorting != null) sortings.add(dtoSorting);

        XQueryExerciseDTO body = new XQueryExerciseDTO();
        body.setQuery(newTaskAssignmentDTO.getxQuerySolution());
        body.setSortedNodes(sortings);

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return -1;
        }
        var response = proxyResource.createXQExercise(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1), jsonBody);
        return response.getBody();
        /*
        token = token.substring(7);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
        baseUrl += "/api/dispatcher/";

        String url = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        url = baseUrl + "xquery/exercise/taskGroup/" + newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1);
        var id = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
        return Integer.parseInt(id);
         */

    }

    /**
     * Returns exercise information for a given id
     * @param taskIdForDispatcher the task id
     * @param token the auth token
     * @param request the HttpServletRequest
     * @return an XQueryExerciseDTO
     */
    public XQueryExerciseDTO getXQExerciseInfo(String taskIdForDispatcher, String token, HttpServletRequest request) throws JsonProcessingException {
        return mapper.readValue(proxyResource.getXQExerciseInfo(Integer.parseInt(taskIdForDispatcher)).getBody(), XQueryExerciseDTO.class);
        /*
        token = token.substring(7);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
        baseUrl += "/api/dispatcher/";

        String url = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        url = baseUrl + "xquery/exercise/solution/id/" + taskIdForDispatcher;
        return restTemplate.exchange(url, HttpMethod.GET, entity, XQueryExerciseDTO.class).getBody();
         */
    }

    private String updateXQExercise(TaskAssignmentDTO taskAssignmentDTO, String token, HttpServletRequest request)  {
        List<String> sortings = new ArrayList<>();
        String dtoSorting = taskAssignmentDTO.getxQueryXPathSorting();
        if(dtoSorting != null) sortings.add(dtoSorting);

        XQueryExerciseDTO body = new XQueryExerciseDTO();
        body.setQuery(taskAssignmentDTO.getxQuerySolution());
        body.setSortedNodes(sortings);

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";

        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return proxyResource.updateXQExercise(Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()), jsonBody).getBody();

        /*
        token = token.substring(7);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
        baseUrl += "/api/dispatcher/";

        String url = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        url = baseUrl + "xquery/exercise/id/" + taskAssignmentDTO.getTaskIdForDispatcher();
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
         */
    }

    public String deleteTaskAssignment(TaskAssignmentDTO taskAssignmentDTO, String token, HttpServletRequest request) {
        String taskType = taskAssignmentDTO.getTaskAssignmentTypeId();
        int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());

        if(taskType.equals(ETutorVocabulary.NoType) || taskType.equals(ETutorVocabulary.UploadTask)) return "";
        /*
        token = token.substring(7);
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
        baseUrl += "/api/dispatcher/";

        String url = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
         */
        if(taskType.equals(ETutorVocabulary.XQueryTask.toString())){
            return proxyResource.deleteXQExercise(id).getBody();
        }else if(taskType.equals(ETutorVocabulary.SQLTask.toString())){
           return proxyResource.deleteSQLExercise(id).getBody();
        }

        return "";
    }

    public String getXMLForXQ(String taskGroup, String token, HttpServletRequest request){
        /*
        token = token.substring(7);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
        baseUrl += "/api/dispatcher/";

        String url = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        url = baseUrl + "xquery/xml/taskGroup/" + taskGroup;
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
         */
        return proxyResource.getXMLForXQByTaskGroup(taskGroup).getBody();
    }

    public String getXMLForXQ(int fileId, String token, HttpServletRequest request){
        /*
        Objects.requireNonNull(fileId);
        token = token.substring(7);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
        baseUrl += "/api/dispatcher/";

        String url = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        url = baseUrl + "xquery/xml/fileid/" + fileId;
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
         */
        return proxyResource.getXMLForXQByFileId(fileId).getBody();
    }

    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO, String token, HttpServletRequest request) throws JsonProcessingException {
        Objects.requireNonNull(newTaskAssignmentDTO);
        Objects.requireNonNull(newTaskAssignmentDTO.getTaskAssignmentTypeId());

        if(newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())){
            if(newTaskAssignmentDTO.getTaskIdForDispatcher() == null){
                int id = this.createXQueryTask(newTaskAssignmentDTO, token, request);
                if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id +"");
            }else{
                XQueryExerciseDTO e = this.getXQExerciseInfo(newTaskAssignmentDTO.getTaskIdForDispatcher(), token, request);
                newTaskAssignmentDTO.setxQuerySolution(e.getQuery());
                if(!e.getSortedNodes().isEmpty())newTaskAssignmentDTO.setxQueryXPathSorting(e.getSortedNodes().get(0));
            }
        }else if(newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) || newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString())){
            if(newTaskAssignmentDTO.getTaskIdForDispatcher() == null){
                int id = this.createSQLTask(newTaskAssignmentDTO, token, request);
                if(id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id+"");
            }else{
                String solution = fetchSQLSolution(newTaskAssignmentDTO.getTaskIdForDispatcher(), token, request);
                newTaskAssignmentDTO.setSqlSolution(solution);
            }
        }
    }

    private String fetchSQLSolution(String taskIdForDispatcher, String token, HttpServletRequest request) {
        /*
        Objects.requireNonNull(taskIdForDispatcher);
        token = token.substring(7);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
        baseUrl += "/api/dispatcher/";

        String url = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        url = baseUrl + "sql/exercise/"+taskIdForDispatcher+"/solution";
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
         */
        return proxyResource.getSQLSolution(Integer.parseInt(taskIdForDispatcher)).getBody();

    }

    private int createSQLTask(NewTaskAssignmentDTO newTaskAssignmentDTO, String token, HttpServletRequest request) {
        Objects.requireNonNull(newTaskAssignmentDTO.getSqlSolution());
        Objects.requireNonNull(newTaskAssignmentDTO.getTaskGroupId());

        String solution = newTaskAssignmentDTO.getSqlSolution();
        String taskGroup = newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1);
        /*
        token = token.substring(7);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
        baseUrl += "/api/dispatcher/";

        String url = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(solution, headers);

        url = baseUrl + "sql/exercise/"+taskGroup;
        var id = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class).getBody();
        return Integer.parseInt(id);
         */
        return Integer.parseInt(proxyResource.createSQLExercise(solution, taskGroup).getBody());
    }

    public void updateTask(TaskAssignmentDTO taskAssignmentDTO, String token, HttpServletRequest request) {
        Objects.requireNonNull(taskAssignmentDTO);
        Objects.requireNonNull(taskAssignmentDTO.getTaskAssignmentTypeId());

        if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())){
            if(StringUtils.isNotBlank(taskAssignmentDTO.getxQuerySolution())
                && StringUtils.isNotBlank(taskAssignmentDTO.getTaskIdForDispatcher())){
                updateXQExercise(taskAssignmentDTO, token, request);
            }
        }else if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) || taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString())){
            if(StringUtils.isNotBlank(taskAssignmentDTO.getSqlSolution()) && StringUtils.isNotBlank(taskAssignmentDTO.getTaskIdForDispatcher())){
                updateSQLExercise(taskAssignmentDTO, token, request);
            }
        }
    }

    private void updateSQLExercise(TaskAssignmentDTO taskAssignmentDTO, String token, HttpServletRequest request) {
        String solution = taskAssignmentDTO.getSqlSolution();
        int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
        /*
        token = token.substring(7);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
        baseUrl += "/api/dispatcher/";

        String url = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(solution, headers);

        url = baseUrl + "sql/exercise/"+id+"/solution";
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
         */
        proxyResource.updateSQLExerciseSolution(id, solution);
    }

    public void createTaskGroup(TaskGroupDTO newTaskGroupDTO, String token, HttpServletRequest request) {
        if(newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())){
           proxyXMLtoDispatcher(newTaskGroupDTO, request, token);
        }else if(newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())){
            createSQLTaskGroup(newTaskGroupDTO, request, token);
        }
    }

    private void createSQLTaskGroup(TaskGroupDTO newTaskGroupDTO, HttpServletRequest request, String token) {
        Objects.requireNonNull(newTaskGroupDTO.getSqlCreateStatements());
        Objects.requireNonNull(newTaskGroupDTO.getSqlInsertStatementsDiagnose());
        Objects.requireNonNull(newTaskGroupDTO.getSqlInsertStatementsSubmission());

        SqlDataDefinitionDTO body = new SqlDataDefinitionDTO();
        body.setCreateStatements(Arrays.stream(newTaskGroupDTO.getSqlCreateStatements().trim().split(";")).toList());
        body.setInsertStatementsDiagnose(Arrays.stream(newTaskGroupDTO.getSqlInsertStatementsDiagnose().trim().split(";")).toList());
        body.setInsertStatementsSubmission(Arrays.stream(newTaskGroupDTO.getSqlInsertStatementsSubmission().trim().split(";")).toList());
        body.setSchemaName(newTaskGroupDTO.getName());

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";

        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        proxyResource.executeDDLForSQL(jsonBody);
        /*
        token = token.substring(7);

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
            .replacePath(null)
            .build()
            .toUriString();
        baseUrl += "/api/dispatcher/";

        String url = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        url = baseUrl + "sql/schema";
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
         */

    }
}
