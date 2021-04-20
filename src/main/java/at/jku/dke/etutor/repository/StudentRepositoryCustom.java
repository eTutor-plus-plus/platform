package at.jku.dke.etutor.repository;

import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;
import java.util.List;
import java.util.Map;

/**
 * Custom student repository.
 *
 * @author fne
 */
public interface StudentRepositoryCustom {
    /**
     * Returns the list of student information for the given
     * matriculation numbers.
     *
     * @param matriculationNumbers the matriculation numbers
     * @return list of student info dtos
     */
    List<StudentInfoDTO> getStudentInfos(List<String> matriculationNumbers);

    /**
     * Returns the list of available students.
     *
     * @return the list of available students
     */
    List<StudentInfoDTO> getAvailableStudentInfos();

    /**
     * Returns the student information map for the given
     * matriculation numbers.
     *
     * @param matriculationNumbers the list of matriculation numbers
     * @return the map of student info dtos (the matriculation number is the key)
     */
    Map<String, StudentInfoDTO> getStudentInfosAsMap(List<String> matriculationNumbers);

    /**
     * Returns whether a student exists for the given matriculation number
     * or not.
     *
     * @param matriculationNumber the matriculation number to check
     * @return {@code true} if the student exists, otherwise {@code false}
     */
    boolean studentExists(String matriculationNumber);
}
