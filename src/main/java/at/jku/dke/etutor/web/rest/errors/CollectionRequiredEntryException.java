package at.jku.dke.etutor.web.rest.errors;

/**
 * Validation exception which will be thrown if a collection which must contain at least one
 * entry does not contain an entry.
 *
 * @author fne
 */
public class CollectionRequiredEntryException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public CollectionRequiredEntryException() {
        super("At least one entry is required!", "userManagement", "emptycollection");
    }
}
