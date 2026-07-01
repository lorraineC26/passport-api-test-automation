package tests;

import config.BaseTest;
import helpers.BookingApi;
import helpers.BookingFactory;
import helpers.Credentials;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Functional (positive) tests covering the booking CRUD lifecycle.
 * Each test creates the data it needs so tests stay independent.
 */
public class BookingTests extends BaseTest {

    @Test
    public void getAllBookingsReturnsList() {
        given(spec)
        .when()
            .get("/booking")
        .then()
            .statusCode(200)
            .body("bookingid", notNullValue());
    }

    @Test
    public void createBookingReturnsBookingId() {
        given(spec)
            .contentType(ContentType.JSON)
            .body(BookingFactory.booking("John", "Smith"))
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
        int id = BookingApi.createBooking(spec, BookingFactory.booking("Alice", "Brown"));

        given(spec)
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
        int id = BookingApi.createBooking(spec, BookingFactory.booking("Grace", lastName));

        given(spec)
            .queryParam("firstname", "Grace")
            .queryParam("lastname", lastName)
        .when()
            .get("/booking")
        .then()
            .statusCode(200)
            .body("bookingid", hasItem(id));
    }

    @Test
    public void updateBookingWithTokenAppliesChanges() {
        int id = BookingApi.createBooking(spec, BookingFactory.booking("Mark", "Green"));
        String token = BookingApi.authToken(spec, Credentials.username(), Credentials.password());

        Map<String, Object> updated = BookingFactory.booking("Mark", "Grey", 999);

        given(spec)
            .contentType(ContentType.JSON)
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
        int id = BookingApi.createBooking(spec, BookingFactory.booking("Nina", "Black"));
        String token = BookingApi.authToken(spec, Credentials.username(), Credentials.password());

        given(spec)
            .contentType(ContentType.JSON)
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
        int id = BookingApi.createBooking(spec, BookingFactory.booking("Oscar", "White"));
        String token = BookingApi.authToken(spec, Credentials.username(), Credentials.password());

        given(spec)
            .cookie("token", token)
        .when()
            .delete("/booking/{id}", id)
        .then()
            .statusCode(201);

        // Confirm it is gone.
        given(spec)
        .when()
            .get("/booking/{id}", id)
        .then()
            .statusCode(404);
    }
}
