package com.shdata.datachain.overview;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
          + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
    })
@AutoConfigureMockMvc
class OverviewApiIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private OverviewDataStore dataStore;

  @BeforeEach
  void setUp() {
    dataStore.resetToSeed();
  }

  @AfterEach
  void tearDown() {
    dataStore.resetToSeed();
  }

  @Test
  void metrics_withSeed_returnsCoreCards() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/overview/metrics").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.assetTotal").value(128))
        .andExpect(jsonPath("$.data.activeEnterprises").value(36))
        .andExpect(jsonPath("$.data.todayAttestations").value(7))
        .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
  }

  @Test
  void metricsAndStream_emptyStore_returnFriendlyZeros() throws Exception {
    dataStore.clear();
    MockHttpSession session = login("admin", "demo");

    mockMvc
        .perform(get("/api/v1/overview/metrics").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.assetTotal").value(0))
        .andExpect(jsonPath("$.data.activeEnterprises").value(0))
        .andExpect(jsonPath("$.data.todayAttestations").value(0))
        .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());

    mockMvc
        .perform(get("/api/v1/overview/stream").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items").isArray())
        .andExpect(jsonPath("$.data.items").isEmpty());

    mockMvc
        .perform(get("/api/v1/overview/trend?days=7").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.points").isArray())
        .andExpect(jsonPath("$.data.points.length()").value(7))
        .andExpect(jsonPath("$.data.points[0].count").value(0));
  }

  @Test
  void stream_fixture_containsThreeTypes_descendingByOccurredAt() throws Exception {
    dataStore.replace(OverviewDataStore.fixtureThreeStreamTypes());
    MockHttpSession session = login("provider", "demo");

    mockMvc
        .perform(get("/api/v1/overview/stream?limit=10").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items.length()").value(3))
        .andExpect(jsonPath("$.data.items[0].type").value("TRADE_ORDER"))
        .andExpect(jsonPath("$.data.items[1].type").value("DATA_REGISTER"))
        .andExpect(jsonPath("$.data.items[2].type").value("CATALOG_REGISTER"))
        .andExpect(
            jsonPath("$.data.items[*].type")
                .value(
                    Matchers.containsInAnyOrder(
                        "CATALOG_REGISTER", "DATA_REGISTER", "TRADE_ORDER")))
        .andExpect(jsonPath("$.data.items[0].subject").isNotEmpty())
        .andExpect(jsonPath("$.data.items[0].actionSummary").isNotEmpty())
        .andExpect(jsonPath("$.data.items[0].relativeTime").isNotEmpty())
        .andExpect(jsonPath("$.data.items[0].occurredAt").value("2026-07-29T05:00:00Z"))
        .andExpect(jsonPath("$.data.items[1].occurredAt").value("2026-07-29T03:00:00Z"))
        .andExpect(jsonPath("$.data.items[2].occurredAt").value("2026-07-29T01:00:00Z"))
        .andExpect(jsonPath("$.data.items[0].chainRecordId").value("chain-trade"));
  }

  @Test
  void top_sortedDescending_andDistributionPresent() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/overview/top?limit=3").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(3))
        .andExpect(jsonPath("$.data.items[0].productName").value("企业信用数据集"))
        .andExpect(jsonPath("$.data.items[0].count").value(42))
        .andExpect(jsonPath("$.data.items[1].count").value(35));

    mockMvc
        .perform(get("/api/v1/overview/distribution").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.byIndustry").isArray())
        .andExpect(jsonPath("$.data.byRegion").isArray())
        .andExpect(jsonPath("$.data.byIndustry[0].name").isNotEmpty());
  }

  @Test
  void overview_requiresLogin() throws Exception {
    mockMvc
        .perform(get("/api/v1/overview/metrics"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("401"));
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
