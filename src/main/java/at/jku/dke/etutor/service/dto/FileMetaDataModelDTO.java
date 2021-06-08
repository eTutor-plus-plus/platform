package at.jku.dke.etutor.service.dto;

import java.time.LocalDateTime;

/**
 * DTO class for transferring file meta data.
 */
public class FileMetaDataModelDTO {
    private String fileName;
    private String contentType;
    private LocalDateTime submissionDate;

    /**
     * Constructor.
     */
    public FileMetaDataModelDTO() {
        // Empty for serialization
    }

    /**
     * Constructor.
     *
     * @param fileName       the file name
     * @param contentType    the content type
     * @param submissionDate the submission date
     */
    public FileMetaDataModelDTO(String fileName, String contentType, LocalDateTime submissionDate) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.submissionDate = submissionDate;
    }

    /**
     * Returns the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName the file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the content type.
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     *
     * @param contentType the content type to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns the submission date.
     *
     * @return the submission date
     */
    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    /**
     * Sets the submission date.
     *
     * @param submissionDate the submission date to set
     */
    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }
}
