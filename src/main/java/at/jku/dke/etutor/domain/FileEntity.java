package at.jku.dke.etutor.domain;

import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity which represents an uploaded file.
 *
 * @author fne
 */
@Entity
@Table(name = "file")
public class FileEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fileEntitySequenceGenerator")
    @SequenceGenerator(name = "fileEntitySequenceGenerator", allocationSize = 20)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String contentType;
    private long size;
    @Lob
    @Column(nullable = false)
    private byte[] content;
    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime submitTime = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "studentId", referencedColumnName = "id")
    private Student student;

    /**
     * Returns the id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
     * Returns the size.
     *
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the size.
     *
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Returns the file's content.
     *
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets the file's content.
     *
     * @param content the file's content to set
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Returns the associated student.
     *
     * @return the associated student
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Sets the student.
     *
     * @param student the student to set
     */
    public void setStudent(Student student) {
        this.student = student;
    }

    /**
     * Returns the submit time.
     *
     * @return the submit time
     */
    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    /**
     * Sets the submit time.
     *
     * @param submitTime the submit time to set
     */
    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }
}
