package at.jku.dke.etutor.repository;

import at.jku.dke.etutor.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String>, AuthorityRepositoryCustom {}
