//package org.casestudy.trade.controller;
//
//
//import io.restassured.RestAssured;
//import io.restassured.http.ContentType;
//import io.restassured.response.Response;
//import org.casestudy.trade.TradeApplication;
//import org.casestudy.trade.dto.OrderRequest;
//import org.casestudy.trade.enums.OrderSide;
//import org.casestudy.trade.enums.OrderStatus;
//import org.casestudy.trade.persistence.entity.AssetEntity;
//import org.casestudy.trade.persistence.entity.OrderEntity;
//import org.casestudy.trade.persistence.repository.AssetRepository;
//import org.casestudy.trade.persistence.repository.OrderRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.server.LocalServerPort;
//
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//import static org.hamcrest.Matchers.*;
//
//@SpringBootTest(classes = TradeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class CustomerOrderControllerTest {
//
//    @LocalServerPort
//    private int port;
//
//    private String baseUrl;
//    private String token;
//
//    @Autowired
//    private AssetRepository assetRepository;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//
//    @BeforeEach
//    void setup() {
//        RestAssured.baseURI = "http://localhost";
//        baseUrl = "http://localhost:" + port;
//
//        String loginJson = "{ \"username\": \"cus1\", \"password\": \"1234\" }";
//
//        Response response = RestAssured
//                .given()
//                .contentType(ContentType.JSON)
//                .body(loginJson)
//                .when()
//                .post(baseUrl + "/auth/login");
//
//        token = response.jsonPath().getString("token");
//
//        AssetEntity asset = new AssetEntity("cus1", "TRY", 1500, 1000);
//        assetRepository.save(asset);
//
//    }
//
//    @Test
//    void shouldCreateOrderSuccessfully() {
//        OrderRequest request = new OrderRequest();
//        request.setAssetName("THYAO");
//        request.setSide(OrderSide.BUY);
//        request.setSize(5);
//        request.setPrice(BigDecimal.valueOf(100));
//
//        RestAssured
//                .given()
//                .contentType(ContentType.JSON)
//                .header("Authorization", "Bearer " + token)
//                .body(request)
//                .when()
//                .post(baseUrl + "/customer/orders")
//                .then()
//                .statusCode(HttpStatus.OK.value())
//                .body(equalTo("Order created."));
//    }
//
//    @Test
//    void shouldListOwnOrders() {
//        RestAssured
//                .given()
//                .header("Authorization", "Bearer " + token)
//                .when()
//                .get(baseUrl + "/customer/orders")
//                .then()
//                .statusCode(HttpStatus.OK.value())
//                .body("size()", greaterThanOrEqualTo(0));
//    }
//
//    @Test
//    void shouldFailToBuyWithInsufficientTRY() {
//        OrderRequest request = new OrderRequest();
//        request.setAssetName("THYAO");
//        request.setSide(OrderSide.BUY);
//        request.setSize(HttpStatus.OK.value()0);
//        request.setPrice(BigDecimal.valueOf(100));
//
//        RestAssured
//                .given()
//                .contentType(ContentType.JSON)
//                .header("Authorization", "Bearer " + token)
//                .body(request)
//                .when()
//                .post(baseUrl + "/customer/orders")
//                .then()
//                .statusCode(HttpStatus.BAD_REQUEST.value())
//                .body(containsString("Not enough TRY"));
//    }
//
//    @Test
//    void shouldCancelOwnOrder() {
//        shouldCreateOrderSuccessfully();
//
//        Long orderId = orderRepository.findAll().stream()
//                .filter(o -> o.getCustomerName().equals("cus1"))
//                .findFirst()
//                .map(o -> o.getId())
//                .orElseThrow();
//
//        RestAssured
//                .given()
//                .header("Authorization", "Bearer " + token)
//                .when()
//                .delete(baseUrl + "/customer/orders/" + orderId)
//                .then()
//                .statusCode(HttpStatus.OK.value())
//                .body(equalTo("Order canceled."));
//    }
//
//    @Test
//    void shouldRejectWithoutAuth() {
//        RestAssured
//                .given()
//                .when()
//                .get(baseUrl + "/customer/orders")
//                .then()
//                .statusCode(403);
//    }
//
//    @Test
//    void shouldFailWhenRequestIsInvalid() {
//        OrderRequest request = new OrderRequest();
//        request.setAssetName(null);
//        request.setSide(OrderSide.BUY);
//        request.setSize(5);
//        request.setPrice(BigDecimal.valueOf(100));
//
//        RestAssured
//                .given()
//                .contentType(ContentType.JSON)
//                .header("Authorization", "Bearer " + token)
//                .body(request)
//                .when()
//                .post(baseUrl + "/customer/orders")
//                .then()
//                .statusCode(HttpStatus.BAD_REQUEST.value());
//    }
//
//    @Test
//    void shouldListOrdersBetweenDates() {
//        RestAssured
//                .given()
//                .header("Authorization", "Bearer " + token)
//                .queryParam("start", "2024-01-01T00:00:00")
//                .queryParam("end", "2030-01-01T00:00:00")
//                .when()
//                .get(baseUrl + "/customer/orders")
//                .then()
//                .statusCode(HttpStatus.OK.value())
//                .body("size()", greaterThanOrEqualTo(0));
//    }
//
//    @Test
//    void shouldFailToSellWithoutEnoughAsset() {
//        OrderRequest request = new OrderRequest();
//        request.setAssetName("THYAO");
//        request.setSide(OrderSide.SELL);
//        request.setSize(10);
//        request.setPrice(BigDecimal.valueOf(100));
//
//        RestAssured
//                .given()
//                .contentType(ContentType.JSON)
//                .header("Authorization", "Bearer " + token)
//                .body(request)
//                .when()
//                .post(baseUrl + "/customer/orders")
//                .then()
//                .statusCode(HttpStatus.BAD_REQUEST.value())
//                .body(containsString("No asset to sell"));
//    }
//
//    @Test
//    void shouldFailToCancelCompletedOrder() {
//        OrderEntity order = new OrderEntity();
//        order.setCustomerName("cus1");
//        order.setAssetName("THYAO");
//        order.setSide(OrderSide.BUY);
//        order.setSize(10);
//        order.setPrice(BigDecimal.valueOf(100));
//        order.setStatus(OrderStatus.MATCHED);
//        order.setCreateDate(LocalDateTime.now());
//
//        orderRepository.save(order);
//
//        RestAssured
//                .given()
//                .header("Authorization", "Bearer " + token)
//                .when()
//                .delete(baseUrl + "/customer/orders/" + order.getId())
//                .then()
//                .statusCode(HttpStatus.BAD_REQUEST.value())
//                .body(containsString("Only PENDING orders can be canceled"));
//    }
//
//
//}
//


