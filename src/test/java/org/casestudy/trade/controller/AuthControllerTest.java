package org.casestudy.trade.controller;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.casestudy.trade.TradeApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = TradeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        String loginJson = """
            {
              "username": "cus1",
              "password": "1234"
            }
        """;

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post(baseUrl + "/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    void shouldFailLoginWithInvalidPassword() {
        String loginJson = """
            {
              "username": "cus1",
              "password": "wrongpassword"
            }
        """;

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post(baseUrl + "/auth/login")
                .then()
                .statusCode(401)
                .body(equalTo("Invalid credentials"));
    }

    @Test
    void shouldFailLoginWithUnknownUser() {
        String loginJson = """
            {
              "username": "unknown",
              "password": "1234"
            }
        """;

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post(baseUrl + "/auth/login")
                .then()
                .statusCode(401)
                .body(equalTo("Invalid credentials"));
    }

    @Test
    void shouldReturnBadRequestWhenUsernameMissing() {
        String loginJson = """
            {
              "password": "1234"
            }
        """;

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post(baseUrl + "/auth/login")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldReturnBadRequestWhenPasswordMissing() {
        String loginJson = """
            {
              "username": "cus1"
            }
        """;

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post(baseUrl + "/auth/login")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsEmpty() {
        String loginJson = "{}";

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post(baseUrl + "/auth/login")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldReturnUnsupportedMediaTypeWhenMissingContentType() {
        String loginJson = """
            {
              "username": "cus1",
              "password": "1234"
            }
        """;

        RestAssured
                .given()
                .body(loginJson)
                .when()
                .post(baseUrl + "/auth/login")
                .then()
                .statusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
    }
}
