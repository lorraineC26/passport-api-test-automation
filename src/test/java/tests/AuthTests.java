package tests;

import helpers.Credentials;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Authentication tests for POST /auth.
 * restful-booker returns the session token in the JSON body, which is then
 * sent back as a Cookie (token=...) on write operations.
 */
public class AuthTests {

    @BeforeClass
    public void setBaseUri() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
    }

    @Test
    public void authWithValidCredentialsReturnsToken() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("username", Credentials.username(), "password", Credentials.password()))
        .when()
            .post("/auth")
        .then()
            .statusCode(200)
            .body("token", not(emptyOrNullString()));
    }

    @Test
    public void authWithInvalidCredentialsReturnsBadCredentials() {
        // restful-booker answers 200 with a "reason" field instead of a 401.
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("username", "wrong", "password", "wrong"))
        .when()
            .post("/auth")
        .then()
            .statusCode(200)
            .body("reason", equalTo("Bad credentials"))
            .body("token", emptyOrNullString());
    }

    @Test
    public void authWithMissingPasswordReturnsBadCredentials() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("username", Credentials.username()))
        .when()
            .post("/auth")
        .then()
            .statusCode(200)
            .body("reason", equalTo("Bad credentials"));
    }
}
