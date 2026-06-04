package app.backend.itemservice.domain.item.repository;

import java.util.Optional;

import app.backend.itemservice.domain.item.entity.Item;

public interface ItemRepository {

	Item saveItem(Item item);

	Optional<Item> findItemById(Long itemId);

	void deleteItemById(Long itemId);

}
