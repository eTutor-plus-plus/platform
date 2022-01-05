package at.jku.dke.etutor.service.dto.dispatcher;

import java.util.List;

public class DatalogExerciseDTO {
    private String solution;
    private List<String> queries;
    private List<DatalogTermDescriptionDTO> uncheckedTerms;
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
