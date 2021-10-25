package at.jku.dke.etutor.service;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.dto.dispatcher.*;
import at.jku.dke.etutor.service.dto.taskassignment.NewTaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;
import at.jku.dke.etutor.service.dto.taskassignment.TaskGroupDTO;
import at.jku.dke.etutor.web.rest.DispatcherProxyResource;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
//TODO: logging
//TODO: documentation
@Service
public class DispatcherProxyService {
    private final AssignmentSPARQLEndpointService assignmentSPARQLEndpointService;
    private final DispatcherProxyResource proxyResource;
    private final ObjectMapper mapper;
    private final Logger LOGGER;

    public DispatcherProxyService(AssignmentSPARQLEndpointService assignmentSPARQLEndpointService, DispatcherProxyResource proxyResource){
        this.assignmentSPARQLEndpointService = assignmentSPARQLEndpointService;
        this.proxyResource = proxyResource;
        this.mapper = new ObjectMapper();
        LOGGER = (Logger) LoggerFactory.getLogger(DispatcherProxyService.class);
    }

    /**
     * Returns the DispatcherSubmission for a given id
     * @param UUID the UUID
     * @return the submission
     * @throws JsonProcessingException if the returned value cannot be deserialized
     */
    public DispatcherSubmissionDTO getSubmission(String UUID) throws JsonProcessingException {
        return  mapper.readValue(proxyResource.getSubmission(UUID).getBody(), DispatcherSubmissionDTO.class);
    }

