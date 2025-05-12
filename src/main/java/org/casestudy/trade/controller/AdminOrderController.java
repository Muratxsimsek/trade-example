package org.casestudy.trade.controller;

import lombok.RequiredArgsConstructor;
import org.casestudy.trade.persistence.entity.OrderEntity;
import org.casestudy.trade.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    private final OrderService orderService;

    @PostMapping("/match/{orderId}")
    public ResponseEntity<String> matchOrder(@PathVariable Long orderId) {
        logger.info("Admin matching order with ID: {}", orderId);
        try {
            orderService.matchOrder(orderId);
            logger.info("Order with ID {} matched successfully.", orderId);
            return ResponseEntity.ok("Order matched successfully.");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to match order with ID {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderEntity>> listAllOrders() {
        logger.info("Admin fetching all orders.");
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}

