package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.InternalModelException;
import at.jku.dke.etutor.service.SPARQLEndpointService;
import at.jku.dke.etutor.service.dto.LearningGoalDTO;
import at.jku.dke.etutor.service.dto.NewLearningGoalDTO;
import at.jku.dke.etutor.service.exception.LearningGoalAlreadyExistsException;
import at.jku.dke.etutor.service.exception.LearningGoalNotExistsException;
import at.jku.dke.etutor.service.exception.PrivateSuperGoalException;
import at.jku.dke.etutor.web.rest.errors.BadRequestAlertException;
import at.jku.dke.etutor.web.rest.errors.LearningGoalNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import net.minidev.json.JSONArray;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

            return ResponseEntity
                .created(new URI(String.format("/api/learninggoals/%s/%s", newGoal.getOwner(), newGoal.getNameForRDF())))
                .body(newGoal);
        } catch (LearningGoalAlreadyExistsException ex) {
            throw new at.jku.dke.etutor.web.rest.errors.LearningGoalAlreadyExistsException();
        }
    }

    /**
     * {@code DELETE /learninggoals/:goalName} : Deletes the given goal and its sub goals.
     *
     * @param goalName the goal's name from the request path
     * @return the {@link ResponseEntity} with no content
     */
    @DeleteMapping("/learninggoals/{goalName}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> deleteLearningGoal(@PathVariable String goalName) {
        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            sparqlEndpointService.removeLearningGoalAndSubGoals(currentLogin, goalName);
            return ResponseEntity.noContent().build();
        } catch (LearningGoalNotExistsException ex) {
            throw new LearningGoalNotFoundException();
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
            throw new BadRequestAlertException(
                "Only the creator is allowed to edit the learning goal!",
                "learningGoalManagement",
                "learningGoalNotOwner"
            );
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
     * {@code PUT /learninggoals/:owner/:goalName/dependencies} : Sets the dependencies of a learning goal
     *
     * @param owner    the learning goal's owner (from the request path)
     * @param goalName the learning goal's name (from the request path)
     * @param goalIds  the dependency ids (from the request body)
     * @return {@link ResponseEntity} with no content
     */
    @PutMapping("/learninggoals/{owner}/{goalName}/dependencies")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> setDependencies(@PathVariable String owner, @PathVariable String goalName, @RequestBody JSONArray goalIds) {
        sparqlEndpointService.setDependencies(owner, goalName, StreamEx.of(goalIds).map(String.class::cast).toList());

        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET /learninggoals/:owner/:goalName/dependencies} : Returns the dependencies of a learning goal.
     *
     * @param owner    the learning goal's owner (from the request path)
     * @param goalName the learning goal's name (from the request path)
     * @return the {@link ResponseEntity} containing the goal ids
     */
    @GetMapping("/learninggoals/{owner}/{goalName}/dependencies")
    public ResponseEntity<Collection<String>> getDependencies(@PathVariable String owner, @PathVariable String goalName) {
        Collection<String> dependencies = sparqlEndpointService.getDependencies(owner, goalName);
        return ResponseEntity.ok(dependencies);
    }

    /**
     * {@code GET /learninggoals/:owner/:goalName/dependencies/displayable} : Returns the dependency names of a learning goal.
     *
     * @param owner    the learning goal's onwer (from the request path)
     * @param goalName the learning goal's name (from the request path
     * @return the {@link ResponseEntity} containing the goal names
     */
    @GetMapping("/learninggoals/{owner}/{goalName}/dependencies/displayable")
    public ResponseEntity<Collection<String>> getDisplayableDependencies(@PathVariable String owner, @PathVariable String goalName) {
        Collection<String> displayableDependencies = sparqlEndpointService.getDisplayableDependencies(owner, goalName);
        return ResponseEntity.ok(displayableDependencies);
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
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<LearningGoalDTO> createSubGoal(
        @Valid @RequestBody NewLearningGoalDTO newLearningGoalDTO,
        @PathVariable("owner") String owner,
        @PathVariable("parentGoalName") String parentGoalName
    ) throws URISyntaxException {
        log.debug("REST request to create a sub goal: {} for parent: {}", newLearningGoalDTO, parentGoalName);

        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!StringUtils.equals(owner, currentLogin)) {
            throw new BadRequestAlertException(
                "Only the creator is allowed to edit the learning goal!",
                "learningGoalManagement",
                "learningGoalNotOwner"
            );
        }

        try {
            var newSubGoal = sparqlEndpointService.insertSubGoal(newLearningGoalDTO, currentLogin, parentGoalName);

            return ResponseEntity
                .created(new URI(String.format("/api/learninggoals/%s/%s", newSubGoal.getOwner(), newSubGoal.getNameForRDF())))
                .body(newSubGoal);
        } catch (LearningGoalAlreadyExistsException ex) {
            throw new at.jku.dke.etutor.web.rest.errors.LearningGoalAlreadyExistsException();
        } catch (LearningGoalNotExistsException e) {
            throw new at.jku.dke.etutor.web.rest.errors.LearningGoalNotFoundException();
        }
    }

    @PostMapping("/learninggoals/{parentGoalOwner}/parentGoal/{parentGoalName}/{subGoalOwner}/subGoal/{subGoalName}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.INSTRUCTOR + "\")")
    public ResponseEntity<Void> addSubGoal(
        @PathVariable("subGoalName") String subGoalName,
        @PathVariable("parentGoalOwner") String parentGoalOwner,
        @PathVariable("parentGoalName") String parentGoalName,
        @PathVariable("subGoalOwner") String subGoalOwner

    ) {
        log.debug("REST request to add a sub goal: {} for parent: {}", subGoalName, parentGoalName);

        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!StringUtils.equals(subGoalOwner, currentLogin) || !StringUtils.equals(parentGoalOwner, subGoalOwner)) {
        throw new BadRequestAlertException(
            "Only the creator is allowed to edit the learning goal!",
            "learningGoalManagement",
            "learningGoalNotOwner"
        );
        }else if (parentGoalName.trim().equals(subGoalName.trim())){
            throw new BadRequestAlertException(
                "Parent-goal and sub-goal must not be equal!",
                "learningGoalManagement",
                "goalsNotEqual"
            );
        }


        try {
            sparqlEndpointService.insertExistingGoalAsSubgoal(subGoalOwner, subGoalName, parentGoalName);

           return ResponseEntity.ok().build();

        } catch (LearningGoalNotExistsException e) {
            throw new LearningGoalNotFoundException();
        } catch(IllegalArgumentException e){
            throw new BadRequestAlertException("Cannot construct cyclic sub-goal relation!", "learningGoalManagement", "cyclicSubGoalRelation");
        }
    }
    /**
     * {@code GET /learninggoals}
     *
     * <p>
     * Returns a list of all learning goals which are visible to the current user.
     * </p>
     *
     * @param showOnlyOwnGoals indicated whether only the user's goals should be displayed or all goals (from the query parameters)
     * @return {@link ResponseEntity} with status {@code 200 (OK)} and the list of visible learning goals in body
     */
    @GetMapping("/learninggoals")
    public ResponseEntity<Collection<LearningGoalDTO>> getVisibleGoals(
        @RequestParam(value = "showOnlyOwnGoals", required = false, defaultValue = "false") boolean showOnlyOwnGoals
    ) {
        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            var goals = sparqlEndpointService.getVisibleLearningGoalsForUser(currentLogin, showOnlyOwnGoals);
            return ResponseEntity.ok(goals);
        } catch (InternalModelException ex) {
            throw new BadRequestAlertException("An internal error occurred!", "learningGoalManagement", "parsingError");
        }
    }
}
