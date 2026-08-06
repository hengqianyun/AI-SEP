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

    @Column(name = "enterprise_name", nullable = false, length = 256)
    private String enterpriseName = "";
}
