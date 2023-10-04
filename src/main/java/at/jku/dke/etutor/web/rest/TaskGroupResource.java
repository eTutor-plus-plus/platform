package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.TaskTypeServiceAggregator;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDisplayDTO;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;
import at.jku.dke.etutor.web.rest.errors.DispatcherRequestFailedException;
import at.jku.dke.etutor.web.rest.errors.TaskGroupAlreadyExistentException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

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
    private final TaskTypeServiceAggregator taskTypeServiceAggregator;

    /**
     * Constructor.
     *
     * @param assignmentSPARQLEndpointService the injected assignment SPARQL endpoint service
     * @param taskTypeServiceAggregator the injected dispatcher proxy service
     */
    public TaskGroupResource(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService, TaskTypeServiceAggregator taskTypeServiceAggregator) {
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.taskTypeServiceAggregator = taskTypeServiceAggregator;
    }

    /**
     * REST endpoint which is used to create a new task group.
     * First, the task-group-type specific service is called to create the task group {@link at.jku.dke.etutor.service.tasktypes.TaskGroupTypeService}
     * If this call succeeds, the task group is created in the RDF graph.
     * @param newTaskGroupDTO the validated new task group from the request body
     * @return {@link ResponseEntity} containing the created task group
     */
    @PostMapping()
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<TaskGroupDTO> createNewTaskGroup(@Valid @RequestBody NewTaskGroupDTO newTaskGroupDTO) {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("");
        TaskGroupDTO taskGroupDTO = new TaskGroupDTO();
        try {
            taskTypeServiceAggregator.createTaskGroup(newTaskGroupDTO);
            taskGroupDTO = assignmentSPARQLEndpointService.createNewTaskGroup(newTaskGroupDTO, currentLogin);
            return ResponseEntity.ok(taskGroupDTO);
        } catch (at.jku.dke.etutor.service.exception.TaskGroupAlreadyExistentException tgaee) {
            throw new TaskGroupAlreadyExistentException();
        } catch(at.jku.dke.etutor.service.exception.DispatcherRequestFailedException drfe){
            throw new DispatcherRequestFailedException(drfe);
        } catch (MissingParameterException e) {
            throw new at.jku.dke.etutor.web.rest.errors.MissingParameterException();
        } catch (TaskTypeSpecificOperationFailedException e) {
            // Should not happen - all specific exceptions should be handled above
            throw new RuntimeException(e);
        }
    }


    /**
     * REST endpoint for deleting task groups.
     * First the task-group-type specific service is called to delete the task group {@link at.jku.dke.etutor.service.tasktypes.TaskGroupTypeService}
     * Success of this call is ignored, and the task group is deleted from the RDF graph.
     * @param name the task group's name from the request path
     * @return empty {@link ResponseEntity}
     */
    @DeleteMapping("{name}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> deleteTaskGroup(@PathVariable String name) {
        try {
            taskTypeServiceAggregator.deleteTaskGroup(getTaskGroup(name).getBody());
        } catch (TaskTypeSpecificOperationFailedException e) {
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
     * First the task-group-type specific service is called to manipulate the task group {@link at.jku.dke.etutor.service.tasktypes.TaskGroupTypeService}
     * If this call succeeds, the task group is updated in the RDF graph.
     * @param taskGroupDTO the task group from teh request body
     * @return the {@link ResponseEntity} containing the modified task group
     */
    @PutMapping
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<TaskGroupDTO> modifyTaskGroup(@Valid @RequestBody TaskGroupDTO taskGroupDTO) {
        try{
            taskTypeServiceAggregator.updateTaskGroup(taskGroupDTO);
            TaskGroupDTO taskGroupDTOFromService = assignmentSPARQLEndpointService.modifyTaskGroup(taskGroupDTO);

            return ResponseEntity.ok(taskGroupDTOFromService);
        } catch(at.jku.dke.etutor.service.exception.DispatcherRequestFailedException drfe){
            throw new DispatcherRequestFailedException(drfe);
        } catch (MissingParameterException e) {
            throw new at.jku.dke.etutor.web.rest.errors.MissingParameterException();
        } catch (TaskTypeSpecificOperationFailedException e) {
            // Should not happen - all specific exceptions should be handled above
            throw new RuntimeException(e);
        }

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
