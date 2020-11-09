package at.jku.dke.etutor.service.dto;

import at.jku.dke.etutor.EtutorPlusPlusApp;
import at.jku.dke.etutor.domain.rdf.ETutorVocabulary;
import at.jku.dke.etutor.service.dto.validation.CourseTypeConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.codec.Charsets;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import javax.validation.constraints.NotBlank;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

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
        return URLEncoder.encode(name.replace(' ', '_').trim(), Charsets.UTF_8);
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
}
