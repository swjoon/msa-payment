package app.backend.itemservice.domain.item.dto.req;

public record UpdateItemDto(
	String name,
	String description,
	Long price
) {
}
