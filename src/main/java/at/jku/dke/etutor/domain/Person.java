package at.jku.dke.etutor.domain;

import javax.persistence.*;
import java.io.Serializable;

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
    @Column(name = "personId")
    private long id;

    @ManyToOne
    @PrimaryKeyJoinColumn(name = "personId", referencedColumnName = "id")
    private User user;

    /**
     * Returns the person's id.
     *
     * @return the person's id to return
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id of this person.
     *
     * @param id the id to set
     */
    public void setId(long id) {
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
}
