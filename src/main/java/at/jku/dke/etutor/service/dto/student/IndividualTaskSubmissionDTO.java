package at.jku.dke.etutor.service.dto.student;

import java.time.Instant;

public class IndividualTaskSubmissionDTO {
    private Instant instant;
    private String submission;
    private boolean hasBeenSubmitted;
    private boolean hasBeenSolved;

    public IndividualTaskSubmissionDTO(){}

    public IndividualTaskSubmissionDTO(Instant instant, String submission, boolean hasBeenSubmitted, boolean hasBeenSolved){
        this.instant=instant;
        this.submission=submission;
        this.hasBeenSolved=hasBeenSolved;
        this.hasBeenSubmitted=hasBeenSubmitted;
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

    public boolean isHasBeenSubmitted() {
        return hasBeenSubmitted;
    }

    public void setHasBeenSubmitted(boolean hasBeenSubmitted) {
        this.hasBeenSubmitted = hasBeenSubmitted;
    }

    public boolean isHasBeenSolved() {
        return hasBeenSolved;
    }

    public void setHasBeenSolved(boolean hasBeenSolved) {
        this.hasBeenSolved = hasBeenSolved;
    }
}
