package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskGroupDTO;
import at.jku.dke.etutor.service.exception.TaskTypeSpecificOperationFailedException;
import at.jku.dke.etutor.service.tasktypes.TaskGroupTypeService;
import at.jku.dke.etutor.service.tasktypes.TaskTypeService;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.service.exception.NotAValidTaskGroupException;
import at.jku.dke.etutor.service.tasktypes.implementation.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Service that aggregates all task-type and task-group-type specific services.
 * {@link TaskTypeService} is the interface for all task-type specific services.
 * {@link TaskGroupTypeService} is the interface for all task-group-type specific services.
 * To add a new task-type or task-group-type specific service, implement the corresponding interface and add it to {@link #getTaskTypeSpecificServiceForTaskAssignmentTypeId(String)}
 * or {@link #getTaskTypeSpecificServiceForTaskGroupTypeId(String)}.
 */
@Service
@Primary
public class TaskTypeServiceDelegate implements TaskTypeService, TaskGroupTypeService {
    private final XQueryService xQueryService;
    private final SqlService sqlService;
    private final DatalogService datalogService;
    private final CalcService calcService;
    private final ProcessMiningService processMiningService;
    private final BpmnService bpmnService;
    private final DroolsService droolsService;

    public TaskTypeServiceDelegate(XQueryService xQueryService,
                                   SqlService sqlService,
                                   DatalogService datalogService,
                                   CalcService calcService,
                                   ProcessMiningService processMiningService,
                                   BpmnService bpmnService,
                                   DroolsService droolsService) {
        this.sqlService = sqlService;
        this.calcService = calcService;
        this.processMiningService = processMiningService;
        this.bpmnService = bpmnService;
        this.xQueryService = xQueryService;
        this.datalogService = datalogService;
        this.droolsService = droolsService;
    }


    /**
     * Calls the task-group-type specific service to create a new task group.
     * If no task-group-type-specific service is available, ignores the request.
     * A call to this method may alter the passed object.
     * @param newTaskGroupDTO the new task group
     */
    @Override
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
     * @param taskGroupDTO the new task group
     */
    @Override
    public void updateTaskGroup(TaskGroupDTO taskGroupDTO) throws TaskTypeSpecificOperationFailedException {
        var taskTypeService = getTaskTypeSpecificServiceForTaskGroupTypeId(taskGroupDTO.getTaskGroupTypeId());
        if(taskTypeService != null){
            taskTypeService.updateTaskGroup(taskGroupDTO);
        }
    }

    /**
     * Calls the task-group-type specific service to delete a task group.
     * If no task-group-type-specific service is available, ignores the request.
     * @param taskGroupDTO the task group
     */
    @Override
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
    @Override
    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws TaskTypeSpecificOperationFailedException, NotAValidTaskGroupException{
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
    @Override
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
    @Override
    public void deleteTask(TaskAssignmentDTO taskAssignmentDTO) throws TaskTypeSpecificOperationFailedException {
        String taskType = taskAssignmentDTO.getTaskAssignmentTypeId();
        var taskTypeService = getTaskTypeSpecificServiceForTaskAssignmentTypeId(taskType);
        if(taskTypeService != null){
            taskTypeService.deleteTask(taskAssignmentDTO);
        }
    }

    // private region

//    private <T extends NewTaskAssignmentDTO> void executeTaskTypeSpecificOperation(NewTaskAssignmentDTO taskAssignmentDTO, BiConsumer<TaskTypeService, NewTaskAssignmentDTO> methodCall, Function<NewTaskAssignmentDTO,? extends NewTaskAssignmentDTO> typeCastFunction){
//        Objects.requireNonNull(taskAssignmentDTO);
//
//        var taskTypeService = getTaskTypeSpecificServiceForTaskAssignmentTypeId(taskAssignmentDTO.getTaskAssignmentTypeId());
//        if(taskTypeService != null){
//            methodCall.accept(taskTypeService, taskAssignmentDTO);
//        }
//    }
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
        } else if(ETutorVocabulary.DroolsTask.toString().equals(taskAssignmentTypId)) {
            return droolsService;
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

