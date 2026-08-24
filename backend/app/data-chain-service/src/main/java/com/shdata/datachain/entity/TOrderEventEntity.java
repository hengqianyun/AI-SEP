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

/**
 * 订单时间线事件实体，对应 {@code t_order_event}。
 *
 * <p>每次订单状态变更（创建/确认/提交合约/确认合约/取消）均写入一条事件记录。
 *
 * @author developer-wsc-913
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_order_event")
@Where(clause = "del_flag = 0")
public class TOrderEventEntity extends BaseEntity {

    /** 所属订单头 ID（业务列，非物理 FK）。 */
    @Column(name = "order_header_id", nullable = false)
    private Long orderHeaderId;

    /** 事件类型（CREATED / CONFIRMED / CONTRACT_UPLOADED / CONTRACT_CONFIRMED / CANCELLED 等）。 */
    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    /** 事件描述。 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 操作人用户 ID（ADMIN 代操作时为 ADMIN 用户标识）。 */
    @Column(name = "operator_id", nullable = false, length = 64)
    private String operatorId;

    /** 操作人角色（ADMIN / PROVIDER / USER）。 */
    @Column(name = "operator_role", nullable = false, length = 16)
    private String operatorRole;
}
