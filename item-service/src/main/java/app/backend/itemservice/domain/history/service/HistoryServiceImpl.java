package app.backend.itemservice.domain.history.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.backend.itemservice.domain.history.entity.History;
import app.backend.itemservice.domain.history.exception.HistoryErrorCode;
import app.backend.itemservice.domain.history.exception.HistoryException;
import app.backend.itemservice.domain.history.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

	private final HistoryRepository historyRepository;

	@Override
	@Transactional
	public void createHistory(final History history) {

		historyRepository.create(history);
	}

	@Override
	@Transactional(readOnly = true)
	public History findHistoryByCommandId(final String commandId) {

		return historyRepository.findByCommandId(commandId).orElseThrow(
			() -> new HistoryException(HistoryErrorCode.HISTORY_NOT_FOUND)
		);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsHistory(final String commandId) {

		return historyRepository.existsByCommandId(commandId);
	}
}
