package com.shdata.datachain.catalog.productimport;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** 导入错误报告内存存储；TTL 24h；对外仅暴露白名单字段。 */
@Component
public class ImportReportStore {

  public static final long TTL_HOURS = 24;

  private final ConcurrentHashMap<String, StoredReport> reports = new ConcurrentHashMap<>();

  public String save(String ownerUserId, List<ErrorRow> rows) {
    cleanupExpired();
    String reportId = "rep-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    Instant expiresAt = Instant.now().plusSeconds(TTL_HOURS * 3600);
    reports.put(reportId, new StoredReport(reportId, ownerUserId, expiresAt, List.copyOf(rows)));
    return reportId;
  }

  public Optional<StoredReport> find(String reportId) {
    cleanupExpired();
    StoredReport r = reports.get(reportId);
    if (r == null) {
      return Optional.empty();
    }
    if (r.expiresAt().isBefore(Instant.now())) {
      reports.remove(reportId);
      return Optional.empty();
    }
    return Optional.of(r);
  }

  /** 白名单序列化：仅 rowNumber / productCode / reasonCode / reasonMessage。 */
  public static Map<String, Object> toPublicMap(StoredReport report) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("reportId", report.reportId());
    data.put("expiresAt", report.expiresAt().toString());
    List<Map<String, Object>> rows = new ArrayList<>();
    for (ErrorRow row : report.rows()) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("rowNumber", row.rowNumber());
      m.put("productCode", row.productCode());
      m.put("reasonCode", row.reasonCode());
      m.put("reasonMessage", row.reasonMessage());
      rows.add(m);
    }
    data.put("rows", rows);
    return data;
  }

  void cleanupExpired() {
    Instant now = Instant.now();
    reports.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
  }

  /** 测试钩子：当前存活报告数。 */
  int size() {
    cleanupExpired();
    return reports.size();
  }

  public record ErrorRow(
      int rowNumber, String productCode, String reasonCode, String reasonMessage) {}

  public record StoredReport(
      String reportId, String ownerUserId, Instant expiresAt, List<ErrorRow> rows) {}
}
