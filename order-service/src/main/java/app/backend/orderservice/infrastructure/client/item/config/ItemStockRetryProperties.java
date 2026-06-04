package app.backend.orderservice.infrastructure.client.item.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal.item.stock.retry")
public record ItemStockRetryProperties(
	int maxAttempts,
	long delay,
	double multiplier,
	long maxDelay,
	Set<String> nonRetryableCodes
) {
}