package at.jku.dke.etutor.service.dto.student;

import java.time.Instant;

public class IndividualTaskSubmissionDTO {
    private Instant instant;
    private String submission;
    private boolean isSubmitted;
    private boolean hasBeenSolved;
    private int dispatcherId;
    private String taskType;

    public IndividualTaskSubmissionDTO(){}

    public IndividualTaskSubmissionDTO(Instant instant, String submission, boolean isSubmitted, boolean hasBeenSolved, int dispatcherId, String taskType){
        this.instant=instant;
        this.submission=submission;
        this.hasBeenSolved=hasBeenSolved;
        this.isSubmitted=isSubmitted;
        this.dispatcherId=dispatcherId;
        this.taskType=taskType;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public String getSubmission() {
        return submission;
    }

    public void setSubmission(String submission) {
        this.submission = submission;
    }

    public boolean isIsSubmitted() {
        return isSubmitted;
    }

    public void setHasBeenSubmitted(boolean hasBeenSubmitted) {
        this.isSubmitted = hasBeenSubmitted;
    }

    public boolean isHasBeenSolved() {
        return hasBeenSolved;
    }

    public void setHasBeenSolved(boolean hasBeenSolved) {
        this.hasBeenSolved = hasBeenSolved;
    }

    public int getDispatcherId() {
        return dispatcherId;
    }

    public void setDispatcherId(int dispatcherId) {
        this.dispatcherId = dispatcherId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
}
