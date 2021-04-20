package at.jku.dke.etutor.repository.impl;

import at.jku.dke.etutor.repository.StudentRepositoryCustom;
import at.jku.dke.etutor.security.AuthoritiesConstants;
import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import one.util.streamex.StreamEx;

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
        TypedQuery<StudentInfoDTO> qry = entityManager.createQuery(
            """
            SELECT new at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO(u.firstName, u.lastName, u.login) FROM User u, Authority a WHERE u.login IN (:matriculationNumbers)
            AND a.name = :studentAuthority AND a MEMBER OF u.authorities
            ORDER BY u.lastName, u.firstName
            """,
            StudentInfoDTO.class
        );
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
        TypedQuery<StudentInfoDTO> qry = entityManager.createQuery(
            """
            SELECT new at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO(u.firstName, u.lastName, u.login) FROM User u, Authority a WHERE a.name = :studentAuthority
            AND a MEMBER OF u.authorities
            ORDER BY u.lastName, u.firstName
            """,
            StudentInfoDTO.class
        );
        qry.setParameter("studentAuthority", AuthoritiesConstants.STUDENT);

        return qry.getResultList();
    }

    /**
     * Returns the student information map for the given
     * matriculation numbers.
     *
     * @param matriculationNumbers the list of matriculation numbers
     * @return the map of student info dtos (the matriculation number is the key)
     */
    @Override
    public Map<String, StudentInfoDTO> getStudentInfosAsMap(List<String> matriculationNumbers) {
        TypedQuery<StudentInfoDTO> qry = entityManager.createQuery(
            """
            SELECT new at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO(u.firstName, u.lastName, u.login) FROM User u, Authority a WHERE u.login IN (:matriculationNumbers)
            AND a.name = :studentAuthority AND a MEMBER OF u.authorities
            ORDER BY u.lastName, u.firstName
            """,
            StudentInfoDTO.class
        );
        qry.setParameter("matriculationNumbers", matriculationNumbers);
        qry.setParameter("studentAuthority", AuthoritiesConstants.STUDENT);

        return StreamEx.of(qry.getResultStream()).collect(Collectors.toMap(StudentInfoDTO::getMatriculationNumber, tuple -> tuple));
    }

    /**
     * Returns whether a student exists for the given matriculation number
     * or not.
     *
     * @param matriculationNumber the matriculation number to check
     * @return {@code true} if the student exists, otherwise {@code false}
     */
    @Override
    public boolean studentExists(String matriculationNumber) {
        TypedQuery<Long> query = entityManager.createQuery(
            """
            SELECT COUNT(u) FROM User u WHERE u.login = :matriculationNumber
            """,
            Long.class
        );
        query.setParameter("matriculationNumber", matriculationNumber);
        return query.getSingleResult() == 1;
    }
}
