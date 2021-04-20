package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.CourseInstanceSPARQLEndpointService;
import at.jku.dke.etutor.service.StudentService;
import at.jku.dke.etutor.service.dto.courseinstance.*;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDisplayDTO;
import at.jku.dke.etutor.web.rest.errors.CourseInstanceNotFoundException;
import at.jku.dke.etutor.web.rest.errors.CourseNotFoundException;
import at.jku.dke.etutor.web.rest.errors.StudentCSVImportException;
import at.jku.dke.etutor.web.rest.vm.CourseInstanceStudentsVM;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import net.minidev.json.JSONArray;
import one.util.streamex.StreamEx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

/**
 * REST controller for managing course instances.
 */
@RestController
@RequestMapping("/api/course-instance")
public class CourseInstanceResource {

    private final CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService;
    private final StudentService studentService;

    /**
     * Constructor.
     *
     * @param courseInstanceSPARQLEndpointService the injected course instance sparql endpoint service
     * @param studentService                      the injected student service
     */
    public CourseInstanceResource(CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService, StudentService studentService) {
        this.courseInstanceSPARQLEndpointService = courseInstanceSPARQLEndpointService;
        this.studentService = studentService;
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

            return ResponseEntity.created(new URI(String.format("/api/course-instance/%s", uuid))).body(String.format("\"%s\"", id));
        } catch (at.jku.dke.etutor.service.exception.CourseNotFoundException courseNotFoundException) {
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
            courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(body.getMatriculationNumbers(), body.getCourseInstanceId());
            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.exception.CourseInstanceNotFoundException courseInstanceNotFoundException) {
            throw new CourseInstanceNotFoundException();
        }
    }

    /**
     * {@code GET /api/course-instance/students/of/:uuid} : Retrieves the students of a course instance.
     *
     * @param uuid the uuid of the course instance from teh request path
     * @return the {@link ResponseEntity} containing the assigned students
     */
    @GetMapping("students/of/{uuid}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Collection<StudentInfoDTO>> getStudentsOfCourseInstance(@PathVariable(name = "uuid") String uuid) {
        Collection<StudentInfoDTO> students = courseInstanceSPARQLEndpointService.getStudentsOfCourseInstance(uuid);
        return ResponseEntity.ok(students);
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
        } catch (at.jku.dke.etutor.service.exception.CourseNotFoundException courseNotFoundException) {
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

    /**
     * {@code GET /api/course-instance/overview-instances/of/:name} : Retrieves the paged overview instances.
     *
     * @param name the course name from the request path
     * @param page the paging information from the request path
     * @return {@link ResponseEntity} containing the page content
     */
    @GetMapping("overview-instances/of/{name}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<DisplayableCourseInstanceDTO>> getPagedCourseInstanceOverview(
        @PathVariable(name = "name") String name,
        Pageable page
    ) {
        Page<DisplayableCourseInstanceDTO> overviewPage = courseInstanceSPARQLEndpointService.getDisplayableCourseInstancesOfCourse(
            name,
            page
        );
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), overviewPage);
        return new ResponseEntity<>(overviewPage.getContent(), headers, HttpStatus.OK);
    }

    /**
     * {@code PUT /api/course-instance/students/of/:uuid/csvupload} : Sets the course instance's
     * students from the given csv file.
     *
     * @param uuid the uuid of the course instance (from the request path)
     * @param file the csv file (from the request body)
     * @return empty {@link ResponseEntity}
     */
    @PutMapping("students/of/{uuid}/csvupload")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> uploadStudents(@PathVariable(name = "uuid") String uuid, @RequestParam("file") MultipartFile file) {
        try {
            List<StudentImportDTO> students = studentService.importStudentsFromFile(file);
            String courseInstanceURI = ETutorVocabulary.createCourseInstanceURLString(uuid);
            courseInstanceSPARQLEndpointService.setStudentsOfCourseInstance(
                StreamEx.of(students).map(StudentInfoDTO::getMatriculationNumber).toList(),
                courseInstanceURI
            );

            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.exception.StudentCSVImportException studentCSVImportException) {
            throw new StudentCSVImportException();
        } catch (at.jku.dke.etutor.service.exception.CourseInstanceNotFoundException courseInstanceNotFoundException) {
            throw new CourseInstanceNotFoundException();
        }
    }

    /**
     * {@code POST /api/course-instance/:uuid/exercise-sheets} : Assigns exercise sheets to the given
     * course instance.
     *
     * @param uuid the uuid of the course instance (from the request path)
     * @param body the json array containing the exercise sheet urls (from the request body)
     * @return empty {@link ResponseEntity}
     */
    @PostMapping("{uuid}/exercise-sheets")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> allocateNewExerciseSheets(@PathVariable String uuid, @RequestBody JSONArray body) {
        List<String> newExerciseSheetIds = StreamEx.of(body.stream()).map(String.class::cast).toList();

        try {
            courseInstanceSPARQLEndpointService.addExerciseSheetCourseInstanceAssignments(uuid, newExerciseSheetIds);
            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.exception.CourseInstanceNotFoundException courseInstanceNotFoundException) {
            throw new CourseInstanceNotFoundException();
        }
    }

    /**
     * {@code GET /api/course-instance/:uuid/exercise-sheets} : Retrieves the exercise sheets of a given course
     * instance.
     *
     * @param uuid the uuid of the course instance (from the request path)
     * @return the {@link ResponseEntity} containing the elements
     */
    @GetMapping("{uuid}/exercise-sheets")
    public ResponseEntity<Collection<ExerciseSheetDisplayDTO>> getExerciseSheetsOfInstance(@PathVariable String uuid) {
        try {
            Collection<ExerciseSheetDisplayDTO> sheets = courseInstanceSPARQLEndpointService.getExerciseSheetsOfCourseInstance(uuid);
            return ResponseEntity.ok(sheets);
        } catch (at.jku.dke.etutor.service.exception.CourseInstanceNotFoundException courseInstanceNotFoundException) {
            throw new CourseInstanceNotFoundException();
        }
    }

    /**
     * {@code DELETE /api/course-instance/:uuid} : Removes the given course instance
     *
     * @param uuid the course instance's internal uuid
     * @return empty {@link ResponseEntity}
     */
    @DeleteMapping("{uuid}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> removeCourseInstance(@PathVariable String uuid) {
        try {
            courseInstanceSPARQLEndpointService.removeCourseInstance(uuid);
            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.exception.CourseInstanceNotFoundException courseInstanceNotFoundException) {
            throw new CourseInstanceNotFoundException();
        }
    }
}
