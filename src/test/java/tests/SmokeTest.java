package tests;

import io.restassured.RestAssured;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * Smoke test verifying the target API is reachable.
 */
public class SmokeTest {

    @Test
    public void getBookingsReturns200() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";

        given()
        .when()
            .get("/booking")
        .then()
            .statusCode(200);
    }
}
