package at.jku.dke.etutor.web.rest.vm;

import java.util.List;
import javax.validation.constraints.NotBlank;

/**
 * View model for setting the students of a
 * course instance.
 *
 * @author fne
 */
public class CourseInstanceStudentsVM {

    @NotBlank
    private String courseInstanceId;

    private List<String> matriculationNumbers;

    /**
     * Returns the course instance id.
     *
     * @return the course instance id
     */
    public String getCourseInstanceId() {
        return courseInstanceId;
    }

    /**
     * Sets the course instance id.
     *
     * @param courseInstanceId the course instance id to set
     */
    public void setCourseInstanceId(String courseInstanceId) {
        this.courseInstanceId = courseInstanceId;
    }

    /**
     * Returns the list of matriculation numbers.
     *
     * @return the list of matriculation numbers
     */
    public List<String> getMatriculationNumbers() {
        return matriculationNumbers;
    }

    /**
     * Sets the list of matriculation numbers.
     *
     * @param matriculationNumbers the list of matriculation numbers to set
     */
    public void setMatriculationNumbers(List<String> matriculationNumbers) {
        this.matriculationNumbers = matriculationNumbers;
    }
}
