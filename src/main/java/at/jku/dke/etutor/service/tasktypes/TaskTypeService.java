package at.jku.dke.etutor.service.tasktypes;

import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;

/**
 * Interface for services that offer task-type specific operations.
 */
public interface TaskTypeService {
    void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws TaskTypeSpecificOperationFailedException, NotAValidTaskGroupException;
    void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException;
    void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException;
}
