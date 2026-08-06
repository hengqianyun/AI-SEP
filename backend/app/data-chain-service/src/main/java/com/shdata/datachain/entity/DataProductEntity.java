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
 * 数据产品，product_type 允许 DATASET / REPORT / API / OTHER。
 * <p>industry_category_code 关联三级分类的 code，不做外键。</p>
 *
 * @author ZhangTao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_data_product")
@Where(clause = "del_flag = 0")
public class DataProductEntity extends BaseEntity {

    /** 产品编码（唯一业务标识，如 MED-EMR-0001）。 */
    @Column(name = "product_code", nullable = false, length = 128)
    private String productCode;

    /** 产品名称。 */
    @Column(name = "product_name", nullable = false, length = 512)
    private String productName;

    /** 产品类型：DATASET / REPORT / API / OTHER。 */
    @Column(name = "product_type", nullable = false, length = 32)
    private String productType;

    /** 三级分类编码（无外键）。 */
    @Column(name = "industry_category_code", length = 64)
    private String industryCategoryCode;

    /** 一级分类编码（冗余，加速查询）。 */
    @Column(name = "l1_category_code", length = 64)
    private String l1CategoryCode;

    /** 二级分类编码（冗余，加速查询）。 */
    @Column(name = "l2_category_code", length = 64)
    private String l2CategoryCode;

    /** 分类路径文本（如 "医疗卫生 / 电子病历 / 脱敏病历"）。 */
    @Column(name = "category_path", length = 512)
    private String categoryPath;

    /** 维护状态：PENDING / MAINTAINED。 */
    @Column(name = "maintenance_status", nullable = false, length = 32)
    private String maintenanceStatus;

    /** 行业门类（GB/T 4754）。 */
    @Column(name = "business_category", length = 256)
    private String businessCategory;

    /** 行业大类。 */
    @Column(name = "business_sub_category", length = 256)
    private String businessSubCategory;

    /** 数据来源。 */
    @Column(name = "data_source", length = 64)
    private String dataSource;

    /** 更新频率。 */
    @Column(name = "update_frequency", length = 64)
    private String updateFrequency;

    /** 交付方式。 */
    @Column(name = "delivery_method", length = 64)
    private String deliveryMethod;

    /** 是否涉及个人信息（0/1）。 */
    @Column(name = "involves_personal_info")
    private Boolean involvesPersonalInfo;

    /** 是否涉及公共数据（0/1）。 */
    @Column(name = "involves_public_data")
    private Boolean involvesPublicData;

    /** 计费方式。 */
    @Column(name = "billing_method", length = 128)
    private String billingMethod;

    /** 价格。 */
    @Column(name = "price", length = 128)
    private String price;

    /** 供应商名称。 */
    @Column(name = "supplier_name", length = 256)
    private String supplierName;

    /** 统一社会信用代码。 */
    @Column(name = "supplier_credit_code", length = 128)
    private String supplierCreditCode;

    /** 产权类型。 */
    @Column(name = "property_rights_type", length = 128)
    private String propertyRightsType;

    /** 标签 JSON。 */
    @Column(name = "tags_json", columnDefinition = "TEXT")
    private String tagsJson;

    /** 产品简介。 */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /** 应用场景。 */
    @Column(name = "scenario", columnDefinition = "TEXT")
    private String scenario;

    /** 数据集专属字段 JSON。 */
    @Column(name = "type_dataset_json", columnDefinition = "TEXT")
    private String typeDatasetJson;

    /** 报告专属字段 JSON。 */
    @Column(name = "type_report_json", columnDefinition = "TEXT")
    private String typeReportJson;

    /** API 专属字段 JSON。 */
    @Column(name = "type_api_json", columnDefinition = "TEXT")
    private String typeApiJson;

    /** 产品状态，默认 LISTED。 */
    @Column(name = "status", nullable = false, length = 32)
    private String status;
}
