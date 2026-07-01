package tests;

import config.BaseTest;
import helpers.BookingApi;
import helpers.BookingFactory;
import helpers.Credentials;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

/**
 * Negative tests: the API must reject invalid input and unauthorized writes.
 */
public class NegativeTests extends BaseTest {

    @Test
    public void getBookingWithNonExistentIdReturns404() {
        given(spec)
        .when()
            .get("/booking/{id}", 9999999)
        .then()
            .statusCode(404);
    }

    @Test
    public void deleteWithoutTokenReturns403() {
        int id = BookingApi.createBooking(spec, BookingFactory.booking("Temp", "Record"));

        given(spec)
        .when()
            .delete("/booking/{id}", id)
        .then()
            .statusCode(403);
    }

    @Test
    public void updateWithoutTokenReturns403() {
        int id = BookingApi.createBooking(spec, BookingFactory.booking("Temp", "Record"));

        given(spec)
            .contentType(ContentType.JSON)
            .body(Map.of("firstname", "NoAuth"))
        .when()
            .put("/booking/{id}", id)
        .then()
            .statusCode(403);
    }

    @Test
    public void deleteWithInvalidTokenReturns403() {
        int id = BookingApi.createBooking(spec, BookingFactory.booking("Temp", "Record"));

        given(spec)
            .cookie("token", "not-a-real-token")
        .when()
            .delete("/booking/{id}", id)
        .then()
            .statusCode(403);
    }

    @Test
    public void createBookingWithMissingFieldsIsRejected() {
        // Missing required fields; restful-booker responds with a 5xx error.
        given(spec)
            .contentType(ContentType.JSON)
            .body(Map.of("firstname", "OnlyName"))
        .when()
            .post("/booking")
        .then()
            .statusCode(500);
    }

    @Test
    public void createBookingWithoutContentTypeIsRejected() {
        // No JSON Content-Type means the server cannot parse the body.
        given(spec)
            .body("firstname=John")
        .when()
            .post("/booking")
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(415), equalTo(418), equalTo(500)));
    }

    @Test
    public void updateNonExistentBookingReturnsError() {
        String token = BookingApi.authToken(spec, Credentials.username(), Credentials.password());

        given(spec)
            .contentType(ContentType.JSON)
            .cookie("token", token)
            .body(Map.of("firstname", "Ghost", "lastname", "User"))
        .when()
            .put("/booking/{id}", 9999999)
        .then()
            // No such record to update; server rejects rather than creating one.
            .statusCode(anyOf(equalTo(404), equalTo(405), equalTo(400), equalTo(500)));
    }

    @Test
    public void getBookingWithNonNumericIdReturns404() {
        given(spec)
        .when()
            .get("/booking/{id}", "abc")
        .then()
            .statusCode(404);
    }
}
