package at.jku.dke.etutor.web.rest.errors;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.startup.ApplicationReadyListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.bouncycastle.pqc.crypto.ExchangePair;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests {@link ExceptionTranslator} controller advice.
 */
@WithMockUser
@AutoConfigureMockMvc
@SpringBootTest(classes = EtutorPlusPlusApp.class)
public class ExceptionTranslatorIT {

    @MockBean
    private ApplicationReadyListener readyListener;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testConcurrencyFailure() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/concurrency-failure"))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value(ErrorConstants.ERR_CONCURRENCY_FAILURE));
    }

    @Test
    public void testMethodArgumentNotValid() throws Exception {
         mockMvc.perform(post("/api/exception-translator-test/method-argument").content("{}").contentType(MediaType.APPLICATION_JSON))
             .andExpect(status().isBadRequest())
             .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
             .andExpect(jsonPath("$.message").value(ErrorConstants.ERR_VALIDATION))
             .andExpect(jsonPath("$.fieldErrors.[0].objectName").value("test"))
             .andExpect(jsonPath("$.fieldErrors.[0].field").value("test"))
             .andExpect(jsonPath("$.fieldErrors.[0].message").value("NotNull"));
    }

    @Test
    public void testMissingServletRequestPartException() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/missing-servlet-request-part"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.http.400"));
    }

    @Test
    public void testMissingServletRequestParameterException() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/missing-servlet-request-parameter"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.http.400"));
    }

    @Test
    public void testAccessDenied() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/access-denied"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.http.403"))
            .andExpect(jsonPath("$.detail").value("test access denied!"));
    }

    @Test
    public void testUnauthorized() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/unauthorized"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.http.401"))
            .andExpect(jsonPath("$.path").value("/api/exception-translator-test/unauthorized"))
            .andExpect(jsonPath("$.detail").value("test authentication failed!"));
    }

    @Test
    public void testMethodNotSupported() throws Exception {
        mockMvc.perform(post("/api/exception-translator-test/access-denied"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.http.405"))
            .andExpect(jsonPath("$.detail").value("Request method 'POST' not supported"));
    }

    @Test
    public void testExceptionWithResponseStatus() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/response-status"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.http.400"))
            .andExpect(jsonPath("$.title").value("test response status"));
    }

    @Test
    public void testInternalServerError() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/internal-server-error"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.http.500"))
            .andExpect(jsonPath("$.title").value("Internal Server Error"));
    }

    @Test
    public void testEmailAlreadyUsed() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/email-already-used"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.emailexists"))
            .andExpect(jsonPath("$.title").value("Email is already in use!"));
    }

    @Test
    public void testUsernameAlreadyUsed() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/username-already-used"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.userexists"))
            .andExpect(jsonPath("$.title").value("Login name already used!"));
    }

    @Test
    public void testCollectionRequiredEntry() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/collection-required-entry"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.emptycollection"))
            .andExpect(jsonPath("$.title").value("At least one entry is required!"));
    }

    @Test
    public void testLoginPatternFailed() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/login-pattern-failed"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.loginPatternFailed"))
            .andExpect(jsonPath("$.title").value("The given login is not a valid JKU ak or matriculation number!"));
    }

    @Test
    public void testLearningGoalAlreadyExists() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/learning-goal-already-exists"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.learningGoalAlreadyExists"))
            .andExpect(jsonPath("$.title").value("The learning goal already exists!"));
    }

    @Test
    public void testLearningGoalNotFound() throws Exception {
        mockMvc.perform(get("/api/exception-translator-test/learning-goal-not-found"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.learningGoalNotFound"))
            .andExpect(jsonPath("$.title").value("The learning goal does not exist!"));
    }
}
