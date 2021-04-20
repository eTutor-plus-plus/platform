package at.jku.dke.etutor.domain;

import at.jku.dke.etutor.config.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.io.Serializable;
import java.time.Instant;
import java.time.Instant;
import java.util.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;

/**
 * Represents an application user.
 */
@Entity
@Table(name = "jhi_user")
public class User extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userSequenceGenerator")
    @SequenceGenerator(name = "userSequenceGenerator", initialValue = 3, allocationSize = 20)
    private Long id;

    @NotNull
    @Pattern(regexp = Constants.COMMON_LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Column(length = 254, unique = true)
    private String email;

    @NotNull
    @Column(nullable = false)
    private boolean activated = false;

    @Size(min = 2, max = 10)
    @Column(name = "lang_key", length = 10)
    private String langKey;

    @Size(max = 256)
    @Column(name = "image_url", length = 256)
    private String imageUrl;

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    @JsonIgnore
    private String resetKey;

    @Column(name = "reset_date")
    private Instant resetDate = null;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "jhi_user_authority",
        joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "authority_name", referencedColumnName = "name") }
    )
    @BatchSize(size = 20)
    private Set<Authority> authorities = new HashSet<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Person> associatedPersons = new ArrayList<>();

    /**
     * Returns the user's id.
     *
     * @return the id of the current user
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user's id.
     *
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the unique user login.
     *
     * @return the unique user login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the unique user login.
     *
     * @param login the unique user login to set
     */
    public void setLogin(String login) {
        // Lowercase the login before saving it in database
        this.login = StringUtils.lowerCase(login, Locale.ENGLISH);
    }

    /**
     * Returns the user's password.
     *
     * @return the user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the firstname of the user.
     *
     * @return the firstname of the user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the firstname of the user.
     *
     * @param firstName the firstname to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name of the user.
     *
     * @return the last name of the user
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the user.
     *
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the user's email address.
     *
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Return the user's image url.
     *
     * @return the user's image url
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the user's image url.
     *
     * @param imageUrl the image url to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Returns whether the user is activated or not.
     *
     * @return {@code true} if the user is activated, otherwise {@code false}
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Sets whether the user is activated or not.
     *
     * @param activated {@code true} if the user is activated, otherwise {@code false}
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * Returns the user's activation key.
     *
     * @return the activation key to return
     */
    public String getActivationKey() {
        return activationKey;
    }

    /**
     * Sets the user's activation key.
     *
     * @param activationKey the activation key to set
     */
    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    /**
     * Returns the user's reset key
     *
     * @return the user's reset key
     */
    public String getResetKey() {
        return resetKey;
    }

    /**
     * Sets the user's reset key.
     *
     * @param resetKey the reset key to set
     */
    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    /**
     * Returns the reset date.
     *
     * @return the reset date
     */
    public Instant getResetDate() {
        return resetDate;
    }

    /**
     * Sets the reset date.
     *
     * @param resetDate the reset date to set
     */
    public void setResetDate(Instant resetDate) {
        this.resetDate = resetDate;
    }

    /**
     * Returns the language key.
     *
     * @return the language key
     */
    public String getLangKey() {
        return langKey;
    }

    /**
     * Sets the language key.
     *
     * @param langKey the language key to set
     */
    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    /**
     * Returns the associated authorities.
     *
     * @return the set of associated authorities
     */
    public Set<Authority> getAuthorities() {
        return authorities;
    }

    /**
     * Sets the associated authorities.
     *
     * @param authorities the associated authorities to set
     */
    public void setAuthorities(Set<Authority> authorities) {
        if (authorities == null) {
            throw new IllegalArgumentException("The parameter 'authorities' must not be null!");
        }
        this.authorities = authorities;
    }

    /**
     * Returns the list of associated persons.
     *
     * @return the list of associated persons
     */
    public List<Person> getAssociatedPersons() {
        return associatedPersons;
    }

    /**
     * Sets the associated persons list.
     *
     * @param associatedPersons the associated persons list to set
     */
    public void setAssociatedPersons(List<Person> associatedPersons) {
        if (associatedPersons == null) {
            throw new IllegalArgumentException("The parameter 'associatedPersons' must not be null!");
        }
        this.associatedPersons = associatedPersons;
    }

    /**
     * Adds a person to the associated persons list.
     *
     * @param person the person to add which must not be null.
     */
    public void addPerson(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("The parameter 'person' must not be null!");
        }
        associatedPersons.add(person);
        person.setUser(this);
    }

    /**
     * Removes a person from the associated persons list.
     *
     * @param person the person to remove which must not be null
     */
    public void removePerson(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("The parameter 'person' must not be null!");
        }
        associatedPersons.remove(person);
        person.setUser(null);
    }

    /**
     * See {@link Object#equals(Object)}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        return id != null && id.equals(((User) o).id);
    }

    /**
     * See {@link Object#hashCode()}
     */
    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "User{" +
            "login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", activated='" + activated + '\'' +
            ", langKey='" + langKey + '\'' +
            ", activationKey='" + activationKey + '\'' +
            "}";
    }
}
