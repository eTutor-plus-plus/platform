package at.jku.dke.etutor.repository;

import at.jku.dke.etutor.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom user repository.
 *
 * @author fne
 */
public interface UserRepositoryCustom {
    /**
     * Removes all deactivated users.
     *
     * @return the count of removed users
     */
    int removeDeactivatedUsers();

    /**
     * Removes a user by its login.
     *
     * @param login the login
     */
    void removeByLogin(String login);

    /**
     * Finds all users paged and uses the given filter.
     *
     * @param pageable the pageable
     * @param filter the optional filter string
     * @return page of users
     */
    Page<User> findAllPagedWithFilter(Pageable pageable, String filter);
}
