package at.jku.dke.etutor.repository;

import at.jku.dke.etutor.domain.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for the {@link Instructor} entity.
 *
 * @author fne
 */
@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {}
