package at.jku.dke.etutor.service.dto.courseinstance;

/**
 * DTO class which contains a student's first name, last name
 * and matriculation number.
 *
 * @author fne
 */
public class StudentInfoDTO {

    private String firstName;
    private String lastName;
    private String matriculationNumber;

    /**
     * Constructor.
     */
    public StudentInfoDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param firstName           the student's first name.
     * @param lastName            the student's last name
     * @param matriculationNumber the student's matriculation number
     */
    public StudentInfoDTO(String firstName, String lastName, String matriculationNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.matriculationNumber = matriculationNumber;
    }

    /**
     * Returns the first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     *
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name.
     *
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the matriculation number.
     *
     * @return the matriculation number
     */
    public String getMatriculationNumber() {
        return matriculationNumber;
    }

    /**
     * Sets the matriculation number.
     *
     * @param matriculationNumber the matriculation number to set
     */
    public void setMatriculationNumber(String matriculationNumber) {
        this.matriculationNumber = matriculationNumber;
    }
}
