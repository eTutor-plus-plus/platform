package at.jku.dke.etutor.repository.impl;

import at.jku.dke.etutor.repository.AuthorityRepositoryCustom;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * Implementation of the custom authority repository.
 *
 * @author fne
 */
public class AuthorityRepositoryCustomImpl implements AuthorityRepositoryCustom {

    private static final String QRY_CLIENT_AUTHORITIES = "SELECT a.name FROM Authority a WHERE a.name <> ?1";

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * See {@link AuthorityRepositoryCustom#getClientAuthorities()}
     */
    @Override
    public List<String> getClientAuthorities() {
        TypedQuery<String> query = entityManager.createQuery(QRY_CLIENT_AUTHORITIES, String.class);
        query.setParameter(1, AuthoritiesConstants.USER);
        return query.getResultList();
    }
}
