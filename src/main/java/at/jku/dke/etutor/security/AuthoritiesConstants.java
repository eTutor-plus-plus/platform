package at.jku.dke.etutor.security;

import at.jku.dke.etutor.domain.*;

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

    /**
     * Returns the class name of the role constant. If the role constant is unknown or 'ROLE_ANONYMOUS',
     * {@code null} will be returned.
     *
     * @param role the mandatory name of the role
     * @return the class name as string or {@code null}
     */
    public static final String getClassNameByRoleConstant(String role) {
        if (role == null) {
            throw new IllegalArgumentException("The parameter 'role' must not be null!");
        }

        return switch (role) {
            case ADMIN -> Administrator.class.getName();
            case USER -> User.class.getName();
            case TUTOR -> Tutor.class.getName();
            case STUDENT -> Student.class.getName();
            case INSTRUCTOR -> Instructor.class.getName();
            default -> null;
        };
    }

    private AuthoritiesConstants() {}
}
