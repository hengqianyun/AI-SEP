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
import javax.persistence.Table;
import java.time.Instant;

/**
 * 总览流事件，记录目录注册/数据注册/交易订单等事件。
 * <p>event_type 仅允许 CATALOG_REGISTER / DATA_REGISTER / TRADE_ORDER。</p>
 *
 * @author ZhangTao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_overview_stream_event")
@Where(clause = "del_flag = 0")
public class OverviewStreamEventEntity extends BaseEntity {

    /** 事件类型：CATALOG_REGISTER / DATA_REGISTER / TRADE_ORDER。 */
    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    /** 事件主题。 */
    @Column(name = "subject", nullable = false, length = 512)
    private String subject;

    /** 事件摘要。 */
    @Column(name = "action_summary", nullable = false, length = 1024)
    private String actionSummary;

    /** 事件发生时间。 */
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /** 链上记录 ID。 */
    @Column(name = "chain_record_id", nullable = false, length = 256)
    private String chainRecordId;
}
