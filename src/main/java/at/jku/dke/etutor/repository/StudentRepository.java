package at.jku.dke.etutor.repository;

import at.jku.dke.etutor.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for the {@link Student} entity.
 *
 * @author fne
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, StudentRepositoryCustom {}
