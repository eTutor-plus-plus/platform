package at.jku.dke.etutor.service.dto.dispatcher;

import java.util.List;

/**
 * DTO representing a datalog exercise
 */
public class DatalogExerciseDTO {
    /**
     * The rules that represent the solution
     */
    private String solution;
    /**
     * The queries that are executed and which results are compared to evaluate the correctness
     */
    private List<String> queries;
    /**
     * A list of {@link DatalogTermDescriptionDTO} that have to be excluded from manipulation
     */
    private List<DatalogTermDescriptionDTO> uncheckedTerms;
    /**
     * The id of the facts
     */
    private int factsId;

    public DatalogExerciseDTO(){
        //empty for serialization
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public List<String> getQueries() {
        return queries;
    }

    public void setQueries(List<String> queries) {
        this.queries = queries;
    }

    public List<DatalogTermDescriptionDTO> getUncheckedTerms() {
        return uncheckedTerms;
    }

    public void setUncheckedTerms(List<DatalogTermDescriptionDTO> uncheckedTerms) {
        this.uncheckedTerms = uncheckedTerms;
    }

    public int getFactsId() {
        return factsId;
    }

    public void setFactsId(int factsId) {
        this.factsId = factsId;
    }
}
