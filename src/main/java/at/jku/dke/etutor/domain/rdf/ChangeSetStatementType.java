package at.jku.dke.etutor.domain.rdf;

import org.apache.jena.rdf.model.Property;

public enum ChangeSetStatementType {
    AFTER(ETutorVocabulary.addition),
    BEFORE(ETutorVocabulary.removal);

    private final Property property;
    private ChangeSetStatementType(Property property){
        this.property = property;
    }

    public Property getProperty() {
        return property;
    }
}
