package at.jku.dke.etutor.service.dto.courseinstance;

/**
 * DTO class which is used to display course instance
 * overview information.
 *
 * @author fne
 */
public class DisplayableCourseInstanceDTO {

    private String id;
    private String name;
    private int studentCount;
    private int year;
    private String termId;

    /**
     * Constructor.
     *
     * @param id           the id
     * @param name         the name
     * @param studentCount the student count
     * @param year         the year
     * @param termId       the term id
     */
    public DisplayableCourseInstanceDTO(String id, String name, int studentCount, int year, String termId) {
        this.id = id;
        this.name = name;
        this.studentCount = studentCount;
        this.year = year;
        this.termId = termId;
    }

    /**
     * Empty constructor.
     */
    public DisplayableCourseInstanceDTO() {
        // Empty for serialization
    }

    /**
     * Returns the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the student count.
     *
     * @return the student count
     */
    public int getStudentCount() {
        return studentCount;
    }

    /**
     * Sets the student count.
     *
     * @param studentCount the student count to set
     */
    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
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
}
