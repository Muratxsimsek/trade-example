package org.casestudy.trade.service;

import lombok.RequiredArgsConstructor;
import org.casestudy.trade.dto.OrderRequest;
import org.casestudy.trade.enums.OrderSide;
import org.casestudy.trade.enums.OrderStatus;
import org.casestudy.trade.persistence.entity.AssetEntity;
import org.casestudy.trade.persistence.entity.OrderEntity;
import org.casestudy.trade.persistence.repository.AssetRepository;
import org.casestudy.trade.persistence.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    @Transactional
    public void matchOrder(Long orderId) {
        logger.info("Matching order with ID: {}", orderId);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new IllegalArgumentException("Only PENDING orders can be matched.");
        }

        String customerId = order.getCustomerName();
        String assetName = order.getAssetName();
        int size = order.getSize();
        BigDecimal price = order.getPrice();

        if (order.getSide().equals(OrderSide.BUY)) {

            AssetEntity asset = assetRepository.findByIdCustomerNameAndIdAssetName(customerId, assetName)
                    .orElse(new AssetEntity(customerId, assetName, 0, 0));

            asset.setSize(asset.getSize() + size);
            asset.setUsableSize(asset.getUsableSize() + size);

            assetRepository.save(asset);

        } else if (order.getSide().equals(OrderSide.SELL)) {

            AssetEntity asset = assetRepository.findByIdCustomerNameAndIdAssetName(customerId, assetName)
                    .orElseThrow(() -> new IllegalArgumentException("No asset to sell"));

            asset.setSize(asset.getSize() - size);
            asset.setUsableSize(asset.getUsableSize() - size);
            assetRepository.save(asset);

            BigDecimal totalValue = price.multiply(BigDecimal.valueOf(size));
            AssetEntity tryAsset = assetRepository.findByIdCustomerNameAndIdAssetName(customerId, "TRY")
                    .orElse(new AssetEntity(customerId, "TRY", 0, 0));

            tryAsset.setSize(tryAsset.getSize() + totalValue.intValue());
            tryAsset.setUsableSize(tryAsset.getUsableSize() + totalValue.intValue());
            assetRepository.save(tryAsset);
        }

        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);
        logger.info("Order with ID {} matched successfully.", orderId);
    }


    @Transactional
    public void createOrder(String customerName, OrderRequest request) {
        logger.info("Creating order for customer: {}", customerName);
        String assetName = request.getAssetName();
        int size = request.getSize();
        BigDecimal price = request.getPrice();

        if (request.getSide() == OrderSide.BUY) {
            AssetEntity tryAsset = assetRepository.findByIdCustomerNameAndIdAssetName(customerName, "TRY")
                    .orElseThrow(() -> new IllegalArgumentException("No TRY asset found"));
            BigDecimal totalCost = price.multiply(BigDecimal.valueOf(size));

            if (tryAsset.getUsableSize() < totalCost.intValue()) {
                throw new IllegalArgumentException("Not enough TRY to buy");
            }
            tryAsset.setUsableSize(tryAsset.getUsableSize() - totalCost.intValue());
            assetRepository.save(tryAsset);
        }

        if (request.getSide() == OrderSide.SELL) {
            AssetEntity asset = assetRepository.findByIdCustomerNameAndIdAssetName(customerName, assetName)
                    .orElseThrow(() -> new IllegalArgumentException("No asset to sell"));
            if (asset.getUsableSize() < size) {
                throw new IllegalArgumentException("Not enough shares to sell");
            }
            asset.setUsableSize(asset.getUsableSize() - size);
            assetRepository.save(asset);
        }

        OrderEntity order = new OrderEntity();
        order.setCustomerName(customerName);
        order.setAssetName(assetName);
        order.setSide(request.getSide());
        order.setSize(size);
        order.setPrice(price);
        order.setCreateDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        orderRepository.save(order);
        logger.info("Order created successfully for customer: {}", customerName);
    }

    public List<OrderEntity> getOrders(String customerId, LocalDateTime start, LocalDateTime end) {
        logger.info("Fetching orders for customer: {} between {} and {}", customerId, start, end);
        if (start != null && end != null) {
            return orderRepository.findByCustomerNameAndCreateDateBetween(customerId, start, end);
        }
        return orderRepository.findByCustomerName(customerId);
    }

    @Transactional
    public void cancelOrder(String customerId, Long orderId) {
        logger.info("Canceling order with ID: {} for customer: {}", orderId, customerId);
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getCustomerName().equals(customerId)) {
            throw new AccessDeniedException("Cannot cancel other customer's order");
        }

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new IllegalArgumentException("Only PENDING orders can be canceled");
        }

        if (order.getSide() == OrderSide.BUY) {
            AssetEntity tryAsset = assetRepository.findByIdCustomerNameAndIdAssetName(customerId, "TRY")
                    .orElseThrow();
            int refund = order.getPrice().multiply(BigDecimal.valueOf(order.getSize())).intValue();
            tryAsset.setUsableSize(tryAsset.getUsableSize() + refund);
            assetRepository.save(tryAsset);
        } else if (order.getSide() == OrderSide.SELL) {
            AssetEntity asset = assetRepository.findByIdCustomerNameAndIdAssetName(customerId, order.getAssetName())
                    .orElseThrow();
            asset.setUsableSize(asset.getUsableSize() + order.getSize());
            assetRepository.save(asset);
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
        logger.info("Order with ID {} canceled successfully.", orderId);
    }

    public List<OrderEntity> getAllOrders() {
        logger.info("Fetching all orders.");
        return orderRepository.findAll();
    }
}
