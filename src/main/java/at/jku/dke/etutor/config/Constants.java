package at.jku.dke.etutor.config;

import java.util.regex.Pattern;

/**
 * Application constants.
 */
public final class Constants {

    /**
     * Regex for logins which only allows system and jku employee or matriculation accounts.
     */
    public static final String LOGIN_REGEX = "^((ak|k)\\d+|system|admin)$";
    /**
     * Common login regex which allows words, email addresses, ...
     */
    public static final String COMMON_LOGIN_REGEX =
        "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";
    /**
     * Pattern for the login field
     */
    public static final Pattern LOGIN_PATTERN = Pattern.compile(LOGIN_REGEX, Pattern.CASE_INSENSITIVE);

    public static final String SYSTEM = "system";
    public static final String DEFAULT_LANGUAGE = "de";

    private Constants() {}
}
