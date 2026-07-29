package com.wsc.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * §4.1 三角色矩阵 + 登出拒绝 + 角色变更后旧凭证拒绝。
 */
@SpringBootTest(
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=none",
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
    })
@AutoConfigureMockMvc
class SessionAuthIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void rbacMatrix_admin_canWriteCategoryAndProduct() throws Exception {
    MockHttpSession session = login("admin", "demo", null);
    mockMvc
        .perform(
            post("/api/v1/catalog/categories")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
  }

  @Test
  void rbacMatrix_provider_categoryForbidden_productOk() throws Exception {
    MockHttpSession session = login("provider", "demo", null);
    mockMvc
        .perform(
            post("/api/v1/catalog/categories")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"))
        .andExpect(jsonPath("$.correlationId").isNotEmpty());
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));
  }

  @Test
  void rbacMatrix_user_allWritesForbidden() throws Exception {
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
  }

  @Test
  void logout_thenWriteApiRejected() throws Exception {
    MockHttpSession session = login("admin", "demo", null);
    mockMvc.perform(delete("/api/v1/auth/session").session(session)).andExpect(status().isOk());
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("401"));
  }

  @Test
  void roleSwitch_oldSessionRejected() throws Exception {
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

    // 旧会话写 API 必须拒绝
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(oldSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnauthorized());

    // 新会话以 USER 角色，产品写亦拒绝
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(newSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
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
