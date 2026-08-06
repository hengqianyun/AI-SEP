package com.shdata.datachain.common.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.yaml.snakeyaml.Yaml;

/**
 * 从「接口定义」单元格解析 OpenAPI/Swagger → endpoints[]（导入路径派生；§3.3 方案 B）。
 */
public final class ImportOpenApiParser {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final List<String> METHODS =
      List.of("get", "post", "put", "delete", "patch", "head", "options", "trace");

  private ImportOpenApiParser() {}

  static List<Map<String, Object>> parseEndpoints(String raw) throws ParseException {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }
    String text = raw.trim();
    Object root;
    try {
      if (text.startsWith("{") || text.startsWith("[")) {
        root = JSON.readValue(text, Object.class);
      } else {
        root = new Yaml().load(text);
      }
    } catch (Exception ex) {
      throw new ParseException("接口定义解析失败：非合法 OpenAPI/Swagger 文本");
    }
    if (!(root instanceof Map<?, ?> map)) {
      throw new ParseException("接口定义解析失败：根节点须为对象");
    }
    Object pathsObj = map.get("paths");
    if (pathsObj == null) {
      // 允许仅有 info 的空 paths；若完全无法识别结构则失败
      if (!map.containsKey("openapi") && !map.containsKey("swagger")) {
        throw new ParseException("接口定义解析失败：缺少 openapi/swagger 或 paths");
      }
      return List.of();
    }
    if (!(pathsObj instanceof Map<?, ?> paths)) {
      throw new ParseException("接口定义解析失败：paths 非法");
    }
    List<Map<String, Object>> endpoints = new ArrayList<>();
    for (Map.Entry<?, ?> pathEntry : paths.entrySet()) {
      String path = String.valueOf(pathEntry.getKey());
      if (!(pathEntry.getValue() instanceof Map<?, ?> ops)) {
        continue;
      }
      for (Map.Entry<?, ?> opEntry : ops.entrySet()) {
        String methodKey = String.valueOf(opEntry.getKey()).toLowerCase(Locale.ROOT);
        if (!METHODS.contains(methodKey)) {
          continue;
        }
        if (!(opEntry.getValue() instanceof Map<?, ?> op)) {
          continue;
        }
        Map<String, Object> ep = new LinkedHashMap<>();
        ep.put("id", "ep-" + UUID.randomUUID().toString().substring(0, 8));
        ep.put("method", methodKey.toUpperCase(Locale.ROOT));
        ep.put("path", path);
        ep.put("summary", stringOrEmpty(op.get("summary")));
        ep.put("description", stringOrEmpty(op.get("description")));
        ep.put("parameters", extractParameters(op.get("parameters")));
        ep.put("responses", extractResponses(op.get("responses")));
        String reqSchema = extractBodySchema(op.get("requestBody"));
        if (reqSchema != null) {
          ep.put("requestBodySchema", reqSchema);
        }
        String resSchema = extractFirstResponseSchema(op.get("responses"));
        if (resSchema != null) {
          ep.put("responseBodySchema", resSchema);
        }
        endpoints.add(ep);
      }
    }
    return endpoints;
  }

  private static List<Map<String, Object>> extractParameters(Object raw) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (!(raw instanceof List<?> list)) {
      return out;
    }
    for (Object item : list) {
      if (!(item instanceof Map<?, ?> m)) {
        continue;
      }
      Map<String, Object> p = new LinkedHashMap<>();
      p.put("name", stringOrEmpty(m.get("name")));
      Object schema = m.get("schema");
      String type = "string";
      if (schema instanceof Map<?, ?> sm && sm.get("type") != null) {
        type = String.valueOf(sm.get("type"));
      } else if (m.get("type") != null) {
        type = String.valueOf(m.get("type"));
      }
      p.put("type", type);
      p.put("required", Boolean.TRUE.equals(m.get("required")));
      p.put("description", stringOrEmpty(m.get("description")));
      out.add(p);
    }
    return out;
  }

  private static List<Map<String, Object>> extractResponses(Object raw) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (!(raw instanceof Map<?, ?> map)) {
      return out;
    }
    for (Map.Entry<?, ?> e : map.entrySet()) {
      Map<String, Object> r = new LinkedHashMap<>();
      r.put("code", String.valueOf(e.getKey()));
      if (e.getValue() instanceof Map<?, ?> rm) {
        r.put("description", stringOrEmpty(rm.get("description")));
      } else {
        r.put("description", "");
      }
      out.add(r);
    }
    return out;
  }

  private static String extractBodySchema(Object requestBody) {
    if (!(requestBody instanceof Map<?, ?> rb)) {
      return null;
    }
    Object content = rb.get("content");
    if (!(content instanceof Map<?, ?> cm)) {
      return null;
    }
    for (Object media : cm.values()) {
      if (media instanceof Map<?, ?> mm && mm.get("schema") != null) {
        try {
          return JSON.writeValueAsString(mm.get("schema"));
        } catch (Exception ignored) {
          return String.valueOf(mm.get("schema"));
        }
      }
    }
    return null;
  }

  private static String extractFirstResponseSchema(Object responses) {
    if (!(responses instanceof Map<?, ?> map)) {
      return null;
    }
    for (Object v : map.values()) {
      if (!(v instanceof Map<?, ?> rm)) {
        continue;
      }
      Object content = rm.get("content");
      if (!(content instanceof Map<?, ?> cm)) {
        continue;
      }
      for (Object media : cm.values()) {
        if (media instanceof Map<?, ?> mm && mm.get("schema") != null) {
          try {
            return JSON.writeValueAsString(mm.get("schema"));
          } catch (Exception ignored) {
            return String.valueOf(mm.get("schema"));
          }
        }
      }
    }
    return null;
  }

  private static String stringOrEmpty(Object v) {
    return v == null ? "" : String.valueOf(v);
  }

  /** 供冲突断言：将原文再解析为 endpoints（与客户端提交对比）。 */
  static List<Map<String, Object>> parseEndpointsOrEmpty(String raw) {
    try {
      return parseEndpoints(raw);
    } catch (ParseException e) {
      return List.of();
    }
  }

  static final class ParseException extends Exception {
    ParseException(String message) {
      super(message);
    }
  }
}
