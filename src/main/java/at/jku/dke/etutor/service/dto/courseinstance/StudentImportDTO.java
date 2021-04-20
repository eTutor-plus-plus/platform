package at.jku.dke.etutor.service.dto.courseinstance;

/**
 * DTO class which used in the student CSV import tool.
 */
public class StudentImportDTO extends StudentInfoDTO {

    private String email;

    /**
     * Empty constructor. For serialization.
     */
    public StudentImportDTO() {
        super();
    }

    /**
     * Constructor.
     *
     * @param firstName           the student's first name.
     * @param lastName            the student's last name
     * @param matriculationNumber the student's matriculation number
     * @param email               the student's email address
     */
    public StudentImportDTO(String firstName, String lastName, String matriculationNumber, String email) {
        super(firstName, lastName, matriculationNumber);
        this.email = email;
    }

    /**
     * Returns the student's mail address.
     *
     * @return the mail address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the mail address.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
}
