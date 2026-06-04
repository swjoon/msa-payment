package app.backend.itemservice.domain.item.service;

import app.backend.itemservice.domain.item.dto.req.CreateItemDto;
import app.backend.itemservice.domain.item.dto.req.UpdateItemDto;
import app.backend.itemservice.domain.item.dto.res.GetItemDto;
import app.backend.itemservice.domain.item.dto.req.UpdateItemStockDto;

public interface ItemService {

	Long createItem(CreateItemDto requestDto);

	GetItemDto readItem(Long itemId);

	GetItemDto updateItemInfo(Long itemId, UpdateItemDto requestDto);

	boolean updateItemStock(Long itemId, UpdateItemStockDto requestDto);

	void deleteItem(Long itemId);

}
