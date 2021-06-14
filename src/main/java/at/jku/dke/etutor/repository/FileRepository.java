package at.jku.dke.etutor.repository;

import at.jku.dke.etutor.domain.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for the {@link FileEntity} class.
 *
 * @author fne
 */
@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long>, FileRepositoryCustom {
}
