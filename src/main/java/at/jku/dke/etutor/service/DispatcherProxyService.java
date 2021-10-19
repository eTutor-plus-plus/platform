package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.dto.dispatcher.DispatcherXMLDTO;
import at.jku.dke.etutor.service.dto.dispatcher.XQueryExerciseDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DispatcherProxyService {
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;

    public DispatcherProxyService(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService){
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
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
        assignmentSPARQLEndpointService.addXMLFileURL(taskGroupDTO, fileURL);
    }

    /**
     * Utility method that triggers deletion of task-group related resources by the dispatcher
     * @param taskGroupDTO  the task group
     * @param request  the request
     * @param token the auth token
     */
    public void deleteDispatcherResourcesForTaskGroup(TaskGroupDTO taskGroupDTO, HttpServletRequest request, String token) {
        if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())){
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
    }

    /**
     * Returns exercise information for a given id
     * @param taskIdForDispatcher the task id
     * @param token the auth token
     * @param request the HttpServletRequest
     * @return an XQueryExerciseDTO
     */
    public XQueryExerciseDTO getExerciseInfo(String taskIdForDispatcher, String token, HttpServletRequest request) {
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
    }
}
