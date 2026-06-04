package app.backend.itemservice.domain.history.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.backend.itemservice.domain.history.entity.History;

public interface HistoryJpaRepository extends JpaRepository<History, Long> {

	Optional<History> findByCommandId(String commandId);

	boolean existsByCommandId(String commandId);

}
