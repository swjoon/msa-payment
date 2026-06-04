package app.backend.itemservice.domain.item.dto.req;

public record CreateItemDto(
	String name,
	String description,
	Long price,
	int amount
) {
}

