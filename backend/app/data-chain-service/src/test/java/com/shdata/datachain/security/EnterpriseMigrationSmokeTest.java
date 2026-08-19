package com.shdata.datachain.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TASK-WSC-904 已有库迁移 smoke：V6 sys_user（仅 enterprise_name）经 V7 回填 enterprise_id。
 */
class EnterpriseMigrationSmokeTest {

  @Test
  void existingDatabase_enterpriseNameBackfill() throws Exception {
    String url =
        "jdbc:h2:mem:wsc_legacy_904;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
    try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
      runClasspathSql(conn, "sql/migration/V6__create_sys_user.sql");
      try (Statement st = conn.createStatement()) {
        st.execute(
            "INSERT INTO sys_user (username, password_hash, display_name, role, enterprise_name, create_by, update_by) "
                + "VALUES ('old_demo', 'x', '旧演示', 'USER', '演示企业', 'system', 'system')");
        st.execute(
            "INSERT INTO sys_user (username, password_hash, display_name, role, enterprise_name, create_by, update_by) "
                + "VALUES ('old_empty', 'x', '空名', 'USER', '', 'system', 'system')");
        st.execute(
            "INSERT INTO sys_user (username, password_hash, display_name, role, enterprise_name, create_by, update_by) "
                + "VALUES ('old_unknown', 'x', '未知', 'USER', '未知公司', 'system', 'system')");
      }
      runClasspathSql(conn, "sql/migration/V9__create_sys_enterprise_and_user_enterprise_id.sql");

      long demoId = queryLong(conn, "SELECT id FROM sys_enterprise WHERE code='DEMO'");
      long unassignedId = queryLong(conn, "SELECT id FROM sys_enterprise WHERE code='UNASSIGNED'");
      assertNotEquals(demoId, unassignedId);
      assertEquals(
          demoId, queryLong(conn, "SELECT enterprise_id FROM sys_user WHERE username='old_demo'"));
      assertEquals(
          unassignedId,
          queryLong(conn, "SELECT enterprise_id FROM sys_user WHERE username='old_empty'"));
      assertEquals(
          unassignedId,
          queryLong(conn, "SELECT enterprise_id FROM sys_user WHERE username='old_unknown'"));
      assertEquals(
          "演示企业",
          queryString(conn, "SELECT enterprise_name FROM sys_user WHERE username='old_demo'"));
      assertEquals(
          "未归属默认企业",
          queryString(conn, "SELECT enterprise_name FROM sys_user WHERE username='old_empty'"));
      // 未知原名：覆盖为企业表规范名；原名仅能从备份表查询（与 V7 脚本头策略一致）
      assertEquals(
          "未归属默认企业",
          queryString(conn, "SELECT enterprise_name FROM sys_user WHERE username='old_unknown'"));
      assertEquals(
          "未知公司",
          queryString(
              conn,
              "SELECT enterprise_name FROM sys_user_v7_enterprise_name_backup WHERE username='old_unknown'"));
      assertEquals(
          0L,
          queryLong(
              conn,
              "SELECT COUNT(*) FROM sys_user_v7_enterprise_name_backup WHERE username IN ('old_demo','old_empty')"));
      assertEquals(
          "system",
          queryString(conn, "SELECT update_by FROM sys_user WHERE username='old_unknown'"));
      assertEquals(
          "system", queryString(conn, "SELECT update_by FROM sys_user WHERE username='old_demo'"));
    }
  }

  private static void runClasspathSql(Connection conn, String classpath) throws Exception {
    byte[] bytes =
        Objects.requireNonNull(
                EnterpriseMigrationSmokeTest.class.getClassLoader().getResourceAsStream(classpath))
            .readAllBytes();
    String raw = new String(bytes, StandardCharsets.UTF_8);
    StringBuilder stmt = new StringBuilder();
    try (Statement st = conn.createStatement()) {
      for (String line : raw.split("\n")) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("--")) {
          continue;
        }
        stmt.append(line).append('\n');
        if (trimmed.endsWith(";")) {
          st.execute(stmt.toString());
          stmt.setLength(0);
        }
      }
    }
  }

  private static long queryLong(Connection conn, String sql) throws Exception {
    try (Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      assertTrue(rs.next());
      return rs.getLong(1);
    }
  }

  private static String queryString(Connection conn, String sql) throws Exception {
    try (Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      assertTrue(rs.next());
      return rs.getString(1);
    }
  }
}
