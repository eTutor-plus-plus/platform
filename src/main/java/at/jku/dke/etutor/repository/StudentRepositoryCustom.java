package at.jku.dke.etutor.repository;

import at.jku.dke.etutor.service.dto.courseinstance.StudentInfoDTO;

import java.util.List;

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
}
