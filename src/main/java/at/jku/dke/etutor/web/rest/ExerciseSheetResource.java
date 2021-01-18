package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.security.SecurityUtils;
import at.jku.dke.etutor.service.ExerciseSheetSPARQLEndpointService;
import at.jku.dke.etutor.service.dto.exercisesheet.ExerciseSheetDTO;
import at.jku.dke.etutor.service.dto.exercisesheet.NewExerciseSheetDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Optional;

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
     * @param newExerciseSheetDTO the dto class from the requeset body
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
     * {@code GET} : Retrieves an exercise sheet by its id.
     *
     * @param id the id of the exercise sheet (from the request path)
     * @return the {@link ResponseEntity} containing the exercise sheet
     */
    @GetMapping("{id}")
    public ResponseEntity<ExerciseSheetDTO> getExerciseSheetById(@PathVariable String id) throws ParseException {
        Optional<ExerciseSheetDTO> optionalSheet = exerciseSheetSPARQLEndpointService.getExerciseSheetById(id);
        return ResponseEntity.of(optionalSheet);
    }
}
