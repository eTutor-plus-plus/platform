package at.jku.dke.etutor.service.dto.dispatcher;

import java.util.Objects;

/**
 * Represents a datalog term.
 * Instances are used in {@link DatalogExerciseDTO} to exclude certain terms from manipulation.
 * (Normally, if a student submits a datalog exercise, the facts are manipulated to avoid
 * that students declare facts instead of deriving them using rules. If the rules that represent the
 * solution contain certain terms, these terms have to be excluded from manipulation)
 */
public class DatalogTermDescriptionDTO {
    /**
     * The predicate of the term
     */
    private String predicate;
    /**
     * The value of the term
     */
    private String term;
    /**
     * The position in the predicate of the term
     */
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
