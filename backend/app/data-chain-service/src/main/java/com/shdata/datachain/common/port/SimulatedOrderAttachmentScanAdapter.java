package com.shdata.datachain.common.port;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 订单附件扫描 mock 适配器 — 对齐 OrderAttachmentScanPort 契约（OQ-V17-002）。
 *
 * <p>对白名单文件（Word/PDF）返回通过；其他文件返回失败。
 * 本期为同步调用且不依赖外部扫描引擎。
 *
 * @author developer-wsc-916
 */
@Component
public class SimulatedOrderAttachmentScanAdapter implements OrderAttachmentScanPort {

  private static final Logger log = LoggerFactory.getLogger(SimulatedOrderAttachmentScanAdapter.class);

  @Override
  public ScanResult scan(ScanRequest request) {
    log.debug("Mock attachment scan: fileName={}, fileType={}, fileSize={}",
        request.fileName(), request.fileType(), request.fileSize());

    // 白名单检查（与 OrderService 中的白名单逻辑一致）
    if (!isAllowedFileType(request.fileName())) {
      return new ScanResult(false, "FAIL", "文件类型不在白名单中");
    }

    // 20MB 限制
    if (request.fileSize() > 20 * 1024 * 1024) {
      return new ScanResult(false, "FAIL", "文件大小超过 20MB 限制");
    }

    // mock 扫描通过
    return new ScanResult(true, "PASS", "扫描通过");
  }

  /**
   * 校验文件类型是否在白名单（Word .doc/.docx、PDF .pdf）。
   *
   * @param fileName 文件名
   * @return 是否在白名单
   */
  private boolean isAllowedFileType(String fileName) {
    if (fileName == null) {
      return false;
    }
    String lower = fileName.toLowerCase();
    return lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".pdf");
  }
}
