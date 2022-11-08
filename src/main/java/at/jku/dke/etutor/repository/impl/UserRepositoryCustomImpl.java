package at.jku.dke.etutor.repository.impl;

import at.jku.dke.etutor.domain.User;
import at.jku.dke.etutor.repository.UserRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

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

    /**
     * Finds all users paged and uses the given filter.
     *
     * @param pageable the pageable
     * @param filter   the optional filter string
     * @return page of users
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> findAllPagedWithFilter(Pageable pageable, String filter) {
        TypedQuery<Long> countQuery = entityManager.createQuery("""
            SELECT COUNT(u) FROM User u
            WHERE u.login ILIKE :qry OR u.firstName ILIKE :qry or u.lastName ILIKE :qry
            ORDER BY u.id
            """, Long.class);
        TypedQuery<User> userQuery = entityManager.createQuery("""
            SELECT u FROM User u
            WHERE u.login ILIKE :qry OR u.firstName ILIKE :qry or u.lastName ILIKE :qry
            ORDER BY u.id
            """, User.class);

        if (pageable.isPaged()) {
            userQuery.setFirstResult(pageable.getPageSize() * pageable.getPageNumber());
            userQuery.setMaxResults(pageable.getPageSize());
        }

        countQuery.setParameter("qry", String.format("%%%s%%", filter));
        userQuery.setParameter("qry", String.format("%%%s%%", filter));

        long count = countQuery.getSingleResult();

        var list = userQuery.getResultList();

        return PageableExecutionUtils.getPage(list, pageable, () -> count);
    }
}
