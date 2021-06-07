package at.jku.dke.etutor.domain;

import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * The entity class for a student.
 *
 * @author fne
 */
@Entity
public class Student extends Person {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "student")
    private List<FileEntity> uploadedFiles = new ArrayList<>();

    /**
     * Returns the list of uploaded files.
     *
     * @return the list of uploaded files
     */
    public List<FileEntity> getUploadedFiles() {
        return uploadedFiles;
    }

    /**
     * Sets the list of uploaded files.
     *
     * @param uploadedFiles the uploaded files to set, must not be null
     */
    public void setUploadedFiles(List<FileEntity> uploadedFiles) {
        if (uploadedFiles == null) {
            throw new IllegalArgumentException("The parameter 'uploadedFiles' must not be null!");
        }

        this.uploadedFiles = uploadedFiles;
    }

    /**
     * Adds an uploaded file.
     *
     * @param fileEntity the uploaded file to add, must not be null
     */
    public void addUploadedFile(FileEntity fileEntity) {
        if (fileEntity == null) {
            throw new IllegalArgumentException("The parameter 'fileEntity' must not be null!");
        }

        uploadedFiles.add(fileEntity);
    }
}
