package com.shdata.datachain.common.constant;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * v0729 权威列名行（xlsx 第 2 行；ISSUE-API-WSC4-R1-001 / OpenAPI ImportTemplateColumns）。
 * V1.1 最小列集（含产品编码等）已废止。
 */
public final class ImportTemplateColumns {

  /** 列名行权威有序列表（与契约 example 一致）。 */
  public static final List<String> COLUMNS =
      List.of(
          "产品名称（必填）",
          "产品类型（必填）",
          "行业分类（必填）",
          "产品简介（必填）",
          "交付方式",
          "时间范围",
          "地域范围",
          "更新频率",
          "接口定义",
          "字段描述",
          "数据样例",
          "数据规模",
          "数据形态",
          "数据内容描述");

  public static final Set<String> COLUMN_SET = new LinkedHashSet<>(COLUMNS);

  /** 已废止的 V1.1 最小列集（仅用于识别旧模板）。 */
  public static final Set<String> LEGACY_V11_COLUMNS =
      Set.of(
          "产品名称",
          "产品编码",
          "产品类型",
          "行业分类",
          "数据来源",
          "更新频率",
          "涉及个人信息",
          "涉及公共数据",
          "交付方式",
          "计费方式",
          "价格");

  private ImportTemplateColumns() {}

  /** 表头是否精确匹配 v0729 列名行集合（分组行不参与）。 */
  public static boolean matchesV0729(Set<String> headers) {
    if (headers == null || headers.isEmpty()) {
      return false;
    }
    Set<String> normalized = new LinkedHashSet<>();
    for (String h : headers) {
      if (h != null && !h.isBlank()) {
        normalized.add(h.trim());
      }
    }
    return COLUMN_SET.equals(normalized);
  }
}
