package config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;

/**
 * Base class for all test classes. Holds the single shared RequestSpecification
 * so baseURI, the JSON Accept header, and timeouts are configured in one place
 * instead of being repeated in every test.
 *
 * Timeouts guard against the free-tier target host cold-starting or hanging:
 * a stalled request fails fast instead of blocking the whole suite.
 */
public abstract class BaseTest {

    protected static final String BASE_URI = "https://restful-booker.herokuapp.com";

    private static final int CONNECTION_TIMEOUT_MS = 10_000;
    private static final int SOCKET_TIMEOUT_MS = 20_000;

    protected static final RequestSpecification spec = buildSpec();

    private static RequestSpecification buildSpec() {
        RestAssuredConfig config = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", CONNECTION_TIMEOUT_MS)
                .setParam("http.socket.timeout", SOCKET_TIMEOUT_MS));

        // Use a single explicit Accept value. RestAssured's ContentType.JSON
        // expands to several media types, which restful-booker rejects with 418
        // on POST /booking.
        return new RequestSpecBuilder()
            .setBaseUri(BASE_URI)
            .addHeader("Accept", "application/json")
            .setConfig(config)
            .build();
    }
}
