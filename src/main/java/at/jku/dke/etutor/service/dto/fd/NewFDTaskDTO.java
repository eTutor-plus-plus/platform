package at.jku.dke.etutor.service.dto.fd;

public class NewFDTaskDTO {
    String taskGroupId;
    String fDSubtype;
    String [] fDClosureIds;

    public NewFDTaskDTO() {
    }

    public NewFDTaskDTO(String taskGroupId, String fDSubtype, String [] fDClosureIds) {
        this.taskGroupId = taskGroupId;
        this.fDSubtype = fDSubtype;
        this.fDClosureIds = fDClosureIds;
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
