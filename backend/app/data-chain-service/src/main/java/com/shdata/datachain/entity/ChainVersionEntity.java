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
 * 上链版本记录，产品内版本号递增，关联产品编码（product_code）。
 *
 * @author ZhangTao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_chain_version")
@Where(clause = "del_flag = 0")
public class ChainVersionEntity extends BaseEntity {

    /** 版本编码（唯一，如 cv-seed-001-v1）。 */
    @Column(name = "version_code", nullable = false, length = 64)
    private String versionCode;

    /** 产品编码（关联 t_data_product.product_code，无外键）。 */
    @Column(name = "product_code", nullable = false, length = 128)
    private String productCode;

    /** 版本号（产品内递增）。 */
    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    /** 元数据哈希。 */
    @Column(name = "metadata_hash", nullable = false, length = 256)
    private String metadataHash;

    /** 权属方 DID。 */
    @Column(name = "owner_did", nullable = false, length = 512)
    private String ownerDid;

    /** 证书权属方名称。 */
    @Column(name = "cert_owner", nullable = false, length = 512)
    private String certOwner;

    /** 存证时间戳。 */
    @Column(name = "ts", nullable = false)
    private Instant ts;
}
