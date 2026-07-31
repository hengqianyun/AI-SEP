package com.shdata.datachain.catalog.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_admin_103;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class CategoryAdminIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void crud_threeLevels() throws Exception {
    MockHttpSession admin = login("admin", "demo");

    MvcResult l1 =
        mockMvc
            .perform(
                post("/api/v1/catalog/categories")
                    .session(admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"测试空间\",\"level\":\"L1\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.level").value("L1"))
            .andReturn();
    String l1Id = JsonPath.read(l1.getResponse().getContentAsString(), "$.data.id");

    MvcResult l2 =
        mockMvc
            .perform(
                post("/api/v1/catalog/categories")
                    .session(admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"name\":\"测试行业\",\"level\":\"L2\",\"parentId\":\"" + l1Id + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.level").value("L2"))
            .andReturn();
    String l2Id = JsonPath.read(l2.getResponse().getContentAsString(), "$.data.id");

    MvcResult l3 =
        mockMvc
            .perform(
                post("/api/v1/catalog/categories")
                    .session(admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"name\":\"测试子类\",\"level\":\"L3\",\"parentId\":\"" + l2Id + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.level").value("L3"))
            .andExpect(jsonPath("$.data.parentId").value(l2Id))
            .andReturn();
    String l3Id = JsonPath.read(l3.getResponse().getContentAsString(), "$.data.id");

    mockMvc
        .perform(
            put("/api/v1/catalog/categories/" + l3Id)
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"测试子类-改\",\"level\":\"L3\",\"parentId\":\"" + l2Id + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("测试子类-改"));

    mockMvc
        .perform(delete("/api/v1/catalog/categories/" + l2Id).session(admin))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ERR_CATEGORY_HAS_PRODUCTS"));
  }

  @Test
  void delete_mountedL3_andL2_andL1_forbidden_emptyOk() throws Exception {
    MockHttpSession admin = login("admin", "demo");

    mockMvc
        .perform(delete("/api/v1/catalog/categories/cat-l3-emr-desense").session(admin))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ERR_CATEGORY_HAS_PRODUCTS"))
        .andExpect(jsonPath("$.data.productCount").isNumber())
        .andExpect(jsonPath("$.data.reason").isNotEmpty());

    mockMvc
        .perform(delete("/api/v1/catalog/categories/cat-l2-emr").session(admin))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ERR_CATEGORY_HAS_PRODUCTS"));

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
    String l1Id = JsonPath.read(l1.getResponse().getContentAsString(), "$.data.id");

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
    String l2Id = JsonPath.read(l2.getResponse().getContentAsString(), "$.data.id");

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
        .perform(delete("/api/v1/catalog/categories/cat-l3-txn-retail").session(user))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            post("/api/v1/catalog/categories")
                .session(user)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"子\",\"level\":\"L3\",\"parentId\":\"cat-l2-emr\"}"))
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
