package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.LecturerSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.LecturerGradingInfoDTO;
import at.jku.dke.etutor.service.dto.courseinstance.taskassignment.StudentAssignmentOverviewInfoDTO;
import at.jku.dke.etutor.web.rest.vm.GradingInfoVM;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

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
}
