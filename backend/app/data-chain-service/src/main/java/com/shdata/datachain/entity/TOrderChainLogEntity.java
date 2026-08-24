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
import java.time.LocalDateTime;

/**
 * 订单上链日志实体，对应 {@code t_order_chain_log}。
 *
 * <p>mock 模拟记录，禁止写入真实链节点 URL/私钥/证书。
 * 哈希/高度/节点字段均为模拟值，不匹配真实链浏览器可验证格式。
 *
 * @author developer-wsc-913
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_order_chain_log")
@Where(clause = "del_flag = 0")
public class TOrderChainLogEntity extends BaseEntity {

    /** 所属订单头 ID（业务列，非物理 FK）。 */
    @Column(name = "order_header_id", nullable = false)
    private Long orderHeaderId;

    /** 事件类型。 */
    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    /** 上链哈希（mock 模拟值，禁止写入真实链节点配置）。 */
    @Column(name = "chain_hash", nullable = false, length = 128)
    private String chainHash;

    /** 区块高度（mock 模拟值）。 */
    @Column(name = "block_height", nullable = false, length = 64)
    private String blockHeight;

    /** 链节点标识（mock 模拟值，禁止真实链节点 URL）。 */
    @Column(name = "chain_node", nullable = false, length = 128)
    private String chainNode;

    /** 链上时间戳（mock 模拟值）。 */
    @Column(name = "chain_timestamp")
    private LocalDateTime chainTimestamp;
}
