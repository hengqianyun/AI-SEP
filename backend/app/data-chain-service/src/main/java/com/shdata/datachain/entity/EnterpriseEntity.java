package com.shdata.datachain.entity;

import com.shdata.datachain.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 系统企业实体，对应 {@code sys_enterprise}（REQ-USER-002 最小企业；无企业 CRUD UI）。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_enterprise")
public class EnterpriseEntity extends BaseEntity {

    /** 演示企业编码。 */
    public static final String CODE_DEMO = "DEMO";

    /** OQ-V16-002 未归属默认企业编码（orphan 占位，不可因此获得其他企业写权限）。 */
    public static final String CODE_UNASSIGNED = "UNASSIGNED";

    /** 演示企业名称（与历史 sys_user.enterprise_name 默认值对齐）。 */
    public static final String NAME_DEMO = "演示企业";

    /** 未归属默认企业名称（可审计占位）。 */
    public static final String NAME_UNASSIGNED = "未归属默认企业";

    /** 企业编码（业务唯一）。 */
    @Column(name = "code", nullable = false, length = 64, unique = true)
    private String code;

    /** 企业名称。 */
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    /** 是否为 orphan 未归属默认企业。 */
    @Column(name = "unassigned_default", nullable = false, columnDefinition = "TINYINT default 0")
    private Boolean unassignedDefault = false;
}
