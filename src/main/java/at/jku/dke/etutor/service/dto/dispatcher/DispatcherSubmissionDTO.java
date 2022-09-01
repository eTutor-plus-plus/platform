package at.jku.dke.etutor.service.dto.dispatcher;

import java.util.Map;

public class DispatcherSubmissionDTO {
    private String submissionId;
    private String taskType;
    private int exerciseId;
    private Map<String, String> passedAttributes;
    private Map<String, String> passedParameters;
    private int maxPoints;

    public DispatcherSubmissionDTO() {
        // empty constructor for json-serialization
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Map<String, String> getPassedAttributes() {
        return passedAttributes;
    }

    public void setPassedAttributes(Map<String, String> passedAttributes) {
        this.passedAttributes = passedAttributes;
    }

    public Map<String, String> getPassedParameters() {
        return passedParameters;
    }

    public void setPassedParameters(Map<String, String> passedParameters) {
        this.passedParameters = passedParameters;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }
}
