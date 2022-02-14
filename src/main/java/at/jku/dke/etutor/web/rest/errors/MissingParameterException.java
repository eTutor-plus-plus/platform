package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Exception which indicates that not enough parameters have been provided to execute the operation (dispatcher)
 */
public class MissingParameterException extends BadRequestAlertException{
    @Serial
    private static final long serialVersionUID = 1L;

    public MissingParameterException(){
        super("Not enought parameter provided", "taskManagement", "missingParameter");
    }
}
