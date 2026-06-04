package app.backend.orderservice.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import app.backend.orderservice.order.entity.Order;
import app.backend.orderservice.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

	private final OrderJpaRepository jpaRepository;

	@Override
	public Order createOrder(final Order order) {

		return jpaRepository.save(order);
	}

	@Override
	public Optional<Order> findOrderById(final Long orderId) {

		return jpaRepository.findById(orderId);
	}

	@Override
	public List<Order> findOrderList(final Long itemId, final OrderStatus status) {

		return jpaRepository.findOrdersByItemIdAndStatus(itemId, status);
	}

	@Override
	public Long getOrderCountByItemId(final Long itemId) {

		return jpaRepository.countOrdersByItemId(itemId);
	}
}
