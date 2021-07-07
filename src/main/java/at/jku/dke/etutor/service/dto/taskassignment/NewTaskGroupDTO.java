package at.jku.dke.etutor.service.dto.taskassignment;

import javax.validation.constraints.NotBlank;

/**
 * DTO class for a new task group.
 *
 * @author fne
 */
public class NewTaskGroupDTO {

    @NotBlank
    private String name;
    private String description;
    private String sqlCreateStatements;
    private String sqlInsertStatementsSubmission;
    private String sqlInsertStatementsDiagnose;

    /**
     * Constructor.
     *
     * @param name        the mandatory name
     * @param description the optional description
     */
    public NewTaskGroupDTO(String name, String description,
                           String sqlCreateStatements,
                           String sqlInsertStatementsSubmission,
                           String sqlInsertStatementsDiagnose) {
        this.name = name;
        this.description = description;
        this.sqlCreateStatements =sqlCreateStatements;
        this.sqlInsertStatementsSubmission=sqlInsertStatementsSubmission;
        this.sqlInsertStatementsDiagnose=sqlInsertStatementsDiagnose;
    }

    /**
     * Empty constructor.
     */
    public NewTaskGroupDTO() {
        // Empty for serialization
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
     * Returns the SQL- Create Table Statements
     * @return the statements
     */
    public String getSqlCreateStatements() {
        return sqlCreateStatements;
    }

    /**
     * Sets the SQL- Create Table Statements
     * @param sqlCreateStatements
     */
    public void setSqlCreateStatements(String sqlCreateStatements) {
        this.sqlCreateStatements = sqlCreateStatements;
    }

    /**
     * Returns the SQL- Insert Statements for the submission schema
     * @return the statements
     */
    public String getSqlInsertStatementsSubmission() {
        return sqlInsertStatementsSubmission;
    }

    /**
     * Sets the SQL- Insert Statements for the submission schema
     * @param sqlInsertStatementsSubmission
     */
    public void setSqlInsertStatementsSubmission(String sqlInsertStatementsSubmission) {
        this.sqlInsertStatementsSubmission = sqlInsertStatementsSubmission;
    }

    /**
     * Returns the SQL- Insert Statements for the diagnose schema
     * @return
     */
    public String getSqlInsertStatementsDiagnose() {
        return sqlInsertStatementsDiagnose;
    }

    /**
     * Sets the SQL- Insert Statements for the diagnose schema
     * @param sqlInsertStatementsDiagnose
     */
    public void setSqlInsertStatementsDiagnose(String sqlInsertStatementsDiagnose) {
        this.sqlInsertStatementsDiagnose = sqlInsertStatementsDiagnose;
    }
}
