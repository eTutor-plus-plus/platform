package at.jku.dke.etutor.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;

/**
 * The base entity class for Roles.
 *
 * @author fne
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "personSequenceGenerator")
    @SequenceGenerator(name = "personSequenceGenerator", initialValue = 2, allocationSize = 20)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id")
    private User user;

    /**
     * Returns the person's id.
     *
     * @return the person's id to return
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id of this person.
     *
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the associated user.
     *
     * @return the associated user to return
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the associated user.
     *
     * @param user the associated user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * See {@link Object#equals(Object)}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id == person.id;
    }

    /**
     * See {@link Object#hashCode()}
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
