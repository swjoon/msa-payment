package app.backend.orderservice.infrastructure.kafka.event.dto.request;

public record OrderItemReleasedEvent(
	String commandId,
	Long itemId,
	int stock
) {
	public static OrderItemReleasedEvent from(String commandId, Long itemId, int stock) {
		return new OrderItemReleasedEvent(commandId, itemId, stock);
	}
}
