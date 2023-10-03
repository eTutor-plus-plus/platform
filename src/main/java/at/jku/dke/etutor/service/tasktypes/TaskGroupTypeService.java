package at.jku.dke.etutor.service.tasktypes;

import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;

/**
 * Interface for task-group-types that are managed by other components.
 */
public interface TaskGroupTypeService {
    void createOrUpdateTaskGroup(TaskGroupDTO newTaskGroupDTO, boolean isNew) throws MissingParameterException, DispatcherRequestFailedException;
    void deleteTaskGroup(TaskGroupDTO taskGroupDTO) throws DispatcherRequestFailedException;
}