package org.casestudy.trade.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.casestudy.trade.TradeApplication;
import org.casestudy.trade.dto.OrderRequest;
import org.casestudy.trade.enums.OrderSide;
import org.casestudy.trade.enums.OrderStatus;
import org.casestudy.trade.persistence.entity.AssetEntity;
import org.casestudy.trade.persistence.entity.OrderEntity;
import org.casestudy.trade.persistence.repository.AssetRepository;
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
public class CustomerOrderControllerTest {

    @LocalServerPort
    private int port;

    private String baseUrl;
    private String token;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        baseUrl = "http://localhost:" + port;

        String loginJson = "{ \"username\": \"cus1\", \"password\": \"1234\" }";

        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post(baseUrl + "/auth/login");

        token = response.jsonPath().getString("token");

        assetRepository.deleteAll();
        AssetEntity asset = new AssetEntity("cus1", "TRY", 1500, 1000);
        assetRepository.save(asset);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        OrderRequest request = new OrderRequest("THYAO", OrderSide.BUY, 5, BigDecimal.valueOf(100));

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .post(baseUrl + "/customer/orders")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Order created."));
    }

    @Test
    void shouldListOwnOrders() {
        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(baseUrl + "/customer/orders")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void shouldFailToBuyWithInsufficientTRY() {
        OrderRequest request = new OrderRequest("THYAO", OrderSide.BUY, HttpStatus.OK.value(), BigDecimal.valueOf(100));

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .post(baseUrl + "/customer/orders")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(containsString("Not enough TRY"));
    }

    @Test
    void shouldCancelOwnOrder() {
        shouldCreateOrderSuccessfully();

        Long orderId = orderRepository.findAll().stream()
                .filter(o -> o.getCustomerName().equals("cus1"))
                .findFirst()
                .map(OrderEntity::getId)
                .orElseThrow();

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(baseUrl + "/customer/orders/" + orderId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("Order canceled."));
    }

    @Test
    void shouldRejectWithoutAuth() {
        RestAssured
                .given()
                .when()
                .get(baseUrl + "/customer/orders")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldFailWhenRequestIsInvalid() {
        OrderRequest request = new OrderRequest(null, OrderSide.BUY, 5, BigDecimal.valueOf(100));

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .post(baseUrl + "/customer/orders")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldListOrdersBetweenDates() {
        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .queryParam("start", "2024-01-01T00:00:00")
                .queryParam("end", "2030-01-01T00:00:00")
                .when()
                .get(baseUrl + "/customer/orders")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void shouldFailToSellWithoutEnoughAsset() {
        OrderRequest request = new OrderRequest("THYAO", OrderSide.SELL, 10, BigDecimal.valueOf(100));

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(request)
                .when()
                .post(baseUrl + "/customer/orders")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(containsString("No asset to sell"));
    }

    @Test
    void shouldFailToCancelCompletedOrder() {
        OrderEntity order = new OrderEntity();
        order.setCustomerName("cus1");
        order.setAssetName("THYAO");
        order.setSide(OrderSide.BUY);
        order.setSize(10);
        order.setPrice(BigDecimal.valueOf(100));
        order.setStatus(OrderStatus.MATCHED);
        order.setCreateDate(LocalDateTime.now());
        orderRepository.save(order);

        RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(baseUrl + "/customer/orders/" + order.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(containsString("Only PENDING orders can be canceled"));
    }

    @Test
    void shouldMatchSellOrderSuccessfully() {

        AssetEntity asset = new AssetEntity("cus1", "THYAO", 50, 50);
        assetRepository.save(asset);

        // SELL emri oluştur
        OrderEntity order = new OrderEntity();
        order.setCustomerName("cus1");
        order.setAssetName("THYAO");
        order.setSide(OrderSide.SELL);
        order.setSize(10);
        order.setPrice(BigDecimal.valueOf(100));
        order.setCreateDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        // Admin login token al
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
                .when()
                .post(baseUrl + "/auth/login");

        String adminToken = response.jsonPath().getString("token");

        RestAssured
                .given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post(baseUrl + "/admin/orders/match/" + order.getId())
                .then()
                .statusCode(200)
                .body(equalTo("Order matched successfully."));
    }

}

