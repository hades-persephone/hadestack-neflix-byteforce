package io.watch.sync.repository;

import io.watch.sync.entity.SyncMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SyncMappingRepository extends JpaRepository<SyncMapping, Long> {
    List<SyncMapping> findByActive(boolean active);
    Optional<SyncMapping> findBySourceTable(String sourceTable);
    List<SyncMapping> findBySourceTableIn(List<String> sourceTableNames);
}