package at.jku.dke.etutor.repository.impl;

import at.jku.dke.etutor.domain.FileEntity;
import at.jku.dke.etutor.domain.Student;
import at.jku.dke.etutor.repository.FileRepositoryCustom;
import at.jku.dke.etutor.service.dto.FileMetaDataModelDTO;
import at.jku.dke.etutor.service.exception.FileNotExistsException;
import at.jku.dke.etutor.service.exception.StudentNotExistsException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * Implements the the custom file repository's functions.
 *
 * @author fne
 */
@SuppressWarnings("unused")
public class FileRepositoryCustomImpl implements FileRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

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
    @Override
    public long uploadFile(String matriculationNumber, String filename, String contentType,
                           byte[] content, long size) throws StudentNotExistsException {
        TypedQuery<Student> studentQry = entityManager.createQuery("""
            SELECT s FROM Student s WHERE s.user.login = :matriculationNo
            """, Student.class);
        studentQry.setParameter("matriculationNo", matriculationNumber);

        Student student;
        try {
            student = studentQry.getSingleResult();
        } catch (NoResultException nre) {
            throw new StudentNotExistsException();
        }
        FileEntity fileEntity = new FileEntity();
        fileEntity.setContent(content);
        fileEntity.setContentType(contentType);
        fileEntity.setName(filename);
        fileEntity.setSize(size);
        fileEntity.setStudent(student);

        entityManager.persist(fileEntity);

        return fileEntity.getId();
    }

    @Override
    public long uploadFile(String filename, String contentType,
                           byte[] content, long size) {

        FileEntity fileEntity = new FileEntity();
        fileEntity.setContent(content);
        fileEntity.setContentType(contentType);
        fileEntity.setName(filename);
        fileEntity.setSize(size);

        entityManager.persist(fileEntity);

        return fileEntity.getId();
    }

    /**
     * Retrieves the file meta data from a stored file.
     *
     * @param id the file's id
     * @return the meta data DTO object
     * @throws FileNotExistsException if the requested file does not exist
     */
    @Override
    public FileMetaDataModelDTO getMetaDataOfFile(long id) throws FileNotExistsException {
        TypedQuery<FileMetaDataModelDTO> query = entityManager.createQuery("""
            SELECT new at.jku.dke.etutor.service.dto.FileMetaDataModelDTO(
            fe.name, fe.contentType, fe.submitTime)
            FROM FileEntity fe WHERE fe.id = :id
            """, FileMetaDataModelDTO.class);
        query.setParameter("id", id);

        try {
            return query.getSingleResult();
        } catch (NoResultException nre) {
            throw new FileNotExistsException();
        }
    }
}
