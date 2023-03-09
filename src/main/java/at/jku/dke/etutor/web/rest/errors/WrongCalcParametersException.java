package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

/**
 * Exception which indicates that with the uploaded calc files it is not possible to create a calc task
 */
public class WrongCalcParametersException extends BadRequestAlertException {
        @Serial
    private static final long serialVersionUID = 1L;

    public WrongCalcParametersException(){
        super("Calc Parameters not valid", "taskManagement", "wrongCalcFiles");
    }
}
