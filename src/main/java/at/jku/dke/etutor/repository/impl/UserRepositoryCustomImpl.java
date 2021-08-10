package at.jku.dke.etutor.repository.impl;

import at.jku.dke.etutor.repository.UserRepositoryCustom;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Implements the custom user repository functions.
 *
 * @author fne
 */
@SuppressWarnings("unused")
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Removes all deactivated users.
     *
     * @return the count of removed users
     */
    @Transactional
    @Override
    public int removeDeactivatedUsers() {
        Query personRemovalQuery = entityManager.createQuery("""
            DELETE FROM Person p WHERE p.user.id IN (
            SELECT u.id FROM User u WHERE
            u.activated = FALSE)
            """);

        personRemovalQuery.executeUpdate();

        Query updateQuery = entityManager.createQuery("""
            DELETE FROM User u WHERE u.activated = FALSE
            """);

        return updateQuery.executeUpdate();
    }

    /**
     * Removes a user by its login.
     *
     * @param login the login
     */
    @Override
    @Transactional
    public void removeByLogin(String login) {
        Query removalQry = entityManager.createQuery("""
            DELETE FROM User u WHERE u.login = :login
            """);
        removalQry.setParameter("login", login);
        removalQry.executeUpdate();
    }
}
