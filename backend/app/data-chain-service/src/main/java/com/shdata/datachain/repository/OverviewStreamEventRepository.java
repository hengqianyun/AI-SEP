package com.shdata.datachain.repository;

import com.shdata.datachain.entity.OverviewStreamEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * 总览流事件 Repository。
 *
 * @author ZhangTao
 */
public interface OverviewStreamEventRepository
        extends JpaRepository<OverviewStreamEventEntity, Long>,
                QuerydslPredicateExecutor<OverviewStreamEventEntity> {
}
