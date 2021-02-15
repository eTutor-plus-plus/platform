package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.CourseInstanceSPARQLEndpointService;
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
    public ResponseEntity<Void> setStudents(@Valid @RequestBody CourseInstanceStudentsVM body) {
        try {
            courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(body.getMatriculationNumbers(), body.getCourseId());
            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.CourseInstanceNotFoundException courseInstanceNotFoundException) {
            throw new CourseInstanceNotFoundException();
        }
    }
}
