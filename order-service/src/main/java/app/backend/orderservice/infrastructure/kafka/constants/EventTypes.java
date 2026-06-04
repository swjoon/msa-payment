package app.backend.orderservice.infrastructure.kafka.constants;

public final class EventTypes {

	public static final String ORDER_ITEM_RELEASE_REQUESTED =
		"OrderItemReleaseRequested";

	public static final String ORDER_ITEM_RELEASE_IF_DEDUCTED_REQUESTED =
		"OrderItemReleaseIfDeductedRequested";

	public static final String PAYMENT_CHECK_REQUIRED =
		"PaymentCheckRequired";

	public static final String ITEM_STOCK_RELEASED =
		"ItemStockReleased";

	public static final String ITEM_STOCK_RELEASE_FAILED =
		"ItemStockReleaseFailed";
}