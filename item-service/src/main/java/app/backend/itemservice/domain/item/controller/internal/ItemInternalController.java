package app.backend.itemservice.domain.item.controller.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.backend.itemservice.domain.item.dto.req.UpdateItemStockDto;
import app.backend.itemservice.domain.item.dto.res.GetItemDto;
import app.backend.itemservice.domain.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/internal/items")
@RequiredArgsConstructor
public class ItemInternalController {

	private final ItemService itemService;

	@GetMapping("/{itemId}")
	public GetItemDto readItemForInternal(@PathVariable("itemId") final Long itemId) {

		return itemService.readItem(itemId);
	}

	@PatchMapping("/{itemId}")
	public boolean updateItemStockForInternal(
		@PathVariable("itemId") final Long itemId,
		@RequestBody UpdateItemStockDto requestDto
	) {

		return itemService.updateItemStock(itemId, requestDto);
	}

}
