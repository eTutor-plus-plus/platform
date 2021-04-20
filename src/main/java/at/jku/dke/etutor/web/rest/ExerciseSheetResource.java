package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.ExerciseSheetSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDisplayDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

/**
 * REST controller for managing exercise sheets.
 *
 * @author fne
 */
@RestController
@RequestMapping("/api/exercise-sheet")
public class ExerciseSheetResource {

    private final ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService;

    /**
     * Constructor.
     *
     * @param exerciseSheetSPARQLEndpointService the injected exercise sheet sparql endpoint service
     */
    public ExerciseSheetResource(ExerciseSheetSPARQLEndpointService exerciseSheetSPARQLEndpointService) {
        this.exerciseSheetSPARQLEndpointService = exerciseSheetSPARQLEndpointService;
    }

    /**
     * {@code POST} : Creates a new exercise sheet.
     *
     * @param newExerciseSheetDTO the dto class from the request body
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with the new exercise sheet in body
     * @throws URISyntaxException if the location URI syntax is incorrect
     * @throws ParseException     if an internal parsing exception occurs
     */
    @PostMapping
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<ExerciseSheetDTO> createExerciseSheet(@Valid @RequestBody NewExerciseSheetDTO newExerciseSheetDTO)
        throws URISyntaxException, ParseException {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("");

        ExerciseSheetDTO newExerciseSheet = exerciseSheetSPARQLEndpointService.insertNewExerciseSheet(newExerciseSheetDTO, currentLogin);
        String uuid = newExerciseSheet.getId().substring(newExerciseSheet.getId().lastIndexOf('#') + 1);

        return ResponseEntity.created(new URI(String.format("/api/exercise-sheet/%s", uuid))).body(newExerciseSheet);
    }

    /**
     * {@code PUT} : Updates an existing exercise sheet.
     *
     * @param exerciseSheetDTO the dto class form the request body
     * @return the {@link ResponseEntity} with no content
     */
    @PutMapping
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> updateExerciseSheet(@Valid @RequestBody ExerciseSheetDTO exerciseSheetDTO) {
        exerciseSheetSPARQLEndpointService.updateExerciseSheet(exerciseSheetDTO);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET :id} : Retrieves an exercise sheet by its id.
     *
     * @param id the id of the exercise sheet (from the request path)
     * @return the {@link ResponseEntity} containing the exercise sheet
     */
    @GetMapping("{id}")
    public ResponseEntity<ExerciseSheetDTO> getExerciseSheetById(@PathVariable String id) throws ParseException {
        Optional<ExerciseSheetDTO> optionalSheet = exerciseSheetSPARQLEndpointService.getExerciseSheetById(id);
        return ResponseEntity.of(optionalSheet);
    }

    /**
     * {@code DELETE :id} : Deletes an exercise sheet by its id.
     *
     * @param id the id of the exercise sheet (from the request path)
     * @return the {@link ResponseEntity} with no content
     */
    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> removeExerciseSheetById(@PathVariable String id) {
        exerciseSheetSPARQLEndpointService.deleteExerciseSheetById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET display/sliced} : Retrieves the sliced exercise sheet displays.
     *
     * @param name     the optional name filter query parameter
     * @param pageable the pagination object
     * @return {@link ResponseEntity} containing the list of exercise sheet displays of the current "page"
     */
    @GetMapping("display/sliced")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<ExerciseSheetDisplayDTO>> getExerciseDisplayList(
        @RequestParam(required = false, defaultValue = "") String name,
        Pageable pageable
    ) {
        Slice<ExerciseSheetDisplayDTO> slice = exerciseSheetSPARQLEndpointService.getFilteredExerciseSheetDisplayDTOs(name, pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Has-Next-Page", String.valueOf(slice.hasNext()));
        return new ResponseEntity<>(slice.getContent(), headers, HttpStatus.OK);
    }

    /**
     * {@code GET display/paged} : Retrieves the paged exercise sheet displays.
     *
     * @param name     the optional name filter query parameter
     * @param pageable the pagination object
     * @return {@link ResponseEntity} containing the list of exercise sheet displays of the currently
     * selected page
     */
    @GetMapping("display/paged")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<List<ExerciseSheetDisplayDTO>> getPagedExerciseDisplayList(
        @RequestParam(required = false, defaultValue = "") String name,
        Pageable pageable
    ) {
        Page<ExerciseSheetDisplayDTO> page = exerciseSheetSPARQLEndpointService.getFilteredExerciseSheetDisplayDTOsAsPage(name, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
