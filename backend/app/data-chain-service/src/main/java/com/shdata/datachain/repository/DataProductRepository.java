package com.shdata.datachain.repository;

import com.shdata.datachain.entity.DataProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * 数据产品 Repository。
 *
 * @author ZhangTao
 */
public interface DataProductRepository
        extends JpaRepository<DataProductEntity, Long>,
                QuerydslPredicateExecutor<DataProductEntity> {
}
