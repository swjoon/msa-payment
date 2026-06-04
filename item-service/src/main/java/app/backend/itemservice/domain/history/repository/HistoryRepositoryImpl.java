package app.backend.itemservice.domain.history.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import app.backend.itemservice.domain.history.entity.History;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HistoryRepositoryImpl implements HistoryRepository {

	private final HistoryJpaRepository jpaRepository;

	@Override
	@Transactional
	public void create(final History history) {

		jpaRepository.save(history);
	}

	@Override
	public Optional<History> findByCommandId(final String commandId) {

		return jpaRepository.findByCommandId(commandId);
	}

	@Override
	public boolean existsByCommandId(final String commandId) {

		return jpaRepository.existsByCommandId(commandId);
	}
}
