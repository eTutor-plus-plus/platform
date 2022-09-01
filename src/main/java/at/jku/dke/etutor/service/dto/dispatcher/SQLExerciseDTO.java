package at.jku.dke.etutor.service.dto.dispatcher;

public class SQLExerciseDTO {
    private String schemaName;
    private String solution;

    public SQLExerciseDTO(){

    }

    public SQLExerciseDTO(String schemaName, String solution){
        this.schemaName=schemaName;
        this.solution=solution;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }
}
