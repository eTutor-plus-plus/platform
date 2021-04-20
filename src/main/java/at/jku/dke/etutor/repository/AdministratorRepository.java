package at.jku.dke.etutor.repository;

import at.jku.dke.etutor.domain.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for the {@link Administrator} entity.
 *
 * @author fne
 */
@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {}
