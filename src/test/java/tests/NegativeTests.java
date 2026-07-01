package tests;

import helpers.Credentials;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

/**
 * Negative tests: the API must reject invalid input and unauthorized writes.
 */
public class NegativeTests {

    @BeforeClass
    public void setBaseUri() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
    }

    @Test
    public void getBookingWithNonExistentIdReturns404() {
        given()
            .accept("application/json")
        .when()
            .get("/booking/{id}", 9999999)
        .then()
            .statusCode(404);
    }

    @Test
    public void deleteWithoutTokenReturns403() {
        int id = createBooking();

        given()
        .when()
            .delete("/booking/{id}", id)
        .then()
            .statusCode(403);
    }

    @Test
    public void updateWithoutTokenReturns403() {
        int id = createBooking();

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("firstname", "NoAuth"))
        .when()
            .put("/booking/{id}", id)
        .then()
            .statusCode(403);
    }

    @Test
    public void deleteWithInvalidTokenReturns403() {
        int id = createBooking();

        given()
            .cookie("token", "not-a-real-token")
        .when()
            .delete("/booking/{id}", id)
        .then()
            .statusCode(403);
    }

    @Test
    public void createBookingWithMissingFieldsIsRejected() {
        // Missing required fields; restful-booker responds with a 5xx error.
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("firstname", "OnlyName"))
        .when()
            .post("/booking")
        .then()
            .statusCode(500);
    }

    @Test
    public void createBookingWithoutContentTypeIsRejected() {
        // No Content-Type means the server cannot parse the body.
        given()
            .body("firstname=John")
        .when()
            .post("/booking")
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(415), equalTo(418), equalTo(500)));
    }

    @Test
    public void updateNonExistentBookingReturnsError() {
        String token = getAuthToken();

        given()
            .contentType(ContentType.JSON)
            .accept("application/json")
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
        given()
            .accept("application/json")
        .when()
            .get("/booking/{id}", "abc")
        .then()
            .statusCode(404);
    }

    // --- local helpers, extracted in the phase 4 refactor ---

    private int createBooking() {
        Map<String, Object> dates = new HashMap<>();
        dates.put("checkin", "2025-01-01");
        dates.put("checkout", "2025-01-05");

        Map<String, Object> booking = new HashMap<>();
        booking.put("firstname", "Temp");
        booking.put("lastname", "Record");
        booking.put("totalprice", 100);
        booking.put("depositpaid", true);
        booking.put("bookingdates", dates);

        return given()
            .contentType(ContentType.JSON)
            .accept("application/json")
            .body(booking)
        .when()
            .post("/booking")
        .then()
            .statusCode(200)
            .extract().path("bookingid");
    }

    private String getAuthToken() {
        return given()
            .contentType(ContentType.JSON)
            .body(Map.of("username", Credentials.username(), "password", Credentials.password()))
        .when()
            .post("/auth")
        .then()
            .statusCode(200)
            .extract().path("token");
    }
}
