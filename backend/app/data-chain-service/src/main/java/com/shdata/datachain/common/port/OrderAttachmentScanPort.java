package com.shdata.datachain.common.port;

/**
 * 订单附件扫描端口 — 对齐 PLAN-WSC-9.2 §2.2 OQ-V17-002。
 *
 * <p>本期默认实现为同步调用且对白名单文件返回通过；可替换为真实扫描引擎。
 * 扫描失败或未扫描 → 附件不合格，不能进入待确认合约。
 *
 * @author developer-wsc-916
 */
public interface OrderAttachmentScanPort {

  /**
   * 扫描附件。
   *
   * @param request 扫描请求
   * @return 扫描结果
   */
  ScanResult scan(ScanRequest request);

  /**
   * 附件扫描请求。
   *
   * @param fileName 文件名
   * @param fileType 文件类型（MIME 或后缀）
   * @param fileSize 文件大小（字节）
   * @param fileContentHash 文件内容哈希（可选，用于去重/校验）
   */
  record ScanRequest(
      String fileName,
      String fileType,
      long fileSize,
      String fileContentHash) {}

  /**
   * 附件扫描结果。
   *
   * @param passed 是否通过扫描
   * @param scanResultCode 扫描结果码（PASS / FAIL / ERROR）
   * @param message 附加说明
   */
  record ScanResult(
      boolean passed,
      String scanResultCode,
      String message) {}
}
