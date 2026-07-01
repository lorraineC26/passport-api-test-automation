package helpers;

/**
 * Supplies API credentials from environment variables so no secret is stored in
 * source control. A missing variable fails the suite fast with a clear message
 * instead of falling back to a default, matching how secrets are injected in CI.
 * See the README for the required variables and local setup.
 */
public final class Credentials {

    private static final String USERNAME_ENV = "RESTFUL_BOOKER_USERNAME";
    private static final String PASSWORD_ENV = "RESTFUL_BOOKER_PASSWORD";

    private Credentials() {
    }

    public static String username() {
        return required(USERNAME_ENV);
    }

    public static String password() {
        return required(PASSWORD_ENV);
    }

    private static String required(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Required environment variable " + key + " is not set. "
                + "Export it before running the tests (see README).");
        }
        return value;
    }
}
