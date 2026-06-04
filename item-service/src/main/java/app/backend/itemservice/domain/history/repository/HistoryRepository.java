package app.backend.itemservice.domain.history.repository;

import java.util.Optional;

import app.backend.itemservice.domain.history.entity.History;

public interface HistoryRepository {

	void create (History history);

	Optional<History> findByCommandId(String commandId);

	boolean existsByCommandId(String commandId);

}
