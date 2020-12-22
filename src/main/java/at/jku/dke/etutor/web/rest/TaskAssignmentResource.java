package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.InternalModelException;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.InternalTaskAssignmentNonexistentException;
import at.jku.dke.etutor.web.rest.errors.BadRequestAlertException;
import at.jku.dke.etutor.web.rest.errors.TaskAssignmentNonexistentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;
import java.util.SortedSet;

/**
 * REST controller for managing task assignments
 *
 * @author fne
 */
@RestController
@RequestMapping("/api")
public class TaskAssignmentResource {

    private final Logger log = LoggerFactory.getLogger(TaskAssignmentResource.class);

    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;

    /**
     * Constructor.
     *
     * @param assignmentSPARQLEndpointService the injected assignment sparql endoinpoint service
     */
    public TaskAssignmentResource(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService) {
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
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
        TaskAssignmentDTO assignment = assignmentSPARQLEndpointService.insertNewTaskAssignment(newTaskAssignmentDTO);
        return ResponseEntity.ok(assignment);
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
    public ResponseEntity<SortedSet<TaskAssignmentDTO>> getTaskAssignmentsOfGoal(@PathVariable String owner, @PathVariable String goalName) {
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
        try {
            assignmentSPARQLEndpointService.updateTaskAssignment(taskAssignmentDTO);
            return ResponseEntity.noContent().build();
        } catch (InternalTaskAssignmentNonexistentException e) {
            throw new TaskAssignmentNonexistentException();
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
    public ResponseEntity<List<TaskAssignmentDTO>> getTaskAssignments(@RequestParam(required = false, defaultValue = "") String taskHeader) {
        try {
            List<TaskAssignmentDTO> taskAssignments = assignmentSPARQLEndpointService.getTaskAssignments(taskHeader);
            return ResponseEntity.ok(taskAssignments);
        } catch (MalformedURLException | ParseException e) {
            throw new BadRequestAlertException("An internal error occurred!", "learningGoalManagement", "parsingError");
        }
    }
}
