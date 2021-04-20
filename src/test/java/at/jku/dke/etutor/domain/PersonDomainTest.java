package at.jku.dke.etutor.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.Objects;
import liquibase.pro.packaged.U;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

/**
 * Test class for all {@link Person} related entities.
 *
 * @author fne
 */
public class PersonDomainTest {

    private Person person;

    /**
     * Initializes the test values before each run.
     */
    @BeforeEach
    public void init() {
        person = new Student();
    }

    /**
     * Tests the person id's set and get methods.
     */
    @Test
    public void testPersonIdSetAndGet() {
        person.setId(5L);
        assertThat(person.getId()).isEqualTo(5);
    }

    /**
     * Tests the person user's set and get methods.
     */
    @Test
    public void testPersonUserSetAndGet() {
        User user = new User();
        person.setUser(user);
        assertThat(person.getUser()).isEqualTo(user);
    }

    /**
     * Tests the person's hash code method.
     */
    @Test
    public void testPersonHashCode() {
        person.setId(3L);
        assertThat(person.hashCode()).isEqualTo(Objects.hash(3L));

        person.setId(9L);
        assertThat(person.hashCode()).isEqualTo(Objects.hash(9L));

        person.setId(null);
        Long id = null;
        assertThat(person.hashCode()).isEqualTo(Objects.hash(id));
    }

    /**
     * Tests the person's equal method.
     */
    @Test
    public void testPersonEquals() {
        person.setId(12L);

        Person other = new Student();
        other.setId(12L);
        assertThat(person.equals(other)).isTrue();

        other.setId(13L);
        assertThat(person.equals(other)).isFalse();

        other = new Administrator();
        other.setId(12L);
        assertThat(person.equals(other)).isFalse();

        assertThat(person.equals(null)).isFalse();
    }
}
