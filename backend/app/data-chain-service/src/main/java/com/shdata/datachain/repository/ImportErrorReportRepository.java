package com.shdata.datachain.repository;

import com.shdata.datachain.entity.ImportErrorReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * 导入错误报告 Repository。
 *
 * @author ZhangTao
 */
public interface ImportErrorReportRepository
        extends JpaRepository<ImportErrorReportEntity, Long>,
                QuerydslPredicateExecutor<ImportErrorReportEntity> {
}
