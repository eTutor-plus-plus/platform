package at.jku.dke.etutor.calc.exception;

import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;

import java.io.Serial;

/**
 * Exception which indicates that with the uploaded calc files it is not possible to create a calc task
 */
public class WrongCalcParametersException extends TaskTypeSpecificOperationFailedException {
        @Serial
    private static final long serialVersionUID = 1L;

    public WrongCalcParametersException(){
        super("Wrong calc parameters");
    }
}
