package at.jku.dke.etutor.service.dto.dispatcher;

import java.util.List;

public class XQueryExerciseDTO {
    private String query;
    private List<String> sortedNodes;

    public XQueryExerciseDTO(){

    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getSortedNodes() {
        return sortedNodes;
    }

    public void setSortedNodes(List<String> sortedNodes) {
        this.sortedNodes = sortedNodes;
    }
}
