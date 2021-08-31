package at.jku.dke.etutor.service.dto.student;

import java.time.Instant;

public class IndividualTaskSubmissionDTO {
    private Instant instant;
    private String submission;

    public IndividualTaskSubmissionDTO(){}

    public IndividualTaskSubmissionDTO(Instant instant, String submission){
        this.instant=instant;
        this.submission=submission;
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
}
