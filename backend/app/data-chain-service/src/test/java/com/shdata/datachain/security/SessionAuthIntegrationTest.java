package com.shdata.datachain.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * PLAN-WSC-2.2 §4 三角色矩阵 + §3.3 登出拒绝 + 角色变更后旧凭证拒绝。
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
  void rbacMatrix_admin_canWriteCategoryProductMaintenanceImport() throws Exception {
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
                    "{\"productCode\":\"TST-ADM-0001\",\"productName\":\"矩阵管理员产品\",\"productType\":\"OTHER\",\"l2CategoryId\":\"cat-l2-emr\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/p-1")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l3-1\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    mockMvc
        .perform(post("/api/v1/catalog/products/import").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    mockMvc
        .perform(get("/api/v1/catalog/products/import/reports/rep-admin").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
  }

  @Test
  void rbacMatrix_provider_maintenanceForbidden_productAndImportOk() throws Exception {
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
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));
    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/p-1")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"productCode\":\"TST-PRV-0001\",\"productName\":\"矩阵提供方产品\",\"productType\":\"OTHER\",\"l2CategoryId\":\"cat-l2-emr\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    mockMvc
        .perform(post("/api/v1/catalog/products/import").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    mockMvc
        .perform(get("/api/v1/catalog/products/import/reports/rep-prv").session(session))
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
  void roleSwitch_oldSessionImportRejected() throws Exception {
    MockHttpSession oldSession = login("admin", "demo", null);

    MvcResult switchResult =
        mockMvc
            .perform(
                post("/api/v1/auth/session/role")
                    .session(oldSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"role\":\"USER\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.role").value("USER"))
            .andReturn();

    MockHttpSession newSession = (MockHttpSession) switchResult.getRequest().getSession(false);

    // 旧会话写 API 必须拒绝（ISSUE-SEC-R1-001 / §3.3）
    mockMvc
        .perform(post("/api/v1/catalog/products/import").session(oldSession))
        .andExpect(status().isUnauthorized());

    // 新会话以 USER 角色，导入亦拒绝
    mockMvc
        .perform(post("/api/v1/catalog/products/import").session(newSession))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
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
