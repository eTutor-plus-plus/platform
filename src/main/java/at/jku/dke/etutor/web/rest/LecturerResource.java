package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.LecturerSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.LecturerGradingInfoDTO;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.StudentAssignmentOverviewInfoDTO;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.TaskPointEntryDTO;
import at.jku.dke.etutor.web.rest.vm.GradingInfoVM;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing lecturer related operations.
 *
 * @author fne
 */
@RestController
@RequestMapping("/api/lecturer")
public class LecturerResource {

    private final LecturerSPARQLEndpointService lecturerSPARQLEndpointService;

    /**
     * Constructor.
     *
     * @param lecturerSPARQLEndpointService the injected lecturer SPARQL endpoint service
     */
    public LecturerResource(LecturerSPARQLEndpointService lecturerSPARQLEndpointService) {
        this.lecturerSPARQLEndpointService = lecturerSPARQLEndpointService;
    }

    /**
     * {@code GET /api/lecturer/overview/:courseInstanceUUID/:exerciseSheetUUID} : Retrieves the grading overview.
     *
     * @param courseInstanceUUID the course instance uuid from the request path
     * @param exerciseSheetUUID  the exercise sheet uuid from the request path
     * @param pageable           the pagination object
     * @return {@link ResponseEntity} containing the list of student assignment overview information of the currently
     * selected page
     */
    @GetMapping("overview/{courseInstanceUUID}/{exerciseSheetUUID}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<StudentAssignmentOverviewInfoDTO>> getPagedLecturerOverview(
        @PathVariable String courseInstanceUUID,
        @PathVariable String exerciseSheetUUID,
        Pageable pageable
    ) {
        Page<StudentAssignmentOverviewInfoDTO> page = lecturerSPARQLEndpointService.getPagedLecturerOverview(
            courseInstanceUUID,
            exerciseSheetUUID,
            pageable
        );
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * {@code GET /api/lecturer/grading/:courseInstanceUUID/:exerciseSheetUUID/:matriculationNo} : Retrieves the
     * grading info of a specific student from a given course and exercise sheet.
     *
     * @param courseInstanceUUID the course instance uuid from the request path
     * @param exerciseSheetUUID  the exercise sheet uuid from the request path
     * @param matriculationNo    the matriculation number from the request path
     * @return {@link ResponseEntity} containing the list of grading infos
     */
    @GetMapping("grading/{courseInstanceUUID}/{exerciseSheetUUID}/{matriculationNo}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<LecturerGradingInfoDTO>> getGradingInfoForStudent(
        @PathVariable String courseInstanceUUID,
        @PathVariable String exerciseSheetUUID,
        @PathVariable String matriculationNo
    ) {
        List<LecturerGradingInfoDTO> gradingInfoList = lecturerSPARQLEndpointService.getGradingInfo(
            courseInstanceUUID,
            exerciseSheetUUID,
            matriculationNo
        );
        return ResponseEntity.ok(gradingInfoList);
    }

    /**
     * {@code PUT /api/lecturer/grading} : Updates the grading info.
     *
     * @param gradingInfoVM the grading info view model from the request body
     * @return empty {@link ResponseEntity}
     */
    @PutMapping("grading")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> setGradeForAssignment(@RequestBody GradingInfoVM gradingInfoVM) {
        lecturerSPARQLEndpointService.updateGradeForAssignment(
            gradingInfoVM.getCourseInstanceUUID(),
            gradingInfoVM.getExerciseSheetUUID(),
            gradingInfoVM.getMatriculationNo(),
            gradingInfoVM.getOrderNo(),
            gradingInfoVM.isGoalCompleted()
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the achieved points and the maximum points for a given course instance and exercise sheet
     *
     * @param courseInstanceUUID the course instance
     * @param exerciseSheetUUID  the exercise sheet
     * @return the ResponseEntity containing the overview of points
     */
    @GetMapping("course-instance/{courseInstanceUUID}/exercise-sheet/{exerciseSheetUUID}/points-overview")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<TaskPointEntryDTO[]> getDispatcherPointsForExercsiseSheet(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID) {
        Optional<List<TaskPointEntryDTO>> optionalPointsOverviewInfo = lecturerSPARQLEndpointService.getPointsOverviewForExerciseSheet(exerciseSheetUUID, courseInstanceUUID);
        List<TaskPointEntryDTO> pointsOverviewInfo = optionalPointsOverviewInfo.orElse(null);

        if (pointsOverviewInfo != null) return ResponseEntity.ok(pointsOverviewInfo.toArray(TaskPointEntryDTO[]::new));
        else return ResponseEntity.ok(null);
    }

    /**
     * Returns the points overview for a specific exercise sheet and course instance as csv
     *
     * @param courseInstanceUUID the course instance
     * @param exerciseSheetUUID  the csv
     * @return a ResponseEntity containing the csv
     */
    @GetMapping(value = "course-instance/{courseInstanceUUID}/exercise-sheet/{exerciseSheetUUID}/csv/points-overview", produces = "text/csv")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Resource> getDispatcherPointsForExerciseSheetAsCSV(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID) {
        String[] csvHeader = {
            "matriculationNo", "taskHeader", "maxPoints", "points"
        };
        Optional<List<TaskPointEntryDTO>> optionalPointsOverviewInfo = lecturerSPARQLEndpointService.getPointsOverviewForExerciseSheet(exerciseSheetUUID, courseInstanceUUID);
        List<TaskPointEntryDTO> pointsOverviewInfo = optionalPointsOverviewInfo.orElse(null);

        ByteArrayInputStream byteArrayOutputStream;

        try (
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVPrinter csvPrinter = new CSVPrinter(
                new PrintWriter(out),
                CSVFormat.DEFAULT.withHeader(csvHeader)
            )
        ) {
            List<String> printableRecord;
            for (TaskPointEntryDTO record : pointsOverviewInfo) {
                printableRecord = new ArrayList<>();
                printableRecord.add(record.getMatriculationNo());
                printableRecord.add(record.getTaskHeader());
                printableRecord.add(Double.toString(record.getMaxPoints()));
                printableRecord.add(Double.toString(record.getPoints()));
                csvPrinter.printRecord(printableRecord);
            }

            csvPrinter.flush();

            byteArrayOutputStream = new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        InputStreamResource fileInputStream = new InputStreamResource(byteArrayOutputStream);

        String csvFileName = "pointOverview.csv";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFileName);
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");


        return new ResponseEntity<>(
            fileInputStream,
            headers,
            HttpStatus.OK
        );
    }

    /**
     * Closes an exercise sheet of a given course instance.
     *
     * @param courseInstanceUUID the course instance's UUID
     * @param exerciseSheetUUID  the exercise sheet's UUID
     * @return empty {@link ResponseEntity}
     */
    @PutMapping("course-instance/{courseInstanceUUID}/exercise-sheet/{exerciseSheetUUID}/close")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> closeExerciseSheetOfCourseInstance(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID) {
        lecturerSPARQLEndpointService.closeExerciseSheetOfCourseInstance(courseInstanceUUID, exerciseSheetUUID);
        return ResponseEntity.noContent().build();
    }

    /**
     * Re-opens an already closed exercise sheet of a given course instance.
     *
     * @param courseInstanceUUID the course instance's UUID
     * @param exerciseSheetUUID  the exercise sheet's UUID
     * @return empty {@link ResponseEntity}
     */
    @PutMapping("course-instance/{courseInstanceUUID}/exercise-sheet/{exerciseSheetUUID}/open")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> openExerciseSheetOfCourseInstance(@PathVariable String courseInstanceUUID, @PathVariable String exerciseSheetUUID) {
        lecturerSPARQLEndpointService.openExerciseSheetOfCourseInstance(courseInstanceUUID, exerciseSheetUUID);
        return ResponseEntity.noContent().build();
    }
}
