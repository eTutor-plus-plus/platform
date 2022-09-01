package at.jku.dke.etutor.service;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.config.RDFConnectionTestConfiguration;
import at.jku.dke.etutor.service.exception.FileNotExistsException;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link UploadFileService}.
 *
 * @author fne
 */
@SpringBootTest(classes = EtutorPlusPlusApp.class)
@ContextConfiguration(classes = RDFConnectionTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class UploadFileServiceIT {

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SpringLiquibase springLiquibase;

    private String matriculationNumber;

    /**
     * Initializes the testing environment.
     *
     * @throws Exception must not be thrown
     */
    @BeforeAll
    public void init() throws Exception {
        springLiquibase.setDropFirst(true);
        springLiquibase.afterPropertiesSet();

        MultipartFile file = new MockMultipartFile(
            "file.csv",
            "file.csv",
            "text/csv",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("test_students.csv"))
        );

        var importedStudents = studentService.importStudentsFromFile(file);
        matriculationNumber = importedStudents.get(0).getMatriculationNumber();
    }

    /**
     * Tests the upload and get file methods.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Transactional
    public void testUploadAndGetFile() throws Exception {
        MultipartFile file = new MockMultipartFile(
            "testfile.pdf",
            "testfile.pdf",
            "application/pdf",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream("test_assignment.pdf"))
        );

        long id = uploadFileService.uploadFile(matriculationNumber, file, file.getName());

        assertThat(id).isGreaterThan(0);

        var fileFromService = uploadFileService.getFile(id);

        assertThat(fileFromService.getName()).isEqualTo(file.getName());
        assertThat(fileFromService.getSize()).isEqualTo(file.getSize());
    }

    /**
     * Tests the upload file method with an empty file name.
     *
     * @throws Exception must not be thrown
     */
    @Test
    @Transactional
    public void testUploadAndGetFileWithEmptyFileName() throws Exception {
        MultipartFile file = new MockMultipartFile(
            "testfile.pdf",
            "testfile.pdf",
            "application/pdf",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream(""))
        );

        long id = uploadFileService.uploadFile(matriculationNumber, file, file.getName());

        assertThat(id).isGreaterThan(0);

        var fileFromService = uploadFileService.getFile(id);
        assertThat(fileFromService.getName()).isEqualTo(file.getOriginalFilename());
    }

    /**
     * Tests the remove file method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testRemoveFile() throws Exception {
        MultipartFile file = new MockMultipartFile(
            "testfile.pdf",
            "testfile.pdf",
            "application/pdf",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream(""))
        );

        long id = uploadFileService.uploadFile(matriculationNumber, file, file.getName());

        assertThat(id).isGreaterThan(0);

        uploadFileService.removeFile(id);

        assertThatThrownBy(() -> uploadFileService.getFile(id))
            .isInstanceOf(FileNotExistsException.class);
    }

    /**
     * Tests the get file meta data method.
     *
     * @throws Exception must not be thrown
     */
    @Test
    public void testGetFileMetaData() throws Exception {
        MultipartFile file = new MockMultipartFile(
            "testfile.pdf",
            "testfile.pdf",
            "application/pdf",
            FileCopyUtils.copyToByteArray(getClass().getResourceAsStream(""))
        );

        long id = uploadFileService.uploadFile(matriculationNumber, file, file.getName());

        assertThat(id).isGreaterThan(0);

        var optionalMetaData = uploadFileService.getFileMetaData(id);

        assertThat(optionalMetaData).isNotEmpty();

        var metaData = optionalMetaData.get();

        assertThat(metaData.getFileName()).isEqualTo(file.getName());
        assertThat(metaData.getContentType()).isEqualTo(file.getContentType());
        assertThat(metaData.getSubmissionDate().toLocalDate()).isEqualTo(LocalDate.now());
    }

    /**
     * Tests the get file meta data method with a nonexistent file.
     */
    @Test
    public void testGetNonexistentFileMetaData() {
        var optionalMetaData = uploadFileService.getFileMetaData(Long.MAX_VALUE - 1);
        assertThat(optionalMetaData).isEmpty();
    }
}
