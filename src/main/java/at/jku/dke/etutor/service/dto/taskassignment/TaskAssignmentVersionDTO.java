package at.jku.dke.etutor.service.dto.taskassignment;

import java.time.Instant;

public class TaskAssignmentVersionDTO{
    private Instant modificationDate;
    private String reasonOfChange;

    private TaskAssignmentDTO version;

    public TaskAssignmentVersionDTO(){

    }

    public Instant getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Instant modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getReasonOfChange() {
        return reasonOfChange;
    }

    public void setReasonOfChange(String reasonOfChange) {
        this.reasonOfChange = reasonOfChange;
    }

    public TaskAssignmentDTO getVersion() {
        return version;
    }

    public void setVersion(TaskAssignmentDTO version) {
        this.version = version;
    }
}
