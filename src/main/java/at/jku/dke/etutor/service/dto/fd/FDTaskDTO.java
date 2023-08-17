package at.jku.dke.etutor.service.dto.fd;

import at.jku.dke.etutor.service.dto.taskassignment.TaskAssignmentDTO;

public class FDTaskDTO {
    String taskId;
    String taskGroupId;
    String fDSubtype;
    String [] fDClosureIds;

    public FDTaskDTO() {
    }

    public FDTaskDTO(TaskAssignmentDTO taskAssignmentDTO) {
        this.taskId = taskAssignmentDTO.getTaskIdForDispatcher();
        this.taskGroupId = taskAssignmentDTO.getTaskGroupId();
        this.fDSubtype = taskAssignmentDTO.getfDSubtype();
        this.fDClosureIds = taskAssignmentDTO.getfDClosureIds();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskGroupId() {
        return taskGroupId;
    }

    public void setTaskGroupId(String taskGroupId) {
        this.taskGroupId = taskGroupId;
    }

    public String getfDSubtype() {
        return fDSubtype;
    }

    public void setfDSubtype(String fDSubtype) {
        this.fDSubtype = fDSubtype;
    }

    public String[] getfDClosureIds() {
        return fDClosureIds;
    }

    public void setfDClosureIds(String[] fDClosureIds) {
        this.fDClosureIds = fDClosureIds;
    }
}
