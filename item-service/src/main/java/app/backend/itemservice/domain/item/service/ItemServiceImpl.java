package app.backend.itemservice.domain.item.service;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.backend.itemservice.domain.history.entity.History;
import app.backend.itemservice.domain.history.entity.StockHistoryType;
import app.backend.itemservice.domain.history.service.HistoryService;
import app.backend.itemservice.domain.item.dto.req.CreateItemDto;
import app.backend.itemservice.domain.item.dto.req.UpdateItemDto;
import app.backend.itemservice.domain.item.dto.req.UpdateItemStockDto;
import app.backend.itemservice.domain.item.dto.res.GetItemDto;
import app.backend.itemservice.domain.item.entity.Item;
import app.backend.itemservice.domain.item.exception.ItemErrorCode;
import app.backend.itemservice.domain.item.exception.ItemException;
import app.backend.itemservice.domain.item.repository.ItemRepository;
import app.backend.itemservice.global.aop.lock.CustomLock;
import app.backend.itemservice.global.constant.TraceConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

	private final ItemRepository itemRepository;

	private final HistoryService historyService;

	@Override
	@Transactional
	public Long createItem(final CreateItemDto requestDto) {

		return itemRepository.saveItem(
			Item.create(
				requestDto.name(),
				requestDto.description(),
				requestDto.price(),
				requestDto.amount()
			)
		).getId();
	}

	@Override
	@Transactional(readOnly = true)
	public GetItemDto readItem(final Long itemId) {

		return GetItemDto.from(getItem(itemId));
	}

	@Override
	@Transactional
	public GetItemDto updateItemInfo(final Long itemId, final UpdateItemDto requestDto) {

		Item item = getItem(itemId);

		item.updateInfo(requestDto.name(), requestDto.description());

		item.updatePrice(requestDto.price());

		return GetItemDto.from(item);
	}

	@Override
	@CustomLock(key = "'Item:' + #itemId", waitTime = 10_000, leaseTime = 5_000)
	public boolean updateItemStock(final Long itemId, final UpdateItemStockDto requestDto) {

		if (historyService.existsHistory(requestDto.commandId())) {
			return true;
		}

		Item item = getItem(itemId);

		calculateWithType(item, requestDto);

		StockHistoryType type = getHistoryType(requestDto);

		historyService.createHistory(
			History.create(
				requestDto.commandId(),
				itemId,
				requestDto.stock(),
				type,
				MDC.get(TraceConstants.TRACE_ID_MDC_KEY
				)
			)
		);

		return true;
	}

	@Override
	@Transactional
	public void deleteItem(final Long itemId) {

		// 실제 비즈니스로 간다면 주문 여부 확인 과정 필요.

		itemRepository.deleteItemById(itemId);
	}

	private Item getItem(final Long itemId) {
		return itemRepository.findItemById(itemId).orElseThrow(
			() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND)
		);
	}

	private void calculateWithType(final Item item, final UpdateItemStockDto requestDto) {

		switch (requestDto.type()) {
			case PLUS -> {
				item.plusStock(requestDto.stock());
			}
			case MINUS -> {
				item.minusStock(requestDto.stock());
			}
			case ALL -> {
				item.updateStock(requestDto.stock());
			}
			default -> throw new ItemException(ItemErrorCode.NOT_SUPPORTED_CALCULATION_TYPE);
		}
	}

	private StockHistoryType getHistoryType(final UpdateItemStockDto requestDto) {

		switch (requestDto.type()) {
			case PLUS -> {
				return StockHistoryType.RELEASE;
			}
			case MINUS -> {
				return StockHistoryType.DECREASE;
			}
			case ALL -> {
				return StockHistoryType.ADJUST;
			}
			default -> throw new ItemException(ItemErrorCode.NOT_SUPPORTED_CALCULATION_TYPE);
		}
	}
}
