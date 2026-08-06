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

/**
 * 上链目录快照，存储版本对应的完整快照 JSON。
 *
 * @author ZhangTao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_chain_catalog_snapshot")
@Where(clause = "del_flag = 0")
public class ChainCatalogSnapshotEntity extends BaseEntity {

    /** 版本编码（关联 t_chain_version.version_code，无外键）。 */
    @Column(name = "version_code", nullable = false, length = 64)
    private String versionCode;

    /** 目录快照 JSON。 */
    @Lob
    @Column(name = "snapshot_json", nullable = false, columnDefinition = "TEXT")
    private String snapshotJson;
}
