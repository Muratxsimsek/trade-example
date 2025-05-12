package org.casestudy.trade.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.casestudy.trade.TradeApplication;
import org.casestudy.trade.enums.OrderSide;
import org.casestudy.trade.enums.OrderStatus;
import org.casestudy.trade.persistence.entity.OrderEntity;
import org.casestudy.trade.persistence.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = TradeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminOrderControllerTest {

    @LocalServerPort
    private int port;

    private String baseUrl;
    private String adminToken;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        baseUrl = "http://localhost:" + port;

        String loginJson = "{ \"username\": \"admin\", \"password\": \"adminpass\" }";

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .post(baseUrl + "/auth/login");

        adminToken = response.jsonPath().getString("token");
    }

    @Test
    void shouldMatchOrderSuccessfully() {
        OrderEntity order = new OrderEntity();
        order.setCustomerName("cus1");
        order.setAssetName("THYAO");
        order.setSide(OrderSide.BUY);
        order.setSize(5);
        order.setPrice(BigDecimal.valueOf(100));
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        orderRepository.save(order);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post(baseUrl + "/admin/orders/match/" + order.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Order matched successfully."));
    }

    @Test
    void shouldFailToMatchNonPendingOrder() {
        OrderEntity order = new OrderEntity();
        order.setCustomerName("cus1");
        order.setAssetName("THYAO");
        order.setSide(OrderSide.BUY);
        order.setSize(5);
        order.setPrice(BigDecimal.valueOf(100));
        order.setStatus(OrderStatus.CANCELED);
        order.setCreateDate(LocalDateTime.now());
        orderRepository.save(order);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post(baseUrl + "/admin/orders/match/" + order.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(containsString("Only PENDING orders can be matched"));
    }

    @Test
    void shouldFailToMatchInvalidOrderId() {
        Long invalidId = 9999L;

        RestAssured
                .given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post(baseUrl + "/admin/orders/match/" + invalidId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(containsString("Order not found"));
    }

    @Test
    void shouldListAllOrders() {
        RestAssured
                .given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(baseUrl + "/admin/orders/all")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void shouldRejectUnauthorizedAccess() {
        RestAssured
                .given()
                .when()
                .get(baseUrl + "/admin/orders/all")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldRejectNonAdminUser() {
        String loginJson = "{ \"username\": \"cus1\", \"password\": \"1234\" }";

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .post(baseUrl + "/auth/login");

        String customerToken = response.jsonPath().getString("token");

        RestAssured
                .given()
                .header("Authorization", "Bearer " + customerToken)
                .when()
                .get(baseUrl + "/admin/orders/all")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
