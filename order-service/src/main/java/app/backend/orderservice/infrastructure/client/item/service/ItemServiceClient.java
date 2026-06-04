package app.backend.orderservice.infrastructure.client.item.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import app.backend.orderservice.infrastructure.client.config.FeignErrorDecoderConfig;
import app.backend.orderservice.infrastructure.client.item.dto.req.GetItemDto;
import app.backend.orderservice.infrastructure.client.item.dto.res.UpdateItemStockDto;

@FeignClient(
	name = "ITEM-SERVICE",
	configuration = FeignErrorDecoderConfig.class
)
public interface ItemServiceClient {

	@GetMapping("/internal/items/{itemId}")
	GetItemDto getItemDto(@PathVariable Long itemId);

	@PatchMapping("/internal/items/{itemId}")
	boolean updateItemStock(
		@PathVariable Long itemId,
		@RequestBody UpdateItemStockDto requestDto
	);
}
