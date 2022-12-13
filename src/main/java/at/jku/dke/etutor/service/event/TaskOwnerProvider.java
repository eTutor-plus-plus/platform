package at.jku.dke.etutor.service.event;

/**
 * Provider interface of a task owner.
 */
@FunctionalInterface
public interface TaskOwnerProvider {
    /**
     * Returns the provided task's owner.
     *
     * @return the provided task's owner
     */
    String getTaskOwner();
}
