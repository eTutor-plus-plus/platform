package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.StudentService;
import at.jku.dke.etutor.service.UserService;
import at.jku.dke.etutor.service.dto.StudentSelfEvaluationLearningGoalDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceInformationDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceProgressOverviewDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import at.jku.dke.etutor.service.dto.dispatcher.DispatcherGradingDTO;
import at.jku.dke.etutor.service.dto.dispatcher.DispatcherSubmissionDTO;
import at.jku.dke.etutor.service.dto.student.IndividualTaskSubmissionDTO;
import at.jku.dke.etutor.service.dto.student.StudentTaskListInfoDTO;
import at.jku.dke.etutor.web.rest.errors.AllTasksAlreadyAssignedException;
import at.jku.dke.etutor.web.rest.errors.ExerciseSheetAlreadyOpenedException;
import at.jku.dke.etutor.web.rest.errors.NoFurtherTasksAvailableException;
import at.jku.dke.etutor.web.rest.errors.WrongTaskTypeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
    private final DispatcherProxyService dispatcherProxyService;

    /**
     * Constructor.
     *
     * @param userService    the injected user service
     * @param studentService the injected student service
     */
    public StudentResource(UserService userService, StudentService studentService, DispatcherProxyService dispatcherProxyService) {
        this.userService = userService;
        this.studentService = studentService;
        this.dispatcherProxyService = dispatcherProxyService;
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
     * Returns all submissions mady by a student for a specific individual task
     * @param courseInstanceUUID the course instance
     * @param exerciseSheetUUID the exercise sheet
     * @param taskNo the task number
     * @return the submissions
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/task/{taskNo}/student/{matriculationNo}/submissions")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<IndividualTaskSubmissionDTO>> getAllSubmissionsOfStudent(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                                                        @PathVariable int taskNo, @PathVariable String matriculationNo){

        Optional<List<IndividualTaskSubmissionDTO>> optionalSubmissions = studentService.getAllSubmissionsForAssignment(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);
        List<IndividualTaskSubmissionDTO> submissions = optionalSubmissions.orElse(null);

        return ResponseEntity.ok(submissions);
    }

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/:taskNo/submission} : Returns
     * the submission.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @return the {@link ResponseEntity} containing the file id
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/{taskNo}/submission")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<String> getLatestSubmission(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                       @PathVariable int taskNo) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        Optional<String> optionalSubmission = studentService.getLatestSubmissionForAssignment(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        String submission = optionalSubmission.orElse("");

        return ResponseEntity.ok(submission);
    }


    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/:taskNo/dispatcherpoints} : Returns
     * the points.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @return the {@link ResponseEntity} containing the points
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/{taskNo}/dispatcherpoints")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Integer> getDispatcherPoints(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                @PathVariable int taskNo) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        Optional<Integer> optionalPoints = studentService.getDispatcherPoints(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        int points = optionalPoints.orElse(0);

        return ResponseEntity.ok(points);
    }

    /**
     * Processes a submission and grading provided by the dispatcher in the course
     * of an individual task assignment's submission by the student
     * @param courseInstanceUUID the course instance
     * @param exerciseSheetUUID the exercise sheet
     * @param taskNo the task number
     * @param dispatcherUUID the UUID identifying the submission
     * @param token the JWT-Token needed to call the proxy to the dispatcher
     * @return
     */
    @PutMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/{taskNo}/dispatcherUUID/{dispatcherUUID}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Void> handleDispatcherUUID(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                     @PathVariable int taskNo, @PathVariable String dispatcherUUID, @RequestHeader(name="Authorization") String token, HttpServletRequest request) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");
        DispatcherSubmissionDTO submission = null;
        DispatcherGradingDTO grading = null;
        try {
            submission = dispatcherProxyService.getSubmission(dispatcherUUID);
            grading = dispatcherProxyService.getGrading(dispatcherUUID);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(submission);

        // comparing excercise-id of assignment and submission
        var optWeightingAndMaxPointsIdArr = studentService.getDiagnoseLevelWeightingAndMaxPointsAndId(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);
        var weightingAndMaxPointsIdArr = optWeightingAndMaxPointsIdArr.orElse(null);

        if(weightingAndMaxPointsIdArr == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        int dispatcherId = weightingAndMaxPointsIdArr[2].intValue();

        if(submission.getExerciseId() != dispatcherId) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        // persisting the submission
        boolean hasBeenSolved = grading != null && (grading.getPoints() == grading.getMaxPoints()) && grading.getMaxPoints() != 0;
        studentService.addSubmissionForIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo, submission, hasBeenSolved);

        // eventually setting a new highest diagnose-level (if current is higher and action not submit and not previously solved)
        var oldDiagnoseLevel = studentService.getDiagnoseLevel(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo).orElse(0);
        var currDiagnoseLevel = Integer.parseInt(submission.getPassedAttributes().get("diagnoseLevel"));
        int highestDiagnoseLevel = oldDiagnoseLevel;

        if(currDiagnoseLevel > oldDiagnoseLevel && !submission.getPassedAttributes().get("action").equals("submit")){
            studentService.setHighestDiagnoseLevel(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo, currDiagnoseLevel);
            highestDiagnoseLevel = currDiagnoseLevel;
        }

        // calculating and setting the points if submission has been solved but not previously solved
        if(grading == null) return ResponseEntity.ok().build();

        double points = studentService.getDispatcherPoints(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo).orElse(0);
        if(points == 0
            && submission.getPassedAttributes().get("action").equals("submit")
            && grading.getMaxPoints() == grading.getPoints()
            && grading.getMaxPoints() != 0
        ){
            var diagnoseLevelWeighting = weightingAndMaxPointsIdArr[0];
            var maxPoints = weightingAndMaxPointsIdArr[1];

            points = maxPoints - (highestDiagnoseLevel * diagnoseLevelWeighting);

            studentService.setDispatcherPointsForAssignment(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo, points);
            studentService.markTaskAssignmentAsSubmitted(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/:taskNo/diagnose-level} : Returns
     * the diagnose level.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @return the {@link ResponseEntity} containing the diagnose level
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/{taskNo}/diagnose-level")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Integer> getDiagnoseLevel(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                       @PathVariable int taskNo) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        Optional<Integer> optionalDiagnoseLevel = studentService.getDiagnoseLevel(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        int diagnoseLevel = optionalDiagnoseLevel.orElse(0);

        return ResponseEntity.ok(diagnoseLevel);
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
