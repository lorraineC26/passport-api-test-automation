package tests;

import helpers.Credentials;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Functional (positive) tests covering the booking CRUD lifecycle.
 * Each test creates the data it needs so tests stay independent.
 */
public class BookingTests {

    @BeforeClass
    public void setBaseUri() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
    }

    @Test
    public void getAllBookingsReturnsList() {
        given()
            .accept("application/json")
        .when()
            .get("/booking")
        .then()
            .statusCode(200)
            .body("bookingid", notNullValue());
    }

    @Test
    public void createBookingReturnsBookingId() {
        given()
            .contentType(ContentType.JSON)
            .accept("application/json")
            .body(sampleBooking("John", "Smith"))
        .when()
            .post("/booking")
        .then()
            .statusCode(200)
            .body("bookingid", greaterThan(0))
            .body("booking.firstname", equalTo("John"))
            .body("booking.lastname", equalTo("Smith"));
    }

    @Test
    public void getBookingByIdReturnsPersistedFields() {
        int id = createBooking(sampleBooking("Alice", "Brown"));

        given()
            .accept("application/json")
        .when()
            .get("/booking/{id}", id)
        .then()
            .statusCode(200)
            .body("firstname", equalTo("Alice"))
            .body("lastname", equalTo("Brown"))
            .body("bookingdates.checkin", equalTo("2025-01-01"));
    }

    @Test
    public void filterBookingsByNameReturnsMatch() {
        // Unique last name so the filter matches only this record.
        String lastName = "Filterby" + System.nanoTime();
        int id = createBooking(sampleBooking("Grace", lastName));

        given()
            .accept("application/json")
            .queryParam("firstname", "Grace")
            .queryParam("lastname", lastName)
        .when()
            .get("/booking")
        .then()
            .statusCode(200)
            .body("bookingid", org.hamcrest.Matchers.hasItem(id));
    }

    @Test
    public void updateBookingWithTokenAppliesChanges() {
        int id = createBooking(sampleBooking("Mark", "Green"));
        String token = getAuthToken();

        Map<String, Object> updated = sampleBooking("Mark", "Grey");
        updated.put("totalprice", 999);

        given()
            .contentType(ContentType.JSON)
            .accept("application/json")
            .cookie("token", token)
            .body(updated)
        .when()
            .put("/booking/{id}", id)
        .then()
            .statusCode(200)
            .body("lastname", equalTo("Grey"))
            .body("totalprice", equalTo(999));
    }

    @Test
    public void partialUpdateBookingWithTokenAppliesChanges() {
        int id = createBooking(sampleBooking("Nina", "Black"));
        String token = getAuthToken();

        given()
            .contentType(ContentType.JSON)
            .accept("application/json")
            .cookie("token", token)
            .body(Map.of("firstname", "Nadia"))
        .when()
            .patch("/booking/{id}", id)
        .then()
            .statusCode(200)
            .body("firstname", equalTo("Nadia"))
            .body("lastname", equalTo("Black"));
    }

    @Test
    public void deleteBookingWithTokenSucceeds() {
        int id = createBooking(sampleBooking("Oscar", "White"));
        String token = getAuthToken();

        given()
            .cookie("token", token)
        .when()
            .delete("/booking/{id}", id)
        .then()
            .statusCode(201);

        // Confirm it is gone.
        given()
            .accept("application/json")
        .when()
            .get("/booking/{id}", id)
        .then()
            .statusCode(404);
    }

    // --- helpers kept local to this class until the phase 4 refactor ---

    private Map<String, Object> sampleBooking(String firstName, String lastName) {
        Map<String, Object> dates = new HashMap<>();
        dates.put("checkin", "2025-01-01");
        dates.put("checkout", "2025-01-05");

        Map<String, Object> booking = new HashMap<>();
        booking.put("firstname", firstName);
        booking.put("lastname", lastName);
        booking.put("totalprice", 150);
        booking.put("depositpaid", true);
        booking.put("bookingdates", dates);
        booking.put("additionalneeds", "Breakfast");
        return booking;
    }

    private int createBooking(Map<String, Object> payload) {
        return given()
            .contentType(ContentType.JSON)
            .accept("application/json")
            .body(payload)
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
