package app.backend.orderservice.infrastructure.kafka.event.handler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.backend.orderservice.global.util.IdempotencyUtil;
import app.backend.orderservice.infrastructure.kafka.event.dto.KafkaEventMeta;
import app.backend.orderservice.infrastructure.kafka.event.dto.response.PaymentApprovedEvent;
import app.backend.orderservice.infrastructure.kafka.event.dto.response.PaymentFailedEvent;
import app.backend.orderservice.infrastructure.kafka.event.dto.response.PaymentUnknownEvent;
import app.backend.orderservice.infrastructure.kafka.message.service.ProcessedMessageService;
import app.backend.orderservice.order.constant.OrderConstants;
import app.backend.orderservice.order.entity.Order;
import app.backend.orderservice.order.event.OrderEventService;
import app.backend.orderservice.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

	private final OrderService orderService;
	private final OrderEventService orderEventService;
	private final ProcessedMessageService processedMessageService;

	@Transactional
	public void handlePaymentApproved(
		final PaymentApprovedEvent event,
		final KafkaEventMeta meta,
		final String consumerName
	) {
		validateEventId(meta);

		boolean marked = processedMessageService.markAsProcessed(
			meta.eventId(),
			consumerName,
			meta.eventType(),
			meta.topicName(),
			meta.partitionNo(),
			meta.offsetNo()
		);

		if (!marked) {
			log.info(
				"이미 처리된 결제 승인 이벤트입니다. eventId={}, consumerName={}",
				meta.eventId(),
				consumerName
			);
			return;
		}

		Order order = orderService.getOrder(event.orderId());

		order.confirm();

		log.info(
			"결제 승인 이벤트 처리 완료. orderId={}, orderNumber={}, paymentKey={}",
			event.orderId(),
			event.orderNumber(),
			event.paymentKey()
		);
	}

	@Transactional
	public void handlePaymentFailed(
		final PaymentFailedEvent event,
		final KafkaEventMeta meta,
		final String consumerName
	) {
		validateEventId(meta);

		boolean marked = processedMessageService.markAsProcessed(
			meta.eventId(),
			consumerName,
			meta.eventType(),
			meta.topicName(),
			meta.partitionNo(),
			meta.offsetNo()
		);

		if (!marked) {
			log.info(
				"이미 처리된 결제 실패 이벤트입니다. eventId={}, consumerName={}",
				meta.eventId(),
				consumerName
			);
			return;
		}

		Order order = orderService.getOrder(event.orderId());

		if (order.isConfirmed()) {
			log.error(
				"이미 확정된 주문에 결제 실패 이벤트가 도착했습니다. 확인 필요. orderId={}, orderNumber={}, paymentKey={}",
				event.orderId(),
				event.orderNumber(),
				event.paymentKey()
			);
			return;
		}

		Long itemId = order.getItemId();
		int stock = order.getStock();

		orderEventService.rejectOrderWithItemReleaseEvent(
			event.orderId(),
			itemId,
			stock,
			IdempotencyUtil.getIdempotencyKey(
				OrderConstants.RELEASE,
				event.orderId(),
				itemId
			)
		);

		log.info(
			"결제 실패 이벤트 처리 완료. 주문 거절 및 재고 복구 이벤트 발행. orderId={}, itemId={}, stock={}",
			event.orderId(),
			itemId,
			stock
		);
	}

	@Transactional
	public void handlePaymentUnknown(
		final PaymentUnknownEvent event,
		final KafkaEventMeta meta,
		final String consumerName
	) {
		validateEventId(meta);

		boolean marked = processedMessageService.markAsProcessed(
			meta.eventId(),
			consumerName,
			meta.eventType(),
			meta.topicName(),
			meta.partitionNo(),
			meta.offsetNo()
		);

		if (!marked) {
			log.info(
				"이미 처리된 결제 불명확 이벤트입니다. eventId={}, consumerName={}",
				meta.eventId(),
				consumerName
			);
			return;
		}

		Order order = orderService.getOrder(event.orderId());

		order.pending();

		log.warn(
			"결제 상태 불명확 이벤트 처리 완료. 주문을 PAYMENT_PENDING 상태로 유지. orderId={}, orderNumber={}, paymentKey={}",
			event.orderId(),
			event.orderNumber(),
			event.paymentKey()
		);
	}

	private void validateEventId(final KafkaEventMeta meta) {
		if (meta.eventId() == null || meta.eventId().isBlank()) {
			throw new IllegalStateException("eventId header가 없습니다.");
		}
	}
}
