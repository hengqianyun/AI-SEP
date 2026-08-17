package com.shdata.datachain.repository;

import com.shdata.datachain.entity.SysUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUserEntity, Long> {

    Optional<SysUserEntity> findByUsernameAndDelFlag(String username, Boolean delFlag);

    boolean existsByUsername(String username);

    List<SysUserEntity> findByDelFlagOrderByIdAsc(Boolean delFlag);

    /** 管理端列表：含软删账号，按 id 升序。 */
    List<SysUserEntity> findAllByOrderByIdAsc();

    /**
     * 按企业与角色列出未删除用户（一企多 ADMIN 验收）。
     *
     * @param enterpriseId 企业主键
     * @param role 角色名
     * @param delFlag 逻辑删除标记
     * @return 用户列表
     */
    List<SysUserEntity> findByEnterpriseIdAndRoleAndDelFlag(
            Long enterpriseId, String role, Boolean delFlag);

    /**
     * 查找仍为迁移占位（enterprise_id=0）的用户，供回填。
     *
     * @param enterpriseId 占位值（0）
     * @return 待回填用户
     */
    List<SysUserEntity> findByEnterpriseId(Long enterpriseId);
}
