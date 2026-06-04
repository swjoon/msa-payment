package app.backend.itemservice.domain.item.entity;

import app.backend.itemservice.domain.item.exception.ItemErrorCode;
import app.backend.itemservice.domain.item.exception.ItemException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tbl_item")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String description;

	private Long price;

	private int stock;

	public static Item create(String name, String description, Long price, int stock) {
		return Item.builder()
			.name(name)
			.description(description)
			.price(price)
			.stock(stock)
			.build();
	}

	public void updateInfo(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public void updatePrice(Long price) {
		this.price = price;
	}

	public void updateStock(int stock) {
		if (stock < 0) {
			throw new ItemException(ItemErrorCode.NOT_ENOUGH_ITEM_STOCK);
		}
		this.stock = stock;
	}

	public void plusStock(int stock) {
		this.stock += stock;
	}

	public void minusStock(int stock) {
		if (this.stock < stock) {
			throw new ItemException(ItemErrorCode.NOT_ENOUGH_ITEM_STOCK);
		}

		this.stock -= stock;
	}

}
