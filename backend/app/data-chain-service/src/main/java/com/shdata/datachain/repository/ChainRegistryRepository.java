package com.shdata.datachain.repository;

import com.shdata.datachain.entity.ChainRegistryEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 溯源链注册表 t_chain 的 JPA Repository。
 *
 * @author ZhangTao
 */
public interface ChainRegistryRepository extends JpaRepository<ChainRegistryEntity, Long> {

    /** 按产品编码查链登记（未删除，由 {@code @Where} 自动过滤 del_flag）。 */
    Optional<ChainRegistryEntity> findFirstByProductCode(String productCode);
}
