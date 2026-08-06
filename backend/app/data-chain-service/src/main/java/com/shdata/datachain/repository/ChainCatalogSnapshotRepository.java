package com.shdata.datachain.repository;

import com.shdata.datachain.entity.ChainCatalogSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * 上链目录快照 Repository。
 *
 * @author ZhangTao
 */
public interface ChainCatalogSnapshotRepository
        extends JpaRepository<ChainCatalogSnapshotEntity, Long>,
                QuerydslPredicateExecutor<ChainCatalogSnapshotEntity> {
}
