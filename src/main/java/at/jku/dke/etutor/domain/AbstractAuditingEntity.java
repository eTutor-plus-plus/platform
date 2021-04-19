package at.jku.dke.etutor.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base abstract class for entities which will hold definitions for created, last modified, created by,
 * last modified by attributes.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 50, updatable = false)
    @JsonIgnore
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    @JsonIgnore
    private Instant createdDate = Instant.now();

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    @JsonIgnore
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    @JsonIgnore
    private Instant lastModifiedDate = Instant.now();

    /**
     * Returns the creator of this current entity.
     *
     * @return the creator of this entity
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the creator of this entity.
     *
     * @param createdBy
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Returns the creation date of this entity.
     *
     * @return the creation date of this entity.
     */
    public Instant getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets the creation date of this entity.
     *
     * @param createdDate the creation date to set
     */
    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Sets teh last modification date.
     *
     * @return the last modification date to set
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * Sets the last modifier of this entity.
     *
     * @param lastModifiedBy the last modifier to set
     */
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * Returns the last modification date.
     *
     * @return the last modification date
     */
    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * Sets the last modification date.
     *
     * @param lastModifiedDate the last modification date to set
     */
    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
