package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDisplayDTO;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.web.rest.errors.DispatcherRequestFailedException;
import at.jku.dke.etutor.web.rest.errors.TaskGroupAlreadyExistentException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.config.Task;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
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
    private final DispatcherProxyService dispatcherProxyService;

    /**
     * Constructor.
     *
     * @param assignmentSPARQLEndpointService the injected assignment SPARQL endpoint service
     * @param dispatcherProxyService the injected dispatcher proxy service
     */
    public TaskGroupResource(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService, DispatcherProxyService dispatcherProxyService) {
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.dispatcherProxyService = dispatcherProxyService;
    }

    /**
     * REST endpoint which is used to create a new task group.
     *
     * @param newTaskGroupDTO the validated new task group from the request body
     * @return {@link ResponseEntity} containing the created task group
     */
    @PostMapping()
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<TaskGroupDTO> createNewTaskGroup(@Valid @RequestBody NewTaskGroupDTO newTaskGroupDTO) {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("");
        TaskGroupDTO taskGroupDTO = new TaskGroupDTO();
        try {
            taskGroupDTO = assignmentSPARQLEndpointService.createNewTaskGroup(newTaskGroupDTO, currentLogin);
            dispatcherProxyService.createTaskGroup(taskGroupDTO, true);
            return ResponseEntity.ok(taskGroupDTO);
        } catch (at.jku.dke.etutor.service.exception.TaskGroupAlreadyExistentException tgaee) {
            throw new TaskGroupAlreadyExistentException();
        } catch(at.jku.dke.etutor.service.exception.DispatcherRequestFailedException drfe){
            assignmentSPARQLEndpointService.deleteTaskGroup(taskGroupDTO.getName());
            throw new DispatcherRequestFailedException();
        } catch (MissingParameterException e) {
            assignmentSPARQLEndpointService.deleteTaskGroup(taskGroupDTO.getName());
            throw new at.jku.dke.etutor.web.rest.errors.MissingParameterException();
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
        try {
            dispatcherProxyService.deleteDispatcherResourcesForTaskGroup(getTaskGroup(name).getBody());
        } catch (at.jku.dke.etutor.service.exception.DispatcherRequestFailedException e) {
            e.printStackTrace();
        }
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
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
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
    public ResponseEntity<TaskGroupDTO> modifyTaskGroup(@Valid @RequestBody TaskGroupDTO taskGroupDTO) {
        TaskGroupDTO taskGroupDTOFromService = assignmentSPARQLEndpointService.modifyTaskGroup(taskGroupDTO);

        try{
            if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())) {
                taskGroupDTOFromService = assignmentSPARQLEndpointService.modifySQLTaskGroup(taskGroupDTOFromService);
                dispatcherProxyService.createTaskGroup(taskGroupDTO, false);
            }else if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())) {
                taskGroupDTOFromService = assignmentSPARQLEndpointService.modifyXQueryTaskGroup(taskGroupDTOFromService);
                dispatcherProxyService.createTaskGroup(taskGroupDTO, false);
            }else if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.DatalogTypeTaskGroup.toString())){
                dispatcherProxyService.createTaskGroup(taskGroupDTO, false);
                taskGroupDTOFromService = assignmentSPARQLEndpointService.modifyDLGTaskGroup(taskGroupDTOFromService);
            }
        } catch(at.jku.dke.etutor.service.exception.DispatcherRequestFailedException drfe){
            throw new DispatcherRequestFailedException();
        } catch (MissingParameterException e) {
            throw new at.jku.dke.etutor.web.rest.errors.MissingParameterException();
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
}
