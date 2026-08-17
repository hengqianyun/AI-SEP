package com.shdata.datachain.catalog.browse;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shdata.datachain.common.port.ChainAttestationPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * TASK-WSC-905 回归：supplierName 模糊搜、未分类殿后跨页（REQ-CAT-012 不回退）。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_filter_sort_905;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class ListProductsFilterSortIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private ChainAttestationPort attestationPort;

  @Test
  void supplierName_fuzzyContains_separateFromQ() throws Exception {
    stubAttest();
    MockHttpSession provider = login("provider", "demo");
    createProduct(provider, "SUP-FLT-9051", "供应方甲", "华康数据科技", null);
    createProduct(provider, "SUP-FLT-9052", "供应方乙", "江南数科", null);

    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(provider)
                .param("supplierName", "华康")
                .param("q", "供应方"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.items[0].productCode").value("SUP-FLT-9051"));

    mockMvc
        .perform(get("/api/v1/catalog/products").session(provider).param("q", "SUP-FLT-9052"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.items[0].productCode").value("SUP-FLT-9052"));
  }

  @Test
  void sort_uncategorizedLast_updatedAtDesc_crossPage() throws Exception {
    stubAttest();
    MockHttpSession provider = login("provider", "demo");
    createProduct(provider, "SRT-UA-9051", "FLT905-UA", null, null);
    Thread.sleep(15);
    createProduct(provider, "SRT-UB-9052", "FLT905-UB", null, null);
    Thread.sleep(15);
    createProduct(provider, "SRT-LA-9053", "FLT905-LA", null, "cat-l3-emr-desense");
    Thread.sleep(15);
    createProduct(provider, "SRT-LB-9054", "FLT905-LB", null, "cat-l3-emr-desense");
    Thread.sleep(15);
    createProduct(provider, "SRT-LC-9055", "FLT905-LC", null, "cat-l3-emr-desense");

    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(provider)
                .param("q", "FLT905")
                .param("page", "1")
                .param("pageSize", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(5))
        .andExpect(jsonPath("$.data.items[*].productCode", contains("SRT-LC-9055", "SRT-LB-9054")));

    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(provider)
                .param("q", "FLT905")
                .param("page", "2")
                .param("pageSize", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items[*].productCode", contains("SRT-LA-9053", "SRT-UB-9052")));
  }

  @Test
  void l3Counts_fullTotal_independentOfPageSize() throws Exception {
    stubAttest();
    MockHttpSession provider = login("provider", "demo");
    createProduct(provider, "CNT-L3A-9091", "计数A1", null, "cat-l3-emr-desense");
    createProduct(provider, "CNT-L3A-9092", "计数A2", null, "cat-l3-emr-desense");
    createProduct(provider, "CNT-L3A-9093", "计数A3", null, "cat-l3-emr-desense");
    createProduct(provider, "CNT-UNC-9094", "计数未分类1", null, null);
    createProduct(provider, "CNT-UNC-9095", "计数未分类2", null, null);

    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(provider)
                .param("q", "计数")
                .param("page", "1")
                .param("pageSize", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(5))
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.l3Counts['cat-l3-emr-desense']").value(3))
        .andExpect(jsonPath("$.data.l3Counts['__uncategorized__']").value(2));

    mockMvc
        .perform(
            get("/api/v1/catalog/products")
                .session(provider)
                .param("q", "计数")
                .param("page", "2")
                .param("pageSize", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.l3Counts['cat-l3-emr-desense']").value(3))
        .andExpect(jsonPath("$.data.l3Counts['__uncategorized__']").value(2));
  }

  private void createProduct(
      MockHttpSession session, String code, String name, String supplierName, String l3)
      throws Exception {
    StringBuilder json = new StringBuilder();
    json.append("{\"productCode\":\"")
        .append(code)
        .append("\",\"productName\":\"")
        .append(name)
        .append("\",\"productType\":\"OTHER\",\"industryCategory\":\"卫生和社会工作\"");
    if (supplierName != null) {
      json.append(",\"supplierName\":\"").append(supplierName).append("\"");
    }
    if (l3 != null) {
      json.append(",\"l3CategoryId\":\"").append(l3).append("\"");
    }
    json.append("}");
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.toString()))
        .andExpect(status().isOk());
  }

  private void stubAttest() {
    Mockito.when(attestationPort.attest(Mockito.any()))
        .thenAnswer(
            inv ->
                new ChainAttestationPort.AttestationResult(
                    "hash-905s",
                    "did:905s",
                    java.time.Instant.parse("2026-08-17T04:20:00Z"),
                    new ChainAttestationPort.AttestationResult.Certificate("模拟权属方-905s")));
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
