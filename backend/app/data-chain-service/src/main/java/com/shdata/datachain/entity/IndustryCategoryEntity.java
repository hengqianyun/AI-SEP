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
 * 行业分类（三级树：L1 空间 / L2 行业 / L3 子类）。
 * <p>parent_code 存上级分类编码，不做外键关联。level 仅允许 L1/L2/L3（应用层校验）。</p>
 *
 * @author ZhangTao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_industry_category")
@Where(clause = "del_flag = 0")
public class IndustryCategoryEntity extends BaseEntity {

    /** 分类编码（业务唯一标识，如 cat-l1-health）。 */
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    /** 分类名称。 */
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    /** 层级：L1 / L2 / L3。 */
    @Column(name = "level", nullable = false, length = 8)
    private String level;

    /** 上级分类编码（无外键，L1 为空）。 */
    @Column(name = "parent_code", length = 64)
    private String parentCode;
}
