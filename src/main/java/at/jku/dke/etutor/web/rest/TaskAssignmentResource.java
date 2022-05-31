package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.InternalModelException;
import at.jku.dke.etutor.service.dto.TaskDisplayDTO;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDisplayDTO;
import at.jku.dke.etutor.service.exception.InternalTaskAssignmentNonexistentException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import at.jku.dke.etutor.web.rest.errors.BadRequestAlertException;
import at.jku.dke.etutor.web.rest.errors.DispatcherRequestFailedException;
import at.jku.dke.etutor.web.rest.errors.TaskAssignmentNonexistentException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

/**
 * REST controller for managing task assignments
 *
 * @author fne
 */
@RestController
@RequestMapping("/api")
public class TaskAssignmentResource {

    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final DispatcherProxyService dispatcherProxyService;

    /**
     * Constructor.
     *
     * @param assignmentSPARQLEndpointService the injected assignment sparql endoinpoint service
     * @param dispatcherProxyService  the injected dispatcher proxy service
     */
    public TaskAssignmentResource(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService,
                                  DispatcherProxyService dispatcherProxyService) {
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.dispatcherProxyService = dispatcherProxyService;
    }

    /**
     * REST endpoint which is used to create a new task assignment ({@code POST /api/tasks/assignments}).
     *
     * @param newTaskAssignmentDTO the validated new task assignment from the request body
     * @return {@link ResponseEntity} containing the newly created task assignment
     */
    @PostMapping("tasks/assignments")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<TaskAssignmentDTO> createNewTaskAssignment(@Valid @RequestBody NewTaskAssignmentDTO newTaskAssignmentDTO) {
        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        TaskAssignmentDTO assignment = null;
        try {
            newTaskAssignmentDTO = dispatcherProxyService.createTask(newTaskAssignmentDTO);
            assignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO, currentLogin);
            return ResponseEntity.ok(assignment);
        } catch (JsonProcessingException | at.jku.dke.etutor.service.exception.DispatcherRequestFailedException e) {
            throw new DispatcherRequestFailedException();
        } catch (MissingParameterException mpe) {
            throw new at.jku.dke.etutor.web.rest.errors.MissingParameterException();
        } catch(NotAValidTaskGroupException navtge){
            throw new at.jku.dke.etutor.web.rest.errors.NotAValidTaskGroupException();
        }
    }

    /**
     * REST endpoint for getting the assignments of a given goal ({@code GET /api/tasks/assignments/:owner/goal/:goalName}).
     *
     * @param owner    the owner's name (path variable)
     * @param goalName the goal's name (path variable)
     * @return {@link ResponseEntity} containing the task assignments of the given learning goal
     */
    @GetMapping("tasks/assignments/{owner}/goal/{goalName}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<SortedSet<TaskAssignmentDTO>> getTaskAssignmentsOfGoal(
        @PathVariable String owner,
        @PathVariable String goalName
    ) {
        try {
            SortedSet<TaskAssignmentDTO> assignments = assignmentSPARQLEndpointService.getTaskAssignmentsOfGoal(goalName, owner);

            return ResponseEntity.ok(assignments);
        } catch (InternalModelException e) {
            throw new BadRequestAlertException("An internal error occurred!", "learningGoalManagement", "parsingError");
        }
    }

    /**
     * REST endpoint for deleting a task assignment ({@code DELETE /api/tasks/assignments/:id}).
     *
     * @param id the internal id of the task (path variable)
     * @return empty {@link ResponseEntity}
     */
    @DeleteMapping("tasks/assignments/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> deleteTaskAssignment(@PathVariable String id) {
        var taskAssignmentDTOOptional = assignmentSPARQLEndpointService.getTaskAssignmentByInternalId(id);
        if(taskAssignmentDTOOptional.isPresent()){
            try {
                dispatcherProxyService.deleteTaskAssignment(taskAssignmentDTOOptional.get());
            } catch (at.jku.dke.etutor.service.exception.DispatcherRequestFailedException e) {
                e.printStackTrace();
            }
        }

        assignmentSPARQLEndpointService.removeTaskAssignment(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * REST endpoint for updating a task assignment ({@code PUT /api/tasks/assignments})
     *
     * @param taskAssignmentDTO the validated task assignment from the request body
     * @return empty {@link ResponseEntity}
     */
    @PutMapping("tasks/assignments")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> updateTaskAssignment(@Valid @RequestBody TaskAssignmentDTO taskAssignmentDTO) {
        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!StringUtils.equals(taskAssignmentDTO.getInternalCreator(), currentLogin)) {
            throw new BadRequestAlertException("Only the creator of the task is allowed to edit it!", "taskManagement", "taskNotOwner");
        }

        try {
            assignmentSPARQLEndpointService.updateTaskAssignment(taskAssignmentDTO);
            dispatcherProxyService.updateTask(taskAssignmentDTO);
            return ResponseEntity.noContent().build();

        } catch (InternalTaskAssignmentNonexistentException e) {
            throw new TaskAssignmentNonexistentException();
        } catch(MissingParameterException mpe){
            throw new at.jku.dke.etutor.web.rest.errors.MissingParameterException();
        } catch(at.jku.dke.etutor.service.exception.DispatcherRequestFailedException drfe){
            throw new DispatcherRequestFailedException();
        }
    }

    /**
     * REST endpoint for setting the task assignments' goal ids.
     *
     * @param assignmentId the internal task assignment id
     * @param goalIds      the goal ids from request body
     * @return empty {@link ResponseEntity}
     */
    @PutMapping("tasks/assignments/{assignmentId}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> setTaskAssignment(@PathVariable String assignmentId, @RequestBody List<String> goalIds) {
        try {
            assignmentSPARQLEndpointService.setTaskAssignment(assignmentId, goalIds);
            return ResponseEntity.noContent().build();
        } catch (InternalTaskAssignmentNonexistentException e) {
            throw new TaskAssignmentNonexistentException();
        }
    }

    /**
     * REST endpoint for retrieving task assignments which may be filtered
     * by an optional task header filter string.
     *
     * @param taskHeader the optional task header filter query parameter
     * @return {@link ResponseEntity} containing the list of task assignments
     */
    @GetMapping("tasks/assignments")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<TaskAssignmentDTO>> getTaskAssignments(
        @RequestParam(required = false, defaultValue = "") String taskHeader
    ) {
        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            List<TaskAssignmentDTO> taskAssignments = assignmentSPARQLEndpointService.getTaskAssignments(taskHeader, currentLogin);
            return ResponseEntity.ok(taskAssignments);
        } catch (MalformedURLException | ParseException e) {
            throw new BadRequestAlertException("An internal error occurred!", "learningGoalManagement", "parsingError");
        }
    }

    /**
     * REST endpoint for retrieving the task display list which may be filtered
     * by an optional task header filter string.
     *
     * @param taskHeader      the optional task header filter query parameter
     * @param taskGroupHeader the optional task group header filter query parameter
     * @param pageable        the pagination object
     * @return {@link ResponseEntity} containing the list of task displays of the current "page"
     */
    @GetMapping("tasks/display")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<TaskDisplayDTO>> getAllTaskDisplayList(
        @RequestParam(required = false, defaultValue = "") String taskHeader,
        @RequestParam(required = false, defaultValue = "") String taskGroupHeader,
        Pageable pageable
    ) {
        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        Slice<TaskDisplayDTO> slice = assignmentSPARQLEndpointService.findAllTasks(taskHeader, pageable, currentLogin, taskGroupHeader);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Has-Next-Page", String.valueOf(slice.hasNext()));
        return new ResponseEntity<>(slice.getContent(), headers, HttpStatus.OK);
    }

    /**
     * REST endpoint for retrieving a single task assignment object by its
     * internal id.
     *
     * @param assignmentId the internal id (path variable)
     * @return the {@link TaskAssignmentDTO} containing the given id
     */
    @GetMapping("tasks/assignments/{assignmentId}")
    public ResponseEntity<TaskAssignmentDTO> getTaskAssignmentById(@PathVariable String assignmentId) {
        Optional<TaskAssignmentDTO> optionalTaskAssignmentDTO = assignmentSPARQLEndpointService.getTaskAssignmentByInternalId(assignmentId);

        return ResponseEntity.of(optionalTaskAssignmentDTO);
    }

    /**
     * REST endpoint for retrieving the calc solution file id by its
     * internal id
     *
     * @param assignmentId the internal id (path variable)
     * @return the id of the calc solution file
     */
    @GetMapping("tasks/assignments/calc_solution/{assignmentId}")
    public ResponseEntity<Integer> getFileIdOfCalcSolution (@PathVariable String assignmentId) {
        Optional<Integer> calc_solution_file_id = assignmentSPARQLEndpointService.getFileIdOfCalcSolution(assignmentId);

        return ResponseEntity.of(calc_solution_file_id);
    }

    /**
     * REST endpoint for retrieving the assigned learning goal ids of a given
     * task assignment.
     *
     * @param assignmentId the task assignment's internal id
     * @return the {@link ResponseEntity} containing the list of associated learning goal ids
     */
    @GetMapping("tasks/assignments/{assignmentId}/learninggoals")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<String>> getAssignedLearningGoalsOfAssignment(@PathVariable String assignmentId) {
        List<String> learningGoalIds = assignmentSPARQLEndpointService.getAssignedLearningGoalIdsOfTaskAssignment(assignmentId);
        return ResponseEntity.ok(learningGoalIds);
    }

    /**
     * REST endpoint for retrieving the list of associated tasks of a given
     * learning goal.
     *
     * @param goalOwner the goal's owner
     * @param goalName  the goal's name
     * @return the {@link ResponseEntity} containing the list of associated display task assignments
     */
    @GetMapping("tasks/of/{goalOwner}/{goalName}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<TaskAssignmentDisplayDTO>> getTasksOfLearningGoal(
        @PathVariable String goalOwner,
        @PathVariable String goalName
    ) {
        List<TaskAssignmentDisplayDTO> taskHeaders = assignmentSPARQLEndpointService.getTasksOfLearningGoal(goalName, goalOwner);
        return ResponseEntity.ok(taskHeaders);
    }
}
