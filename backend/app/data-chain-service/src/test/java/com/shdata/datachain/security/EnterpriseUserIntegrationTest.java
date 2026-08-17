package com.shdata.datachain.security;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.shdata.datachain.common.security.PasswordHasher;
import com.shdata.datachain.entity.EnterpriseEntity;
import com.shdata.datachain.entity.SysUserEntity;
import com.shdata.datachain.repository.SysUserRepository;
import com.shdata.datachain.service.security.UserAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TASK-WSC-904：会话 enterpriseId、一企多 ADMIN、用户 CRUD 企业字段、OQ-V16-002 orphan 负例。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_ent_904;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class EnterpriseUserIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserAccountService users;
  @Autowired private SysUserRepository userRepo;
  @Autowired private PasswordHasher passwordHasher;

  @Test
  @DisplayName("904-session-enterprise-id")
  void session_enterprise_id_904() throws Exception {
    MvcResult login =
        mockMvc
            .perform(
                post("/api/v1/auth/session")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"admin\",\"password\":\"demo\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andExpect(jsonPath("$.data.enterpriseId").isNotEmpty())
            .andExpect(jsonPath("$.data.enterpriseName").value(EnterpriseEntity.NAME_DEMO))
            .andReturn();
    String enterpriseId =
        JsonPath.read(login.getResponse().getContentAsString(), "$.data.enterpriseId");
    MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

    mockMvc
        .perform(get("/api/v1/auth/session").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.enterpriseId").value(enterpriseId))
        .andExpect(jsonPath("$.data.enterpriseName").value(EnterpriseEntity.NAME_DEMO));
  }

  @Test
  void seed_multipleAdminsSameEnterprise() throws Exception {
    EnterpriseEntity demo = users.findEnterpriseByCode(EnterpriseEntity.CODE_DEMO).orElseThrow();
    List<SysUserEntity> admins =
        userRepo.findByEnterpriseIdAndRoleAndDelFlag(demo.getId(), "ADMIN", false);
    assertTrue(admins.size() >= 2, "seed must support multiple ADMINs in one enterprise");

    MockHttpSession session = login("admin", "demo");
    mockMvc
        .perform(get("/api/v1/admin/users").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[?(@.username=='admin')].role").value(hasItem("ADMIN")))
        .andExpect(jsonPath("$.data.items[?(@.username=='admin2')].role").value(hasItem("ADMIN")))
        .andExpect(
            jsonPath("$.data.items[?(@.username=='admin')].enterpriseId")
                .value(hasItem(String.valueOf(demo.getId()))))
        .andExpect(
            jsonPath("$.data.items[?(@.username=='admin2')].enterpriseId")
                .value(hasItem(String.valueOf(demo.getId()))));
  }

  @Test
  void createUser_inheritsOperatorEnterprise_andCanSpecifyEnterpriseId() throws Exception {
    MockHttpSession session = login("admin", "demo");
    MvcResult self =
        mockMvc
            .perform(get("/api/v1/auth/session").session(session))
            .andExpect(status().isOk())
            .andReturn();
    String operatorEnterpriseId =
        JsonPath.read(self.getResponse().getContentAsString(), "$.data.enterpriseId");

    mockMvc
        .perform(
            post("/api/v1/admin/users")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"username\":\"tmp904inherit\",\"password\":\"secret904\",\"role\":\"USER\",\"displayName\":\"继承企业\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.enterpriseId").value(operatorEnterpriseId))
        .andExpect(jsonPath("$.data.enterpriseName").value(EnterpriseEntity.NAME_DEMO));

    EnterpriseEntity unassigned =
        users.findEnterpriseByCode(EnterpriseEntity.CODE_UNASSIGNED).orElseThrow();
    mockMvc
        .perform(
            post("/api/v1/admin/users")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"username\":\"tmp904spec\",\"password\":\"secret904\",\"role\":\"USER\","
                        + "\"enterpriseId\":\""
                        + unassigned.getId()
                        + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.enterpriseId").value(String.valueOf(unassigned.getId())))
        .andExpect(jsonPath("$.data.enterpriseName").value(EnterpriseEntity.NAME_UNASSIGNED));

    mockMvc
        .perform(get("/api/v1/admin/users").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[?(@.username=='tmp904spec')].enterpriseId").isNotEmpty())
        .andExpect(jsonPath("$.data.items", not(hasSize(0))));
  }

  @Test
  void orphanDefault_doesNotShareDemoEnterpriseId() {
    EnterpriseEntity demo = users.findEnterpriseByCode(EnterpriseEntity.CODE_DEMO).orElseThrow();
    EnterpriseEntity unassigned =
        users.findEnterpriseByCode(EnterpriseEntity.CODE_UNASSIGNED).orElseThrow();
    assertNotEquals(
        demo.getId(), unassigned.getId(), "UNASSIGNED must be a distinct enterprise from DEMO");

    SysUserEntity orphan = new SysUserEntity();
    orphan.setUsername("orphan904");
    orphan.setPasswordHash(passwordHasher.hash("demo"));
    orphan.setDisplayName("未归属用户");
    orphan.setRole("USER");
    orphan.setEnterpriseId(0L);
    orphan.setEnterpriseName("");
    orphan.setCreateBy("test");
    orphan.setUpdateBy("test");
    userRepo.save(orphan);

    users.backfillEnterpriseIds();

    SysUserEntity reloaded =
        userRepo.findByUsernameAndDelFlag("orphan904", false).orElseThrow();
    assertEquals(unassigned.getId(), reloaded.getEnterpriseId());
    assertEquals(EnterpriseEntity.NAME_UNASSIGNED, reloaded.getEnterpriseName());
    assertNotEquals(
        demo.getId(),
        reloaded.getEnterpriseId(),
        "orphan default must not grant DEMO enterprise product write scope");
  }

  private MockHttpSession login(String username, String password) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/session")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }
}
