package app.backend.itemservice.domain.item.controller.external;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.backend.itemservice.global.response.ApiResponse;
import app.backend.itemservice.domain.item.dto.req.CreateItemDto;
import app.backend.itemservice.domain.item.dto.req.UpdateItemDto;
import app.backend.itemservice.domain.item.dto.req.UpdateItemStockDto;
import app.backend.itemservice.domain.item.dto.res.GetItemDto;
import app.backend.itemservice.domain.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@PostMapping
	public ApiResponse<Long> createItem(
		@RequestBody final CreateItemDto requestDto
	) {

		log.info("생성 요청된 item 정보 및 traceId: {}", requestDto);

		Long res = itemService.createItem(requestDto);

		return ApiResponse.of(true, HttpStatus.CREATED, "ITEM CREATED SUCCESSFULLY", res);
	}

	@GetMapping("/{itemId}")
	public ApiResponse<GetItemDto> readItem(@PathVariable("itemId") final Long itemId) {

		GetItemDto res = itemService.readItem(itemId);

		return ApiResponse.of(true, HttpStatus.OK, "ITEM READ SUCCESSFULLY", res);
	}

	@PatchMapping("/{itemId}")
	public ApiResponse<GetItemDto> updateItem(
		@PathVariable("itemId") final Long itemId,
		@RequestBody final UpdateItemDto requestDto
	) {

		GetItemDto res = itemService.updateItemInfo(itemId, requestDto);

		return ApiResponse.of(true, HttpStatus.OK, "ITEM UPDATE SUCCESSFULLY", res);
	}

	@PatchMapping("/{itemId}/stock")
	public ApiResponse<Void> updateItemStock(
		@PathVariable("itemId") final Long itemId,
		@RequestBody final UpdateItemStockDto requestDto
	) {

		itemService.updateItemStock(itemId, requestDto);

		return ApiResponse.of(true, HttpStatus.OK, "ITEM UPDATE STOCK SUCCESSFULLY");
	}

	@DeleteMapping("/{itemId}")
	public ApiResponse<Void> deleteItem(@PathVariable("itemId") final Long itemId) {

		itemService.deleteItem(itemId);

		return ApiResponse.of(true, HttpStatus.OK, "ITEM DELETE SUCCESSFULLY");
	}
}
