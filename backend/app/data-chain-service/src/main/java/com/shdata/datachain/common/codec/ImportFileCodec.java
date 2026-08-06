package com.shdata.datachain.common.codec;

import com.shdata.datachain.common.constant.ImportTemplateColumns;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * CSV / 简易 XLSX（OOXML）解析与模板写出，无外部 POI 依赖（pom 不在 writeSet）。
 * v0729：xlsx 第 1 行分组、第 2 行列名、第 3 行说明、第 4 行起数据。
 */
public final class ImportFileCodec {

  private ImportFileCodec() {}

  public record ParsedSheet(
      List<String> headers, List<Map<String, String>> rows, int dataStartRowNumber) {}

  public static ParsedSheet parse(byte[] bytes, String filename) throws FormatException {
    String name = filename == null ? "" : filename.toLowerCase();
    if (name.endsWith(".csv") || looksLikeCsv(bytes)) {
      return parseCsv(bytes);
    }
    if (name.endsWith(".xlsx") || looksLikeZip(bytes)) {
      return parseXlsx(bytes);
    }
    throw new FormatException("无法解析文件或非 xlsx/csv");
  }

  public static byte[] writeTemplate(String format) throws IOException {
    if ("csv".equalsIgnoreCase(format)) {
      StringBuilder sb = new StringBuilder();
      sb.append(String.join(",", ImportTemplateColumns.COLUMNS));
      sb.append('\n');
      return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
    byte[] authoritative = loadAuthoritativeXlsx();
    if (authoritative != null && authoritative.length > 0) {
      return authoritative;
    }
    // 回退：仅列名行（测试/资源缺失时）
    return writeMinimalXlsx(List.of(ImportTemplateColumns.COLUMNS));
  }

  private static byte[] loadAuthoritativeXlsx() {
    String[] paths = {
      "import/product-import-template-v0729.xlsx",
      "/import/product-import-template-v0729.xlsx"
    };
    ClassLoader cl = ImportFileCodec.class.getClassLoader();
    for (String p : paths) {
      try (InputStream in = cl.getResourceAsStream(p.startsWith("/") ? p.substring(1) : p)) {
        if (in != null) {
          return in.readAllBytes();
        }
      } catch (IOException ignored) {
        // try next
      }
    }
    return null;
  }

  private static boolean looksLikeZip(byte[] bytes) {
    return bytes != null && bytes.length >= 4 && bytes[0] == 'P' && bytes[1] == 'K';
  }

  private static boolean looksLikeCsv(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return false;
    }
    if (looksLikeZip(bytes)) {
      return false;
    }
    String head = new String(bytes, 0, Math.min(bytes.length, 400), StandardCharsets.UTF_8);
    return head.contains("产品名称") || head.contains(",");
  }

