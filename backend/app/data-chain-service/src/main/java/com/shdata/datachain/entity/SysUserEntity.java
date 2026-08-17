package com.shdata.datachain.entity;

import com.shdata.datachain.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 系统用户实体，对应 {@code sys_user}。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user")
public class SysUserEntity extends BaseEntity {

    @Column(name = "username", nullable = false, length = 64, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName = "";

    @Column(name = "role", nullable = false, length = 32)
    private String role;

    /**
     * 归属企业 ID（逻辑 FK，对应 {@code sys_enterprise.id}；无物理外键）。
     * 会话/授权单一真源；0 仅表示迁移占位，运行期应回填为真实企业。
     */
    @Column(name = "enterprise_id", nullable = false)
    private Long enterpriseId = 0L;

    /**
     * 企业名称反规范化缓存（V1.6 兼容列）。写入时与 {@link #enterpriseId} 指向的企业名称同步；
     * 不得单独作为 scope 真源。
     */
    @Column(name = "enterprise_name", nullable = false, length = 256)
    private String enterpriseName = "";
}
