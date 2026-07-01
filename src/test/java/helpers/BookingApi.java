package helpers;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Reusable API actions that tests need for setup: authenticating and creating a
 * booking to operate on. Extracting these keeps each test expressing only the
 * behaviour under test, not the plumbing to reach it. All actions run through
 * the shared RequestSpecification passed in by the caller.
 */
public final class BookingApi {

    private BookingApi() {
    }

    /**
     * Fetches a session token via POST /auth. restful-booker returns the token
     * in the JSON body; write operations send it back as a Cookie (token=...).
     */
    public static String authToken(RequestSpecification spec, String username, String password) {
        return given(spec)
            .contentType(ContentType.JSON)
            .body(Map.of("username", username, "password", password))
        .when()
            .post("/auth")
        .then()
            .statusCode(200)
            .extract().path("token");
    }

    /**
     * Creates a booking and returns its id, for tests that need an existing
     * record to read, update, or delete.
     */
    public static int createBooking(RequestSpecification spec, Map<String, Object> payload) {
        return given(spec)
            .contentType(ContentType.JSON)
            .body(payload)
        .when()
            .post("/booking")
        .then()
            .statusCode(200)
            .extract().path("bookingid");
    }
}
