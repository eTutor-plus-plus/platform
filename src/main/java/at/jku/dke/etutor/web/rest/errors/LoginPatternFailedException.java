package at.jku.dke.etutor.web.rest.errors;

/**
 * Validation exception which will be thrown if a login name doesn't match the
 * login pattern.
 *
 * @author fne
 */
public class LoginPatternFailedException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public LoginPatternFailedException() {
        super("The given login is not a valid JKU ak or matriculation number!", "validation", "loginPatternFailed");
    }
}
