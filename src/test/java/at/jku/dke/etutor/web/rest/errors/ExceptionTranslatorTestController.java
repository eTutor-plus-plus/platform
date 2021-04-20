package at.jku.dke.etutor.web.rest.errors;

import at.jku.dke.etutor.service.UsernameAlreadyUsedException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exception-translator-test")
public class ExceptionTranslatorTestController {

    @GetMapping("/concurrency-failure")
    public void concurrencyFailure() {
        throw new ConcurrencyFailureException("test concurrency failure");
    }

    @PostMapping("/method-argument")
    public void methodArgument(@Valid @RequestBody TestDTO testDTO) {}

    @GetMapping("/missing-servlet-request-part")
    public void missingServletRequestPartException(@RequestPart String part) {}

    @GetMapping("/missing-servlet-request-parameter")
    public void missingServletRequestParameterException(@RequestParam String param) {}

    @GetMapping("/access-denied")
    public void accessdenied() {
        throw new AccessDeniedException("test access denied!");
    }

    @GetMapping("/unauthorized")
    public void unauthorized() {
        throw new BadCredentialsException("test authentication failed!");
    }

    @GetMapping("/response-status")
    public void exceptionWithResponseStatus() {
        throw new TestResponseStatusException();
    }

    @GetMapping("/internal-server-error")
    public void internalServerError() {
        throw new RuntimeException();
    }

    @GetMapping("/email-already-used")
    public void emailAlreadyUsed() {
        throw new EmailAlreadyUsedException();
    }

    @GetMapping("/username-already-used")
    public void usernameAlreadyUsed() {
        throw new UsernameAlreadyUsedException();
    }

    @GetMapping("/collection-required-entry")
    public void collectionRequiredEntry() {
        throw new CollectionRequiredEntryException();
    }

    @GetMapping("/login-pattern-failed")
    public void loginPatternFailed() {
        throw new LoginPatternFailedException();
    }

    @GetMapping("/learning-goal-already-exists")
    public void learningGoalAlreadyExists() {
        throw new LearningGoalAlreadyExistsException();
    }

    @GetMapping("/learning-goal-not-found")
    public void learningGoalNotFound() {
        throw new LearningGoalNotFoundException();
    }

    @GetMapping("/private-super-goal")
    public void privateSuperGoal() {
        throw new PrivateSuperGoalException();
    }

    @GetMapping("/course-already-exists")
    public void courseAlreadyExists() {
        throw new CourseAlreadyExistsException();
    }

    @GetMapping("/course-not-found")
    public void courseNotFound() {
        throw new CourseNotFoundException();
    }

    @GetMapping("/learning-goal-assignment-already-exists")
    public void learningGoalAssignmentAlreadyExists() {
        throw new LearningGoalAssignmentAlreadyExistsException();
    }

    @GetMapping("/learning-goal-assignment-non-existent")
    public void learningGoalAssignmentNonExistent() {
        throw new LearningGoalAssignmentNonExistentException();
    }

    @GetMapping("/task-assignment-non-existent")
    public void taskAssignmentNonExistent() {
        throw new TaskAssignmentNonexistentException();
    }

    public static class TestDTO {

        @NotNull
        private String test;

        public String getTest() {
            return test;
        }

        public void setTest(String test) {
            this.test = test;
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "test response status")
    @SuppressWarnings("serial")
    public static class TestResponseStatusException extends RuntimeException {}
}
