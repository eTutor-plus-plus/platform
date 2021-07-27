package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.FileEntity;
import at.jku.dke.etutor.repository.FileRepository;
import at.jku.dke.etutor.service.dto.FileMetaDataModelDTO;
import at.jku.dke.etutor.service.exception.FileNotExistsException;
import at.jku.dke.etutor.service.exception.StudentNotExistsException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
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
     * Returns a stored file.
     *
     * @param fileId the internal file id
     * @return file entity
     * @throws FileNotExistsException if the file does not exist
     */
    @Transactional(readOnly = true)
    public FileEntity getFile(long fileId) throws FileNotExistsException {
        try {
            return fileRepository.getOne(fileId);
        } catch (EntityNotFoundException | JpaObjectRetrievalFailureException ex) {
            throw new FileNotExistsException();
        }
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
