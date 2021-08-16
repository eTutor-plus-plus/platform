package at.jku.dke.etutor.service.dto;

import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.dto.validation.CourseTypeConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;

import javax.validation.constraints.NotBlank;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * DTO class which represents a course.
 *
 * @author fne
 */
public class CourseDTO implements Comparable<CourseDTO> {

    private String id;

    @NotBlank
    private String name;

    private String description;
    private URL link;

    @NotBlank
    @CourseTypeConstraint
    private String courseType;

    private String creator;
    private int instanceCount;

    /**
     * Empty constructor; needed for serialization.
     */
    public CourseDTO() {
    }

    /**
     * Constructor.
     *
     * @param resource the rdf resource of this course
     */
    public CourseDTO(Resource resource) {
        this();
        setId(resource.getURI());
        setName(resource.getProperty(RDFS.label).getString());
        setDescription(resource.getProperty(ETutorVocabulary.hasCourseDescription).getString());
        setCourseType(resource.getProperty(ETutorVocabulary.hasCourseType).getString());
        setCreator(resource.getProperty(ETutorVocabulary.hasCourseCreator).getString());
        Statement instanceCountStmt = resource.getProperty(ETutorVocabulary.hasInstanceCount);

        if (instanceCountStmt != null) {
            setInstanceCount(instanceCountStmt.getInt());
        } else {
            setInstanceCount(0);
        }

        String courseLink = resource.getProperty(ETutorVocabulary.hasCourseLink).getString();
        try {
            URL url = new URL(courseLink);
            setLink(url);
        } catch (Exception ex) {
            setLink(null);
        }
    }

    /**
     * Returns the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name for RDF usage.
     *
     * @return the name prepared for RDF usage
     */
    @JsonIgnore
    public String getNameForRDF() {
        if (name == null) {
            return null;
        }
        return URLEncoder.encode(name.replace(' ', '_').trim(), StandardCharsets.UTF_8);
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
     * Returns the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the optional link
     *
     * @return the link
     */
    public URL getLink() {
        return link;
    }

    /**
     * Sets the link.
     *
     * @param link the link to set
     */
    public void setLink(URL link) {
        this.link = link;
    }

    /**
     * Returns the course type.
     *
     * @return the course type
     */
    public String getCourseType() {
        return courseType;
    }

    /**
     * Sets the course type.
     *
     * @param courseType the course type to set
     */
    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    /**
     * Returns the creator.
     *
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Sets the creator.
     *
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Returns the instance count.
     *
     * @return the instance count
     */
    public int getInstanceCount() {
        return instanceCount;
    }

    /**
     * Sets the course instance count.
     *
     * @param instanceCount the course instance count to set
     */
    public void setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(CourseDTO o) {
        return name.compareTo(o.name);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     *     {@code x}, {@code x.equals(x)} should return
     *     {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     *     {@code x} and {@code y}, {@code x.equals(y)}
     *     should return {@code true} if and only if
     *     {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     *     {@code x}, {@code y}, and {@code z}, if
     *     {@code x.equals(y)} returns {@code true} and
     *     {@code y.equals(z)} returns {@code true}, then
     *     {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     *     {@code x} and {@code y}, multiple invocations of
     *     {@code x.equals(y)} consistently return {@code true}
     *     or consistently return {@code false}, provided no
     *     information used in {@code equals} comparisons on the
     *     objects is modified.
     * <li>For any non-null reference value {@code x},
     *     {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @see #hashCode()
     * @see java.util.HashMap
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CourseDTO courseDTO = (CourseDTO) o;

        return new EqualsBuilder().append(name, courseDTO.name).isEquals();
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link java.util.HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     *     an execution of a Java application, the {@code hashCode} method
     *     must consistently return the same integer, provided no information
     *     used in {@code equals} comparisons on the object is modified.
     *     This integer need not remain consistent from one execution of an
     *     application to another execution of the same application.
     * <li>If two objects are equal according to the {@code equals(Object)}
     *     method, then calling the {@code hashCode} method on each of
     *     the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     *     according to the {@link java.lang.Object#equals(java.lang.Object)}
     *     method, then calling the {@code hashCode} method on each of the
     *     two objects must produce distinct integer results.  However, the
     *     programmer should be aware that producing distinct integer results
     *     for unequal objects may improve the performance of hash tables.
     * </ul>
     *
     * @return a hash code value for this object.
     * @implSpec As far as is reasonably practical, the {@code hashCode} method defined
     * by class {@code Object} returns distinct integers for distinct objects.
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.lang.System#identityHashCode
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).toHashCode();
    }
}
