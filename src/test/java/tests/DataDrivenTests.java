package tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
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
public class DataDrivenTests {

    @BeforeClass
    public void setBaseUri() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
    }

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
        given()
            .contentType(ContentType.JSON)
            .accept("application/json")
            .body(booking(firstName, lastName, totalPrice))
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
        given()
            .accept("application/json")
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
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("username", username, "password", password))
        .when()
            .post("/auth")
        .then()
            .statusCode(200)
            .body("reason", equalTo("Bad credentials"))
            .body("token", emptyOrNullString());
    }

    private Map<String, Object> booking(String firstName, String lastName, int totalPrice) {
        Map<String, Object> dates = new HashMap<>();
        dates.put("checkin", "2025-01-01");
        dates.put("checkout", "2025-01-05");

        Map<String, Object> booking = new HashMap<>();
        booking.put("firstname", firstName);
        booking.put("lastname", lastName);
        booking.put("totalprice", totalPrice);
        booking.put("depositpaid", true);
        booking.put("bookingdates", dates);
        booking.put("additionalneeds", "Breakfast");
        return booking;
    }
}
