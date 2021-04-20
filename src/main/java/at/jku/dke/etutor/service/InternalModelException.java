package at.jku.dke.etutor.service;

/**
 * Validation exception which will be thrown if an internal model error occurs.
 *
 * @author fne
 */
public class InternalModelException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param cause the cause of the error
     */
    public InternalModelException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     */
    public InternalModelException() {}

    /**
     * Constructor.
     *
     * @param message the exception message
     */
    public InternalModelException(String message) {
        super(message);
    }
}
