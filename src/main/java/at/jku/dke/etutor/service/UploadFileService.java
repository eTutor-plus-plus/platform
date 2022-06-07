package at.jku.dke.etutor.service;

import at.jku.dke.etutor.calc.functions.CalcCorrection;
import at.jku.dke.etutor.calc.functions.CreateRandomInstruction;
import at.jku.dke.etutor.calc.functions.DecodeMultipartFile;
import at.jku.dke.etutor.domain.FileEntity;
import at.jku.dke.etutor.repository.FileRepository;
import at.jku.dke.etutor.service.dto.FileMetaDataModelDTO;
import at.jku.dke.etutor.service.exception.FileNotExistsException;
import at.jku.dke.etutor.service.exception.StudentNotExistsException;
import io.swagger.models.auth.In;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for upload file related operations.
 *
 * @author fne
 */
@Service
public class UploadFileService {

    private final FileRepository fileRepository;

    /**
     * Constructor.
     *
     * @param fileRepository the injected file repository
     */
    public UploadFileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    /**
     * Uploads a file.
     *
     * @param matriculationNumber the matriculation number
     * @param file                the multipart file to update
     * @param fileName            the optional file name
     * @return the internal file id
     * @throws StudentNotExistsException if the student does not exist
     * @throws IOException               if a file related error occurs
     */
    @Transactional
    public long uploadFile(String matriculationNumber, MultipartFile file, String fileName) throws StudentNotExistsException, IOException {
        Objects.requireNonNull(matriculationNumber);
        Objects.requireNonNull(file);

        String fileNameStr = fileName;

        if (ObjectUtils.isEmpty(fileNameStr)) {
            fileNameStr = file.getOriginalFilename();
        }
        String name = StringUtils.cleanPath(Objects.requireNonNull(fileNameStr));
        return fileRepository.uploadFile(matriculationNumber, name, file.getContentType(),
            file.getBytes(), file.getSize());
    }

    /**
     * Creates a randomised instruction and return the file id
     *
     * @param file_id id of the task's instruction file
     * @param login login of the student
     * @return the id of the generated instruction
     */
    @Transactional
    public long createRandomCalcFileInstruction (Long file_id, String login) throws Exception {
         FileEntity file_old = fileRepository.getById(file_id);
         InputStream stream_old = new ByteArrayInputStream(file_old.getContent());
         XSSFWorkbook workbook_old = new XSSFWorkbook(stream_old);
         XSSFWorkbook workbook_new = CreateRandomInstruction.createRandomInstruction(workbook_old);
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         workbook_new.write(bos);
         MultipartFile file_new = new DecodeMultipartFile(bos.toByteArray());
         String new_filename = file_old.getName().substring(0, file_old.getName().lastIndexOf('.'))
             + "_"
             + login
             + file_old.getName().substring(file_old.getName().lastIndexOf('.'));

         return fileRepository.uploadFile(new_filename , file_old.getContentType(), file_new.getBytes(), file_new.getSize());
    }


    /**
     * Returns a stored file.
     *
     * @param fileId the internal file id
     * @return file entity
     * @throws FileNotExistsException if the file does not exist
     */
    @Transactional(readOnly = true)
    public FileEntity getFile(long fileId) throws FileNotExistsException {
        Optional<FileEntity> file = fileRepository.findById(fileId);

        if (file.isPresent()) {
            return file.get();
        }
        throw new FileNotExistsException();
    }

    /**
     * Deletes a file from the database.
     *
     * @param fileId the internal file id
     */
    @Transactional
    public void removeFile(long fileId) {
        fileRepository.deleteById(fileId);
    }

    /**
     * Retrieves the file meta data model.
     *
     * @param fileId the file id
     * @return {@code Optional} containing the {@link FileMetaDataModelDTO}
     */
    public Optional<FileMetaDataModelDTO> getFileMetaData(long fileId) {
        try {
            return Optional.of(fileRepository.getMetaDataOfFile(fileId));
        } catch (FileNotExistsException e) {
            return Optional.empty();
        }
    }
}
