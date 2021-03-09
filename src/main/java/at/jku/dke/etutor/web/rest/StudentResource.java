package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.StudentService;
import at.jku.dke.etutor.service.UserService;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceInformationDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceProgressOverviewDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * REST controller for managing students.
 *
 * @author fne
 */
@RestController
@RequestMapping("/api/student")
public class StudentResource {

    private final UserService userService;
    private final StudentService studentService;

    /**
     * Constructor.
     *
     * @param userService    the injected user service
     * @param studentService the injected student service
     */
    public StudentResource(UserService userService, StudentService studentService) {
        this.userService = userService;
        this.studentService = studentService;
    }

    /**
     * {@code GET} : Retrieves all available students.
     *
     * @return the {@link ResponseEntity} containing the available students
     */
    @GetMapping
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<StudentInfoDTO>> retrieveAllStudents() {
        List<StudentInfoDTO> studentList = userService.getAvailableStudents();
        return ResponseEntity.ok(studentList);
    }

    /**
     * {@code GET /api/student/courses} : Retrieves a student's courses.
     *
     * @return the {@link ResponseEntity} containing the student's courses
     */
    @GetMapping("courses")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Collection<CourseInstanceInformationDTO>> getAStudentsCourse() {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");
        Collection<CourseInstanceInformationDTO> courses = studentService.getCoursesFromStudent(matriculationNumber);
        return ResponseEntity.ok(courses);
    }

    /**
     * {@code GET /api/student/courses/:uuid/progress} : Retrieves the progress on course assignments.
     *
     * @param uuid the uuid of the course
     * @return the {@link ResponseEntity} containing the progress
     */
    @GetMapping("courses/{uuid}/progress")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Collection<CourseInstanceProgressOverviewDTO>> getProgressInformation(@PathVariable(name = "uuid") String uuid) {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");
        Collection<CourseInstanceProgressOverviewDTO> items = studentService.getProgressOverview(matriculationNumber, uuid);

        return ResponseEntity.ok(items);
    }
}
