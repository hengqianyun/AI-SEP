package com.shdata.datachain.model;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 目录浏览集成：三级分类、筛选、分页 page/pageSize/total、筛选后翻页仍满足筛选。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_browse_104;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
  void listCategories_containsL1L2L3() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/categories").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items.length()").value(Matchers.greaterThanOrEqualTo(11)))
        .andExpect(jsonPath("$.data.items[?(@.level=='L1')]").isNotEmpty())
        .andExpect(jsonPath("$.data.items[?(@.level=='L2')]").isNotEmpty())
        .andExpect(jsonPath("$.data.items[?(@.level=='L3')]").isNotEmpty())
        .andExpect(
            jsonPath("$.data.items[?(@.id=='cat-l3-emr-desense')].pathLabels").isNotEmpty());
  }

  @Test
  void listProducts_filterByL1AndProductType() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(session)
                .param("l1CategoryId", "cat-l1-health")
                .param("productType", "DATASET")
                .param("q", "MED-EMR-0001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.pageSize").value(20))
        .andExpect(jsonPath("$.data.items[0].productCode").value("MED-EMR-0001"))
        .andExpect(jsonPath("$.data.items[0].chainCount").value(3))
        .andExpect(jsonPath("$.data.items[0].l3CategoryId").value("cat-l3-emr-desense"));
  }

  @Test
  void listProducts_filterByL3() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(session)
                .param("l3CategoryId", "cat-l3-credit-score"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.items[0].productCode").value("FIN-CRD-0003"));
  }

  @Test
  void listProducts_filterByL2_includesChildL3Products() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(session)
                .param("l2CategoryId", "cat-l2-emr")
                .param("productType", "OTHER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.items[0].productCode").value("GEN-MISC-0005"));
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
        .perform(get("/api/v1/catalog/products").session(session).param("q", "med-emr-0001"))
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
  void getProduct_previewFieldsAndThreeLevelPath() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/products/prod-credit-001").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.id").value("prod-credit-001"))
        .andExpect(jsonPath("$.data.productName").value("企业征信评分接口"))
        .andExpect(jsonPath("$.data.categoryPath").value("金融服务 / 征信评估 / 征信评分"))
        .andExpect(jsonPath("$.data.l3CategoryId").value("cat-l3-credit-score"))
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

  @Test
  void listProducts_pagination_pagePageSizeTotal_andFilterPreservedOnPage2() throws Exception {
    MockHttpSession session = login("user", "demo");
    // 脱敏病历 L3 下：2 个核心 + 18 分页样例 = 20
    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(session)
                .param("l3CategoryId", "cat-l3-emr-desense")
                .param("page", "1")
                .param("pageSize", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.pageSize").value(5))
        .andExpect(jsonPath("$.data.total").value(20))
        .andExpect(jsonPath("$.data.items.length()").value(5))
        .andExpect(jsonPath("$.data.items[*].l3CategoryId", Matchers.everyItem(Matchers.is("cat-l3-emr-desense"))));

    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(session)
                .param("l3CategoryId", "cat-l3-emr-desense")
                .param("page", "2")
                .param("pageSize", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.page").value(2))
        .andExpect(jsonPath("$.data.pageSize").value(5))
        .andExpect(jsonPath("$.data.total").value(20))
        .andExpect(jsonPath("$.data.items.length()").value(5))
        .andExpect(
            jsonPath(
                "$.data.items[*].l3CategoryId", Matchers.everyItem(Matchers.is("cat-l3-emr-desense"))))
        .andExpect(
            jsonPath(
                "$.data.items[*].categoryPath",
                Matchers.everyItem(Matchers.containsString("脱敏病历"))));
  }

  @Test
  void listProducts_filterByIndustryCategory_gbtMenlei() throws Exception {
    MockHttpSession session = login("provider", "demo");
    String code = "TST-IND-" + String.format("%04d", System.currentTimeMillis() % 10000);
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"productCode\":\""
                        + code
                        + "\",\"productName\":\"行业门类筛选样例\",\"productType\":\"OTHER\","
                        + "\"industryCategory\":\"建筑业\","
                        + "\"typeSpecific\":{\"other\":{\"contentDescription\":\"606\"}}}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.industryCategory").value("建筑业"));

    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(session)
                .param("industryCategory", "建筑业")
                .param("q", code))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.items[0].productCode").value(code))
        .andExpect(jsonPath("$.data.items[0].industryCategory").value("建筑业"));

    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(session)
                .param("industryCategory", "金融业")
                .param("q", code))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(0))
        .andExpect(jsonPath("$.data.items").isEmpty());
  }

  @Test
  void listProducts_sortedByUpdatedAtDesc() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(session)
                .param("l3CategoryId", "cat-l3-emr-desense")
                .param("page", "1")
                .param("pageSize", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items[0].productCode").value("MED-PG-0018"))
        .andExpect(jsonPath("$.data.items[0].updatedAt").isNotEmpty())
        .andExpect(jsonPath("$.data.items[1].updatedAt").isNotEmpty());
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
