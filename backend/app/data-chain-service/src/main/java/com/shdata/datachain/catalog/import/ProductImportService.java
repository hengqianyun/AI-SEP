package com.shdata.datachain.catalog.productimport;

import com.shdata.datachain.catalog.browse.CatalogBrowseSeedStore;
import com.shdata.datachain.catalog.editor.ProductEditorService;
import com.shdata.datachain.security.Role;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 同步批量导入：请求级拒绝 vs 行级结果；成功行经 ProductEditorService 写入并上链。 */
@Service
public class ProductImportService {

  private final ProductEditorService productEditorService;
  private final CatalogBrowseSeedStore catalog;
  private final ImportReportStore reportStore;
  private final AtomicInteger tempFilesCleaned = new AtomicInteger();

  public ProductImportService(
      ProductEditorService productEditorService,
      CatalogBrowseSeedStore catalog,
      ImportReportStore reportStore) {
    this.productEditorService = productEditorService;
    this.catalog = catalog;
    this.reportStore = reportStore;
  }

  public Result importFile(MultipartFile file, String actorUserId, String correlationId) {
    if (file == null || file.isEmpty()) {
      return Result.fail(400, "ERR_IMPORT_FORMAT_INVALID", "无法解析文件或非 xlsx/csv", null);
    }
    long size = file.getSize();
    if (size > ImportMultipartConfig.MAX_BYTES) {
      return Result.fail(400, "ERR_IMPORT_FILE_TOO_LARGE", "导入文件超过 10MB", null);
    }

    String original = file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename();
    Path temp = null;
    try {
      String suffix = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".bin";
      temp = Files.createTempFile("wsc-import-", suffix);
      file.transferTo(temp.toFile());
      byte[] bytes = Files.readAllBytes(temp);

      ImportFileCodec.ParsedSheet sheet;
      try {
        sheet = ImportFileCodec.parse(bytes, original);
      } catch (ImportFileCodec.FormatException ex) {
        return Result.fail(400, "ERR_IMPORT_FORMAT_INVALID", "无法解析文件或非 xlsx/csv", null);
      }

      Set<String> headerSet = new LinkedHashSet<>();
      for (String h : sheet.headers()) {
        if (h != null && !h.isBlank()) {
          headerSet.add(h.trim());
        }
      }
      if (!ImportTemplateColumns.matchesV0729(headerSet)) {
        return Result.fail(
            400,
            "ERR_IMPORT_TEMPLATE_UNSUPPORTED",
            "导入模板不受支持：表头须为 v0729 权威列名行",
            null);
      }

      ImportFieldMapper mapper = new ImportFieldMapper(catalog);
      int success = 0;
      List<ImportReportStore.ErrorRow> errors = new ArrayList<>();

      for (int i = 0; i < sheet.rows().size(); i++) {
        int rowNumber = sheet.dataStartRowNumber() + i;
        Map<String, String> cells = sheet.rows().get(i);
        String codeHint = null;
        try {
          Map<String, Object> body = mapper.toWriteBody(cells);
          codeHint = stringFromBody(body, "productCode");
          ProductEditorService.Result created =
              productEditorService.create(body, actorUserId, correlationId);
          if (created.ok()) {
            success++;
          } else {
            String reasonCode =
                created.code() == null || "0".equals(created.code())
                    ? "ERR_IMPORT_ROW_INVALID"
                    : created.code();
            errors.add(
                new ImportReportStore.ErrorRow(
                    rowNumber,
                    codeHint,
                    reasonCode,
                    created.message() == null ? "行级校验失败" : created.message()));
          }
        } catch (ImportFieldMapper.RowException ex) {
          errors.add(new ImportReportStore.ErrorRow(rowNumber, codeHint, ex.code, ex.message));
        }
      }

      Map<String, Object> data = new LinkedHashMap<>();
      data.put("successCount", success);
      data.put("failureCount", errors.size());
      if (errors.isEmpty()) {
        data.put("reportId", null);
      } else {
        String reportId = reportStore.save(actorUserId, errors);
        data.put("reportId", reportId);
      }
      return Result.ok(data);
    } catch (IOException ex) {
      return Result.fail(400, "ERR_IMPORT_FORMAT_INVALID", "无法解析文件或非 xlsx/csv", null);
    } finally {
      if (temp != null) {
        try {
          if (Files.deleteIfExists(temp)) {
            tempFilesCleaned.incrementAndGet();
          }
        } catch (IOException ignored) {
          // best-effort cleanup
        }
      }
    }
  }

  public Result getReport(String reportId, String actorUserId, Role role) {
    var opt = reportStore.find(reportId);
    if (opt.isEmpty()) {
      return Result.fail(404, "ERR_IMPORT_REPORT_NOT_FOUND", "错误报告不存在或已过期", null);
    }
    ImportReportStore.StoredReport report = opt.get();
    boolean allowed =
        role == Role.ADMIN
            || (role == Role.PROVIDER && report.ownerUserId().equals(actorUserId));
    if (!allowed) {
      return Result.fail(403, "ERR_FORBIDDEN", "当前角色无权执行该写操作", null);
    }
    return Result.ok(ImportReportStore.toPublicMap(report));
  }

  public byte[] templateBytes(String format) throws IOException {
    String f = format == null || format.isBlank() ? "xlsx" : format;
    if (!"xlsx".equalsIgnoreCase(f) && !"csv".equalsIgnoreCase(f)) {
      f = "xlsx";
    }
    return ImportFileCodec.writeTemplate(f);
  }

  /** 测试钩子：临时文件成功清理次数。 */
  public int tempFilesCleanedCount() {
    return tempFilesCleaned.get();
  }

  private static String stringFromBody(Map<String, Object> body, String key) {
    Object v = body.get(key);
    return v == null ? null : String.valueOf(v);
  }

  public record Result(boolean ok, int httpStatus, String code, String message, Object data) {
    static Result ok(Object data) {
      return new Result(true, 200, "0", "OK", data);
    }

    static Result fail(int httpStatus, String code, String message, Object data) {
      return new Result(false, httpStatus, code, message, data);
    }
  }
}
