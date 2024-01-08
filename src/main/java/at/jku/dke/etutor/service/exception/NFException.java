package at.jku.dke.etutor.service.exception;

public class NFException extends TaskTypeSpecificOperationFailedException {
    public NFException(String message) {
        super(message);
    }
}
