package app.backend.orderservice.global.util;

import app.backend.orderservice.order.constant.OrderConstants;

public class IdempotencyUtil {

	public static String getIdempotencyKey(final String type, final Long focusId) {

		return String.format("%s-%s-%s", OrderConstants.STOCK, type, focusId);
	}

	public static String getIdempotencyKey(final String type, final Long focusId1, final Long focusId2) {

		return String.format("%s-%s-%s-%s", OrderConstants.STOCK, type, focusId1, focusId2);

	}
}
