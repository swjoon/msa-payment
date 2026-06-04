package app.backend.itemservice.domain.history.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tbl_item_stock_history")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class History {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 100)
	private String commandId;

	@Column(nullable = false)
	private Long itemId;

	@Column(nullable = false)
	private int stock;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StockHistoryType type;

	@Column(length = 100)
	private String traceId;

	@Column(nullable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	public static History create(
		String commandId,
		Long itemId,
		int stock,
		StockHistoryType type,
		String traceId
	) {

		return History.builder()
			.commandId(commandId)
			.itemId(itemId)
			.stock(stock)
			.type(type)
			.traceId(traceId)
			.build();
	}

}
