package at.jku.dke.etutor.service.exception;

import java.io.Serial;

/**
 * Exception which indicates that not enough parameters have been provided to execute the request (dispatcher)
 */
public class MissingParameterException extends TaskTypeSpecificOperationFailedException{
    @Serial
    private static final long serialVersionUID = 1L;

    public MissingParameterException(String message){
        super(message);
    }
}
