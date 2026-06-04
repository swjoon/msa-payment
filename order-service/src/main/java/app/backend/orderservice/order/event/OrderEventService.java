package app.backend.orderservice.order.event;

import app.backend.orderservice.order.entity.Order;

public interface OrderEventService {

	Order rejectOrderWithItemReleaseEvent(Long orderId, Long itemId, int stock, String releaseCommandId);

	void rejectOrderWithItemReleaseIfDeductedEvent(Long orderId, Long itemId, int stock, String decreaseCommandId, String releaseCommandId);

	Order pendingOrderWithPaymentCheckEvent(Long orderId, String orderNumber, String paymentKey);

}
