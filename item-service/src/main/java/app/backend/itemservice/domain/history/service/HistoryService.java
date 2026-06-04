package app.backend.itemservice.domain.history.service;

import java.util.Optional;

import app.backend.itemservice.domain.history.entity.History;

public interface HistoryService {

	void createHistory(History history);

	History findHistoryByCommandId(String commandId);

	boolean existsHistory(String commandId);
}
