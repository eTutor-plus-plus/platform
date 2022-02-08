package at.jku.dke.etutor.web.rest.errors;

import java.io.Serial;

public class NotAValidTaskGroupException extends BadRequestAlertException {
    @Serial
    private static final long serialVersionUID = 1L;

    public NotAValidTaskGroupException(){
        super("Task group not valid", "taskManagement", "notAValidTaskGroup");
    }
}
