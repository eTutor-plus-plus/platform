package at.jku.dke.etutor.web.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import one.util.streamex.StreamEx;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Utility class for testing REST controllers.
 */
public final class TestUtil {

    private static final ObjectMapper mapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Convert an object to JSON byte array.
     *
     * @param object the object to convert.
     * @return the JSON byte array.
     * @throws IOException
     */
    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        return mapper.writeValueAsBytes(object);
    }

    /**
     * Create a byte array with a specific size filled with specified data.
     *
     * @param size the size of the byte array.
     * @param data the data to put in the byte array.
     * @return the JSON byte array.
     */
    public static byte[] createByteArray(int size, String data) {
        byte[] byteArray = new byte[size];
        for (int i = 0; i < size; i++) {
            byteArray[i] = Byte.parseByte(data, 2);
        }
        return byteArray;
    }

    /**
     * A matcher that tests that the examined string represents the same instant as the reference datetime.
     */
    public static class ZonedDateTimeMatcher extends TypeSafeDiagnosingMatcher<String> {

        private final ZonedDateTime date;

        public ZonedDateTimeMatcher(ZonedDateTime date) {
            this.date = date;
        }

        @Override
        protected boolean matchesSafely(String item, Description mismatchDescription) {
            try {
                if (!date.isEqual(ZonedDateTime.parse(item))) {
                    mismatchDescription.appendText("was ").appendValue(item);
                    return false;
                }
                return true;
            } catch (DateTimeParseException e) {
                mismatchDescription.appendText("was ").appendValue(item).appendText(", which could not be parsed as a ZonedDateTime");
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a String representing the same Instant as ").appendValue(date);
        }
    }

    /**
     * Creates a matcher that matches when the examined string represents the same instant as the reference datetime.
     *
     * @param date the reference datetime against which the examined string is checked.
     */
    public static ZonedDateTimeMatcher sameInstant(ZonedDateTime date) {
        return new ZonedDateTimeMatcher(date);
    }

    /**
     * A matcher that tests that the examined number represents the same value - it can be Long, Double, etc - as the reference BigDecimal.
     */
    public static class NumberMatcher extends TypeSafeMatcher<Number> {

        final BigDecimal value;

        public NumberMatcher(BigDecimal value) {
            this.value = value;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a numeric value is ").appendValue(value);
        }

        @Override
        protected boolean matchesSafely(Number item) {
            BigDecimal bigDecimal = asDecimal(item);
            return bigDecimal != null && value.compareTo(bigDecimal) == 0;
        }

        private static BigDecimal asDecimal(Number item) {
            if (item == null) {
                return null;
            }
            if (item instanceof BigDecimal) {
                return (BigDecimal) item;
            } else if (item instanceof Long) {
                return BigDecimal.valueOf((Long) item);
            } else if (item instanceof Integer) {
                return BigDecimal.valueOf((Integer) item);
            } else if (item instanceof Double) {
                return BigDecimal.valueOf((Double) item);
            } else if (item instanceof Float) {
                return BigDecimal.valueOf((Float) item);
            } else {
                return BigDecimal.valueOf(item.doubleValue());
            }
        }
    }

    /**
     * Creates a matcher that matches when the examined number represents the same value as the reference BigDecimal.
     *
     * @param number the reference BigDecimal against which the examined number is checked.
     */
    public static NumberMatcher sameNumber(BigDecimal number) {
        return new NumberMatcher(number);
    }

    /**
     * Verifies the equals/hashcode contract on the domain object.
     */
    public static <T> void equalsVerifier(Class<T> clazz) throws Exception {
        T domainObject1 = clazz.getConstructor().newInstance();
        assertThat(domainObject1.toString()).isNotNull();
        assertThat(domainObject1).isEqualTo(domainObject1);
        assertThat(domainObject1).hasSameHashCodeAs(domainObject1);
        // Test with an instance of another class
        Object testOtherObject = new Object();
        assertThat(domainObject1).isNotEqualTo(testOtherObject);
        assertThat(domainObject1).isNotEqualTo(null);
        // Test with an instance of the same class
        T domainObject2 = clazz.getConstructor().newInstance();
        assertThat(domainObject1).isNotEqualTo(domainObject2);
        // HashCodes are equals because the objects are not persisted yet
        assertThat(domainObject1).hasSameHashCodeAs(domainObject2);
    }

    /**
     * Create a {@link FormattingConversionService} which use ISO date format, instead of the localized one.
     *
     * @return the {@link FormattingConversionService}.
     */
    public static FormattingConversionService createFormattingConversionService() {
        DefaultFormattingConversionService dfcs = new DefaultFormattingConversionService();
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(dfcs);
        return dfcs;
    }

    /**
     * Makes a an executes a query to the EntityManager finding all stored objects.
     *
     * @param <T>  The type of objects to be searched
     * @param em   The instance of the EntityManager
     * @param clss The class type to be searched
     * @return A list of all found objects
     */
    public static <T> List<T> findAll(EntityManager em, Class<T> clss) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clss);
        Root<T> rootEntry = cq.from(clss);
        CriteriaQuery<T> all = cq.select(rootEntry);
        TypedQuery<T> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

    /**
     * Converts the given json string into the corresponding java object.
     *
     * @param jsonData the json object as string
     * @param type     the type of the object
     * @param <T>      the generic
     * @return the parsed java object
     * @throws JsonProcessingException is thrown when a conversion error occurs
     */
    public static <T> T convertFromJSONString(String jsonData, Class<T> type) throws JsonProcessingException {
        return mapper.readValue(jsonData, type);
    }

    /**
     * Converts the given json string into the corresponding java object.
     *
     * @param jsonData       the json object as string
     * @param type           the type of the object
     * @param collectionType the type of the collection
     * @param <T>            the type generic
     * @param <C>            the collection generic
     * @return the parsed java object
     * @throws JsonProcessingException is thrown when a conversion error occurs
     */
    public static <T, C extends Collection> C convertCollectionFromJSONString(String jsonData, Class<T> type, Class<C> collectionType)
        throws JsonProcessingException {
        return mapper.readValue(jsonData, mapper.getTypeFactory().constructCollectionType(collectionType, type));
    }

    /**
     * Generates a new test user details class.
     *
     * @param username    the username
     * @param authorities the associated authorities
     * @return instance of a mock user details class
     */
    public static UserDetails generateTestUserDetails(String username, String... authorities) {
        return new TestUserDetails(username, authorities);
    }

    private TestUtil() {
    }

    /**
     * Class which represents a mock user details implementation.
     */
    private static class TestUserDetails implements UserDetails {

        private final String username;
        private final String[] authorities;

        /**
         * Constructor.
         *
         * @param username    the user name
         * @param authorities the authorities
         */
        public TestUserDetails(String username, String... authorities) {
            this.username = username;
            this.authorities = authorities;
        }

        /**
         * Returns the authorities granted to the user. Cannot return <code>null</code>.
         *
         * @return the authorities, sorted by natural key (never <code>null</code>)
         */
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return StreamEx.of(authorities).map(x -> (GrantedAuthority) () -> x).toList();
        }

        /**
         * Returns the password used to authenticate the user.
         *
         * @return the password
         */
        @Override
        public String getPassword() {
            return null;
        }

        /**
         * Returns the username used to authenticate the user. Cannot return
         * <code>null</code>.
         *
         * @return the username (never <code>null</code>)
         */
        @Override
        public String getUsername() {
            return username;
        }

        /**
         * Indicates whether the user's account has expired. An expired account cannot be
         * authenticated.
         *
         * @return <code>true</code> if the user's account is valid (ie non-expired),
         * <code>false</code> if no longer valid (ie expired)
         */
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        /**
         * Indicates whether the user is locked or unlocked. A locked user cannot be
         * authenticated.
         *
         * @return <code>true</code> if the user is not locked, <code>false</code> otherwise
         */
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        /**
         * Indicates whether the user's credentials (password) has expired. Expired
         * credentials prevent authentication.
         *
         * @return <code>true</code> if the user's credentials are valid (ie non-expired),
         * <code>false</code> if no longer valid (ie expired)
         */
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        /**
         * Indicates whether the user is enabled or disabled. A disabled user cannot be
         * authenticated.
         *
         * @return <code>true</code> if the user is enabled, <code>false</code> otherwise
         */
        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
