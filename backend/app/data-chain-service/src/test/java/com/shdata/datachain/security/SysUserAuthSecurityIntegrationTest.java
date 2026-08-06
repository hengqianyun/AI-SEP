package com.shdata.datachain.security;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * TASK-WSC-602 P0 对账自动化：真登录、切角色禁用、ADMIN CRUD、非 ADMIN 403、响应无 password/hash。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_sys_user_602;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class SysUserAuthSecurityIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void login_success_bindsAccountRole() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"demo\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.role").value("ADMIN"))
        .andExpect(jsonPath("$.data.displayName").value("演示管理员"))
        .andExpect(content().string(not(containsString("password"))))
        .andExpect(content().string(not(containsString("passwordHash"))));
  }

  @Test
  void login_wrongPassword_returns401() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("401"));
  }

  @Test
  void login_providerAndUser_bindOwnRoles() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"provider\",\"password\":\"demo\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.role").value("PROVIDER"));
    mockMvc
        .perform(
            post("/api/v1/auth/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"demo\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.role").value("USER"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"admin", "provider", "user"})
  void sessionRoleSwitch_disabledForAllRoles(String username) throws Exception {
    MockHttpSession session = login(username, "demo");
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
  void adminUsers_crudAndSoftDelete_noPasswordLeak() throws Exception {
    MockHttpSession session = login("admin", "demo");

    mockMvc
        .perform(get("/api/v1/admin/users").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items").isArray())
        .andExpect(content().string(not(containsString("password"))))
        .andExpect(content().string(not(containsString("passwordHash"))));

    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/admin/users")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"username\":\"tmp602\",\"password\":\"secret602\",\"role\":\"USER\",\"displayName\":\"临时\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andExpect(jsonPath("$.data.username").value("tmp602"))
            .andExpect(jsonPath("$.data.role").value("USER"))
            .andExpect(jsonPath("$.data.deleted").value(false))
            .andExpect(jsonPath("$.data.password").doesNotExist())
            .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
            .andExpect(content().string(not(containsString("secret602"))))
            .andReturn();

    String userId =
        com.jayway.jsonpath.JsonPath.read(
            created.getResponse().getContentAsString(), "$.data.userId");

    mockMvc
        .perform(
            put("/api/v1/admin/users/" + userId)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"PROVIDER\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.role").value("PROVIDER"))
        .andExpect(content().string(not(containsString("passwordHash"))));

    mockMvc
        .perform(
            put("/api/v1/admin/users/" + userId)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deleted\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.deleted").value(true));

    mockMvc
        .perform(
            post("/api/v1/auth/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"tmp602\",\"password\":\"secret602\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void adminUsers_usernameConflict_returns409() throws Exception {
    MockHttpSession session = login("admin", "demo");
    mockMvc
        .perform(
            post("/api/v1/admin/users")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"username\":\"admin\",\"password\":\"x\",\"role\":\"USER\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ERR_USER_USERNAME_CONFLICT"));
  }

  @Test
  void adminUsers_notFound_returns404() throws Exception {
    MockHttpSession session = login("admin", "demo");
    mockMvc
        .perform(
            put("/api/v1/admin/users/999999")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"ghost\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ERR_USER_NOT_FOUND"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"provider", "user"})
  void adminUsers_nonAdmin_forbidden(String username) throws Exception {
    MockHttpSession session = login(username, "demo");
    mockMvc
        .perform(get("/api/v1/admin/users").session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
    mockMvc
        .perform(
            post("/api/v1/admin/users")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"username\":\"x\",\"password\":\"y\",\"role\":\"USER\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
  }

  private MockHttpSession login(String username, String password) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/session")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"username\":\""
                            + username
                            + "\",\"password\":\""
                            + password
                            + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }
}
