package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.DispatcherProxyService;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDisplayDTO;
import at.jku.dke.etutor.service.exception.MissingParameterException;
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
    public ResponseEntity<TaskGroupDTO> createNewTaskGroup(@Valid @RequestBody NewTaskGroupDTO newTaskGroupDTO)  {
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
            throw new DispatcherRequestFailedException(drfe);
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
        try{
            // Update task group in dispatcher if applicable
            if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString()) ||
            taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString()) ||
            taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.DatalogTypeTaskGroup.toString())||
            taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.FDTypeTaskGroup.toString())){
                dispatcherProxyService.createTaskGroup(taskGroupDTO, false);
            }

            // Update task group in RDF
            TaskGroupDTO taskGroupDTOFromService = assignmentSPARQLEndpointService.modifyTaskGroup(taskGroupDTO);
            if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())) {
                taskGroupDTOFromService = assignmentSPARQLEndpointService.modifySQLTaskGroup(taskGroupDTOFromService);
            }else if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())) {
                taskGroupDTOFromService = assignmentSPARQLEndpointService.modifyXQueryTaskGroup(taskGroupDTOFromService);
            }else if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.DatalogTypeTaskGroup.toString())){
                taskGroupDTOFromService = assignmentSPARQLEndpointService.modifyDLGTaskGroup(taskGroupDTOFromService);
            } else if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.FDTypeTaskGroup.toString())){
                taskGroupDTOFromService = assignmentSPARQLEndpointService.modifyFDTaskGroup(taskGroupDTOFromService);
            }

            return ResponseEntity.ok(taskGroupDTOFromService);
        } catch(at.jku.dke.etutor.service.exception.DispatcherRequestFailedException drfe){
            throw new DispatcherRequestFailedException(drfe);
        } catch (MissingParameterException e) {
            throw new at.jku.dke.etutor.web.rest.errors.MissingParameterException();
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
    @GetMapping("/fd-create/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> getFDGroupById(@PathVariable String id) {
        String fdExercise = dispatcherProxyService.getFDGroupById(id);

        return ResponseEntity.ok(fdExercise);
    }

    @GetMapping("/fd-solve/{id}")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<String> getFDGroupByIdStudents(@PathVariable String id) {
        String fdExercise = dispatcherProxyService.getFDGroupByIdStudent(id);


        return ResponseEntity.ok(fdExercise);
    }


    @GetMapping("/fd/next_id")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public Long nextFdId() {
        Long id = dispatcherProxyService.nextFdId();
        return id;
    }



}
