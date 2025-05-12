package org.casestudy.trade.controller;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.casestudy.trade.TradeApplication;
import org.casestudy.trade.persistence.entity.AssetEntity;
import org.casestudy.trade.persistence.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = TradeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerAssetControllerTest {

    @LocalServerPort
    private int port;

    private String baseUrl;
    private String token;

    @Autowired
    private AssetRepository assetRepository;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        baseUrl = "http://localhost:" + port;

        String loginJson = """
            {
              "username": "cus1",
              "password": "1234"
            }
        """;

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .post(baseUrl + "/auth/login");

        token = response.jsonPath().getString("token");

        assetRepository.deleteAll();
        assetRepository.save(new AssetEntity("cus1", "TRY", 1500, 1000));
        assetRepository.save(new AssetEntity("cus1", "THYAO", 500, 300));
    }

    @Test
    void shouldReturnCustomerAssets() {
        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(baseUrl + "/customer/assets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThanOrEqualTo(2))
                .body("assetName", hasItems("TRY", "THYAO"));
    }

    @Test
    void shouldRejectRequestWithoutToken() {
        RestAssured
                .given()
                .when()
                .get(baseUrl + "/customer/assets")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value()); // Forbidden
    }

    @Test
    void shouldReturnEmptyListIfNoAssets() {
        String loginJson = """
            {
              "username": "cus2",
              "password": "4567"
            }
        """;

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .post(baseUrl + "/auth/login");

        String newToken = response.jsonPath().getString("token");

        RestAssured
                .given()
                .header("Authorization", "Bearer " + newToken)
                .when()
                .get(baseUrl + "/customer/assets")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(0));
    }
}
