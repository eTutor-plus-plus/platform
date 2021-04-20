package at.jku.dke.etutor.service.dto.courseinstance;

import at.jku.dke.etutor.service.dto.validation.TermConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * DTO class for a new course instance.
 *
 * @author fne
 */
public class NewCourseInstanceDTO {

    @NotBlank
    private String courseId;

    @Min(2021)
    private int year;

    @TermConstraint
    private String termId;

    private String description;

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
     * Returns the year.
     *
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets the year.
     *
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Returns the term id.
     *
     * @return the term id
     */
    public String getTermId() {
        return termId;
    }

    /**
     * Sets the term id.
     *
     * @param termId the term id to set
     */
    public void setTermId(String termId) {
        this.termId = termId;
    }

    /**
     * Returns the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
