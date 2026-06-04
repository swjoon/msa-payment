package app.backend.itemservice.domain.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.backend.itemservice.domain.item.entity.Item;

@Repository
public interface ItemJpaRepository extends JpaRepository<Item, Long> {



}
