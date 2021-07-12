package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.LecturerOverviewService;
import at.jku.dke.etutor.service.dto.CourseOverviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * Constructor.
     *
     * @param lecturerOverviewService the injected lecturer overview service
     */
    public LecturerOverviewResource(LecturerOverviewService lecturerOverviewService) {
        this.lecturerOverviewService = lecturerOverviewService;
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
    public ResponseEntity<List<CourseOverviewDTO>> getPagedCoursesOfUser(Pageable pageable) {
        String user = SecurityUtils.getCurrentUserLogin().orElse("");

        Page<CourseOverviewDTO> page = lecturerOverviewService.getPagedCoursesOfUser(user, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
