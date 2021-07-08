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
}
