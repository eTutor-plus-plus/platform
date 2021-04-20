package at.jku.dke.etutor.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for the {@link User} entity.
 *
 * @author fne
 */
public class UserTest {

    private User user;

    private Set<Authority> authorities;

    private List<Person> associatedPersons;

    /**
     * Method which initializes the test values before each test run.
     */
    @BeforeEach
    public void init() {
        user = new User();

        authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName("Test1");
        authorities.add(authority);
        authority = new Authority();
        authority.setName("Test2");
        authorities.add(authority);

        associatedPersons = new ArrayList<>();
        associatedPersons.add(new Student());
        associatedPersons.add(new Tutor());
    }

    /**
     * Method which tests the user id's set and get methods.
     */
    @Test
    public void testUserIdSetAndGet() {
        user.setId(10L);
        assertThat(user.getId()).isEqualTo(10L);

        user.setId(12L);
        assertThat(user.getId()).isEqualTo(12L);
    }

    /**
     * Method which tests the user login's set and get methods.
     */
    @Test
    public void testUserLoginSetAndGet() {
        user.setLogin("Testlogin");
        assertThat(user.getLogin()).isEqualTo("testlogin");

        user.setLogin("k1155901");
        assertThat(user.getLogin()).isEqualTo("k1155901");
    }

    /**
     * Method which tests the user password's set and get methods.
     */
    @Test
    public void testUserPasswordSetAndGet() {
        user.setPassword("Secure123");
        assertThat(user.getPassword()).isEqualTo("Secure123");

        user.setPassword("Secure12345!");
        assertThat(user.getPassword()).isEqualTo("Secure12345!");

        user.setPassword(null);
        assertThat(user.getPassword()).isNull();
    }

    /**
     * Method which tests the user first name's set and get methods.
     */
    @Test
    public void testUserFirstNameSetAndGet() {
        user.setFirstName("Test");
        assertThat(user.getFirstName()).isEqualTo("Test");

        user.setFirstName("Test123");
        assertThat(user.getFirstName()).isEqualTo("Test123");

        user.setFirstName(null);
        assertThat(user.getFirstName()).isNull();
    }

    /**
     * Method which tests the user last name's set and get methods.
     */
    @Test
    public void testUserLastNameSetAndGet() {
        user.setLastName("Testname");
        assertThat(user.getLastName()).isEqualTo("Testname");

        user.setLastName("New name");
        assertThat(user.getLastName()).isEqualTo("New name");

        user.setLastName(null);
        assertThat(user.getLastName()).isNull();
    }

    /**
     * Method which tests the user email's set and get methods.
     */
    @Test
    public void testUserEmailSetAndGet() {
        user.setFirstName("Name");
        assertThat(user.getFirstName()).isEqualTo("Name");

        user.setFirstName("New first name");
        assertThat(user.getFirstName()).isEqualTo("New first name");

        user.setFirstName(null);
        assertThat(user.getFirstName()).isNull();
    }

    /**
     * Method which tests the user activated property's set and get methods.
     */
    @Test
    public void testUserActivatedSetAndGet() {
        user.setActivated(true);
        assertThat(user.isActivated()).isTrue();

        user.setActivated(false);
        assertThat(user.isActivated()).isFalse();
    }

    /**
     * Tests the user language key property's set and get methods.
     */
    @Test
    public void testUserLangKeySetAndGet() {
        user.setLangKey("DE");
        assertThat(user.getLangKey()).isEqualTo("DE");

        user.setLangKey("EN");
        assertThat(user.getLangKey()).isEqualTo("EN");

        user.setLangKey(null);
        assertThat(user.getLangKey()).isNull();
    }

    /**
     * Tests the user image url property's set and get methods.
     */
    @Test
    public void testUserImageUrlSetAndGet() {
        user.setImageUrl("http://www.test.at");
        assertThat(user.getImageUrl()).isEqualTo("http://www.test.at");

        user.setImageUrl("http://www.test2.at");
        assertThat(user.getImageUrl()).isEqualTo("http://www.test2.at");

        user.setImageUrl(null);
        assertThat(user.getImageUrl()).isNull();
    }

    /**
     * Tests the user activation key property's set and get methods.
     */
    @Test
    public void testUserActivationKeySetAndGet() {
        user.setActivationKey("testkey");
        assertThat(user.getActivationKey()).isEqualTo("testkey");

        user.setActivationKey("newkey");
        assertThat(user.getActivationKey()).isEqualTo("newkey");

        user.setActivationKey(null);
        assertThat(user.getActivationKey()).isNull();
    }

    /**
     * Tests the user reset key property's set and get methods.
     */
    @Test
    public void testUserResetKeySetAndGet() {
        user.setResetKey("testkey");
        assertThat(user.getResetKey()).isEqualTo("testkey");

        user.setResetKey("newkey");
        assertThat(user.getResetKey()).isEqualTo("newkey");

        user.setResetKey(null);
        assertThat(user.getResetKey()).isNull();
    }

    /**
     * Tests the user reset date property's set and get methods.
     */
    @Test
    public void testUserResetDateSetAndGet() {
        Instant date = Instant.now();

        user.setResetDate(date);
        assertThat(user.getResetDate()).isEqualTo(date);

        date = Instant.MIN;
        user.setResetDate(date);
        assertThat(user.getResetDate()).isEqualTo(date);

        user.setResetDate(null);
        assertThat(user.getResetDate()).isNull();
    }

    /**
     * Tests the user authorities property's set and get methods.
     */
    @Test
    public void testUserAuthoritiesGetAndSet() {
        user.setAuthorities(authorities);
        assertThat(user.getAuthorities()).isEqualTo(authorities);
    }

    /**
     * Tests the user authorities property's set method with a null value.
     */
    @Test
    public void testUserAuthorititesSetException() {
        assertThatThrownBy(() -> user.setAuthorities(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("authorities");
    }

    /**
     * Tests the user associated persons property's set and get methods.
     */
    @Test
    public void testUserAssociatedPersonsGetAndSet() {
        user.setAssociatedPersons(associatedPersons);
        assertThat(user.getAssociatedPersons()).isEqualTo(associatedPersons);
    }

    /**
     * Tests the user associated persons property's get method with a null value.
     */
    @Test
    public void testUserAssociatedPersonSetException() {
        assertThatThrownBy(() -> user.setAssociatedPersons(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("associatedPersons");
    }

    /**
     * Tests the user's add person method with a null value.
     */
    @Test
    public void testUserAddPersonException() {
        assertThatThrownBy(() -> user.addPerson(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("person");
    }

    /**
     * Tests the user's add person method.
     */
    @Test
    public void testUserAddPerson() {
        Person person = new Student();
        person.setId(1L);

        user.addPerson(person);

        assertThat(user.getAssociatedPersons()).contains(person);
    }

    /**
     * Tests the user's remove person method with a null value.
     */
    @Test
    public void testUserRemovePersonException() {
        assertThatThrownBy(() -> user.removePerson(null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("person");
    }

    /**
     * Tests the user's remove person method.
     */
    @Test
    public void testUserRemovePerson() {
        Person person = new Student();
        person.setId(1L);

        user.addPerson(person);

        assertThat(user.getAssociatedPersons()).contains(person);

        user.removePerson(person);

        assertThat(user.getAssociatedPersons().size()).isZero();
    }
}
