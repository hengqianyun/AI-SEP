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
import java.math.BigDecimal;

/**
 * 订单明细（计费行）实体，对应 {@code t_order_line}。
 *
 * <p>仅由 {@code POST /orders/{orderId}/contract} 阶段写入，
 * 创建订单时不包含计费明细。
 *
 * @author developer-wsc-913
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_order_line")
@Where(clause = "del_flag = 0")
public class TOrderLineEntity extends BaseEntity {

    /** 所属订单头 ID（业务列，非物理 FK）。 */
    @Column(name = "order_header_id", nullable = false)
    private Long orderHeaderId;

    /** 计量单位。 */
    @Column(name = "unit", nullable = false, length = 32)
    private String unit;

    /** 数量。 */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** 单价（人民币两位小数）。 */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** 小计（人民币两位小数）。 */
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
