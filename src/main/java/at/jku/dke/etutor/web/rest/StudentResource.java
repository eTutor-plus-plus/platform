package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.StudentService;
import at.jku.dke.etutor.service.UserService;
import at.jku.dke.etutor.service.dto.StudentSelfEvaluationLearningGoalDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceInformationDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceProgressOverviewDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import at.jku.dke.etutor.service.dto.student.StudentTaskListInfoDTO;
import at.jku.dke.etutor.web.rest.errors.ExerciseSheetAlreadyOpenedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    /**
     * {@code POST /api/student/courses/:uuid/self-evaluation}
     *
     * @param uuid                the uuid of the course
     * @param selfEvaluationGoals the self evaluation goals from the request body
     * @return empty {@link ResponseEntity}
     */
    @PostMapping("courses/{uuid}/self-evaluation")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Void> postSelfEvaluation(
        @PathVariable(name = "uuid") String uuid,
        @RequestBody ArrayList<StudentSelfEvaluationLearningGoalDTO> selfEvaluationGoals
    ) {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");
        studentService.saveSelfEvaluation(uuid, matriculationNumber, selfEvaluationGoals);

        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/list} : Retrieves the exercise
     * sheet tasks of an assignment.
     *
     * @param courseInstanceUUID the course instance uuid from the request path
     * @param exerciseSheetUUID  the exercise sheet uuid from the request path
     * @return the {@link ResponseEntity} containing the task list entries
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/list")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Collection<StudentTaskListInfoDTO>> getStudentTaskList(
        @PathVariable String courseInstanceUUID,
        @PathVariable String exerciseSheetUUID
    ) {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");

        Collection<StudentTaskListInfoDTO> list = studentService.getStudentTaskList(
            courseInstanceUUID,
            exerciseSheetUUID,
            matriculationNumber
        );
        return ResponseEntity.ok(list);
    }

    /**
     * {@code POST /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/open} : Opens the
     * given exercise sheet for the currently logged-in student.
     *
     * @param courseInstanceUUID the course instance uuid from the request path
     * @param exerciseSheetUUID  the exercise sheet uuid from the request path
     * @return empty {@link ResponseEntity}
     */
    @PostMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/open")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Void> openExerciseSheetForStudent(@PathVariable String courseInstanceUUID,
                                                            @PathVariable String exerciseSheetUUID) {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");
        try {
            studentService.openExerciseSheetForStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID);
            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.exception.ExerciseSheetAlreadyOpenedException esaoe) {
            throw new ExerciseSheetAlreadyOpenedException();
        }
    }

    /**
     * {@code POST /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/task/:taskNo/submit} : Submits
     * a task.
     *
     * @param courseInstanceUUID the course instance uuid from the request path
     * @param exerciseSheetUUID  the exercise sheet uuid from the request path
     * @param taskNo             task no from the request path
     * @return empty {@link ResponseEntity}
     */
    @PostMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/task/{taskNo}/submit")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Void> submitTask(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID, @PathVariable int taskNo) {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");

        studentService.markTaskAssignmentAsSubmitted(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, taskNo);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/task/:taskNo/submitted} : Returns
     * whether the given task has already been submitted, or not.
     *
     * @param courseInstanceUUID the course instance uuid from the request path
     * @param exerciseSheetUUID  the exercise sheet uuid from the request path
     * @param taskNo             the task no from the request path
     * @return the {@link ResponseEntity} containing the result
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/task/{taskNo}/submitted")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Boolean> isTaskSubmitted(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID, @PathVariable int taskNo) {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");

        boolean value = studentService.isTaskSubmitted(courseInstanceUUID, exerciseSheetUUID, matriculationNumber, taskNo);
        return ResponseEntity.ok(value);
    }
}
