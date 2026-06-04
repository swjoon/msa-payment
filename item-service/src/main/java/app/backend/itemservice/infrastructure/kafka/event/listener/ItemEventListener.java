package app.backend.itemservice.infrastructure.kafka.event.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.backend.itemservice.global.constant.TraceConstants;
import app.backend.itemservice.infrastructure.kafka.constants.EventTopics;
import app.backend.itemservice.infrastructure.kafka.event.dto.KafkaEventMeta;
import app.backend.itemservice.infrastructure.kafka.event.dto.request.OrderItemReleaseIfDeductedEvent;
import app.backend.itemservice.infrastructure.kafka.event.dto.request.OrderItemReleasedEvent;
import app.backend.itemservice.infrastructure.kafka.event.handler.ItemEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemEventListener {

	private final ObjectMapper objectMapper;
	private final ItemEventHandler itemEventHandler;

	@KafkaListener(
		topics = EventTopics.ORDER_ITEM_RELEASE_REQUESTED,
		groupId = "item-service"
	)
	public void listen(
		ConsumerRecord<String, String> record,
		Acknowledgment ack
	) throws JsonProcessingException {

		log.info(
			"OrderItemReleaseRequested 수신. topic={}, partition={}, offset={}",
			record.topic(),
			record.partition(),
			record.offset()
		);

		OrderItemReleasedEvent event = objectMapper.readValue(record.value(), OrderItemReleasedEvent.class);

		KafkaEventMeta meta = KafkaEventMeta.from(record);

		MDC.put(TraceConstants.TRACE_ID_MDC_KEY, currentTraceId(meta));

		try {
			itemEventHandler.increaseItemStock(
				event,
				meta,
				String.format("%s.%s", EventTopics.CONSUMER, EventTopics.ORDER_ITEM_RELEASE_REQUESTED)
			);

			ack.acknowledge();

		} finally {
			MDC.remove(TraceConstants.TRACE_ID_MDC_KEY);
		}

	}

	@KafkaListener(
		topics = EventTopics.ORDER_ITEM_RELEASE_IF_DEDUCTED_REQUESTED,
		groupId = "item-service"
	)
	public void listenItemReleaseIfDeductedRequested(
		ConsumerRecord<String, String> record,
		Acknowledgment ack
	) throws JsonProcessingException {

		log.info(
			"OrderItemReleaseIfDeductedRequested 수신. topic={}, partition={}, offset={}",
			record.topic(),
			record.partition(),
			record.offset()
		);

		OrderItemReleaseIfDeductedEvent event =
			objectMapper.readValue(record.value(), OrderItemReleaseIfDeductedEvent.class);

		KafkaEventMeta meta = KafkaEventMeta.from(record);

		MDC.put(TraceConstants.TRACE_ID_MDC_KEY, currentTraceId(meta));

		try {
			itemEventHandler.releaseItemStockIfDeducted(
				event,
				meta,
				String.format(
					"%s.%s",
					EventTopics.CONSUMER,
					EventTopics.ORDER_ITEM_RELEASE_IF_DEDUCTED_REQUESTED
				)
			);

			ack.acknowledge();

		} finally {
			MDC.remove(TraceConstants.TRACE_ID_MDC_KEY);
		}
	}

	private String currentTraceId(KafkaEventMeta meta) {
		if (meta.traceId() == null || meta.traceId().isBlank()) {
			return TraceConstants.NO_TRACE;
		}

		return meta.traceId();
	}

}
