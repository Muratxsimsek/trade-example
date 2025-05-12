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

import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = TradeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminAssetControllerTest {

    @LocalServerPort
    private int port;

    private String baseUrl;
    private String adminToken;
    private String customerName = "cus3";

    @Autowired
    private AssetRepository assetRepository;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        baseUrl = "http://localhost:" + port;

        // Login as admin
        String loginJson = """
            {
              "username": "admin",
              "password": "adminpass"
            }
        """;

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .post(baseUrl + "/auth/login");

        adminToken = response.jsonPath().getString("token");

        assetRepository.save(new AssetEntity(customerName, "THYAO", 100, 50));
        assetRepository.save(new AssetEntity(customerName, "TRY", 2000, 2000));
    }

    @Test
    void shouldReturnAssetsForGivenCustomerId() {
        RestAssured
                .given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(baseUrl + "/admin/assets/" + customerName)
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2))
                .body("assetName", hasItems("THYAO", "TRY"));
    }

    @Test
    void shouldReturnAllAssets() {
        RestAssured
                .given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(baseUrl + "/admin/assets/all")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    void shouldRejectAccessIfNoAdminRole() {
        String loginJson = """
            {
              "username": "cus1",
              "password": "1234"
            }
        """;

        String customerToken = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .post(baseUrl + "/auth/login")
                .jsonPath()
                .getString("token");

        RestAssured
                .given()
                .header("Authorization", "Bearer " + customerToken)
                .when()
                .get(baseUrl + "/admin/assets/all")
                .then()
                .statusCode(403);
    }

    @Test
    void shouldReturnEmptyListIfNoAssetsForCustomer() {
        String noAssetCustomer = "nonexist";
        RestAssured
                .given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(baseUrl + "/admin/assets/" + noAssetCustomer)
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }
}
