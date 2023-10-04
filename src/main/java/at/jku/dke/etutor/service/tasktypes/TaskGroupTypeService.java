package at.jku.dke.etutor.service.tasktypes;

import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;
import org.springframework.scheduling.config.Task;

/**
 * Interface for services that offer task-group-type specific operations.
 */
public interface TaskGroupTypeService {
    void createTaskGroup(NewTaskGroupDTO newTaskGroupDTO) throws TaskTypeSpecificOperationFailedException;
    void updateTaskGroup(TaskGroupDTO taskGroupDTO) throws TaskTypeSpecificOperationFailedException;
    void deleteTaskGroup(TaskGroupDTO taskGroupDTO) throws TaskTypeSpecificOperationFailedException;
}
