package at.jku.dke.etutor.service.dto;

import java.time.Instant;
import java.util.function.Function;

/**
 * Represents a versions of a resource.
 * @param <T> the type of the resource
 */

public class ResourceVersionDTO<T> {
    /**
     * The date of the creation of the specific version
     */
    private Instant creationDate;
    /**
     * The reason the resource has been modified to this version
     */
    private String reasonOfChange;

    /**
     * The version of the resource
     */
    private T version;

    /**
     * Maps the version type to a target type and returns a new resource-version
     * @param mapper the mapper
     * @return a new {@link ResourceVersionDTO<R>} with the mapped resource
     * @param <R> the target type
     */
    public <R> ResourceVersionDTO<R> map(Function<T,R> mapper){
        var result = new ResourceVersionDTO<R>();
        result.setVersion(mapper.apply(this.version));
        result.setCreationDate(this.creationDate);
        result.setReasonOfChange(this.reasonOfChange);
        return result;
    }

    public ResourceVersionDTO(){

    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public String getReasonOfChange() {
        return reasonOfChange;
    }

    public void setReasonOfChange(String reasonOfChange) {
        this.reasonOfChange = reasonOfChange;
    }

    public T getVersion() {
        return version;
    }

    public void setVersion(T version) {
        this.version = version;
    }
}
