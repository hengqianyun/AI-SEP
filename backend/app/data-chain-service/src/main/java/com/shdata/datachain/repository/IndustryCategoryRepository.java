package com.shdata.datachain.repository;

import com.shdata.datachain.entity.IndustryCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * 行业分类 Repository。
 *
 * @author ZhangTao
 */
public interface IndustryCategoryRepository
        extends JpaRepository<IndustryCategoryEntity, Long>,
                QuerydslPredicateExecutor<IndustryCategoryEntity> {
}
