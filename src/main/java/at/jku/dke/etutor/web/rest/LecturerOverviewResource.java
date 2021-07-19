package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.CourseInstanceSPARQLEndpointService;
import at.jku.dke.etutor.service.LecturerOverviewService;
import at.jku.dke.etutor.service.dto.courseinstance.DisplayableCourseInstanceDTO;
import at.jku.dke.etutor.service.dto.lectureroverview.StatisticsOverviewModelDTO;
import at.jku.dke.etutor.web.rest.errors.CourseInstanceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.util.List;

/**
 * REST controller for managing lecturer overview related
 * operations.
 *
 * @author fne
 */
@RestController
@RequestMapping("/api/lecturer-overview")
public class LecturerOverviewResource {

    private final LecturerOverviewService lecturerOverviewService;
    private final CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService;

    /**
     * Constructor.
     *
     * @param lecturerOverviewService             the injected lecturer overview service
     * @param courseInstanceSPARQLEndpointService the injected course instance SPARQL endpoint service
     */
    public LecturerOverviewResource(LecturerOverviewService lecturerOverviewService, CourseInstanceSPARQLEndpointService courseInstanceSPARQLEndpointService) {
        this.lecturerOverviewService = lecturerOverviewService;
        this.courseInstanceSPARQLEndpointService = courseInstanceSPARQLEndpointService;
    }

    /**
     * REST endpoint for retrieving a paged course overview of the currently
     * logged-in user.
     *
     * @param pageable the pagination object
     * @return the {@link ResponseEntity} containing the list of paged courses
     */
    @GetMapping("courses")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<DisplayableCourseInstanceDTO>> getPagedCoursesOfUser(Pageable pageable) {
        String user = SecurityUtils.getCurrentUserLogin().orElse("");

        Page<DisplayableCourseInstanceDTO> page = courseInstanceSPARQLEndpointService.getDisplayableCourseInstancesForLecturer(user, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * REST endpoint for retrieving a course instance's statistical information.
     *
     * @param courseInstanceId the course instance id from the request path
     * @return the {@link ResponseEntity} containing the statistical information
     */
    @GetMapping("statistics/{courseInstanceId}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<StatisticsOverviewModelDTO> getOverviewStatisticsForCourseInstance(@PathVariable String courseInstanceId) {
        try {
            var statistics = this.lecturerOverviewService.getCourseInstanceOverviewStatistics(courseInstanceId);
            return ResponseEntity.ok(statistics);
        } catch (at.jku.dke.etutor.service.exception.CourseInstanceNotFoundException courseInstanceNotFoundException) {
            throw new CourseInstanceNotFoundException();
        }
    }
}
