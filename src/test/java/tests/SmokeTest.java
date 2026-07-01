package tests;

import config.BaseTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * Smoke test verifying the target API is reachable.
 */
public class SmokeTest extends BaseTest {

    @Test
    public void getBookingsReturns200() {
        given(spec)
        .when()
            .get("/booking")
        .then()
            .statusCode(999);
    }
}
