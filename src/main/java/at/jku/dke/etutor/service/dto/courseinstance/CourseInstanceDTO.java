package at.jku.dke.etutor.service.dto.courseinstance;

import java.util.List;

/**
 * DTO class for an existing course instance which
 * includes the names and matriculation numbers of its
 * students.
 *
 * @author fne
 */
public class CourseInstanceDTO {

    private int year;
    private String termId;
    private String description;
    private String id;
    private List<StudentInfoDTO> students;
    private String courseName;
    private String instanceName;

    /**
     * Constructor.
     */
    public CourseInstanceDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param year         the year
     * @param termId       the term id
     * @param description  the description
     * @param id           the internal id
     * @param students     the list of students
     * @param courseName   the name of the underlying course
     * @param instanceName the instance name
     */
    public CourseInstanceDTO(
        int year,
        String termId,
        String description,
        String id,
        List<StudentInfoDTO> students,
        String courseName,
        String instanceName
    ) {
        this.year = year;
        this.termId = termId;
        this.description = description;
        this.id = id;
        this.students = students;
        this.courseName = courseName;
        this.instanceName = instanceName;
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

    /**
     * Returns the internal id.
     *
     * @return the internal id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the internal id.
     *
     * @param id the internal id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the list of students.
     *
     * @return the list of students
     */
    public List<StudentInfoDTO> getStudents() {
        return students;
    }

    /**
     * Sets the list of students.
     *
     * @param students the list of students
     */
    public void setStudents(List<StudentInfoDTO> students) {
        this.students = students;
    }

    /**
     * Returns the course name.
     *
     * @return the course name
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Sets the course name.
     *
     * @param courseName the course name to set
     */
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    /**
     * Returns the instance name.
     *
     * @return the instance name
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Sets the instance name.
     *
     * @param instanceName the instance name to set
     */
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
