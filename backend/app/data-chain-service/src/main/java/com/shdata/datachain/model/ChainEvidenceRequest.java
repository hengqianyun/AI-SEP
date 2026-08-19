package com.shdata.datachain.model;

import java.util.Map;

/**
 * 通用上链请求。原文仅在本地计算 SHA-256，发送到 ChainMP 的始终是哈希值。
 *
 * @param evidenceId 全局业务存证编号
 * @param content 待存证原文
 * @param metadata 随存证保存的业务描述
 */
public record ChainEvidenceRequest(
    String evidenceId, String content, Map<String, Object> metadata) {}
