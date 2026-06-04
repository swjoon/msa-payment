package app.backend.orderservice.order.event;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.backend.orderservice.infrastructure.kafka.constants.AggregateTypes;
import app.backend.orderservice.infrastructure.kafka.constants.EventTopics;
import app.backend.orderservice.infrastructure.kafka.constants.EventTypes;
import app.backend.orderservice.infrastructure.kafka.event.dto.request.OrderItemReleaseIfDeductedEvent;
import app.backend.orderservice.infrastructure.kafka.event.dto.request.OrderItemReleasedEvent;
import app.backend.orderservice.infrastructure.kafka.event.dto.request.PaymentCheckedEvent;
import app.backend.orderservice.infrastructure.kafka.outbox.service.OutboxEventService;
import app.backend.orderservice.order.entity.Order;
import app.backend.orderservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderEventServiceImpl implements OrderEventService {

	private final OrderService orderService;
	private final OutboxEventService outboxEventService;

	@Override
	@Transactional
	public Order rejectOrderWithItemReleaseEvent(
		final Long orderId,
		final Long itemId,
		final int stock,
		final String commandId
	) {

		Order order = orderService.getOrder(orderId);

		order.reject();

		OrderItemReleasedEvent event = OrderItemReleasedEvent.from(commandId, itemId, stock);

		outboxEventService.publish(
			AggregateTypes.ITEM,
			String.valueOf(itemId),
			EventTypes.ORDER_ITEM_RELEASE_REQUESTED,
			EventTopics.ORDER_ITEM_RELEASE_REQUESTED,
			event
		);

		return order;
	}

	@Override
	@Transactional
	public void rejectOrderWithItemReleaseIfDeductedEvent(
		final Long orderId,
		final Long itemId,
		final int stock,
		final String decreaseCommandId,
		final String releaseCommandId
	) {

		Order order = orderService.getOrder(orderId);

		order.reject();

		OrderItemReleaseIfDeductedEvent event = OrderItemReleaseIfDeductedEvent.from(
			orderId,
			itemId,
			stock,
			decreaseCommandId,
			releaseCommandId
		);

		outboxEventService.publish(
			AggregateTypes.ITEM,
			String.valueOf(itemId),
			EventTypes.ORDER_ITEM_RELEASE_IF_DEDUCTED_REQUESTED,
			EventTopics.ORDER_ITEM_RELEASE_IF_DEDUCTED_REQUESTED,
			event
		);
	}

	@Override
	@Transactional
	public Order pendingOrderWithPaymentCheckEvent(
		final Long orderId,
		final String orderNumber,
		final String paymentKey
	) {

		Order order = orderService.getOrder(orderId);

		order.pending();

		PaymentCheckedEvent event = PaymentCheckedEvent.from(orderId, orderNumber, paymentKey);

		outboxEventService.publish(
			AggregateTypes.PAYMENT,
			String.valueOf(orderId),
			EventTypes.PAYMENT_CHECK_REQUIRED,
			EventTopics.PAYMENT_CHECK_REQUIRED,
			event
		);

		return order;
	}

}
