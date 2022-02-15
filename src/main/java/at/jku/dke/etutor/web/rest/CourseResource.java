package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.InternalModelException;
import at.jku.dke.etutor.service.SPARQLEndpointService;
import at.jku.dke.etutor.service.dto.*;
import at.jku.dke.etutor.web.rest.errors.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing courses.
 *
 * @author fne
 */
@RestController
@RequestMapping("/api")
public class CourseResource {

    private final Logger log = LoggerFactory.getLogger(CourseResource.class);

    private final SPARQLEndpointService sparqlEndpointService;

    /**
     * Constructor.
     *
     * @param sparqlEndpointService the injected sparql endpoint service
     */
    public CourseResource(SPARQLEndpointService sparqlEndpointService) {
        this.sparqlEndpointService = sparqlEndpointService;
    }

    /**
     * {@code POST /course} : Creates a new course.
     *
     * <p>
     * If the course name already exists, an exception will be thrown.
     * </p>
     *
     * @param courseDTO the course to create (from request body)
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with the new course in body
     * @throws URISyntaxException if the location URI syntax is incorrect
     */
    @PostMapping("/course")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) throws URISyntaxException {
        log.debug("REST request to save a new course: {}", courseDTO);

        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            var newCourse = sparqlEndpointService.insertNewCourse(courseDTO, currentLogin);

            return ResponseEntity.created(new URI(String.format("/api/course/%s", newCourse.getNameForRDF()))).body(newCourse);
        } catch (at.jku.dke.etutor.service.exception.CourseAlreadyExistsException e) {
            throw new CourseAlreadyExistsException();
        }
    }

    /**
     * {@code GET /course} : Gets all courses
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with all courses in body
     */
    @GetMapping("/course")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Set<CourseDTO>> getCourses() {
        return ResponseEntity.ok(sparqlEndpointService.getAllCourses());
    }

    /**
     * {@code GET /course/:name} : Returns the course with the given name
     *
     * @param name the rdf encoded name
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with the requested course
     * or status {@code 404 (Not Found)} if the requested course doesn't exist
     */
    @GetMapping("/course/{name}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<CourseDTO> getCourse(@PathVariable String name) {
        Optional<CourseDTO> course = sparqlEndpointService.getCourse(name);

        if (course.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course.get());
    }

    /**
     * {@code DELETE /course/:name} : Deletes the course with the given name
     *
     * <p>
     * If the course does not exist, an exception will be thrown.
     * </p>
     *
     * @param name the rdf encoded name
     * @return the {@link ResponseEntity} with no content
     */
    @DeleteMapping("/course/{name}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> deleteCourse(@PathVariable String name) {
        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            sparqlEndpointService.deleteCourse(name, currentLogin);
            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.exception.CourseNotFoundException e) {
            throw new CourseNotFoundException();
        }
    }

    /**
     * {@code PUT /course} : Updates the given course
     *
     * <p>
     * If the course does not exist, an exception will be thrown.
     * </p>
     *
     * @param body the course dto from the request body
     * @return a {@link ResponseEntity} with no content
     */
    @PutMapping("/course")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> updateCourse(@Valid @RequestBody CourseDTO body) {
        try {
            sparqlEndpointService.updateCourse(body);
            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.exception.CourseNotFoundException e) {
            throw new CourseNotFoundException();
        }
    }

    /**
     * {@code POST /course/goal} : Inserts a new learning goal assignment for a course
     *
     * <p>
     * If the course does not exist, an exception will be thrown.
     * </p>
     *
     * @param learningGoalAssignmentDTO the dto from the request body
     * @return a {@link ResponseEntity} with no content
     */
    @PostMapping("/course/goal")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> insertGoalAssignment(@Valid @RequestBody LearningGoalAssignmentDTO learningGoalAssignmentDTO) {
        try {
            sparqlEndpointService.addGoalAssignment(learningGoalAssignmentDTO);
            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.exception.LearningGoalAssignmentAlreadyExistsException e) {
            throw new LearningGoalAssignmentAlreadyExistsException();
        }
    }

    /**
     * {@code DELETE /course/goal} : Deletes a learning goal assignment from a course
     *
     * <p>
     * If the course does not exist, an exception will be thrown.
     * </p>
     *
     * @param learningGoalAssignmentDTO the learning goal dto from the request body
     * @return a {@link ResponseEntity} with no content
     */
    @DeleteMapping("/course/goal")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> removeGoalAssignment(@Valid @RequestBody LearningGoalAssignmentDTO learningGoalAssignmentDTO) {
        try {
            sparqlEndpointService.removeGoalAssignment(learningGoalAssignmentDTO);
            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.exception.LearningGoalAssignmentNonExistentException e) {
            throw new LearningGoalAssignmentNonExistentException();
        }
    }

    /**
     * {@code GET /course/:courseNmae/goals} : Gets all learning goals of a course
     *
     * @param courseName the course name
     * @return a {@link ResponseEntity} with the learning goals
     */
    @GetMapping("/course/{courseName}/goals")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Set<DisplayLearningGoalAssignmentDTO>> getLearningGoalsForCourse(@PathVariable String courseName) {
        try {
            var goals = sparqlEndpointService.getLearningGoalsForCourse(courseName);
            return ResponseEntity.ok(goals);
        } catch (at.jku.dke.etutor.service.exception.CourseNotFoundException e) {
            throw new CourseNotFoundException();
        } catch (InternalModelException ex) {
            throw new BadRequestAlertException("An internal error occurred!", "learningGoalManagement", "parsingError");
        }
    }

    /**
     * {@code PUT /course/goal} : Sets the course's learning goal assignments.
     *
     * @param learningGoalUpdateAssignment the learning goal update dto from the request body
     * @return a {@link ResponseEntity} with no content
     */
    @PutMapping("/course/goal")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> setLearningGoalsForCourse(
        @Valid @RequestBody LearningGoalUpdateAssignmentDTO learningGoalUpdateAssignment
    ) {
        sparqlEndpointService.setGoalAssignment(learningGoalUpdateAssignment);
        return ResponseEntity.noContent().build();
    }
}
