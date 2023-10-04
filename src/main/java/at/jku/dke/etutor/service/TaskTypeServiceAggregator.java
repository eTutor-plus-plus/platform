package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.objects.dispatcher.*;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;
import at.jku.dke.etutor.service.tasktypes.TaskGroupTypeService;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.service.exception.DispatcherRequestFailedException;
import at.jku.dke.etutor.service.exception.MissingParameterException;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import at.jku.dke.etutor.calc.exception.WrongCalcParametersException;
import at.jku.dke.etutor.service.tasktypes.implementation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Objects;

/**
 * Service that aggregates all task-type and task-group-type specific services.
 * {@link TaskTypeService} is the interface for all task-type specific services.
 * {@link TaskGroupTypeService} is the interface for all task-group-type specific services.
 */
@Service
@Transactional
public class TaskTypeServiceAggregator {
    private final XQueryService xQueryService;
    private final SqlService sqlService;
    private final DatalogService datalogService;
    private final CalcService calcService;
    private final ProcessMiningService processMiningService;
    private final BpmnService bpmnService;

    public TaskTypeServiceAggregator(XQueryService xQueryService,
                                     SqlService sqlService,
                                     DatalogService datalogService,
                                     CalcService calcService,
                                     ProcessMiningService processMiningService,
                                     BpmnService bpmnService) {
        this.sqlService = sqlService;
        this.calcService = calcService;
        this.processMiningService = processMiningService;
        this.bpmnService = bpmnService;
        this.xQueryService = xQueryService;
        this.datalogService = datalogService;
    }


    /**
     * Calls the task-group-type specific service to create a new task group.
     * If no task-group-type-specific service is available, ignores the request.
     * A call to this method may alter the passed object.
     * @param newTaskGroupDTO the new task group
     */
    public void createTaskGroup(NewTaskGroupDTO newTaskGroupDTO) throws TaskTypeSpecificOperationFailedException {
        var taskTypeService = getTaskTypeSpecificServiceForTaskGroupTypeId(newTaskGroupDTO.getTaskGroupTypeId());
        if(taskTypeService != null){
            taskTypeService.createTaskGroup(newTaskGroupDTO);
        }
    }

    /**
     * Calls the task-group-type specific service to update a task group.
     * If no task-group-type-specific service is available, ignores the request.
     * A call to this method may alter the passed object.
     * @param newTaskGroupDTO the new task group
     */
    public void updateTaskGroup(TaskGroupDTO newTaskGroupDTO) throws TaskTypeSpecificOperationFailedException {
        var taskTypeService = getTaskTypeSpecificServiceForTaskGroupTypeId(newTaskGroupDTO.getTaskGroupTypeId());
        if(taskTypeService != null){
            taskTypeService.updateTaskGroup(newTaskGroupDTO);
        }
    }

    /**
     * Calls the task-group-type specific service to delete a task group.
     * If no task-group-type-specific service is available, ignores the request.
     * @param taskGroupDTO the task group
     */
    public void deleteTaskGroup(TaskGroupDTO taskGroupDTO) throws TaskTypeSpecificOperationFailedException {
        Objects.requireNonNull(taskGroupDTO);

        var taskTypeService = getTaskTypeSpecificServiceForTaskGroupTypeId(taskGroupDTO.getTaskGroupTypeId());
        if(taskTypeService != null)
            taskTypeService.deleteTaskGroup(taskGroupDTO);
    }

    /**
     * Calls the task-group-type specific service to delete a task.
     * If task-type is not related to the dispatcher, ignores the request.
     * A call to this method may alter the passed object.
     * @param newTaskAssignmentDTO the task assignment
     */
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws TaskTypeSpecificOperationFailedException, NotAValidTaskGroupException, WrongCalcParametersException {
        Objects.requireNonNull(newTaskAssignmentDTO);

        var taskTypeService = getTaskTypeSpecificServiceForTaskAssignmentTypeId(newTaskAssignmentDTO.getTaskAssignmentTypeId());
        if(taskTypeService != null){
            taskTypeService.createTask(newTaskAssignmentDTO);
        }
    }


    /**
     * Updates an exercise in the dispatcher according to the task-type.
     * If task-type is not related to the dispatcher, ignores the request.
     * A call to this method may alter the passed object.
     * @param taskAssignmentDTO the task assignment to be updated
     */
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        Objects.requireNonNull(taskAssignmentDTO);

        var taskTypeService = getTaskTypeSpecificServiceForTaskAssignmentTypeId(taskAssignmentDTO.getTaskAssignmentTypeId());
        if(taskTypeService != null){
            taskTypeService.updateTask(taskAssignmentDTO);
        }
    }

    /**
     * Calls the task-type-specific service to delete a task.
     * If no service is available, ignores the request.
     * @param taskAssignmentDTO the task assignment to be deleted
     */
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        String taskType = taskAssignmentDTO.getTaskAssignmentTypeId();
        var taskTypeService = getTaskTypeSpecificServiceForTaskAssignmentTypeId(taskType);
        if(taskTypeService != null){
            taskTypeService.deleteTask(taskAssignmentDTO);
        }
    }

    /**
     * Returns the task-type-specific service for the given task assignment type id.
     * @param taskAssignmentTypId the task assignment type id
     * @return the task-type-specific service {@link TaskTypeService
     */
    private TaskTypeService getTaskTypeSpecificServiceForTaskAssignmentTypeId(String taskAssignmentTypId){
        if(ETutorVocabulary.XQueryTask.toString().equals(taskAssignmentTypId)) {
            return xQueryService;
        } else if(ETutorVocabulary.SQLTask.toString().equals(taskAssignmentTypId) ||
            ETutorVocabulary.RATask.toString().equals(taskAssignmentTypId)){
            return sqlService;
        } else if(ETutorVocabulary.DatalogTask.toString().equals(taskAssignmentTypId)){
            return datalogService;
        } else if(ETutorVocabulary.CalcTask.toString().equals(taskAssignmentTypId)){
            return calcService;
        } else if(ETutorVocabulary.PmTask.toString().equals(taskAssignmentTypId)){
            return processMiningService;
        } else if(ETutorVocabulary.BpmnTask.toString().equals(taskAssignmentTypId)){
            return bpmnService;
        }
        return null;
    }

    /**
     * Returns the task-group-type-specific service for the given task group type id.
     * @param taskGroupTypeId the task group type id
     * @return the task-group-type-specific service {@link TaskGroupTypeService}
     */
    private TaskGroupTypeService getTaskTypeSpecificServiceForTaskGroupTypeId(String taskGroupTypeId){
        if (ETutorVocabulary.XQueryTypeTaskGroup.toString().equals(taskGroupTypeId)) {
            return xQueryService;
        } else if (ETutorVocabulary.SQLTypeTaskGroup.toString().equals(taskGroupTypeId)){
            return sqlService;
        } else if(ETutorVocabulary.DatalogTypeTaskGroup.toString().equals(taskGroupTypeId)){
            return datalogService;
        }
        return null;
    }
}

