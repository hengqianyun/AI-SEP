package com.shdata.datachain.repository;

import com.shdata.datachain.entity.ChainRegistryEntity;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 溯源链注册表访问：维护 {@code product_code ↔ chain_id} 映射，提供并发安全的建链登记。
 *
 * <p>{@link #registerChainId} 采用 insert-if-absent：唯一约束 {@code uk_t_chain_product}
 * 冲突时回查已生效记录，保证同一产品全局只认一条链（多线程/多实例均安全）。</p>
 *
 * @author ZhangTao
 */
@Component
public class ChainRegistryStore {

    private static final Logger log = LoggerFactory.getLogger(ChainRegistryStore.class);

    private final ChainRegistryRepository repo;

    public ChainRegistryStore(ChainRegistryRepository repo) {
        this.repo = repo;
    }

    /**
     * 查产品已登记的溯源链 ID。
     *
     * @param productCode 产品编码
     * @return 已登记的 chain_id；未登记或入参空返回 empty
     */
    @Transactional(readOnly = true)
    public Optional<String> findChainId(String productCode) {
        if (productCode == null || productCode.isBlank()) {
            return Optional.empty();
        }
        return repo.findFirstByProductCode(productCode).map(ChainRegistryEntity::getChainId);
    }

    /**
     * 登记产品与溯源链的映射（幂等）。
     *
     * <p>并发下两个线程同时为同一新产品登记不同 chain_id 时，唯一约束使仅一条 INSERT 成功，
     * 失败方捕获 {@link DataIntegrityViolationException} 后回查，返回已生效的 chain_id。
     * 因此调用方拿到的 chain_id 可能不是自己刚建的那条，但保证产品维度唯一。</p>
     *
     * @param productCode 产品编码
     * @param chainId 平台返回的溯源链 ID
     * @return 最终生效的 chain_id（可能是既有值）
     */
    @Transactional
    public String registerChainId(String productCode, String chainId) {
        ChainRegistryEntity entity =
                ChainRegistryEntity.builder()
                        .productCode(productCode)
                        .chainId(chainId)
                        .status("active")
                        .build();
        // BaseEntity 的审计字段不在子类 @Builder 范围内，用 setter 单独填充。
        entity.setCreateBy("chain-service");
        entity.setUpdateBy("chain-service");
        try {
            // saveAndFlush 立即触发 INSERT，确保违反唯一约束时在当前事务内抛出可被捕获。
            return repo.saveAndFlush(entity).getChainId();
        } catch (DataIntegrityViolationException duplicate) {
            log.warn(
                    "Chain registry duplicate on product_code={}, returning existing chainId",
                    productCode);
            return repo.findFirstByProductCode(productCode)
                    .map(ChainRegistryEntity::getChainId)
                    .orElse(chainId);
        }
    }
}
