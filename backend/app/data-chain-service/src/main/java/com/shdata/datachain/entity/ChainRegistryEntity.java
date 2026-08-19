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
 * 溯源链注册表（t_chain）：维护数据产品 product_code 与平台溯源链 chain_id 的一一映射。
 *
 * <p>同一产品的所有版本节点共享一条溯源链；并发首版上链由 {@code uk_t_chain_product} 兜底，
 * 经 {@code registerChainId} 的 insert-if-absent 保证全局唯一。</p>
 *
 * @author ZhangTao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_chain")
@Where(clause = "del_flag = 0")
public class ChainRegistryEntity extends BaseEntity {

    /** 平台溯源链 ID（建链返回）。 */
    @Column(name = "chain_id", nullable = false, length = 128)
    private String chainId;

    /** 产品编码（关联 t_data_product.product_code，无外键）。 */
    @Column(name = "product_code", nullable = false, length = 128)
    private String productCode;

    /** 链登记状态（active）。 */
    @Column(name = "status", nullable = false, length = 32)
    private String status;
}
