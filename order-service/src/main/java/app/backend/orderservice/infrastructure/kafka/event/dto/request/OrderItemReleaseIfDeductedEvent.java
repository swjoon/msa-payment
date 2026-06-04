package app.backend.orderservice.infrastructure.kafka.event.dto.request;

public record OrderItemReleaseIfDeductedEvent(
	Long orderId,
	Long itemId,
	int stock,
	String decreaseCommandId,
	String releaseCommandId
) {

	public static OrderItemReleaseIfDeductedEvent from(
		Long orderId,
		Long itemId,
		int stock,
		String decreaseCommandId,
		String releaseCommandId
	) {
		return new OrderItemReleaseIfDeductedEvent(
			orderId,
			itemId,
			stock,
			decreaseCommandId,
			releaseCommandId
		);
	}
}