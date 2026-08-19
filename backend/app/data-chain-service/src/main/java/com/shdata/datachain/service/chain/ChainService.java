package com.shdata.datachain.service.chain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.shdata.datachain.repository.InMemoryChainStore;

/**
 * 上链版本查询服务，负责版本列表/快照的业务数据组装，
 * 使 {@link ChainController} 仅做请求参数提取与响应封装。
 *
 * @author ZhangTao
 */
@Service
public class ChainService {

    private final InMemoryChainStore store;

    public ChainService(InMemoryChainStore store) {
        this.store = store;
    }

    /**
     * 获取指定产品的上链版本列表（版本号降序）。
     *
     * @param productId 产品 ID
     * @return 版本列表，key 为 {@code items}
     */
    public Map<String, Object> listVersions(String productId) {
        List<Map<String, Object>> items =
                store.listByProductId(productId).stream().map(this::toVersionView).toList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);
        return data;
    }

    /**
     * 获取指定版本的上链快照详情。
     *
     * @param versionId 版本 ID
     * @return 快照数据，不存在返回 {@link Optional#empty()}
     */
    public Optional<Map<String, Object>> getSnapshot(String versionId) {
        return store.findByVersionId(versionId).map(v -> new LinkedHashMap<>(v.snapshot()));
    }

    /**
     * 将存储版本转为 API 视图，包含证书、时间戳等链上元数据。
     */
    private Map<String, Object> toVersionView(InMemoryChainStore.StoredVersion v) {
        Map<String, Object> cert = new LinkedHashMap<>();
        cert.put("owner", v.certOwner());
        cert.put("timestamp", v.timestamp().toString());

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("versionId", v.versionId());
        item.put("versionNo", v.versionNo());
        item.put("timestamp", v.timestamp().toString());
        item.put("metadataHash", v.metadataHash());
        item.put("ownerDID", v.ownerDID());
        item.put("certificate", cert);
        // mock/真实链状态存放在快照 attestation 中；版本列表直接透出关键字段供页面展示。
        Object rawAttestation = v.snapshot().get("attestation");
        if (rawAttestation instanceof Map<?, ?> attestation) {
            item.put("evidenceId", attestation.get("evidenceId"));
            item.put("chainId", attestation.get("chainId"));
            item.put("status", attestation.get("status"));
            item.put("transactionHash", attestation.get("transactionHash"));
            item.put("blockNumber", attestation.get("blockNumber"));
            item.put("mocked", attestation.get("mocked"));
        }
        return item;
    }
}
