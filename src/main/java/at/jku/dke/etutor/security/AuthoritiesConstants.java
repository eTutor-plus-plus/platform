package at.jku.dke.etutor.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String TUTOR = "ROLE_TUTOR";

    public static final String STUDENT = "ROLE_STUDENT";

    public static final String INSTRUCTOR = "ROLE_INSTRUCTOR";

    private AuthoritiesConstants() {
    }
}
