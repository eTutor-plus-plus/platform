package at.jku.dke.etutor.repository.impl;

import at.jku.dke.etutor.repository.StudentRepositoryCustom;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Implements the custom student repository functions.
 *
 * @author fne
 */
@SuppressWarnings("unused")
public class StudentRepositoryCustomImpl implements StudentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Returns the list of student information for the given
     * matriculation numbers.
     *
     * @param matriculationNumbers the matriculation numbers
     * @return list of student info dtos
     */
    @Override
    public List<StudentInfoDTO> getStudentInfos(List<String> matriculationNumbers) {
        TypedQuery<StudentInfoDTO> qry = entityManager.createQuery("""
            SELECT new at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO(u.firstName, u.lastName, u.login) FROM User u, Authority a WHERE u.login IN (:matriculationNumbers)
            AND a.name = :studentAuthority AND a MEMBER OF u.authorities
            ORDER BY u.lastName, u.firstName
            """, StudentInfoDTO.class);
        qry.setParameter("matriculationNumbers", matriculationNumbers);
        qry.setParameter("studentAuthority", AuthoritiesConstants.STUDENT);

        return qry.getResultList();
    }

    /**
     * Returns the list of available students.
     *
     * @return the list of available students
     */
    @Override
    public List<StudentInfoDTO> getAvailableStudentInfos() {
        TypedQuery<StudentInfoDTO> qry = entityManager.createQuery("""
            SELECT new at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO(u.firstName, u.lastName, u.login) FROM User u, Authority a WHERE a.name = :studentAuthority
            AND a MEMBER OF u.authorities
            ORDER BY u.lastName, u.firstName
            """, StudentInfoDTO.class);
        qry.setParameter("studentAuthority", AuthoritiesConstants.STUDENT);

        return qry.getResultList();
    }
}
