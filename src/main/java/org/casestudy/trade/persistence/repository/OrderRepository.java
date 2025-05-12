package org.casestudy.trade.persistence.repository;

import org.casestudy.trade.enums.OrderStatus;
import org.casestudy.trade.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByCustomerName(String customerId);

    List<OrderEntity> findByCustomerNameAndCreateDateBetween(String customerName, LocalDateTime startDate, LocalDateTime endDate);

}

