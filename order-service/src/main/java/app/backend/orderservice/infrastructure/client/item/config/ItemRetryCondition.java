package app.backend.orderservice.infrastructure.client.item.config;

import org.springframework.stereotype.Component;

import app.backend.orderservice.infrastructure.client.item.exception.ItemInternalException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ItemRetryCondition {

	private final ItemStockRetryProperties properties;

	public boolean canRetry(Throwable throwable) {
		if (!(throwable instanceof ItemInternalException ex)) {
			return false;
		}

		return !properties.nonRetryableCodes().contains(ex.getCode());
	}

}
