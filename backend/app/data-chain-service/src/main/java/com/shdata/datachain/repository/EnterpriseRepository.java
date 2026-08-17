package com.shdata.datachain.repository;

import com.shdata.datachain.entity.EnterpriseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 企业仓储（逻辑 FK，无物理外键）。
 */
public interface EnterpriseRepository extends JpaRepository<EnterpriseEntity, Long> {

    /**
     * 按编码查询未删除企业。
     *
     * @param code 企业编码
     * @param delFlag 逻辑删除标记
     * @return 匹配企业
     */
    Optional<EnterpriseEntity> findByCodeAndDelFlag(String code, Boolean delFlag);

    /**
     * 按主键查询未删除企业。
     *
     * @param id 企业主键
     * @param delFlag 逻辑删除标记
     * @return 匹配企业
     */
    Optional<EnterpriseEntity> findByIdAndDelFlag(Long id, Boolean delFlag);

    /**
     * 按名称查询未删除、非未归属占位的企业（名称匹配回填优先）。
     *
     * @param name 企业名称
     * @param delFlag 逻辑删除标记
     * @param unassignedDefault 是否未归属默认企业
     * @return 最小 id 匹配
     */
    Optional<EnterpriseEntity> findFirstByNameAndDelFlagAndUnassignedDefaultOrderByIdAsc(
            String name, Boolean delFlag, Boolean unassignedDefault);

    /**
     * 按名称查询未删除企业（含未归属占位）。
     *
     * @param name 企业名称
     * @param delFlag 逻辑删除标记
     * @return 最小 id 匹配
     */
    Optional<EnterpriseEntity> findFirstByNameAndDelFlagOrderByIdAsc(String name, Boolean delFlag);

    /**
     * 查询未归属默认企业。
     *
     * @param unassignedDefault 应为 true
     * @param delFlag 逻辑删除标记
     * @return 占位企业
     */
    Optional<EnterpriseEntity> findFirstByUnassignedDefaultAndDelFlagOrderByIdAsc(
            Boolean unassignedDefault, Boolean delFlag);
}
