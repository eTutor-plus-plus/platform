package at.jku.dke.etutor.calc.exception;

import java.io.Serial;

/**
 * Exception which indicates that with the uploaded calc files it is not possible to create a calc task
 */
public class WrongCalcParametersException extends Exception{
        @Serial
    private static final long serialVersionUID = 1L;

    public WrongCalcParametersException(){}
}
