package com.wsc.catalog.admin;

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
class CategoryAdminIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void delete_mountedL2_andL1_forbidden_withReason_emptyOk() throws Exception {
    MockHttpSession admin = login("admin", "demo");

    mockMvc
        .perform(delete("/api/v1/catalog/categories/cat-l2-emr").session(admin))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ERR_CATEGORY_HAS_PRODUCTS"))
        .andExpect(jsonPath("$.data.productCount").isNumber())
        .andExpect(jsonPath("$.data.reason").isNotEmpty());

    mockMvc
        .perform(delete("/api/v1/catalog/categories/cat-l1-health").session(admin))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ERR_CATEGORY_HAS_PRODUCTS"));

    MvcResult l1 =
        mockMvc
            .perform(
                post("/api/v1/catalog/categories")
                    .session(admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"空一级\",\"level\":\"L1\"}"))
            .andExpect(status().isOk())
            .andReturn();
    String l1Id =
        com.jayway.jsonpath.JsonPath.read(l1.getResponse().getContentAsString(), "$.data.id");

    MvcResult l2 =
        mockMvc
            .perform(
                post("/api/v1/catalog/categories")
                    .session(admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"name\":\"空二级\",\"level\":\"L2\",\"parentId\":\"" + l1Id + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
    String l2Id =
        com.jayway.jsonpath.JsonPath.read(l2.getResponse().getContentAsString(), "$.data.id");

    mockMvc
        .perform(delete("/api/v1/catalog/categories/" + l2Id).session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"));

    mockMvc
        .perform(delete("/api/v1/catalog/categories/" + l1Id).session(admin))
        .andExpect(status().isOk());
  }

  @Test
  void nonAdmin_cannotReachCategoryWrite() throws Exception {
    MockHttpSession provider = login("provider", "demo");
    mockMvc
        .perform(
            post("/api/v1/catalog/categories")
                .session(provider)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"x\",\"level\":\"L1\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));

    MockHttpSession user = login("user", "demo");
    mockMvc
        .perform(delete("/api/v1/catalog/categories/cat-l2-txn").session(user))
        .andExpect(status().isForbidden());
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
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }
}
