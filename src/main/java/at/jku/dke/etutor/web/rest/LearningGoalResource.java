package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.*;
import at.jku.dke.etutor.service.dto.LearningGoalDTO;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import at.jku.dke.etutor.web.rest.errors.BadRequestAlertException;
import at.jku.dke.etutor.web.rest.errors.LearningGoalNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * REST controller for managing learning goals.
 *
 * @author fne
 */
@RestController
@RequestMapping("/api")
public class LearningGoalResource {

    private final Logger log = LoggerFactory.getLogger(LearningGoalResource.class);

    private final SPARQLEndpointService sparqlEndpointService;

    /**
     * Constructor.
     *
     * @param sparqlEndpointService the injected sparql endpoint service
     */
    public LearningGoalResource(SPARQLEndpointService sparqlEndpointService) {
        this.sparqlEndpointService = sparqlEndpointService;
    }

    /**
     * {@code POST /learninggoals} : Creates a new learning goal.
     * <p>
     * If the learning goal already exists, an exception will be thrown.
     * </p>
     *
     * @param newLearningGoalDTO the learning goal to create
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with the new learning goal in body
     * @throws URISyntaxException if the location URI syntax is incorrect
     */
    @PostMapping("/learninggoals")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<LearningGoalDTO> createLearningGoal(@Valid @RequestBody NewLearningGoalDTO newLearningGoalDTO)
        throws URISyntaxException {
        log.debug("REST request to save a new learning goal: {}", newLearningGoalDTO);

        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            var newGoal = sparqlEndpointService.insertNewLearningGoal(newLearningGoalDTO, currentLogin);

            return ResponseEntity.created(new URI(String.format("/api/learninggoals/%s/%s", newGoal.getOwner(),
                newGoal.getNameForRDF())))
                .body(newGoal);
        } catch (LearningGoalAlreadyExistsException ex) {
            throw new at.jku.dke.etutor.web.rest.errors.LearningGoalAlreadyExistsException();
        }
    }

    /**
     * {@code PUT /learninggoals} : Updates an existing learning goal or creates one.
     *
     * @param learninggoal the learning goal from the request body
     * @return the {@link ResponseEntity} with status {@code 204 (No content)}.
     * @throws BadRequestAlertException                                        if the logged in user is not the owner of the learning goal
     * @throws at.jku.dke.etutor.web.rest.errors.LearningGoalNotFoundException if the learning goal does not exist
     */
    @PutMapping("/learninggoals")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> updateLearningGoal(@Valid @RequestBody @NotNull LearningGoalDTO learninggoal) {
        log.debug("REST request to update a learninggoal : {}", learninggoal);

        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!StringUtils.equals(learninggoal.getOwner(), currentLogin)) {
            throw new BadRequestAlertException("Only the creator is allowed to edit the learning goal!",
                "learningGoalManagement", "learningGoalNotOwner");
        }

        try {
            sparqlEndpointService.updateLearningGoal(learninggoal);
        } catch (LearningGoalNotExistsException ex) {
            throw new LearningGoalNotFoundException();
        } catch (PrivateSuperGoalException ex) {
            throw new at.jku.dke.etutor.web.rest.errors.PrivateSuperGoalException();
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * {@code POST /learninggoals/{owner}/{parentGoalName}/subGoal} : Creates a new sub goal
     *
     * @param newLearningGoalDTO the sub goal to create
     * @param owner              the owner's name
     * @param parentGoalName     the parent goal's name
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with the new learning goal in body
     * @throws URISyntaxException                                                   if the location URI syntax is incorrect
     * @throws BadRequestAlertException                                             if the logged in user is not the given owner
     * @throws at.jku.dke.etutor.web.rest.errors.LearningGoalNotFoundException      the parent goal does not exist
     * @throws at.jku.dke.etutor.web.rest.errors.LearningGoalAlreadyExistsException the name of the sub goal does
     *                                                                              already exist
     */
    @PostMapping("/learninggoals/{owner}/{parentGoalName}/subGoal")
    public ResponseEntity<LearningGoalDTO> createSubGoal(@Valid @RequestBody NewLearningGoalDTO newLearningGoalDTO,
                                                         @PathVariable("owner") String owner,
                                                         @PathVariable("parentGoalName") String parentGoalName) throws URISyntaxException {
        log.debug("REST request to create a sub goal: {} for parent: {}", newLearningGoalDTO, parentGoalName);

        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!StringUtils.equals(owner, currentLogin)) {
            throw new BadRequestAlertException("Only the creator is allowed to edit the learning goal!",
                "learningGoalManagement", "learningGoalNotOwner");
        }

        try {
            var newSubGoal = sparqlEndpointService.insertSubGoal(newLearningGoalDTO,
                currentLogin, parentGoalName);

            return ResponseEntity.created(new URI(String.format("/api/learninggoals/%s/%s", newSubGoal.getOwner(),
                newSubGoal.getNameForRDF())))
                .body(newSubGoal);
        } catch (LearningGoalAlreadyExistsException ex) {
            throw new at.jku.dke.etutor.web.rest.errors.LearningGoalAlreadyExistsException();
        } catch (LearningGoalNotExistsException e) {
            throw new at.jku.dke.etutor.web.rest.errors.LearningGoalNotFoundException();
        }
    }

    /**
     * {@code GET /learninggoals}
     *
     * <p>
     * Returns a list of all learning goals which are visible to the current user.
     * </p>
     *
     * @return {@link ResponseEntity} with status {@code 200 (OK)} and the list of visible learning goals in body
     */
    @GetMapping("/learninggoals")
    public ResponseEntity<Collection<LearningGoalDTO>> getVisibleGoals() {

        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(currentLogin);
            return ResponseEntity.ok(goals);
        } catch (InternalModelException ex) {
            throw new BadRequestAlertException("An internal error occurred!", "learningGoalManagement", "parsingError");
        }
    }
}
