package app.backend.orderservice.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import app.backend.orderservice.order.entity.Order;
import app.backend.orderservice.order.entity.OrderStatus;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

	List<Order> findOrdersByItemIdAndStatus(Long itemId, OrderStatus status);

	Long countOrdersByItemId(Long itemId);
}
