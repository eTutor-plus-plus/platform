package at.jku.dke.etutor.web.rest.vm;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * View model for setting the students of a
 * course instance.
 *
 * @author fne
 */
public class CourseInstanceStudentsVM {
    @NotBlank
    private String courseId;
    private List<String> matriculationNumbers;

    /**
     * Returns the course id.
     *
     * @return the course id
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * Sets the course id.
     *
     * @param courseId the course id to set
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
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
