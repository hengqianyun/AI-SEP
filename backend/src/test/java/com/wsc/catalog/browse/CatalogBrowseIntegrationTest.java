package com.wsc.catalog.browse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
 * 目录浏览集成：登录门禁、筛选组合、无结果空列表、keyword 大小写不敏感。
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
class CatalogBrowseIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void unauthenticated_getCategories_401() throws Exception {
    mockMvc
        .perform(get("/api/v1/catalog/categories"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("401"))
        .andExpect(jsonPath("$.correlationId").isNotEmpty());
  }

  @Test
  void listCategories_containsL1AndL2() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/categories").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(6)))
        .andExpect(jsonPath("$.data.items[?(@.level=='L1')]").isNotEmpty())
        .andExpect(jsonPath("$.data.items[?(@.level=='L2')]").isNotEmpty());
  }

  @Test
  void listProducts_filterByL1AndProductType() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(session)
                .param("l1CategoryId", "cat-l1-health")
                .param("productType", "DATASET"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.items[0].productCode").value("MED-EMR-0001"))
        .andExpect(jsonPath("$.data.items[0].chainCount").value(3));
  }

  @Test
  void listProducts_filterNoMatch_returnsEmptyList() throws Exception {
    MockHttpSession session = login("provider", "demo");
    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(session)
                .param("q", "ZZZ-NO-SUCH-PRODUCT")
                .param("productType", "API"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.total").value(0))
        .andExpect(jsonPath("$.data.items").isEmpty());
  }

  @Test
  void listProducts_keywordCaseInsensitive_onNameAndCode() throws Exception {
    MockHttpSession session = login("admin", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/products").session(session).param("q", "med-emr"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.items[0].productCode").value("MED-EMR-0001"));

    mockMvc
        .perform(get("/api/v1/catalog/products").session(session).param("q", "胸部 ct"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.items[0].productType").value("REPORT"));
  }

  @Test
  void getProduct_previewFieldsAndChainCount() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/products/prod-credit-001").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.id").value("prod-credit-001"))
        .andExpect(jsonPath("$.data.productName").value("企业征信评分接口"))
        .andExpect(jsonPath("$.data.categoryPath").value("金融服务 / 征信评估"))
        .andExpect(jsonPath("$.data.chainCount").value(5))
        .andExpect(jsonPath("$.data.summary").isNotEmpty())
        .andExpect(jsonPath("$.data.supplierCreditCode").isNotEmpty());
  }

  @Test
  void getProduct_notFound() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/products/missing-id").session(session))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("404"));
  }

  @Test
  void listProducts_combinedFilters_dataSourceAndPublicData() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(session)
                .param("dataSource", "PUBLIC_COLLECT")
                .param("involvesPublicData", "true")
                .param("deliveryMethod", "SANDBOX"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.items[0].productCode").value("FIN-TXN-0004"));
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
            .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }
}