    /**
     * Returns the grading for a given id
     * @param UUID the UUID
     * @return the grading
     * @throws JsonProcessingException if the returned value cannot be parsed
     */
    public DispatcherGradingDTO getGrading(String UUID) throws JsonProcessingException {
        return mapper.readValue(proxyResource.getGrading(UUID).getBody(), DispatcherGradingDTO.class);
    }
    /**
     * Sends the request to add the xml-files for an xquery task group to the DispatcherProxyResource
     * @param taskGroupDTO the task group
     */
    public void proxyXMLtoDispatcher(TaskGroupDTO taskGroupDTO) {
        Objects.requireNonNull(taskGroupDTO.getId());
        Objects.requireNonNull(taskGroupDTO.getxQueryDiagnoseXML());
        Objects.requireNonNull(taskGroupDTO.getxQuerySubmissionXML());

        String diagnoseXML = taskGroupDTO.getxQueryDiagnoseXML();
        String submisisonXML = taskGroupDTO.getxQuerySubmissionXML();
        DispatcherXMLDTO body = new DispatcherXMLDTO(diagnoseXML,submisisonXML);
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }
        var response = proxyResource.addXMLForXQTaskGroup(taskGroupDTO.getName(), jsonBody);
        var fileURL = response.getBody();
        assignmentSPARQLEndpointService.addXMLFileURL(taskGroupDTO, fileURL);
    }

    /**
     * Utility method that triggers deletion of task-group related resources by the dispatcher
     * @param taskGroupDTO  the task group
     */
    public void deleteDispatcherResourcesForTaskGroup(TaskGroupDTO taskGroupDTO) {
        Objects.requireNonNull(taskGroupDTO);
        Objects.requireNonNull(taskGroupDTO.getTaskGroupTypeId());
        Objects.requireNonNull(taskGroupDTO.getName());

        if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())){
            proxyResource.deleteXMLofXQTaskGroup(taskGroupDTO.getName());
        }else if(taskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())){
            proxyResource.deleteSQLSchema(taskGroupDTO.getName());
            proxyResource.deleteSQLConnection(taskGroupDTO.getName());
        }
    }

    public int createXQueryTask(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        Objects.requireNonNull(newTaskAssignmentDTO.getxQuerySolution());

        List<String> sortings = new ArrayList<>();
        String dtoSorting = newTaskAssignmentDTO.getxQueryXPathSorting();
        if(dtoSorting != null) sortings.add(dtoSorting);

        XQueryExerciseDTO body = new XQueryExerciseDTO();
        body.setQuery(newTaskAssignmentDTO.getxQuerySolution());
        body.setSortedNodes(sortings);

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return -1;
        }
        var response = proxyResource.createXQExercise(newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1), jsonBody);
        return response.getBody();

    }

    /**
     * Returns exercise information for a given id
     * @param taskIdForDispatcher the task id
     * @return an XQueryExerciseDTO
     */
    public XQueryExerciseDTO getXQExerciseInfo(String taskIdForDispatcher) throws JsonProcessingException {
        return mapper.readValue(proxyResource.getXQExerciseInfo(Integer.parseInt(taskIdForDispatcher)).getBody(), XQueryExerciseDTO.class);
    }

    private String updateXQExercise(TaskAssignmentDTO taskAssignmentDTO)  {
        List<String> sortings = new ArrayList<>();
        String dtoSorting = taskAssignmentDTO.getxQueryXPathSorting();
        if(dtoSorting != null) sortings.add(dtoSorting);

        XQueryExerciseDTO body = new XQueryExerciseDTO();
        body.setQuery(taskAssignmentDTO.getxQuerySolution());
        body.setSortedNodes(sortings);

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";

        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return proxyResource.updateXQExercise(Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher()), jsonBody).getBody();

    }

    public String deleteTaskAssignment(TaskAssignmentDTO taskAssignmentDTO) {
        String taskType = taskAssignmentDTO.getTaskAssignmentTypeId();
        int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());

        if(taskType.equals(ETutorVocabulary.NoType) || taskType.equals(ETutorVocabulary.UploadTask)) return "";
        if(taskType.equals(ETutorVocabulary.XQueryTask.toString())){
            return proxyResource.deleteXQExercise(id).getBody();
        }else if(taskType.equals(ETutorVocabulary.SQLTask.toString())){
           return proxyResource.deleteSQLExercise(id).getBody();
        }

        return "";
    }

    public String getXMLForXQ(String taskGroup){
        return proxyResource.getXMLForXQByTaskGroup(taskGroup).getBody();
    }

    public String getXMLForXQ(int fileId){
        return proxyResource.getXMLForXQByFileId(fileId).getBody();
    }

    public void createTask(NewTaskAssignmentDTO newTaskAssignmentDTO) throws JsonProcessingException {
        Objects.requireNonNull(newTaskAssignmentDTO);
        Objects.requireNonNull(newTaskAssignmentDTO.getTaskAssignmentTypeId());

        if(newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())){
            if(newTaskAssignmentDTO.getTaskIdForDispatcher() == null){
                int id = this.createXQueryTask(newTaskAssignmentDTO);
                if (id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id +"");
            }else{
                XQueryExerciseDTO e = this.getXQExerciseInfo(newTaskAssignmentDTO.getTaskIdForDispatcher());
                newTaskAssignmentDTO.setxQuerySolution(e.getQuery());
                if(!e.getSortedNodes().isEmpty())newTaskAssignmentDTO.setxQueryXPathSorting(e.getSortedNodes().get(0));
            }
        }else if(newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) || newTaskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString())){
            if(newTaskAssignmentDTO.getTaskIdForDispatcher() == null){
                int id = this.createSQLTask(newTaskAssignmentDTO);
                if(id != -1) newTaskAssignmentDTO.setTaskIdForDispatcher(id+"");
            }else{
                String solution = fetchSQLSolution(newTaskAssignmentDTO.getTaskIdForDispatcher());
                newTaskAssignmentDTO.setSqlSolution(solution);
            }
        }
    }

    private String fetchSQLSolution(String taskIdForDispatcher) {
        return proxyResource.getSQLSolution(Integer.parseInt(taskIdForDispatcher)).getBody();

    }

    private int createSQLTask(NewTaskAssignmentDTO newTaskAssignmentDTO) {
        Objects.requireNonNull(newTaskAssignmentDTO.getSqlSolution());
        Objects.requireNonNull(newTaskAssignmentDTO.getTaskGroupId());

        String solution = newTaskAssignmentDTO.getSqlSolution();
        String taskGroup = newTaskAssignmentDTO.getTaskGroupId().substring(newTaskAssignmentDTO.getTaskGroupId().indexOf("#")+1);
        return Integer.parseInt(proxyResource.createSQLExercise(solution, taskGroup).getBody());
    }

    public void updateTask(TaskAssignmentDTO taskAssignmentDTO) {
        Objects.requireNonNull(taskAssignmentDTO);
        Objects.requireNonNull(taskAssignmentDTO.getTaskAssignmentTypeId());

        if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.XQueryTask.toString())){
            if(StringUtils.isNotBlank(taskAssignmentDTO.getxQuerySolution())
                && StringUtils.isNotBlank(taskAssignmentDTO.getTaskIdForDispatcher())){
                updateXQExercise(taskAssignmentDTO);
            }
        }else if(taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.SQLTask.toString()) || taskAssignmentDTO.getTaskAssignmentTypeId().equals(ETutorVocabulary.RATask.toString())){
            if(StringUtils.isNotBlank(taskAssignmentDTO.getSqlSolution()) && StringUtils.isNotBlank(taskAssignmentDTO.getTaskIdForDispatcher())){
                updateSQLExercise(taskAssignmentDTO);
            }
        }
    }

    private void updateSQLExercise(TaskAssignmentDTO taskAssignmentDTO) {
        String solution = taskAssignmentDTO.getSqlSolution();
        int id = Integer.parseInt(taskAssignmentDTO.getTaskIdForDispatcher());
        proxyResource.updateSQLExerciseSolution(id, solution);
    }

    public void createTaskGroup(TaskGroupDTO newTaskGroupDTO, String token, HttpServletRequest request) {
        if(newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.XQueryTypeTaskGroup.toString())){
           proxyXMLtoDispatcher(newTaskGroupDTO);
        }else if(newTaskGroupDTO.getTaskGroupTypeId().equals(ETutorVocabulary.SQLTypeTaskGroup.toString())){
            createSQLTaskGroup(newTaskGroupDTO);
        }
    }

    private void createSQLTaskGroup(TaskGroupDTO newTaskGroupDTO) {
        Objects.requireNonNull(newTaskGroupDTO.getSqlCreateStatements());
        Objects.requireNonNull(newTaskGroupDTO.getSqlInsertStatementsDiagnose());
        Objects.requireNonNull(newTaskGroupDTO.getSqlInsertStatementsSubmission());

        SqlDataDefinitionDTO body = new SqlDataDefinitionDTO();
        body.setCreateStatements(Arrays.stream(newTaskGroupDTO.getSqlCreateStatements().trim().split(";")).toList());
        body.setInsertStatementsDiagnose(Arrays.stream(newTaskGroupDTO.getSqlInsertStatementsDiagnose().trim().split(";")).toList());
        body.setInsertStatementsSubmission(Arrays.stream(newTaskGroupDTO.getSqlInsertStatementsSubmission().trim().split(";")).toList());
        body.setSchemaName(newTaskGroupDTO.getName());

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = "";

        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        proxyResource.executeDDLForSQL(jsonBody);

    }
}
