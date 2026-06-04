package app.backend.itemservice.domain.history.exception;

import app.backend.itemservice.global.error.exception.DomainErrorCode;
import app.backend.itemservice.global.error.exception.DomainException;

public class HistoryException extends DomainException {
	public HistoryException(DomainErrorCode errorCode) {
		super(errorCode);
	}
}
