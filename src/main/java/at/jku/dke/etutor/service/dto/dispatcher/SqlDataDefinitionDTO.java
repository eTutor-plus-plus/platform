package at.jku.dke.etutor.service.dto.dispatcher;

import java.util.List;

public class SqlDataDefinitionDTO {
    private List<String> createStatements;
    private List<String> insertStatementsSubmission;
    private List<String> insertStatementsDiagnose;
    String schemaName;

    public SqlDataDefinitionDTO(){

    }

    public List<String> getCreateStatements() {
        return createStatements;
    }

    public void setCreateStatements(List<String> createStatements) {
        this.createStatements = createStatements;
    }

    public List<String> getInsertStatementsSubmission() {
        return insertStatementsSubmission;
    }

    public void setInsertStatementsSubmission(List<String> insertStatementsSubmission) {
        this.insertStatementsSubmission = insertStatementsSubmission;
    }

    public List<String> getInsertStatementsDiagnose() {
        return insertStatementsDiagnose;
    }

    public void setInsertStatementsDiagnose(List<String> insertStatementsDiagnose) {
        this.insertStatementsDiagnose = insertStatementsDiagnose;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}
