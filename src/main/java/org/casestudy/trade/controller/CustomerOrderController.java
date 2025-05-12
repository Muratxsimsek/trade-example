package org.casestudy.trade.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.casestudy.trade.dto.OrderRequest;
import org.casestudy.trade.persistence.entity.OrderEntity;
import org.casestudy.trade.queue.OrderMessage;
import org.casestudy.trade.queue.OrderQueueConsumer;
import org.casestudy.trade.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/customer/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerOrderController {

    private final OrderService orderService;

    private final OrderQueueConsumer orderQueueConsumer;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        String customerName = userDetails.getUsername();
        orderService.createOrder(customerName, request);
        //TODO Aslinda olmasi gereken yapi kuyruk seklinde istekleri alip tek tek islemektir. fakat testlerin askenkron calisan kodun test edilememesinden dolayi burayi commment'li birakiyorum
//      orderQueueConsumer.enqueue(new OrderMessage(userDetails.getUsername(), request));
//      return ResponseEntity.accepted().body("Order received.");

        return ResponseEntity.ok("Order created.");
    }

    @GetMapping
    public ResponseEntity<List<OrderEntity>> listOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @AuthenticationPrincipal UserDetails userDetails) {
        String customerName = userDetails.getUsername();
        return ResponseEntity.ok(orderService.getOrders(customerName, start, end));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, @AuthenticationPrincipal UserDetails userDetails) {
        String customerName = userDetails.getUsername();
        orderService.cancelOrder(customerName, orderId);
        return ResponseEntity.ok("Order canceled.");
    }
}
