package com.order_service.repository;

import com.order_service.entities.Order;
import com.order_service.entities.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Integer> {
    Optional<Order> findByOrderId(Integer orderId);

    List<OrderLine> findAllByOrderId(Integer orderId);
}
