package at.jku.dke.etutor.repository;

import at.jku.dke.etutor.service.dto.FileMetaDataModelDTO;
import at.jku.dke.etutor.service.exception.FileNotExistsException;
import at.jku.dke.etutor.service.exception.StudentNotExistsException;

/**
 * ostgresql history table
 * Custom file repository.
 *
 * @author fne
 */
public interface FileRepositoryCustom {
    /**
     * Saves a file in the db.
     *
     * @param matriculationNumber the student's matriculation number
     * @param filename            the file name
     * @param contentType         the content type
     * @param content             the content
     * @param size                the file size
     * @return the id from the database
     * @throws StudentNotExistsException if the student does not exist
     */
    long uploadFile(String matriculationNumber, String filename, String contentType, byte[] content, long size) throws StudentNotExistsException;

    long uploadFile(String filename, String contentType, byte[] content, long size) ;

    /**
     * Retrieves the file meta data from a stored file.
     *
     * @param id the file's id
     * @return the meta data DTO object
     * @throws FileNotExistsException if the requested file does not exist
     */
    FileMetaDataModelDTO getMetaDataOfFile(long id) throws FileNotExistsException;
}
