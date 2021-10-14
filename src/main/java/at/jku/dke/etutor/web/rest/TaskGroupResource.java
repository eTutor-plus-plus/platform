package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.dispatcher.DispatcherSubmissionDTO;
import at.jku.dke.etutor.service.dto.dispatcher.DispatcherXMLDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDisplayDTO;
import at.jku.dke.etutor.web.rest.errors.TaskGroupAlreadyExistentException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.auth.In;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing task groups.
 *
 * @author fne
 */
@RestController
@RequestMapping("/api/task-group")
public class TaskGroupResource {

    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;

    /**
     * Constructor.
     *
     * @param assignmentSPARQLEndpointService the injected assignment SPARQL endpoint service
     */
    public TaskGroupResource(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService) {
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
    }

    /**
     * REST endpoint which is used to create a new task group.
     *
     * @param newTaskGroupDTO the validated new task group from the request body
     * @return {@link ResponseEntity} containing the created task group
     */
    @PostMapping()
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<TaskGroupDTO> createNewTaskGroup(@Valid @RequestBody NewTaskGroupDTO newTaskGroupDTO, HttpServletRequest request, @RequestHeader(name="Authorization") String token) {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("");
        try {
            TaskGroupDTO taskGroupDTO = assignmentSPARQLEndpointService.createNewTaskGroup(newTaskGroupDTO, currentLogin);
            if(newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString()));{
                proxyXMLtoDispatcher(taskGroupDTO, request, token);
            }
            return ResponseEntity.ok(taskGroupDTO);
        } catch (at.jku.dke.etutor.service.exception.TaskGroupAlreadyExistentException tgaee) {
            throw new TaskGroupAlreadyExistentException();
        }
    }


    /**
     * REST endpoint for deleting task groups.
     *
     * @param name the task group's name from the request path
     * @return empty {@link ResponseEntity}
     */
    @DeleteMapping("{name}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> deleteTaskGroup(@PathVariable String name) {
        assignmentSPARQLEndpointService.deleteTaskGroup(name);
        return ResponseEntity.noContent().build();
    }

    /**
     * REST endpoint for retrieving a single task group.
     *
     * @param name the task group's name from the request path
     * @return the {@link ResponseEntity} containing the single task group
     */
    @GetMapping("{name}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<TaskGroupDTO> getTaskGroup(@PathVariable String name) {
        Optional<TaskGroupDTO> taskGroup = assignmentSPARQLEndpointService.getTaskGroupByName(name);
        return ResponseEntity.of(taskGroup);
    }

    /**
     * REST endpoint for manipulation task groups.
     *
     * @param taskGroupDTO the task group from teh request body
     * @return the {@link ResponseEntity} containing the modified task group
     */
    @PutMapping
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<TaskGroupDTO> modifyTaskGroup(@Valid @RequestBody TaskGroupDTO taskGroupDTO, HttpServletRequest request, @RequestHeader(name = "Authorization") String token) {
        TaskGroupDTO taskGroupDTOFromService = assignmentSPARQLEndpointService.modifyTaskGroup(taskGroupDTO);
        if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())) {
            taskGroupDTOFromService = assignmentSPARQLEndpointService.modifySQLTaskGroup(taskGroupDTOFromService);
        }else if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())){
            taskGroupDTOFromService = assignmentSPARQLEndpointService.modifyXQueryTaskGroup(taskGroupDTOFromService);
            proxyXMLtoDispatcher(taskGroupDTO, request, token);
        }
        return ResponseEntity.ok(taskGroupDTOFromService);
    }

    /**
     * REST endpoint for retrieving a paged task group list.
     *
     * @param filter   the optional filter
     * @param pageable the pagination object
     * @return the {@link ResponseEntity} containing the list of paged task groups
     */
    @GetMapping("displayable/list")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<TaskGroupDisplayDTO>> getPagedTaskGroups(@RequestParam(required = false, defaultValue = "") String filter, Pageable pageable) {
        Page<TaskGroupDisplayDTO> page = assignmentSPARQLEndpointService.getFilteredTaskGroupPaged(filter, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Utility-method: Sends the request to add the xml-files for an xquery task group to the DispatcherProxyResource
     * @param taskGroupDTO the task group
     * @param request the http request to retrieve the base url
     * @param token the authorization token
     */
    private void proxyXMLtoDispatcher(TaskGroupDTO taskGroupDTO, HttpServletRequest request, String token) {
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
        var fileId = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
        int i = 1;
    }

}
