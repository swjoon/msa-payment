package app.backend.itemservice.domain.item.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import app.backend.itemservice.domain.item.entity.Item;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

	private final ItemJpaRepository itemJpaRepository;

	@Override
	public Item saveItem(final Item item) {

		return itemJpaRepository.save(item);
	}

	@Override
	public Optional<Item> findItemById(final Long itemId) {

		return itemJpaRepository.findById(itemId);
	}

	@Override
	public void deleteItemById(final Long itemId) {

		itemJpaRepository.deleteById(itemId);
	}
}
