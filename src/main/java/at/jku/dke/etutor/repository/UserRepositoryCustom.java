package at.jku.dke.etutor.repository;

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
}
