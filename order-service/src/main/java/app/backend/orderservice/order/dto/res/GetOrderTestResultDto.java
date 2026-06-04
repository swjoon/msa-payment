package app.backend.orderservice.order.dto.res;

import app.backend.orderservice.infrastructure.client.item.dto.req.GetItemDto;

public record GetOrderTestResultDto(
	Long itemId,
	Long totalOrderCount,
	int readyCount,
	int readyItemCount,
	int successCount,
	int successItemStock,
	int rejectCount,
	int rejectItemStock,
	int pendingCount,
	int pendingItemStock,
	GetItemDto itemInfo
) {
	public static GetOrderTestResultDto from(
		Long itemId,
		Long totalOrderCount,
		int readyCount,
		int readyItemStock,
		int successCount,
		int successItemStock,
		int rejectCount,
		int rejectItemStock,
		int pendingCount,
		int pendingItemStock,
		GetItemDto itemInfo
	) {
		return new GetOrderTestResultDto(
			itemId,
			totalOrderCount,
			readyCount,
			readyItemStock,
			successCount,
			successItemStock,
			rejectCount,
			rejectItemStock,
			pendingCount,
			pendingItemStock,
			itemInfo
		);
	}

}
