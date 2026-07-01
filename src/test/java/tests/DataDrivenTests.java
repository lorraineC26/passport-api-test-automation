package tests;

import config.BaseTest;
import helpers.BookingFactory;
import io.restassured.http.ContentType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Data-driven tests. Each DataProvider feeds one test method several input rows,
 * so a single method produces one independent test instance per row. This keeps
 * coverage broad without duplicating assertion logic.
 */
public class DataDrivenTests extends BaseTest {

    @DataProvider(name = "validBookingData")
    public Object[][] validBookingData() {
        return new Object[][]{
            {"John", "Smith", 100},
            {"Alice", "Brown", 250},
            {"Mei", "Chen", 1},
            {"Diego", "Garcia", 9999},
            {"Fatima", "Al-Sayed", 0},
            {"Olga", "Petrova", 500},
        };
    }

    @Test(dataProvider = "validBookingData")
    public void createBookingWithVariedGuests(String firstName, String lastName, int totalPrice) {
        given(spec)
            .contentType(ContentType.JSON)
            .body(BookingFactory.booking(firstName, lastName, totalPrice))
        .when()
            .post("/booking")
        .then()
            .statusCode(200)
            .body("bookingid", greaterThan(0))
            .body("booking.firstname", equalTo(firstName))
            .body("booking.lastname", equalTo(lastName))
            .body("booking.totalprice", equalTo(totalPrice));
    }

    @DataProvider(name = "invalidBookingIds")
    public Object[][] invalidBookingIds() {
        // Non-existent numeric ids and non-numeric paths both resolve to 404.
        return new Object[][]{
            {"9999998"},
            {"8888888"},
            {"0"},
            {"abc"},
            {"-1"},
        };
    }

    @Test(dataProvider = "invalidBookingIds")
    public void getBookingWithInvalidIdReturns404(String id) {
        given(spec)
        .when()
            .get("/booking/{id}", id)
        .then()
            .statusCode(404);
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        // Every combination is rejected; restful-booker answers 200 with a reason
        // and no token rather than a 401.
        return new Object[][]{
            {"wrong", "wrong"},
            {"admin", "wrongpassword"},
            {"nosuchuser", "password123"},
            {"admin", ""},
            {"", "password123"},
        };
    }

    @Test(dataProvider = "invalidCredentials")
    public void authWithInvalidCredentialsIsRejected(String username, String password) {
        given(spec)
            .contentType(ContentType.JSON)
            .body(Map.of("username", username, "password", password))
        .when()
            .post("/auth")
        .then()
            .statusCode(200)
            .body("reason", equalTo("Bad credentials"))
            .body("token", emptyOrNullString());
    }
}
