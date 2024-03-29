package at.jku.dke.etutor.service.exception;

import java.io.Serial;

/**
 * Exception which indicates that a request that has been sent to the dispatcher failed
 */
public class DispatcherRequestFailedException extends TaskTypeSpecificOperationFailedException{
    @Serial
    private static final long serialVersionUID = 1L;

    public DispatcherRequestFailedException(String message) {
        super(message);
    }
}
