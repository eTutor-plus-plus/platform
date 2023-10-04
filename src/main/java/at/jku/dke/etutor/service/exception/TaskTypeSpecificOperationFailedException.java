package at.jku.dke.etutor.service.exception;

public class TaskTypeSpecificOperationFailedException extends Exception{
    public TaskTypeSpecificOperationFailedException(String message) {
        super(message);
    }
}
