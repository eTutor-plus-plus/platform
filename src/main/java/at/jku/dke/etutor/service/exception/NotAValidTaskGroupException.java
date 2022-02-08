package at.jku.dke.etutor.service.exception;

import java.io.Serial;

/**
 * Exception which indicates that a task group has been chosen for an assignment which does not fit (e.g. a NoType task group for an SQL task)
 */
public class NotAValidTaskGroupException extends Exception{
    @Serial
    private static final long serialVersionUID = 1L;
}
