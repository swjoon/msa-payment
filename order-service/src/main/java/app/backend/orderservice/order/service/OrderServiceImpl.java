package app.backend.orderservice.order.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.backend.orderservice.global.util.UuidUtil;
import app.backend.orderservice.order.dto.req.CreateOrderReqDto;
import app.backend.orderservice.order.dto.res.CreateOrderResDto;
import app.backend.orderservice.order.dto.res.GetOrderResDto;
import app.backend.orderservice.order.entity.Order;
import app.backend.orderservice.order.entity.OrderStatus;
import app.backend.orderservice.order.exception.OrderErrorCode;
import app.backend.orderservice.order.exception.OrderException;
import app.backend.orderservice.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;

	@Override
	@Transactional
	public CreateOrderResDto createOrder(final CreateOrderReqDto createOrderReqDto) {

		Order order = orderRepository.createOrder(Order.create(
			createOrderReqDto.memberId(),
			createOrderReqDto.itemId(),
			UuidUtil.getUUIDWithLocalDateTimeFormat(5),
			createOrderReqDto.stock(),
			createOrderReqDto.totalPrice(),
			LocalDateTime.now()
		));

		return CreateOrderResDto.from(order);
	}

	@Override
	@Transactional(readOnly = true)
	public GetOrderResDto readOrder(final Long orderId) {

		return GetOrderResDto.from(getOrderEntity(orderId));
	}

	@Override
	@Transactional(readOnly = true)
	public Order getOrder(final Long orderId) {

		return getOrderEntity(orderId);
	}

	@Override
	@Transactional
	public Order confirmOrder(final Long orderId) {
		Order order = getOrderEntity(orderId);

		order.confirm();

		return order;
	}

	@Override
	@Transactional
	public Order pendingOrder(final Long orderId) {
		Order order = getOrderEntity(orderId);

		order.pending();

		return order;
	}

	@Override
	@Transactional
	public void rejectOrder(final Long orderId) {
		Order order = getOrderEntity(orderId);

		order.reject();
	}

	@Override
	@Transactional
	public Order cancelOrder(final Long orderId) {
		Order order = getOrderEntity(orderId);

		order.cancel();

		return order;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Order> getOrderTestResult(final Long itemId, final OrderStatus orderStatus) {

		return orderRepository.findOrderList(itemId, orderStatus);
	}

	@Override
	public Long getOrderCountByItemId(final Long itemId) {

		return orderRepository.getOrderCountByItemId(itemId);
	}

	/**
	 * Order 정보 조회 메서드
	 *
	 * @param orderId - ID
	 * @return 주문정보
	 */
	private Order getOrderEntity(final Long orderId) {

		return orderRepository.findOrderById(orderId).orElseThrow(
			() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND)
		);
	}

}
