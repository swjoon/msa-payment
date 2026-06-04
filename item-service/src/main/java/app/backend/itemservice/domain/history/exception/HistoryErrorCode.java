package app.backend.itemservice.domain.history.exception;

import org.springframework.http.HttpStatus;

import app.backend.itemservice.global.error.exception.DomainErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HistoryErrorCode implements DomainErrorCode {

	HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "H001", "History not found"),
	;

	private final HttpStatus status;

	private final String code;

	private final String message;

}
