package at.jku.dke.etutor.web.rest;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.StudentService;
import at.jku.dke.etutor.service.dto.FileMetaDataModelDTO;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the file resource.
 *
 * @author fne
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = {AuthoritiesConstants.ADMIN, AuthoritiesConstants.INSTRUCTOR}, username = "admin")
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileResourceIT {

    @Autowired
    private SpringLiquibase springLiquibase;

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private StudentService studentService;

    private String matriculationNumber;
    private MockMultipartFile file;
    private UserDetails user;

    /**
     * Inits the testing environment before all tests.
     *
     * @throws Exception must not be thrown
     */
    @BeforeAll
    public void initBeforeAllTests() throws Exception {
        springLiquibase.setDropFirst(true);
        springLiquibase.afterPropertiesSet();

        MultipartFile file = new MockMultipartFile(
            "file.csv",
            "file.csv",
            "text/csv",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("/at/jku/dke/etutor/service/test_students.csv"))
        );

        var importedStudents = studentService.importStudentsFromFile(file);
        matriculationNumber = importedStudents.get(0).getMatriculationNumber();

        user = TestUtil.generateTestUserDetails(matriculationNumber, AuthoritiesConstants.STUDENT);

        this.file = new MockMultipartFile(
            "file",
            "testfile.pdf",
            "application/pdf",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("/at/jku/dke/etutor/service/test_assignment.pdf"))
        );
    }

    /**
     * Tests the post file REST endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testPostFile() throws Exception {
        var fileId = uploadTestFile();

        assertThat(fileId).isGreaterThan(0);
    }

    /**
     * Tests the post file REST endpoint with an empty file.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testPostEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", (byte[]) null);

        restMockMvc.perform(multipart("/api/files")
            .file(emptyFile)
            .with(user(user)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the the post file endpoint with a nonexistent student.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @WithMockUser(value = "k567", authorities = AuthoritiesConstants.STUDENT)
    public void testPostFileWithInvalidStudent() throws Exception {
        restMockMvc.perform(multipart("/api/files")
            .file(file))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the retrieval of a file.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetFile() throws Exception {
        var fileId = uploadTestFile();

        var result = restMockMvc
            .perform(get("/api/files/{fileId}", fileId)
                .with(user(user)))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentLength()).isGreaterThan(0);
        assertThat(result.getResponse().getHeader("X-Filename")).isEqualTo(file.getOriginalFilename());
        assertThat(result.getResponse().getHeader("X-Content-Type")).isEqualTo(file.getContentType());
    }

    /**
     * Tests the retrieval of an invalid file.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetInvalidFile() throws Exception {
        long fileId = Long.MAX_VALUE - 1;

        restMockMvc
            .perform(get("/api/files/{fileId}", fileId)
                .with(user(user)))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the get file meta data REST endpoint.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetFileMetaData() throws Exception {
        var fileId = uploadTestFile();

        var result = restMockMvc.perform(get("/api/files/{fileId}/metadata", fileId)
            .with(user(user)))
            .andExpect(status().isOk())
            .andReturn();

        String jsonData = result.getResponse().getContentAsString();
        FileMetaDataModelDTO model = TestUtil.convertFromJSONString(jsonData, FileMetaDataModelDTO.class);
        LocalDate today = LocalDate.now();

        assertThat(model.getContentType()).isEqualTo(file.getContentType());
        assertThat(model.getFileName()).isEqualTo(file.getOriginalFilename());
        assertThat(model.getSubmissionDate().toLocalDate()).isEqualTo(today);
    }

    /**
     * Tests the get file meta data endpoint with a nonexistent id.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetInvalidFileMetaData() throws Exception {
        long fileId = Long.MAX_VALUE - 1;

        restMockMvc
            .perform(get("/api/files/{fileId}/metadata", fileId)
                .with(user(user)))
            .andExpect(status().isNotFound());
    }

    /**
     * Tests the removal of an uploaded file.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testDeleteUploadedFile() throws Exception {
        var fileId = uploadTestFile();

        restMockMvc.perform(delete("/api/files/{fileId}", fileId)
            .with(user(TestUtil.generateTestUserDetails(matriculationNumber, AuthoritiesConstants.STUDENT))))
            .andExpect(status().isNoContent());
    }

    /**
     * Uploads the test file.
     *
     * @return id of the uploaded test file
     * @throws Exception must not be thrown
     */
    private long uploadTestFile() throws Exception {
        var result = restMockMvc
            .perform(multipart("/api/files").file(file)
                .with(user(TestUtil.generateTestUserDetails(matriculationNumber, AuthoritiesConstants.STUDENT))))
            .andExpect(status().isOk())
            .andReturn();

        return Long.parseLong(result.getResponse().getContentAsString());
    }
}
