package app.backend.orderservice.infrastructure.client.item.adapter;

import org.slf4j.MDC;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import app.backend.orderservice.global.constant.TraceConstants;
import app.backend.orderservice.infrastructure.client.constants.InternalServiceType;
import app.backend.orderservice.infrastructure.client.error.InternalFeignCallExecutor;
import app.backend.orderservice.infrastructure.client.item.dto.req.GetItemDto;
import app.backend.orderservice.infrastructure.client.item.dto.res.UpdateItemStockDto;
import app.backend.orderservice.infrastructure.client.item.exception.ItemInternalException;
import app.backend.orderservice.infrastructure.client.item.service.ItemServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemAdapter {

	private final ItemServiceClient itemServiceClient;
	private final InternalFeignCallExecutor feignCallExecutor;

	public GetItemDto getItem(final Long itemId) {

		return feignCallExecutor.execute(
			InternalServiceType.ITEM,
			() -> itemServiceClient.getItemDto(itemId)
		);
	}

	@Retryable(
		retryFor = ItemInternalException.class,
		maxAttemptsExpression = "${internal.item.stock.retry.max-attempts:3}",
		backoff = @Backoff(
			delayExpression = "${internal.item.stock.retry.delay:100}",
			multiplierExpression = "${internal.item.stock.retry.multiplier:2.0}",
			maxDelayExpression = "${internal.item.stock.retry.max-delay:1000}"
		),
		exceptionExpression = "@itemRetryCondition.canRetry(#root)"
	)
	public boolean updateItemStock(final Long itemId, final UpdateItemStockDto requestDto) {

		log.info("updateItem - transaction : {}", MDC.get(TraceConstants.TRACE_ID_MDC_KEY));

		return feignCallExecutor.execute(
			InternalServiceType.ITEM,
			() -> itemServiceClient.updateItemStock(itemId, requestDto)
		);
	}

}
