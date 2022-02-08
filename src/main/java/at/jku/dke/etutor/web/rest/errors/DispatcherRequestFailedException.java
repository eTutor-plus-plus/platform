package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Exception which indicates that a request that has been sent to the dispatcher failed
 *
 * @author ks
 */

public class DispatcherRequestFailedException extends BadRequestAlertException{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     */
    public DispatcherRequestFailedException(){
        super("The request that has been sent to the dispatcher failed", "taskManagement", "dispatcherRequestFailed");
    }

}
