package com.shdata.datachain.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shdata.datachain.entity.ImportErrorReportEntity;
import com.shdata.datachain.repository.ImportErrorReportRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 导入错误报告存储（JPA 版），替代原 ConcurrentHashMap 内存实现。
 * <p>TTL 24h，过期报告惰性清理。</p>
 */
@Component
public class ImportReportStore {

    public static final long TTL_HOURS = 24;

    private final ImportErrorReportRepository reportRepo;
    private final ObjectMapper objectMapper;

    public ImportReportStore(ImportErrorReportRepository reportRepo, ObjectMapper objectMapper) {
        this.reportRepo = reportRepo;
        this.objectMapper = objectMapper;
    }

    /**
     * 保存错误报告，返回 reportId（DB 主键 ID 的字符串形式）。
     */
    @Transactional
    public String save(String ownerUserId, List<ErrorRow> rows) {
        cleanupExpired();
        Instant expiresAt = Instant.now().plus(TTL_HOURS, ChronoUnit.HOURS);
        String rowsJson;
        try {
            rowsJson = objectMapper.writeValueAsString(rows);
        } catch (JsonProcessingException e) {
            rowsJson = "[]";
        }
        ImportErrorReportEntity entity = ImportErrorReportEntity.builder()
                .expiresAt(expiresAt)
                .successCount(0)
                .failureCount(rows.size())
                .rowsJson(rowsJson)
                .build();
        entity.setCreateBy(ownerUserId);
        entity = reportRepo.save(entity);
        return String.valueOf(entity.getId());
    }

    /**
     * 按主键 ID 查报告（未过期则返回）。
     */
    @Transactional
    public Optional<StoredReport> find(String reportId) {
        cleanupExpired();
        try {
            long id = Long.parseLong(reportId);
            Optional<ImportErrorReportEntity> opt = reportRepo.findById(id);
            if (opt.isEmpty()) return Optional.empty();
            ImportErrorReportEntity e = opt.get();
            if (e.getExpiresAt().isBefore(Instant.now())) {
                reportRepo.delete(e);
                return Optional.empty();
            }
            List<ErrorRow> rows = parseRows(e.getRowsJson());
            return Optional.of(new StoredReport(reportId, e.getCreateBy(), e.getExpiresAt(), rows));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    @Transactional
    void cleanupExpired() {
        Instant now = Instant.now();
        List<ImportErrorReportEntity> all = reportRepo.findAll();
        for (ImportErrorReportEntity e : all) {
            if (e.getExpiresAt().isBefore(now)) {
                reportRepo.delete(e);
            }
        }
    }

    int size() {
        cleanupExpired();
        return (int) reportRepo.count();
    }

    private List<ErrorRow> parseRows(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

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

    public record ErrorRow(int rowNumber, String productCode, String reasonCode, String reasonMessage) {}
    public record StoredReport(String reportId, String ownerUserId, Instant expiresAt, List<ErrorRow> rows) {}
}
