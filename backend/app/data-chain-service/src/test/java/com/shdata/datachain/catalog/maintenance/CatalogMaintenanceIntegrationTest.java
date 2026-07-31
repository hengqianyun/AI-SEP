package com.shdata.datachain.catalog.maintenance;

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

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_maint_106;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class CatalogMaintenanceIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void pending_to_maintained_singleAssociate() throws Exception {
    MockHttpSession admin = login("admin", "demo");

    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries?status=PENDING&page=1&pageSize=20").session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.data.items[0].maintenanceStatus").value("PENDING"));

    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/prod-pending-001")
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l3-txn-retail\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.maintenanceStatus").value("MAINTAINED"))
        .andExpect(jsonPath("$.data.l3CategoryId").value("cat-l3-txn-retail"))
        .andExpect(jsonPath("$.data.categoryPath").isNotEmpty());

    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries?status=PENDING").session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[?(@.id=='prod-pending-001')]").isEmpty());
  }

  @Test
  void batchAssociate_and_leafRequired() throws Exception {
    MockHttpSession admin = login("admin", "demo");

    mockMvc
        .perform(
            put("/api/v1/catalog/maintenance/entries/prod-pending-002")
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"l3CategoryId\":\"cat-l2-emr\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ERR_CATEGORY_LEAF_REQUIRED"));

    mockMvc
        .perform(
            post("/api/v1/catalog/maintenance/entries/batch")
                .session(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"productIds\":[\"prod-pending-002\",\"prod-pending-003\",\"missing-x\"],"
                        + "\"l3CategoryId\":\"cat-l3-emr-struct\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.successCount").value(2))
        .andExpect(jsonPath("$.data.failureCount").value(1));
  }

  @Test
  void pagination_pagePageSizeTotal() throws Exception {
    MockHttpSession admin = login("admin", "demo");

    mockMvc
        .perform(
            get("/api/v1/catalog/maintenance/entries?status=ALL&page=1&pageSize=5").session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.pageSize").value(5))
        .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThan(5)))
        .andExpect(jsonPath("$.data.items.length()").value(5));

    mockMvc
        .perform(
            get("/api/v1/catalog/maintenance/entries?status=ALL&page=2&pageSize=5").session(admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page").value(2))
        .andExpect(jsonPath("$.data.items.length()").value(5));
  }

  @Test
  void nonAdmin_forbidden() throws Exception {
    MockHttpSession provider = login("provider", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/maintenance/entries").session(provider))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));

    MockHttpSession user = login("user", "demo");
    mockMvc
        .perform(
            post("/api/v1/catalog/maintenance/entries/batch")
                .session(user)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productIds\":[\"prod-emr-001\"],\"l3CategoryId\":\"cat-l3-emr-desense\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_MAINTENANCE_FORBIDDEN"));
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
