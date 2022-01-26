package at.jku.dke.etutor.service.dto.dispatcher;

public class DatalogTaskGroupDTO {
    private String name;
    private String facts;

    public DatalogTaskGroupDTO(){
        // empty for serialization
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFacts() {
        return facts;
    }

    public void setFacts(String facts) {
        this.facts = facts;
    }
}
