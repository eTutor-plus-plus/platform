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
import at.jku.dke.etutor.web.rest.errors.AllTasksAlreadyAssignedException;
import at.jku.dke.etutor.web.rest.errors.ExerciseSheetAlreadyOpenedException;
import at.jku.dke.etutor.web.rest.errors.NoFurtherTasksAvailableException;
import at.jku.dke.etutor.web.rest.errors.WrongTaskTypeException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
        } catch (at.jku.dke.etutor.service.exception.NoFurtherTasksAvailableException nfta) {
            throw new NoFurtherTasksAvailableException();
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

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/can-assign-new-task} : Returns
     * whether a new task can be assigned or not.
     *
     * @param courseInstanceUUID the course instance uuid from the request path
     * @param exerciseSheetUUID  the exercise sheet uuid from the request path
     * @return the {@link ResponseEntity} containing the result
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/can-assign-new-task")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Boolean> canAssignNextTask(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID) {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");

        boolean value = studentService.canAssignNextTask(courseInstanceUUID, exerciseSheetUUID, matriculationNumber);
        return ResponseEntity.ok(value);
    }

    /**
     * {@code POST /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/assign-new-task} : Assigns a new
     * task.
     *
     * @param courseInstanceUUID the course instance uuid
     * @param exerciseSheetUUID  the exercise sheet uuid
     * @return the {@link ResponseEntity} containing {@code true} if a new task could be assigned,
     * otherwise {@code false} is returned
     */
    @PostMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/assign-new-task")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Boolean> assignNewTask(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID) {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");

        try {
            studentService.assignNextTaskForStudent(courseInstanceUUID, exerciseSheetUUID, matriculationNumber);
            return ResponseEntity.ok(true);
        } catch (at.jku.dke.etutor.service.exception.AllTasksAlreadyAssignedException ataae) {
            throw new AllTasksAlreadyAssignedException();
        } catch (at.jku.dke.etutor.service.exception.NoFurtherTasksAvailableException nfta) {
            studentService.closeExerciseSheetFromAnIndividualStudent(matriculationNumber, courseInstanceUUID, exerciseSheetUUID);
            return ResponseEntity.ok(false);
        }
    }

    /**
     * {@code DELETE /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/uploadTask/:taskNo/:fileId} : Removes
     * the file attachment of an individual task assignment.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @param fileId             the file id
     * @return empty {@link ResponseEntity}
     */
    @DeleteMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/uploadTask/{taskNo}/{fileId}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Void> removeUploadTask(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                 @PathVariable int taskNo, @PathVariable int fileId) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        studentService.removeFileFromUploadTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo, fileId);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code PUT /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/uploadTask/:taskNo/:fileId} : Sets
     * the file attachment id for an individual task assignment.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @param fileId             the file id
     * @return empty {@link ResponseEntity}
     */
    @PutMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/uploadTask/{taskNo}/{fileId}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Void> setUploadTask(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                              @PathVariable int taskNo, @PathVariable int fileId) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        try {
            studentService.setFileForUploadTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo, fileId);
            return ResponseEntity.noContent().build();
        } catch (at.jku.dke.etutor.service.exception.NoUploadFileTypeException nufte) {
            throw new WrongTaskTypeException();
        }
    }

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/uploadTask/:taskNo/file-attachment} : Returns
     * the file attachment's id.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @return the {@link ResponseEntity} containing the file id
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/uploadTask/{taskNo}/file-attachment")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Integer> getFileAttachmentId(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                       @PathVariable int taskNo) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        Optional<Integer> optionalId = studentService.getFileIdOfAssignment(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        int id = optionalId.orElse(-1);

        return ResponseEntity.ok(id);
    }

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/:taskNo/submission}
     * Sets the submission for an individual task
     *
     * @param courseInstanceUUID the course instance id
     * @param exerciseSheetUUID the exercise sheet id
     * @param taskNo the task no
     * @param submission the submission
     * @return
     */

    @PutMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/{taskNo}/submission")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Void> setSubmission(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                              @PathVariable int taskNo, @RequestBody String submission) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        studentService.setLastSubmissionForAssignment(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo, submission);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/uploadTask/:taskNo/file-attachment} : Returns
     * the file attachment's id.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @return the {@link ResponseEntity} containing the file id
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/{taskNo}/submission")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<String> getSubmission(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                       @PathVariable int taskNo) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        Optional<String> optionalSubmission = studentService.getSubmissionForAssignment(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        String id = optionalSubmission.orElse("");

        return ResponseEntity.ok(id);
    }

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/uploadTask/:taskNo/file-attachment/of-student/:matriculationNo} : Returns
     * the file attachment's id.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @param matriculationNo    the student's matriculation number
     * @return the {@link ResponseEntity} containing the file id
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/uploadTask/{taskNo}/file-attachment/of-student/{matriculationNo}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Integer> getFileAttachmentIdOfStudent(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                                @PathVariable int taskNo, @PathVariable String matriculationNo) {

        Optional<Integer> optionalId = studentService.getFileIdOfAssignment(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        int id = optionalId.orElse(-1);

        return ResponseEntity.ok(id);
    }
}
