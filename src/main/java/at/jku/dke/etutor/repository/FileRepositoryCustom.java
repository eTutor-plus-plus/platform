package at.jku.dke.etutor.repository;

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
}
