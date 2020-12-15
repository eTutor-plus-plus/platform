package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.AssignmentSPARQLEndpointService;
import at.jku.dke.etutor.service.InternalModelException;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
}
