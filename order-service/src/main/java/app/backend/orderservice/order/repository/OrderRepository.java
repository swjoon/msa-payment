package app.backend.orderservice.order.repository;

import java.util.List;
import java.util.Optional;

import app.backend.orderservice.order.entity.Order;
import app.backend.orderservice.order.entity.OrderStatus;

public interface OrderRepository {

	Order createOrder(Order order);

	Optional<Order> findOrderById(Long id);

	List<Order> findOrderList(Long itemId, OrderStatus status);

	Long getOrderCountByItemId(Long itemId);
}
