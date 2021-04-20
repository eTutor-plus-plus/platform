package at.jku.dke.etutor.repository;

import java.util.List;

/**
 * Custom repository for the {@link at.jku.dke.etutor.domain.Authority} entity
 * which contains custom queries.
 *
 * @author fne
 */
public interface AuthorityRepositoryCustom {
    /**
     * Returns the authorities (without the user one)
     * for the client to display.
     *
     * @return the authorities as string
     */
    List<String> getClientAuthorities();
}
