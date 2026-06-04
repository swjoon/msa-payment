package app.backend.itemservice.domain.item.dto.req;

import app.backend.itemservice.domain.item.entity.CalculationType;

public record UpdateItemStockDto(
	String commandId,
	int stock,
	CalculationType type
) {
	public static UpdateItemStockDto from(String commandId, int stock, CalculationType type) {
		return new UpdateItemStockDto(commandId, stock, type);
	}
}
