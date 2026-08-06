package com.shdata.datachain.entity;

import com.shdata.datachain.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.Instant;

/**
 * 导入错误报告，TTL 24h，到期后应用层或定时任务物理清理。
 * <p>含 del_flag 以保持与其他实体统一，正常情况 del_flag=0。</p>
 *
 * @author ZhangTao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_import_error_report")
@Where(clause = "del_flag = 0")
public class ImportErrorReportEntity extends BaseEntity {

    /** 过期时间（创建时间 +24h）。 */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** 导入成功数。 */
    @Column(name = "success_count", nullable = false)
    private Integer successCount;

    /** 导入失败数。 */
    @Column(name = "failure_count", nullable = false)
    private Integer failureCount;

    /** 错误行详情 JSON。 */
    @Lob
    @Column(name = "rows_json", nullable = false, columnDefinition = "TEXT")
    private String rowsJson;
}
