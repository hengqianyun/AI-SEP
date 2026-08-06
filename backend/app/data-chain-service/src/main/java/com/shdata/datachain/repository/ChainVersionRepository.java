package com.shdata.datachain.repository;

import com.shdata.datachain.entity.ChainVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * 上链版本 Repository。
 *
 * @author ZhangTao
 */
public interface ChainVersionRepository
        extends JpaRepository<ChainVersionEntity, Long>,
                QuerydslPredicateExecutor<ChainVersionEntity> {
}
