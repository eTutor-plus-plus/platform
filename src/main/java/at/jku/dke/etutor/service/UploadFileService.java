package at.jku.dke.etutor.service;

import at.jku.dke.etutor.calc.functions.CreateRandomInstruction;
import at.jku.dke.etutor.calc.functions.DecodeMultipartFile;
import at.jku.dke.etutor.domain.FileEntity;
import at.jku.dke.etutor.repository.FileRepository;
import at.jku.dke.etutor.service.dto.FileMetaDataModelDTO;
import at.jku.dke.etutor.service.exception.FileNotExistsException;
import at.jku.dke.etutor.service.exception.StudentNotExistsException;
import liquibase.pro.packaged.B;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResizableByteArrayOutputStream;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

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

    @Transactional
    public long createRandomCalcFileInstruction (Long file_id) throws Exception {
         FileEntity file_old = fileRepository.getById(file_id);
//         try {
             InputStream stream_old = new ByteArrayInputStream(file_old.getContent());
             XSSFWorkbook workbook_old = new XSSFWorkbook(stream_old);
             XSSFWorkbook workbook_new = CreateRandomInstruction.createRandomInstruction(workbook_old);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             workbook_new.write(bos);
//             FileEntity file_new = new FileEntity();
//             file_new.setContent(bos.toByteArray());
//             file_new.setName(file_old.getName() + " Random Instruction");
//             file_new.setContentType(file_old.getContentType());
//             file_new.setSize(bos.toByteArray().length);
//             file_new.setStudent(file_old.getStudent());
//             fileRepository.save(file_new);
             MultipartFile file_new = new DecodeMultipartFile(bos.toByteArray());

             return fileRepository.uploadFile(file_old.getName() + " randomised", file_old.getContentType(), file_new.getBytes(), file_new.getSize());
//
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
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
