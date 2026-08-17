package com.shdata.datachain.catalog.browse;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.shdata.datachain.controller.catalog.browse.CatalogBrowseController;
import com.shdata.datachain.controller.catalog.browse.L2DistributionController;
import com.shdata.datachain.model.CatalogProduct;
import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import com.shdata.datachain.repository.DataProductRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * L2 distribution 集成测（TASK-WSC-604 / REQ-CAT-009）：计数与种子一致、总数断言、空目录、预落地对账。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_l2_604;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class L2DistributionIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CatalogBrowseSeedStore catalog;
  @Autowired private DataProductRepository productRepo;
  @Autowired
  @Qualifier("requestMappingHandlerMapping")
  private RequestMappingHandlerMapping handlerMapping;

  @BeforeEach
  void clearProducts() {
    productRepo.deleteAll();
  }

  @Test
  void unauthenticated_l2Distribution_401() throws Exception {
    mockMvc
        .perform(get("/api/v1/catalog/l2-distribution"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("401"));
  }

  @Test
  void emptyCatalog_returnsZeroTotalAndEmptyItems() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/l2-distribution").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.totalProducts").value(0))
        .andExpect(jsonPath("$.data.items").isEmpty());
  }

  @Test
  void seededProducts_totalProductsReal_andItemsSortedDesc() throws Exception {
    // 3×emr + 2×imaging + 1×credit = 6；另加 1 无 L2 产品计入总数但不进 items
    seed("L2-EMR-0001", "cat-l2-emr", "cat-l1-health", "cat-l3-emr-desense");
    seed("L2-EMR-0002", "cat-l2-emr", "cat-l1-health", "cat-l3-emr-desense");
    seed("L2-EMR-0003", "cat-l2-emr", "cat-l1-health", "cat-l3-emr-struct");
    seed("L2-IMG-0001", "cat-l2-imaging", "cat-l1-health", "cat-l3-img-ct");
    seed("L2-IMG-0002", "cat-l2-imaging", "cat-l1-health", "cat-l3-img-ct");
    seed("L2-CRD-0001", "cat-l2-credit", "cat-l1-finance", "cat-l3-credit-score");
    seed("L2-NOL2-0001", null, "cat-l1-health", null);

    MockHttpSession session = login("user", "demo");
    MvcResult result =
        mockMvc
            .perform(get("/api/v1/catalog/l2-distribution").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andExpect(jsonPath("$.data.totalProducts").value(7))
            .andExpect(jsonPath("$.data.items.length()").value(greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.data.items[0].code").value("cat-l2-emr"))
            .andExpect(jsonPath("$.data.items[0].count").value(3))
            .andExpect(jsonPath("$.data.items[0].name").value("电子病历"))
            .andExpect(jsonPath("$.data.items[1].code").value("cat-l2-imaging"))
            .andExpect(jsonPath("$.data.items[1].count").value(2))
            .andExpect(jsonPath("$.data.items[2].code").value("cat-l2-credit"))
            .andExpect(jsonPath("$.data.items[2].count").value(1))
            .andReturn();

    List<Integer> counts =
        JsonPath.read(result.getResponse().getContentAsString(), "$.data.items[*].count");
    for (int i = 1; i < counts.size(); i++) {
      assertTrue(counts.get(i - 1) >= counts.get(i), "items must be count-desc");
    }
    int top5Sum = counts.stream().limit(5).mapToInt(Integer::intValue).sum();
    int total =
        JsonPath.read(result.getResponse().getContentAsString(), "$.data.totalProducts");
    assertEquals(7, total);
    assertTrue(total > top5Sum, "totalProducts must exceed Top items sum (includes no-L2 row)");
    assertEquals(7, productRepo.findAll().size());
  }

  @Test
  void prelandReconcile_604_l2EndpointOwnedByL2DistributionController_notBrowse() {
    boolean foundOnL2 = false;
    boolean foundOnBrowse = false;
    for (Map.Entry<RequestMappingInfo, HandlerMethod> e :
        handlerMapping.getHandlerMethods().entrySet()) {
      if (!matchesL2Distribution(e.getKey())) {
        continue;
      }
      Class<?> beanType = e.getValue().getBeanType();
      if (L2DistributionController.class.isAssignableFrom(beanType)) {
        foundOnL2 = true;
      }
      if (CatalogBrowseController.class.isAssignableFrom(beanType)) {
        foundOnBrowse = true;
      }
    }
    assertTrue(foundOnL2, "GET /catalog/l2-distribution must be on L2DistributionController");
    assertFalse(foundOnBrowse, "l2-distribution must not remain on CatalogBrowseController");
  }

  @Test
  void l1CategoryId_filtersToMatchingL1Only() throws Exception {
    seed("L2-EMR-0001", "cat-l2-emr", "cat-l1-health", "cat-l3-emr-desense");
    seed("L2-EMR-0002", "cat-l2-emr", "cat-l1-health", "cat-l3-emr-desense");
    seed("L2-IMG-0001", "cat-l2-imaging", "cat-l1-health", "cat-l3-img-ct");
    seed("L2-CRD-0001", "cat-l2-credit", "cat-l1-finance", "cat-l3-credit-score");

    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(
            get("/api/v1/catalog/l2-distribution")
                .param("l1CategoryId", "cat-l1-health")
                .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.totalProducts").value(3))
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.items[0].code").value("cat-l2-emr"))
        .andExpect(jsonPath("$.data.items[0].count").value(2))
        .andExpect(jsonPath("$.data.items[1].code").value("cat-l2-imaging"))
        .andExpect(jsonPath("$.data.items[1].count").value(1));
  }

  @Test
  void l1CategoryId_totalProductsMatchesL1Subset() throws Exception {
    seed("L2-EMR-0001", "cat-l2-emr", "cat-l1-health", "cat-l3-emr-desense");
    seed("L2-NOL2-0001", null, "cat-l1-health", null);
    seed("L2-CRD-0001", "cat-l2-credit", "cat-l1-finance", "cat-l3-credit-score");

    MockHttpSession session = login("user", "demo");
    MvcResult result =
        mockMvc
            .perform(
                get("/api/v1/catalog/l2-distribution")
                    .param("l1CategoryId", "cat-l1-health")
                    .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalProducts").value(2))
            .andReturn();

    int total =
        JsonPath.read(result.getResponse().getContentAsString(), "$.data.totalProducts");
    List<Integer> counts =
        JsonPath.read(result.getResponse().getContentAsString(), "$.data.items[*].count");
    int cardSum = counts.stream().mapToInt(Integer::intValue).sum();
    assertTrue(total >= cardSum, "totalProducts includes products without L2 in L1");
    assertEquals(2, total);
    assertEquals(1, cardSum);
  }

  @Test
  void crossEnterpriseParity_adminAndUserSameFullChainResult() throws Exception {
    seed("L2-EMR-0001", "cat-l2-emr", "cat-l1-health", "cat-l3-emr-desense");
    seed("L2-CRD-0001", "cat-l2-credit", "cat-l1-finance", "cat-l3-credit-score");

    MockHttpSession userSession = login("user", "demo");
    MockHttpSession adminSession = login("admin", "demo");

    MvcResult userAll =
        mockMvc
            .perform(get("/api/v1/catalog/l2-distribution").session(userSession))
            .andExpect(status().isOk())
            .andReturn();
    MvcResult adminAll =
        mockMvc
            .perform(get("/api/v1/catalog/l2-distribution").session(adminSession))
            .andExpect(status().isOk())
            .andReturn();

    assertDistributionDataEqual(userAll, adminAll, "full-chain distribution must not vary by role/enterprise session");

    MvcResult userL1 =
        mockMvc
            .perform(
                get("/api/v1/catalog/l2-distribution")
                    .param("l1CategoryId", "cat-l1-health")
                    .session(userSession))
            .andExpect(status().isOk())
            .andReturn();
    MvcResult adminL1 =
        mockMvc
            .perform(
                get("/api/v1/catalog/l2-distribution")
                    .param("l1CategoryId", "cat-l1-health")
                    .session(adminSession))
            .andExpect(status().isOk())
            .andReturn();

    assertDistributionDataEqual(
        userL1, adminL1, "L1-filtered distribution must not vary by role/enterprise session");
  }

  @Test
  void noEnterpriseScopeParam_responseUnchanged() throws Exception {
    seed("L2-EMR-0001", "cat-l2-emr", "cat-l1-health", "cat-l3-emr-desense");

    MockHttpSession session = login("user", "demo");
    MvcResult baseline =
        mockMvc
            .perform(get("/api/v1/catalog/l2-distribution").session(session))
            .andExpect(status().isOk())
            .andReturn();

    MvcResult withEnterpriseParam =
        mockMvc
            .perform(
                get("/api/v1/catalog/l2-distribution")
                    .param("enterpriseId", "999")
                    .param("enterpriseName", "篡改企业")
                    .session(session))
            .andExpect(status().isOk())
            .andReturn();

    assertDistributionDataEqual(
        baseline,
        withEnterpriseParam,
        "enterprise query params must not filter l2-distribution");
    assertFalse(
        withEnterpriseParam.getResponse().getContentAsString().contains("enterprise"),
        "response must not expose enterprise filter fields");
  }

  private static void assertDistributionDataEqual(MvcResult a, MvcResult b, String message)
      throws Exception {
    String bodyA = a.getResponse().getContentAsString();
    String bodyB = b.getResponse().getContentAsString();
    Object totalA = JsonPath.read(bodyA, "$.data.totalProducts");
    Object totalB = JsonPath.read(bodyB, "$.data.totalProducts");
    assertEquals(totalA, totalB, message + " (totalProducts)");
    Object itemsA = JsonPath.read(bodyA, "$.data.items");
    Object itemsB = JsonPath.read(bodyB, "$.data.items");
    assertEquals(itemsA, itemsB, message + " (items)");
  }

  private static boolean matchesL2Distribution(RequestMappingInfo info) {
    if (info.getPathPatternsCondition() != null) {
      return info.getPathPatternsCondition().getPatterns().stream()
          .anyMatch(p -> p.getPatternString().contains("/catalog/l2-distribution"));
    }
    if (info.getPatternsCondition() != null) {
      Set<String> patterns = info.getPatternsCondition().getPatterns();
      return patterns.stream().anyMatch(p -> p.contains("/catalog/l2-distribution"));
    }
    return false;
  }

  private void seed(String code, String l2, String l1, String l3) {
    catalog.upsertProduct(
        new CatalogProduct(
            catalog.nextProductId(),
            code,
            "seat-" + code,
            "DATASET",
            l2,
            l1,
            "path/" + code,
            0,
            null,
            "SELF_PRODUCED",
            "FILE",
            false,
            false,
            "summary",
            "scenario",
            "供应商",
            "91110000MA00000000",
            List.of(),
            Map.of(),
            "卫生和社会工作",
            null,
            "DAY",
            "按次",
            "面议",
            "数据使用权",
            l3,
            "卫生和社会工作",
            null));
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
