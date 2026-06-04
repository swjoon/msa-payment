package app.backend.itemservice.domain.item.exception;

import org.springframework.http.HttpStatus;

import app.backend.itemservice.global.error.exception.DomainErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemErrorCode implements DomainErrorCode {

	ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "상품 정보가 존재 하지 않습니다."),
	NOT_ENOUGH_ITEM_STOCK(HttpStatus.CONFLICT, "I002", "상품 잔여 수량이 부족합니다."),
	NOT_SUPPORTED_CALCULATION_TYPE(HttpStatus.BAD_REQUEST, "I003", "지원하지 않는 계산식입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;
}
