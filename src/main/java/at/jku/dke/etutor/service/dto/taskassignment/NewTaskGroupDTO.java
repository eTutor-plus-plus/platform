package at.jku.dke.etutor.service.dto.taskassignment;

import at.jku.dke.etutor.service.dto.validation.TaskGroupTypeConstraint;

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
    @NotBlank
    @TaskGroupTypeConstraint
    private String taskGroupTypeId;
    private String sqlCreateStatements;
    private String sqlInsertStatementsSubmission;
    private String sqlInsertStatementsDiagnose;
    private String xQueryDiagnoseXML;
    private String xQuerySubmissionXML;
    private String fileUrl;
    private String datalogFacts;

    /**
     * Constructor.
     *
     * @param name        the mandatory name
     * @param description the optional description
     */
    public NewTaskGroupDTO(String name, String description,
                           String taskGroupTypeId,
                           String sqlCreateStatements,
                           String sqlInsertStatementsSubmission,
                           String sqlInsertStatementsDiagnose,
                           String xQueryDiagnoseXML,
                           String xQuerySubmissionXML,
                           String datalogFacts) {
        this.name = name;
        this.description = description;
        this.taskGroupTypeId = taskGroupTypeId;
        this.sqlCreateStatements =sqlCreateStatements;
        this.sqlInsertStatementsSubmission=sqlInsertStatementsSubmission;
        this.sqlInsertStatementsDiagnose=sqlInsertStatementsDiagnose;
        this.xQueryDiagnoseXML=xQueryDiagnoseXML;
        this.xQuerySubmissionXML=xQuerySubmissionXML;
        this.datalogFacts=datalogFacts;
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
     * @param sqlCreateStatements the statements
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
     * @return the statements
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

    /**
     * Returns the XML for diagnose xquery submissions
     * @return the XML-String
     */
    public String getxQueryDiagnoseXML() {
        return xQueryDiagnoseXML;
    }

    /**
     * Sets the XML for diagnose xquery submissions
     * @param xQueryDiagnoseXML the XML-String
     */
    public void setxQueryDiagnoseXML(String xQueryDiagnoseXML) {
        this.xQueryDiagnoseXML = xQueryDiagnoseXML;
    }

    /**
     * Returns the XML for submitted xquery submissions
     * @return the XML-String
     */
    public String getxQuerySubmissionXML() {
        return xQuerySubmissionXML;
    }

    /**
     * Sets the XML for submitted xquery submissions
     * @param xQuerySubmissionXML the XML-String
     */
    public void setxQuerySubmissionXML(String xQuerySubmissionXML) {
        this.xQuerySubmissionXML = xQuerySubmissionXML;
    }

    /**
     * Returns the task group type
     * @return the task group type
     */
    public String getTaskGroupTypeId() {
        return taskGroupTypeId;
    }

    /**
     * Sets the task group type
     * @param taskGroupTypeId the type
     */
    public void setTaskGroupTypeId(String taskGroupTypeId) {
        this.taskGroupTypeId = taskGroupTypeId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getDatalogFacts() {
        return datalogFacts;
    }

    public void setDatalogFacts(String datalogFacts) {
        this.datalogFacts = datalogFacts;
    }
}
