package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.calc.models.Feedback;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.objects.dispatcher.processmining.PmExerciseLogDTO;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.*;
import at.jku.dke.etutor.service.dto.StudentSelfEvaluationLearningGoalDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceInformationDTO;
import at.jku.dke.etutor.service.dto.courseinstance.CourseInstanceProgressOverviewDTO;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import at.jku.dke.etutor.objects.dispatcher.GradingDTO;
import at.jku.dke.etutor.objects.dispatcher.SubmissionDTO;
import at.jku.dke.etutor.service.dto.student.IndividualTaskSubmissionDTO;
import at.jku.dke.etutor.service.dto.student.StudentTaskListInfoDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
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
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;

    private final CourseInstanceSPARQLEndpointService courseInstanceService;

    /**
     * Constructor.
     *
     * @param userService    the injected user service
     * @param studentService the injected student service
     */
    public StudentResource(UserService userService, StudentService studentService, DispatcherProxyService dispatcherProxyService, AssignmentSPARQLEndpointService assignmentSPARQLEndpointService, CourseInstanceSPARQLEndpointService courseInstanceService) {
        this.userService = userService;
        this.studentService = studentService;
        this.dispatcherProxyService = dispatcherProxyService;
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.courseInstanceService = courseInstanceService;
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

    @GetMapping("matriculationNumber")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<String> getCurrentStudentsMatriculationNumber () {
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");
        return ResponseEntity
            .ok()
            .body(matriculationNumber);
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
     * Returns the reached goals of a student, for a course instance
     * @param uuid the uuid of the course instance
     * @return a collection with all the reached goals
     */
    @GetMapping("courses/{uuid}/goals/reached")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Collection<String>> getReachedGoalsForCourse(@PathVariable(name = "uuid") String uuid){
        String matriculationNumber = SecurityUtils.getCurrentUserLogin().orElse("");
        String courseInstanceURL = ETutorVocabulary.createCourseInstanceURLString(uuid);

        Collection<String> reachedGoals = studentService.getReachedGoalsOfStudentAndCourseInstance(courseInstanceURL, matriculationNumber);

        return ResponseEntity.ok(reachedGoals);
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
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/uploadTask/:taskNo/file-attachment} :
     * Returns the file attachment's id for an individual task.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @return the {@link ResponseEntity} containing the file id
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/uploadTask/{taskNo}/file-attachment")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Integer> getFileAttachmentIdOfIndividualTask(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                                       @PathVariable int taskNo) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        Optional<Integer> optionalId = studentService.getFileIdOfIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        int id = optionalId.orElse(-1);

        return ResponseEntity.ok(id);
    }

    /**
     * Returns the calc instruction file attachment's id for an individual task.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @return the {@link ResponseEntity} containing the file id
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/calcTask/{taskNo}/individual-calc-instruction")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Integer> getFileAttachmentIdOfIndividualCalcInstruction (@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                                                   @PathVariable int taskNo) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        Optional<Integer> optionalId = studentService.getFileIdIndividualCalcInstruction(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        int id = optionalId.orElse(-1);

        return ResponseEntity.ok(id);
    }

    /**
     * Returns the calc solution file attachment's id for an individual task.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @return the {@link ResponseEntity} containing the file id
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/calcTask/{taskNo}/individual-calc-solution")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Integer> getFileAttachmentIdOfIndividualCalcSolution (@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                                                   @PathVariable int taskNo) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        Optional<Integer> optionalId = studentService.getFileIdIndividualCalcSolution(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        int id = optionalId.orElse(-1);

        return ResponseEntity.ok(id);
    }

    /**
     * Returns the writer instruction file attachment's id for an individual task.
     *
     * @param courseInstanceUUID the course instance UUID
     * @param exerciseSheetUUID  the exercise sheet UUID
     * @param taskNo             the task no
     * @return the {@link ResponseEntity} containing the file id
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/calcTask/{taskNo}/individual-writer-instruction")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Integer> getFileAttachmentIdOfIndividualWriterInstruction (@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                                                   @PathVariable int taskNo) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        Optional<Integer> optionalId = studentService.getFileIdIndividualWriterInstruction(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        int id = optionalId.orElse(-1);

        return ResponseEntity.ok(id);
    }

    /**
     * Corrects a calc submission and returns feedback
     *
     * @param writerInstructionFileId id of the instruction writer file
     * @param calcSolutionFileId id of the solution calc file
     * @param calcSubmissionFileId if of the submission calc file
     * @return a string which contains the feedback of the correction
     */
    @PutMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/calcTask/{taskNo}/student/{matriculationNo}/calcSubmission/{writerInstructionFileId}/{calcSolutionFileId}/{calcSubmissionFileId}/diagnose_task")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> diagnoseAndPersistCalcTaskSubmission(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                                       @PathVariable int taskNo, @PathVariable String matriculationNo,
                                                                       @PathVariable long writerInstructionFileId, @PathVariable long calcSolutionFileId,
                                                                       @PathVariable long calcSubmissionFileId) {
        Feedback feedback = studentService.correctCalcTask(writerInstructionFileId, calcSolutionFileId, calcSubmissionFileId);
        double maxPoints = assignmentSPARQLEndpointService.getMaxPointsForTaskAssignmentByIndividualTask(matriculationNo, courseInstanceUUID, exerciseSheetUUID, taskNo).get();
        double achievedPoints = maxPoints;
        if (!feedback.isCorrect()) {
            achievedPoints = 0.0;
        }

        StudentService.persistGradingOfCalcTaskSubmission(matriculationNo, courseInstanceUUID, exerciseSheetUUID, taskNo, maxPoints, achievedPoints, "diagnose", feedback.getTextualFeedback());


        return ResponseEntity
            .ok()
            .body(feedback.getTextualFeedback());
    }

    @GetMapping("courses/calcSubmission/{writerInstructionFileId}/{calcSolutionFileId}/{calcSubmissionFileId}/diagnose_task")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\", \"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<String> diagnoseCalcTaskSubmission(
                                                   @PathVariable long writerInstructionFileId, @PathVariable long calcSolutionFileId,
                                                   @PathVariable long calcSubmissionFileId) {
        Feedback feedback = studentService.correctCalcTask(writerInstructionFileId, calcSolutionFileId, calcSubmissionFileId);
        return ResponseEntity
            .ok()
            .body(feedback.getTextualFeedback());
    }




    /**
     * @param courseInstanceUUID the course instance
     * @param exerciseSheetUUID the exercise sheet
     * @param taskNo the task number
     * @param matriculationNo the matriculation number
     * @param writerInstructionFileId id of the instruction writer file
     * @param calcSolutionFileId id of the solution calc file
     * @param calcSubmissionFileId if of the submission calc file
     */
    @PutMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/calcTask/{taskNo}/student/{matriculationNo}/calcSubmission/{writerInstructionFileId}/{calcSolutionFileId}/{calcSubmissionFileId}/submit_task")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Void> processSubmittedCalcTaskSubmission(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                                   @PathVariable int taskNo, @PathVariable String matriculationNo,
                                                                   @PathVariable long writerInstructionFileId, @PathVariable long calcSolutionFileId,
                                                                   @PathVariable long calcSubmissionFileId) {
        Feedback feedback = studentService.correctCalcTask(writerInstructionFileId, calcSolutionFileId, calcSubmissionFileId);
        double maxPoints = assignmentSPARQLEndpointService.getMaxPointsForTaskAssignmentByIndividualTask(matriculationNo, courseInstanceUUID, exerciseSheetUUID, taskNo).get();
        if (feedback.isCorrect()) {
            studentService.setDispatcherPointsForIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo, maxPoints);
            studentService.markTaskAssignmentAsSubmitted(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);
        }
        double achievedPoints = maxPoints;
        if (!feedback.isCorrect()) {
            achievedPoints = 0.0;
        }
        StudentService.persistGradingOfCalcTaskSubmission(matriculationNo, courseInstanceUUID, exerciseSheetUUID, taskNo, maxPoints, achievedPoints, "submit", feedback.getTextualFeedback());
        return ResponseEntity.ok().build();
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

        Optional<List<IndividualTaskSubmissionDTO>> optionalSubmissions = studentService.getAllDispatcherSubmissionsForIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);
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

        Optional<String> optionalSubmission = studentService.getLatestDispatcherSubmissionForIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

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

        Optional<Integer> optionalPoints = studentService.getAchievedDispatcherPointsForIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

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
    public ResponseEntity<Integer> processDispatcherSubmissionForIndividualTask(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                                                     @PathVariable int taskNo, @PathVariable String dispatcherUUID, @RequestHeader(name="Authorization") String token, HttpServletRequest request) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");
        SubmissionDTO submission;
        GradingDTO grading;
        int diagnoseLevelWeighting;
        int maxPoints;
        int dispatcherId;

        // Get submission and grading according to UUID from dispatcher
        try {
            submission = dispatcherProxyService.getSubmission(dispatcherUUID);
            Objects.requireNonNull(submission);
            grading = dispatcherProxyService.getGrading(dispatcherUUID);
        } catch (JsonProcessingException | DispatcherRequestFailedException | NullPointerException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

        boolean isExerciseSheetClosed = courseInstanceService.isAssignedExerciseSheetClosed(courseInstanceUUID, exerciseSheetUUID);

        // Get required information about task assignment, diagnose-level-weighting, max-points, dispatcher id
        var optWeightingAndMaxPointsIdArr = studentService.getDiagnoseLevelWeightingAndMaxPointsAndId(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);
        var weightingAndMaxPointsIdArr = optWeightingAndMaxPointsIdArr.orElse(null);
        if(weightingAndMaxPointsIdArr == null)
            return ResponseEntity.internalServerError().build();

        dispatcherId = weightingAndMaxPointsIdArr[2];
        diagnoseLevelWeighting = weightingAndMaxPointsIdArr[0];
        maxPoints = weightingAndMaxPointsIdArr[1];

        if(submission.getExerciseId() != dispatcherId)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        // Process
        var achievedPoints = studentService.processDispatcherSubmissionForIndividualTask(matriculationNo, courseInstanceUUID, exerciseSheetUUID, taskNo,
            submission, grading, maxPoints, diagnoseLevelWeighting, isExerciseSheetClosed);

        return ResponseEntity.ok(achievedPoints);
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
    @PutMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/{taskNo}/dispatcherUUID/bpmn/{dispatcherUUID}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Void> handleBpmnDispatcherUUID(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID,
                                                     @PathVariable int taskNo, @PathVariable String dispatcherUUID, @RequestHeader(name="Authorization") String token, HttpServletRequest request) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");
        SubmissionDTO submission = null;
        GradingDTO grading = null;
        try {
            submission = dispatcherProxyService.getBpmnSubmission(dispatcherUUID);
            grading = dispatcherProxyService.getBpmnGrading(dispatcherUUID);
        } catch (JsonProcessingException | DispatcherRequestFailedException e) {
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
        studentService.addSubmissionForIndividualTaskByDispatcherSubmission(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo, submission, hasBeenSolved);

        // eventually setting a new highest diagnose-level (if current is higher and action not submit and not previously solved)
        var oldDiagnoseLevel = studentService.getHighestEverChosenDiagnoseLevelForIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo).orElse(0);
        var currDiagnoseLevel = Integer.parseInt(submission.getPassedAttributes().get("diagnoseLevel"));
        int highestDiagnoseLevel = oldDiagnoseLevel;

        if(currDiagnoseLevel > oldDiagnoseLevel && !submission.getPassedAttributes().get("action").equals("submit")){
            studentService.setHighestChosenDiagnoseLevelForIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo, currDiagnoseLevel);
            highestDiagnoseLevel = currDiagnoseLevel;
        }

        // calculating and setting the points if submission has been solved but not previously solved
        if(grading == null) return ResponseEntity.ok().build();

        double points = studentService.getAchievedDispatcherPointsForIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo).orElse(0);
        if(points == 0
            && submission.getPassedAttributes().get("action").equals("submit")
            && grading.getMaxPoints() == grading.getPoints()
            && grading.getMaxPoints() != 0
        ){
            var diagnoseLevelWeighting = weightingAndMaxPointsIdArr[0];
            var maxPoints = weightingAndMaxPointsIdArr[1];

            points = maxPoints - (highestDiagnoseLevel * diagnoseLevelWeighting);

            studentService.setDispatcherPointsForIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo, points);
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

        Optional<Integer> optionalDiagnoseLevel = studentService.getHighestEverChosenDiagnoseLevelForIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        int diagnoseLevel = optionalDiagnoseLevel.orElse(0);

        return ResponseEntity.ok(diagnoseLevel);
    }

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/uploadTask/:taskNo/file-attachment/of-student/:matriculationNo} : Returns
     * the file attachment's id of an individual task for a specific student.
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

        Optional<Integer> optionalId = studentService.getFileIdOfIndividualTask(courseInstanceUUID, exerciseSheetUUID, matriculationNo, taskNo);

        int id = optionalId.orElse(-1);

        return ResponseEntity.ok(id);
    }

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/file-attachment}
     * Returns the file id of an individual assignment (an assigned exercise sheet) for the logged in student.
     *
     * @param courseInstanceUUID The course instance
     * @param exerciseSheetUUID the exercise sheet
     * @return the id of the file
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/file-attachment")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<Integer> getFileAttachementIdOfAssignment(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID) {
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");

        Optional<Integer> optionalId = studentService.getFileIdOfIndividualTaskAssignment(matriculationNo, courseInstanceUUID, exerciseSheetUUID);

        int id = optionalId.orElse(-1);

        return ResponseEntity.ok(id);
    }

    /**
     * {@code GET /api/student/courses/:courseInstanceUUID/exercises/:exerciseSheetUUID/task/:taskNo}
     * Returns the log corresponding to the given exercise
     * @param courseInstanceUUID the course instance
     * @param exerciseSheetUUID the exercise sheet
     * @param taskNo the task number
     * @return the log of the exercise
     */
    @GetMapping("courses/{courseInstanceUUID}/exercises/{exerciseSheetUUID}/task/{taskNo}/pmlog")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.STUDENT + "\")")
    public ResponseEntity<PmExerciseLogDTO> getLogToCorrespondingExerciseId(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID, @PathVariable int taskNo){
        String matriculationNo = SecurityUtils.getCurrentUserLogin().orElse("");
        PmExerciseLogDTO pmExerciseLogDTO = null;

        // fetches the dispatcher exercise id corresponding to the assigned exercise
        Optional<Integer> dispatcherExerciseId = studentService.getDispatcherTaskId(matriculationNo, courseInstanceUUID, exerciseSheetUUID, taskNo);

        try{
            // fetches the log information corresponding to exercise, wrapped in DTO
            pmExerciseLogDTO = dispatcherProxyService.getLogToExercise(dispatcherExerciseId.orElse(-1));
        }catch(DispatcherRequestFailedException e){
            e.printStackTrace();
        }
        return ResponseEntity.ok(pmExerciseLogDTO);
    }
}
