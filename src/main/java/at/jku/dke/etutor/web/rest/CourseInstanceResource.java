package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.CourseInstanceSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceDTO;
import at.jku.dke.etutor.service.dto.courseinstance.NewCourseInstanceDTO;
import at.jku.dke.etutor.web.rest.errors.CourseInstanceNotFoundException;
import at.jku.dke.etutor.web.rest.errors.CourseNotFoundException;
import at.jku.dke.etutor.web.rest.vm.CourseInstanceStudentsVM;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Optional;

/**
 * REST controller for managing course instances.
 */
@RestController
@RequestMapping("/api/course-instance")
public class CourseInstanceResource {

    private final CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService;

    /**
     * Constructor.
     *
     * @param courseInstanceSPARQLEndpointService the injected course instance sparql endpoint service
     */
    public CourseInstanceResource(CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService) {
        this.courseInstanceSPARQLEndpointService = courseInstanceSPARQLEndpointService;
    }

    /**
     * {@code POST /api/course-instance} : Creates a new course instance.
     *
     * @param newCourseInstanceDTO the new course instance dto from the request body
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with the id in the body
     * @throws URISyntaxException if the location URI is incorrect
     */
    @PostMapping
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> createCourseInstance(@Valid @RequestBody NewCourseInstanceDTO newCourseInstanceDTO)
        throws URISyntaxException {
        try {
            String id = courseInstanceSPARQLEndpointService.createNewCourseInstance(newCourseInstanceDTO);
            String uuid = id.substring(id.lastIndexOf('#') + 1);

            return ResponseEntity.created(new URI(String.format("/api/course-instance/%s", uuid))).body(id);
        } catch (at.jku.dke.etutor.service.CourseNotFoundException courseNotFoundException) {
            throw new CourseNotFoundException();
        }
    }

    /**
     * {@code PUT /api/course-instance/students} : Sets the students for a specific course instance.
     *
     * @param body the vm from the request body
     * @return empty {@code ResponseEntity}
     */
    @PutMapping("students")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> setStudents(@Valid @RequestBody CourseInstanceStudentsVM body) {
        try {
            courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(body.getMatriculationNumbers(), body.getCourseId());
            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.CourseInstanceNotFoundException courseInstanceNotFoundException) {
            throw new CourseInstanceNotFoundException();
        }
    }

    /**
     * {@code GET /api/course-instance/instances/of/:name} : Retrieves the instances of a given course.
     *
     * @param name the course name from the request path
     * @return {@link ResponseEntity} containing the collection of course instances
     */
    @GetMapping("instances/of/{name}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Collection<CourseInstanceDTO>> getCourseInstancesOfCourse(@PathVariable(name = "name") String name) {
        try {
            Collection<CourseInstanceDTO> instances = courseInstanceSPARQLEndpointService.getInstancesOfCourse(name);
            return ResponseEntity.ok(instances);
        } catch (at.jku.dke.etutor.service.CourseNotFoundException courseNotFoundException) {
            throw new CourseNotFoundException();
        }
    }

    /**
     * {@code GET /api/course-instance/:uuid} : Retrieves a specific course instance.
     *
     * @param uuid the course instance's uuid
     * @return {@link ResponseEntity} containing the course instance object
     */
    @GetMapping("{uuid}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<CourseInstanceDTO> getCourseInstance(@PathVariable(name = "uuid") String uuid) {
        Optional<CourseInstanceDTO> optionalCourseInstance = courseInstanceSPARQLEndpointService.getCourseInstance(uuid);
        return ResponseEntity.of(optionalCourseInstance);
    }
}
