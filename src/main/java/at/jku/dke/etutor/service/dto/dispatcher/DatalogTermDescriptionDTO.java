package at.jku.dke.etutor.service.dto.dispatcher;

import java.util.Objects;

public class DatalogTermDescriptionDTO {
    private String predicate;
    private String term;
    private String position;

    /**
     *
     */
    public DatalogTermDescriptionDTO() {
        this.predicate = "";
        this.term = "";
        this.position = "";
    }
    /**
     * @return Returns the position.
     */
    public String getPosition() {
        return position;
    }
    /**
     * @param position The position to set.
     */
    public void setPosition(String position) {
        this.position = position;
    }
    /**
     * @return Returns the predicate.
     */
    public String getPredicate() {
        return predicate;
    }
    /**
     * @param predicate The predicate to set.
     */
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }
    /**
     * @return Returns the term.
     */
    public String getTerm() {
        return term;
    }
    /**
     * @param term The term to set.
     */
    public void setTerm(String term) {
        this.term = term;
    }

    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append(predicate);
        s.append("(");
        for(int i = 1; i < Integer.parseInt(position); i++){
            s.append("_, ");
        }
        s.append(term).append(")");
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatalogTermDescriptionDTO that = (DatalogTermDescriptionDTO) o;
        return predicate.equals(that.predicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(predicate);
    }
}
