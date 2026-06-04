package app.backend.orderservice.infrastructure.client.item.dto.res;

import app.backend.orderservice.infrastructure.client.item.entity.CalculationType;

public record UpdateItemStockDto(
	String commandId,
	int stock,
	CalculationType type
) {

	public static UpdateItemStockDto from(String commandId, int stock, CalculationType type) {

		return new UpdateItemStockDto(commandId, stock, type);
	}
}
