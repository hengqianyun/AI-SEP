package com.shdata.datachain.model;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * PLAN-WSC-2.2 §4 三角色矩阵（含 TASK-WSC-607：PROVIDER 允许目录维护）+ §3.3 登出拒绝 + 角色切换禁用。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_rbac_102;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
      "spring.flyway.enabled=false",
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
    })
@AutoConfigureMockMvc
class SessionAuthIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void rbacMatrix_admin_categoryMaintenanceOk_productImportForbidden() throws Exception {
    MockHttpSession session = login("admin", "demo", null);
    mockMvc
        .perform(
            post("/api/v1/catalog/categories")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"矩阵测试一级\",\"level\":\"L1\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"productCode\":\"TST-ADM-0001\",\"productName\":\"矩阵管理员产品\",\"productType\":\"OTHER\",\"industryCategory\":\"卫生和社会工作\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    mockMvc
        .perform(post("/api/v1/catalog/products/import").session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
  }

  /**
   * TASK-WSC-607：PROVIDER 允许目录维护（GET/PUT，仅 create_by 隔离由业务层保证）；分类树写仍拒绝。
   */
  @Test
  void rbacMatrix_provider_maintenanceAllowed_categoryForbidden_productAndImportOk()
      throws Exception {
    MockHttpSession session = login("provider", "demo", null);
    mockMvc
        .perform(
            post("/api/v1/catalog/categories")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"应被拒绝\",\"level\":\"L1\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"))
        .andExpect(jsonPath("$.correlationId").isNotEmpty());
    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    MvcResult createProduct =
        mockMvc
            .perform(
                post("/api/v1/catalog/products")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"productCode\":\"TST-PRV-0001\",\"productName\":\"矩阵提供方产品\",\"productType\":\"OTHER\",\"industryCategory\":\"卫生和社会工作\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andReturn();
    String ownProductId =
        JsonPath.read(createProduct.getResponse().getContentAsString(), "$.data.id");
    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/" + ownProductId)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l3-emr-desense\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    String reportId = importWithErrors(session);
    mockMvc
        .perform(get("/api/v1/catalog/products/import/reports/" + reportId).session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
  }

  @Test
  void rbacMatrix_user_allWritesAndMaintenanceImportForbidden() throws Exception {
    MockHttpSession session = login("user", "demo", null);
    mockMvc
        .perform(
            post("/api/v1/catalog/categories")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries").session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));
    mockMvc
        .perform(post("/api/v1/catalog/products/import").session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
    mockMvc
        .perform(get("/api/v1/catalog/products/import/reports/rep-user").session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
  }

  @Test
  void logout_thenImportWriteApiRejected() throws Exception {
    MockHttpSession session = login("admin", "demo", null);
    mockMvc.perform(delete("/api/v1/auth/session").session(session)).andExpect(status().isOk());
    mockMvc
        .perform(post("/api/v1/catalog/products/import").session(session))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("401"));
  }

  @Test
  void roleSwitch_disabledReturnsGone() throws Exception {
    MockHttpSession session = login("admin", "demo", null);
    mockMvc
        .perform(
            post("/api/v1/auth/session/role")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"USER\"}"))
        .andExpect(status().isGone())
        .andExpect(jsonPath("$.code").value("ERR_ROLE_SWITCH_DISABLED"));
  }

  @Test
  void loginFailed_returns401() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("401"));
  }

  /** 真实导入 multipart（行级失败）以探测写权限并拿到可下载 reportId。 */
  private String importWithErrors(MockHttpSession session) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                multipart("/api/v1/catalog/products/import")
                    .file(importFixture("all-fail.csv"))
                    .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andExpect(jsonPath("$.data.reportId").isNotEmpty())
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.data.reportId");
  }

  private static MockMultipartFile importFixture(String name) throws Exception {
    byte[] bytes = Files.readAllBytes(resolveImportFixtures().resolve(name));
    return new MockMultipartFile("file", name, "text/csv", bytes);
  }

  private static Path resolveImportFixtures() {
    Path cwd = Path.of("").toAbsolutePath();
    Path[] candidates =
        new Path[] {
          cwd.resolve("tests/fixtures/import"),
          cwd.resolve("../../tests/fixtures/import").normalize(),
          cwd.resolve("../../../tests/fixtures/import").normalize(),
          Path.of("C:/WorkSpace/AI-SEP/tests/fixtures/import")
        };
    for (Path p : candidates) {
      if (Files.isDirectory(p) && Files.exists(p.resolve("all-fail.csv"))) {
        return p;
      }
    }
    return cwd.resolve("tests/fixtures/import");
  }

  private MockHttpSession login(String username, String password, String role) throws Exception {
    String body =
        role == null
            ? "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"
            : "{\"username\":\""
                + username
                + "\",\"password\":\""
                + password
                + "\",\"role\":\""
                + role
                + "\"}";
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/session")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }
}
