package at.jku.dke.etutor.service.dto.courseinstance;

/**
 * DTO class which contains the course instance information
 *
 * @author fne
 */
public class CourseInstanceInformationDTO {
    private String courseName;
    private String termId;
    private String instructor;
    private String instanceId;
    private int year;

    /**
     * Constructor.
     */
    public CourseInstanceInformationDTO() {
        // Empty for serialization.
    }

    /**
     * Constructor.
     *
     * @param courseName the course name
     * @param termId     the term id
     * @param instructor the instructor's name
     * @param instanceId the instance id
     * @param year       the year
     */
    public CourseInstanceInformationDTO(String courseName, String termId, String instructor, String instanceId, int year) {
        this.courseName = courseName;
        this.termId = termId;
        this.instructor = instructor;
        this.instanceId = instanceId;
        this.year = year;
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
     * Returns the instructor.
     *
     * @return the instructor
     */
    public String getInstructor() {
        return instructor;
    }

    /**
     * Sets the instructor.
     *
     * @param instructor the instructor to set
     */
    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    /**
     * Returns the instance id.
     *
     * @return the instance id
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Sets the instance id.
     *
     * @param instanceId the instance id to set
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
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
}
