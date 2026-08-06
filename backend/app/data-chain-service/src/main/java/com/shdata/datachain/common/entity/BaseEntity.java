package com.shdata.datachain.common.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

/**
 * Entity 抽象基类，统一主键策略（BIGINT UNSIGNED AUTO_INCREMENT）、
 * 审计四件套（create_time/create_by/update_time/update_by）和逻辑删除标记（del_flag）。
 * <p>所有业务实体继承此类，由 {@link PrePersist} / {@link PreUpdate} 自动填充时间戳；
 * create_by / update_by 需在 Service 层或审计切面中从安全上下文注入。</p>
 *
 * @author ZhangTao
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    /** 主键 ID（BIGINT UNSIGNED AUTO_INCREMENT）。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 创建时间。 */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /** 创建人（存用户登录名或用户 ID 字符串）。 */
    @Column(name = "create_by", nullable = false, length = 64)
    private String createBy = "";

    /** 更新时间。 */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /** 更新人。 */
    @Column(name = "update_by", nullable = false, length = 64)
    private String updateBy = "";

    /** 逻辑删除标记：false=未删除，true=已删除。 */
    @Column(name = "del_flag", nullable = false, columnDefinition = "TINYINT default 0")
    private Boolean delFlag = false;

    /**
     * 插入前自动填充创建时间与更新时间、逻辑删除默认值。
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        if (updateTime == null) {
            updateTime = now;
        }
        if (createBy == null) {
            createBy = "";
        }
        if (updateBy == null) {
            updateBy = "";
        }
        if (delFlag == null) {
            delFlag = false;
        }
    }

    /**
     * 更新前自动刷新更新时间。
     */
    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
        if (updateBy == null) {
            updateBy = "";
        }
    }
}
