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
}
