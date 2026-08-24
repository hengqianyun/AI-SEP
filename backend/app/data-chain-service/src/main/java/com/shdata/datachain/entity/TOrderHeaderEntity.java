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
 * 订单头表实体，对应 {@code t_order_header}。
 *
 * <p>字段与 V10 Flyway 脚本一致；产品快照 JSON 冻结在下单时刻，不跟产品漂移。
 * 订单号 {@code order_no} 为 UUID v4 格式，不含企业 ID/产品编码/用户 ID 等业务语义。
 *
 * @author developer-wsc-913
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_order_header")
@Where(clause = "del_flag = 0")
public class TOrderHeaderEntity extends BaseEntity {

    /** 订单号（UUID v4，不含业务语义）。 */
    @Column(name = "order_no", nullable = false, length = 36, unique = true)
    private String orderNo;

    /** 订单状态（PENDING_CONFIRM / PENDING_UPLOAD / PENDING_CONTRACT_CONFIRM / CONTRACT_REACHED / CANCELLED）。 */
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    /** 需求方用户 ID。 */
    @Column(name = "demand_user_id", nullable = false, length = 64)
    private String demandUserId;

    /** 提供方企业 ID（产品 create_by 对应用户的 enterprise_id）。 */
    @Column(name = "provider_enterprise_id", nullable = false, length = 64)
    private String providerEnterpriseId;

    /** 产品 ID。 */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** 产品编码。 */
    @Column(name = "product_code", nullable = false, length = 64)
    private String productCode;

    /** 下单时产品快照 JSON（冻结，不跟产品漂移）。 */
    @Column(name = "product_snapshot", columnDefinition = "JSON")
    private String productSnapshot;

    /** 备注。 */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    /** 平台统一须知版本号（服务端自动填充）。 */
    @Column(name = "notice_version", nullable = false, length = 32)
    private String noticeVersion;

    /** 含税金额（人民币两位小数）。 */
    @Column(name = "amount_tax_inclusive", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountTaxInclusive;

    /** 未税金额（人民币两位小数，本期与含税同价）。 */
    @Column(name = "amount_tax_exclusive", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountTaxExclusive;

    /** 币种。 */
    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    /** 上链次数缓存（mock 记录条数，与详情时间线一致）。 */
    @Column(name = "chain_count", nullable = false)
    private Integer chainCount;
}
