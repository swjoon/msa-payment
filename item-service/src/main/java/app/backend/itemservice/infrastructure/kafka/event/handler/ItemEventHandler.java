package app.backend.itemservice.infrastructure.kafka.event.handler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.backend.itemservice.domain.history.service.HistoryService;
import app.backend.itemservice.domain.item.dto.req.UpdateItemStockDto;
import app.backend.itemservice.domain.item.entity.CalculationType;
import app.backend.itemservice.domain.item.service.ItemService;
import app.backend.itemservice.infrastructure.kafka.event.dto.KafkaEventMeta;
import app.backend.itemservice.infrastructure.kafka.event.dto.request.OrderItemReleaseIfDeductedEvent;
import app.backend.itemservice.infrastructure.kafka.event.dto.request.OrderItemReleasedEvent;
import app.backend.itemservice.infrastructure.kafka.message.service.ProcessedMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemEventHandler {

	private final ItemService itemService;
	private final HistoryService historyService;

	private final ProcessedMessageService processedMessageService;

	@Transactional
	public void increaseItemStock(
		final OrderItemReleasedEvent event,
		final KafkaEventMeta meta,
		final String consumerName
	) {
		validateEventId(meta);
		validateCommandId(event);

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
				"이미 처리된 재고 복구 이벤트입니다. eventId={}, consumerName={}",
				meta.eventId(),
				consumerName
			);
			return;
		}

		itemService.updateItemStock(
			event.itemId(),
			UpdateItemStockDto.from(
				event.commandId(),
				event.stock(),
				CalculationType.PLUS
			)
		);

		log.info(
			"재고 복구 완료. itemId={}, stock={}",
			event.itemId(),
			event.stock()
		);
	}

	@Transactional
	public void releaseItemStockIfDeducted(
		final OrderItemReleaseIfDeductedEvent event,
		final KafkaEventMeta meta,
		final String consumerName
	) {
		validateEventId(meta);
		validateDecreaseCommandId(event);
		validateReleaseCommandId(event);

		boolean marked = processedMessageService.markAsProcessed(
			meta.eventId(),
			consumerName,
			meta.eventType(),
			meta.topicName(),
			meta.partitionNo(),
			meta.offsetNo()
		);

		if (!marked) {
			return;
		}

		boolean deducted = historyService.existsHistory(event.decreaseCommandId());

		if (!deducted) {
			log.info(
				"재고 차감 이력이 없어 복구하지 않습니다. orderId={}, itemId={}, decreaseCommandId={}",
				event.orderId(),
				event.itemId(),
				event.decreaseCommandId()
			);
			return;
		}

		itemService.updateItemStock(
			event.itemId(),
			UpdateItemStockDto.from(
				event.releaseCommandId(),
				event.stock(),
				CalculationType.PLUS
			)
		);
	}

	private void validateEventId(KafkaEventMeta meta) {
		if (meta.eventId() == null || meta.eventId().isBlank()) {
			throw new IllegalStateException("eventId header 가 없습니다.");
		}
	}

	private void validateCommandId(OrderItemReleasedEvent event) {
		if (event.commandId() == null || event.commandId().isBlank()) {
			throw new IllegalStateException("commandId 가 없습니다.");
		}
	}

	private void validateDecreaseCommandId(OrderItemReleaseIfDeductedEvent event) {
		if (event.decreaseCommandId() == null || event.decreaseCommandId().isBlank()) {
			throw new IllegalStateException("decreaseCommandId 가 없습니다.");
		}
	}

	private void validateReleaseCommandId(OrderItemReleaseIfDeductedEvent event) {
		if (event.releaseCommandId() == null || event.releaseCommandId().isBlank()) {
			throw new IllegalStateException("releaseCommandId 가 없습니다.");
		}
	}
}
