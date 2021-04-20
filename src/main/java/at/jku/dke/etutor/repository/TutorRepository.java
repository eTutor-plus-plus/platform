package at.jku.dke.etutor.repository;

import at.jku.dke.etutor.domain.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for the {@link Tutor} entity.
 *
 * @author fne
 */
@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {}
