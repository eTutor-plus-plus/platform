package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.objects.dispatcher.*;
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
     * Adds task group related resources to the dispatcher.
     * If task-group-type is not related to the dispatcher, ignores the request.
     *
     * @param newTaskGroupDTO the new task group
     */
    public void createOrUpdateTaskGroup(TaskGroupDTO newTaskGroupDTO, boolean isNew) throws DispatcherRequestFailedException, MissingParameterException {
        var taskTypeService = getTaskTypeSpecificServiceForTaskGroupTypeId(newTaskGroupDTO.getTaskGroupTypeId());
        if(taskTypeService != null){
            taskTypeService.createOrUpdateTaskGroup(newTaskGroupDTO, isNew);
        }
    }


    /**
     * Triggers the deletion of task-group related resources by the dispatcher
     * If task-group-type is not related to the dispatcher, ignores the request.
     * @param taskGroupDTO the task group
     */
    public void deleteTaskGroup(TaskGroupDTO taskGroupDTO) throws DispatcherRequestFailedException {
        Objects.requireNonNull(taskGroupDTO);

        var taskTypeService = getTaskTypeSpecificServiceForTaskGroupTypeId(taskGroupDTO.getTaskGroupTypeId());
        if(taskTypeService != null)
            taskTypeService.deleteTaskGroup(taskGroupDTO);
    }

    /**
     * Handles the Dispatcher-related tasks for the creation of a new task assignment
     * If task-type is not related to the dispatcher, ignores the request.
     * A call to this method may alter the passed object.
     * @param newTaskAssignmentDTO the task assignment
     */
    public NewTaskAssignmentDTO createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws MissingParameterException, NotAValidTaskGroupException, DispatcherRequestFailedException, WrongCalcParametersException {
        Objects.requireNonNull(newTaskAssignmentDTO);

        var taskTypeService = getTaskTypeSpecificServiceForTaskAssignmentTypeId(newTaskAssignmentDTO.getTaskAssignmentTypeId());
        if(taskTypeService != null){
            taskTypeService.createTask(newTaskAssignmentDTO);
        }

        return newTaskAssignmentDTO;
    }


    /**
     * Updates an exercise in the dispatcher according to the task-type.
     * If task-type is not related to the dispatcher, ignores the request.
     *
     * @param taskAssignmentDTO the task assignment to be updated
     */
    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) throws MissingParameterException, DispatcherRequestFailedException {
        Objects.requireNonNull(taskAssignmentDTO);

        var taskTypeService = getTaskTypeSpecificServiceForTaskAssignmentTypeId(taskAssignmentDTO.getTaskAssignmentTypeId());
        if(taskTypeService != null){
            taskTypeService.updateTask(taskAssignmentDTO);
        }
    }

    /**
     * Deletes a task assignment (exercise) in the dispatcher according to the task-type
     * If task-type is not related to the dispatcher, ignores the request.
     * @param taskAssignmentDTO the task assignment to be deleted
     */
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws DispatcherRequestFailedException {
        String taskType = taskAssignmentDTO.getTaskAssignmentTypeId();
        var taskTypeService = getTaskTypeSpecificServiceForTaskAssignmentTypeId(taskType);
        if(taskTypeService != null){
            taskTypeService.deleteTask(taskAssignmentDTO);
        }
    }


    private TaskTypeService getTaskTypeSpecificServiceForTaskAssignmentTypeId(String taskAssignmentTypId){
        if(ETutorVocabulary.XQueryTask.toString().equals(taskAssignmentTypId)) {
            return xQueryService;
        } else if(ETutorVocabulary.SQLTask.toString().equals(taskAssignmentTypId)){
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

