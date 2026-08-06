package com.shdata.datachain.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shdata.datachain.entity.ChainCatalogSnapshotEntity;
import com.shdata.datachain.entity.ChainVersionEntity;
import com.shdata.datachain.repository.ChainCatalogSnapshotRepository;
import com.shdata.datachain.repository.ChainVersionRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 上链版本存储（JPA 版），替代原 ConcurrentHashMap 内存实现。
 *
 * <p>口径：未知 productId → 空列表（HTTP 200），不 404。
 */
@Component
public class InMemoryChainStore {

    private final ChainVersionRepository versionRepo;
    private final ChainCatalogSnapshotRepository snapshotRepo;
    private final ObjectMapper objectMapper;

    public InMemoryChainStore(ChainVersionRepository versionRepo,
                              ChainCatalogSnapshotRepository snapshotRepo,
                              ObjectMapper objectMapper) {
        this.versionRepo = versionRepo;
        this.snapshotRepo = snapshotRepo;
        this.objectMapper = objectMapper;
    }

    /** 最新优先（versionNo 降序）。 */
    @Transactional(readOnly = true)
    public List<StoredVersion> listByProductId(String productCode) {
        return versionRepo.findAll().stream()
                .filter(v -> productCode.equals(v.getProductCode()))
                .map(this::toStoredVersion)
                .sorted(Comparator.comparingInt(StoredVersion::versionNo).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<StoredVersion> findByVersionId(String versionCode) {
        return versionRepo.findAll().stream()
                .filter(v -> versionCode.equals(v.getVersionCode()))
                .findFirst()
                .map(this::toStoredVersion);
    }

    /**
     * 写入新产品/编辑产生的上链版本；同时写入快照。
     */
    @Transactional
    public synchronized StoredVersion appendVersion(
            String versionCode,
            String productCode,
            int versionNo,
            String metadataHash,
            String ownerDID,
            Instant timestamp,
            String certOwner,
            String snapshotJson) {
        ChainVersionEntity v = ChainVersionEntity.builder()
                .versionCode(versionCode)
                .productCode(productCode)
                .versionNo(versionNo)
                .metadataHash(metadataHash)
                .ownerDid(ownerDID)
                .certOwner(certOwner)
                .ts(timestamp)
                .build();
        versionRepo.save(v);

        ChainCatalogSnapshotEntity snap = ChainCatalogSnapshotEntity.builder()
                .versionCode(versionCode)
                .snapshotJson(snapshotJson)
                .build();
        snapshotRepo.save(snap);

        Map<String, Object> snapshot = parseSnapshot(snapshotJson);
        return new StoredVersion(versionCode, productCode, versionNo, metadataHash, ownerDID,
                timestamp, certOwner, snapshotJson, snapshot);
    }

    /** 当前产品最大版本号；无记录返回 0。 */
    @Transactional(readOnly = true)
    public int latestVersionNo(String productCode) {
        return versionRepo.findAll().stream()
                .filter(v -> productCode.equals(v.getProductCode()))
                .mapToInt(ChainVersionEntity::getVersionNo)
                .max()
                .orElse(0);
    }

    private StoredVersion toStoredVersion(ChainVersionEntity v) {
        String snapshotJson = snapshotRepo.findAll().stream()
                .filter(s -> v.getVersionCode().equals(s.getVersionCode()))
                .findFirst()
                .map(ChainCatalogSnapshotEntity::getSnapshotJson)
                .orElse("{}");
        Map<String, Object> snapshot = parseSnapshot(snapshotJson);
        return new StoredVersion(v.getVersionCode(), v.getProductCode(), v.getVersionNo(),
                v.getMetadataHash(), v.getOwnerDid(), v.getTs(), v.getCertOwner(),
                snapshotJson, snapshot);
    }

    private Map<String, Object> parseSnapshot(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return new LinkedHashMap<>();
        }
    }

    public record StoredVersion(
            String versionId,
            String productId,
            int versionNo,
            String metadataHash,
            String ownerDID,
            Instant timestamp,
            String certOwner,
            String snapshotJson,
            Map<String, Object> snapshot) {}
}
