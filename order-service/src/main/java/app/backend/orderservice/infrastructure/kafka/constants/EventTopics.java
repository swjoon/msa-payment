package app.backend.orderservice.infrastructure.kafka.constants;

public final class EventTopics {

	public static final String CONSUMER = "order-service";

	public static final String ORDER_CONFIRMED = "order.confirmed";
	public static final String ORDER_REJECTED = "order.rejected";
	public static final String ORDER_PAYMENT_PENDING = "order.payment-pending";

	public static final String ORDER_ITEM_RELEASE_REQUESTED = "order.item-release-requested";
	public static final String ITEM_STOCK_RELEASED = "item.stock-released";
	public static final String ITEM_STOCK_RELEASE_FAILED = "item.stock-release-failed";
	public static final String ORDER_ITEM_RELEASE_IF_DEDUCTED_REQUESTED =
		"order.item-release-if-deducted-requested";

	public static final String PAYMENT_CHECK_REQUIRED = "payment.check-required";

	public static final String PAYMENT_APPROVED = "payment.approved";
	public static final String PAYMENT_FAILED = "payment.failed";
	public static final String PAYMENT_UNKNOWN = "payment.unknown";
}