  public static ParsedSheet parseCsv(byte[] bytes) throws FormatException {
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8))) {
      String headerLine = reader.readLine();
      if (headerLine == null || headerLine.isBlank()) {
        throw new FormatException("无法解析文件或非 xlsx/csv");
      }
      if (headerLine.startsWith("\uFEFF")) {
        headerLine = headerLine.substring(1);
      }
      List<String> headers = splitCsvLine(headerLine);
      if (headers.stream().noneMatch(h -> h != null && h.contains("产品名称"))) {
        throw new FormatException("无法解析文件或非 xlsx/csv");
      }
      List<Map<String, String>> rows = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        List<String> cols = splitCsvLine(line);
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
          String key = headers.get(i).trim();
          String val = i < cols.size() ? cols.get(i) : "";
          map.put(key, val);
        }
        rows.add(map);
      }
      return new ParsedSheet(trimHeaders(headers), rows, 2);
    } catch (FormatException e) {
      throw e;
    } catch (IOException e) {
      throw new FormatException("无法解析文件或非 xlsx/csv");
    }
  }

  private static List<String> splitCsvLine(String line) {
    List<String> out = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          cur.append('"');
          i++;
        } else {
          inQuotes = !inQuotes;
        }
      } else if (c == ',' && !inQuotes) {
        out.add(cur.toString());
        cur.setLength(0);
      } else {
        cur.append(c);
      }
    }
    out.add(cur.toString());
    return out;
  }

  public static ParsedSheet parseXlsx(byte[] bytes) throws FormatException {
    try {
      List<String> shared = new ArrayList<>();
      byte[] sheetXml = null;
      try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
          String n = entry.getName();
          if ("xl/sharedStrings.xml".equals(n)) {
            shared = parseSharedStrings(readAll(zis));
          } else if ("xl/worksheets/sheet1.xml".equals(n)) {
            sheetXml = readAll(zis);
          }
        }
      }
      if (sheetXml == null) {
        throw new FormatException("无法解析文件或非 xlsx/csv");
      }
      List<List<String>> grid = parseSheet(sheetXml, shared);
      if (grid.isEmpty()) {
        throw new FormatException("无法解析文件或非 xlsx/csv");
      }
      int headerIdx = detectHeaderRowIndex(grid);
      List<String> headers = trimHeaders(grid.get(headerIdx));
      if (headers.stream().noneMatch(h -> h.contains("产品名称"))) {
        throw new FormatException("无法解析文件或非 xlsx/csv");
      }
      int dataStart = headerIdx + 1;
      // v0729：列名行后通常有一行填写说明
      if (headerIdx >= 1 && dataStart < grid.size() && looksLikeInstructionRow(grid.get(dataStart))) {
        dataStart++;
      }
      List<Map<String, String>> rows = new ArrayList<>();
      for (int r = dataStart; r < grid.size(); r++) {
        List<String> cols = grid.get(r);
        if (cols.stream().allMatch(s -> s == null || s.isBlank())) {
          continue;
        }
        // 跳过示例标题行（如「接口类产品示例」）
        if (cols.size() > 0 && cols.get(0) != null && cols.get(0).contains("示例") && cols.size() < 4) {
          continue;
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
          String key = headers.get(i);
          String val = i < cols.size() && cols.get(i) != null ? cols.get(i) : "";
          map.put(key, val);
        }
        rows.add(map);
      }
      return new ParsedSheet(headers, rows, dataStart + 1);
    } catch (FormatException e) {
      throw e;
    } catch (Exception e) {
      throw new FormatException("无法解析文件或非 xlsx/csv");
    }
  }

  private static int detectHeaderRowIndex(List<List<String>> grid) {
    for (int i = 0; i < Math.min(grid.size(), 5); i++) {
      List<String> row = grid.get(i);
      Set<String> set = new LinkedHashSet<>();
      for (String c : row) {
        if (c != null && !c.isBlank()) {
          set.add(c.trim());
        }
      }
      if (ImportTemplateColumns.matchesV0729(set)) {
        return i;
      }
      if (row.stream().anyMatch(c -> c != null && c.contains("产品名称（必填）"))) {
        return i;
      }
      if (row.stream().anyMatch(c -> c != null && c.equals("产品名称"))
          && row.stream().anyMatch(c -> c != null && c.equals("产品编码"))) {
        return i;
      }
    }
    return 0;
  }

  private static boolean looksLikeInstructionRow(List<String> row) {
    if (row == null || row.isEmpty()) {
      return false;
    }
    String joined = String.join(" ", row);
    return joined.contains("产品的中文名称")
        || joined.contains("填写")
        || joined.contains("可选值")
        || joined.contains("GB/T");
  }

  private static List<String> trimHeaders(List<String> headers) {
    List<String> out = new ArrayList<>();
    for (String h : headers) {
      out.add(h == null ? "" : h.trim());
    }
    return out;
  }

  private static List<String> parseSharedStrings(byte[] xml) throws Exception {
    Document doc = parseXml(xml);
    NodeList si = doc.getElementsByTagName("si");
    List<String> out = new ArrayList<>();
    for (int i = 0; i < si.getLength(); i++) {
      Element el = (Element) si.item(i);
      out.add(el.getTextContent());
    }
    return out;
  }

  private static List<List<String>> parseSheet(byte[] xml, List<String> shared) throws Exception {
    Document doc = parseXml(xml);
    NodeList rowNodes = doc.getElementsByTagName("row");
    List<List<String>> grid = new ArrayList<>();
    for (int i = 0; i < rowNodes.getLength(); i++) {
      Element row = (Element) rowNodes.item(i);
      NodeList cells = row.getElementsByTagName("c");
      List<String> cols = new ArrayList<>();
      int expectedCol = 0;
      for (int j = 0; j < cells.getLength(); j++) {
        Element c = (Element) cells.item(j);
        String ref = c.getAttribute("r");
        int colIdx = columnIndex(ref);
        while (expectedCol < colIdx) {
          cols.add("");
          expectedCol++;
        }
        String t = c.getAttribute("t");
        String v = textOfFirst(c, "v");
        String val = "";
        if ("s".equals(t) && v != null && !v.isBlank()) {
          int idx = Integer.parseInt(v.trim());
          val = idx < shared.size() ? shared.get(idx) : "";
        } else if ("inlineStr".equals(t)) {
          String is = textOfFirst(c, "t");
          val = is == null ? "" : is;
        } else if (v != null) {
          val = v;
        } else {
          String is = textOfFirst(c, "t");
          if (is != null) {
            val = is;
          }
        }
        cols.add(val);
        expectedCol = colIdx + 1;
      }
      grid.add(cols);
    }
    return grid;
  }

  private static int columnIndex(String cellRef) {
    if (cellRef == null || cellRef.isEmpty()) {
      return 0;
    }
    int i = 0;
    int col = 0;
    while (i < cellRef.length() && Character.isLetter(cellRef.charAt(i))) {
      col = col * 26 + (Character.toUpperCase(cellRef.charAt(i)) - 'A' + 1);
      i++;
    }
    return Math.max(0, col - 1);
  }

  private static String textOfFirst(Element parent, String tag) {
    NodeList list = parent.getElementsByTagName(tag);
    if (list.getLength() == 0) {
      return null;
    }
    Node n = list.item(0);
    return n.getTextContent();
  }

  private static Document parseXml(byte[] xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(false);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
  }

  private static byte[] writeMinimalXlsx(List<List<String>> rows) throws IOException {
    List<String> shared = new ArrayList<>();
    Map<String, Integer> index = new LinkedHashMap<>();
    for (List<String> row : rows) {
      for (String cell : row) {
        if (!index.containsKey(cell)) {
          index.put(cell, shared.size());
          shared.add(cell);
        }
      }
    }
    StringBuilder sst = new StringBuilder();
    sst.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
    sst.append("<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"")
        .append(shared.size())
        .append("\" uniqueCount=\"")
        .append(shared.size())
        .append("\">");
    for (String s : shared) {
      sst.append("<si><t>").append(xmlEscape(s)).append("</t></si>");
    }
    sst.append("</sst>");

    StringBuilder sheet = new StringBuilder();
    sheet.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
    sheet.append(
        "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
    for (int r = 0; r < rows.size(); r++) {
      sheet.append("<row r=\"").append(r + 1).append("\">");
      List<String> row = rows.get(r);
      for (int c = 0; c < row.size(); c++) {
        String ref = colName(c) + (r + 1);
        int si = index.get(row.get(c));
        sheet
            .append("<c r=\"")
            .append(ref)
            .append("\" t=\"s\"><v>")
            .append(si)
            .append("</v></c>");
      }
      sheet.append("</row>");
    }
    sheet.append("</sheetData></worksheet>");

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(bos)) {
      put(
          zos,
          "[Content_Types].xml",
          """
          <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
          <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
            <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
            <Default Extension="xml" ContentType="application/xml"/>
            <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
            <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
            <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
          </Types>
          """
              .getBytes(StandardCharsets.UTF_8));
      put(
          zos,
          "_rels/.rels",
          """
          <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
          <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
          </Relationships>
          """
              .getBytes(StandardCharsets.UTF_8));
      put(
          zos,
          "xl/workbook.xml",
          """
          <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
          <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
            <sheets><sheet name="Sheet1" sheetId="1" r:id="rId1"/></sheets>
          </workbook>
          """
              .getBytes(StandardCharsets.UTF_8));
      put(
          zos,
          "xl/_rels/workbook.xml.rels",
          """
          <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
          <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
            <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
          </Relationships>
          """
              .getBytes(StandardCharsets.UTF_8));
      put(zos, "xl/sharedStrings.xml", sst.toString().getBytes(StandardCharsets.UTF_8));
      put(zos, "xl/worksheets/sheet1.xml", sheet.toString().getBytes(StandardCharsets.UTF_8));
    }
    return bos.toByteArray();
  }

  private static void put(ZipOutputStream zos, String name, byte[] data) throws IOException {
    zos.putNextEntry(new ZipEntry(name));
    zos.write(data);
    zos.closeEntry();
  }

  private static String colName(int index) {
    StringBuilder sb = new StringBuilder();
    int n = index;
    do {
      sb.insert(0, (char) ('A' + (n % 26)));
      n = n / 26 - 1;
    } while (n >= 0);
    return sb.toString();
  }

  private static String xmlEscape(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  private static byte[] readAll(InputStream in) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    in.transferTo(bos);
    return bos.toByteArray();
  }

  public static void writeTo(OutputStream out, byte[] data) throws IOException {
    out.write(data);
  }

  public static final class FormatException extends Exception {
    FormatException(String message) {
      super(message);
    }
  }
}
