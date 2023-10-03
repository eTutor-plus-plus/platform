package at.jku.dke.etutor.service.tasktypes;

import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
/**
 * Interface for task-types that are managed by other components.
 */
public interface TaskTypeService {
    void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException, NotAValidTaskGroupException;
    void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException;
    void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException;
}
