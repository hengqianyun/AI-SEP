package com.shdata.datachain.catalog.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shdata.datachain.catalog.browse.CatalogBrowseSeedStore;
import com.shdata.datachain.chain.ChainAttestationPort;
import com.shdata.datachain.chain.InMemoryChainStore;
import java.util.Map;
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

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_editor_105;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
class ProductEditorIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CatalogBrowseSeedStore catalog;
  @Autowired private InMemoryChainStore chainStore;

  @MockBean private ChainAttestationPort attestationPort;

  @Test
  void create_other_withContentDescription_version1() throws Exception {
    stubAttest("sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "did:wsc:sim:test");

    MockHttpSession session = login("admin", "demo");

    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/catalog/products")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "productCode":"GEN-OTH-9001",
                          "productName":"其他类型产品",
                          "productType":"OTHER",
                          "industryCategory":"卫生和社会工作",
                          "l3CategoryId":"cat-l3-emr-desense",
                          "summary":"OQ-004-revised",
                          "updateFrequency":"NO_UPDATE",
                          "typeSpecific":{
                            "other":{
                              "contentDescription":"其他产品内容",
                              "timeRange":"2026/01/01 -",
                              "regionScope":"全国"
                            }
                          }
                        }
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.latestVersionNo").value(1))
            .andExpect(jsonPath("$.data.chainCount").value(1))
            .andExpect(jsonPath("$.data.l3CategoryId").value("cat-l3-emr-desense"))
            .andExpect(jsonPath("$.data.categoryPath").value("医疗卫生 / 电子病历 / 脱敏病历"))
            .andExpect(jsonPath("$.data.updateFrequency").value("NO_UPDATE"))
            .andExpect(jsonPath("$.data.typeSpecific.other.contentDescription").value("其他产品内容"))
            .andReturn();

    String productId =
        com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

    mockMvc
        .perform(get("/api/v1/chain/products/" + productId + "/versions").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(1))
        .andExpect(jsonPath("$.data.items[0].versionNo").value(1))
        .andExpect(jsonPath("$.data.items[0].metadataHash").isNotEmpty())
        .andExpect(jsonPath("$.data.items[0].ownerDID").isNotEmpty())
        .andExpect(jsonPath("$.data.items[0].timestamp").isNotEmpty())
        .andExpect(jsonPath("$.data.items[0].certificate.owner").isNotEmpty());

    String v1Id = chainStore.listByProductId(productId).get(0).versionId();
    mockMvc
        .perform(get("/api/v1/chain/versions/" + v1Id + "/snapshot").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.categoryPath").value("医疗卫生 / 电子病历 / 脱敏病历"))
        .andExpect(jsonPath("$.data.categoryPathParts.l1").value("医疗卫生"))
        .andExpect(jsonPath("$.data.categoryPathParts.l2").value("电子病历"))
        .andExpect(jsonPath("$.data.categoryPathParts.l3").value("脱敏病历"))
        .andExpect(jsonPath("$.data.typeSpecific.other.contentDescription").value("其他产品内容"));
  }

  @Test
  void create_api_endpointsConflict_keepsClientEndpoints() throws Exception {
    stubAttest("sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd", "did:wsc:sim:api");
    MockHttpSession session = login("admin", "demo");

    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/catalog/products")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "productCode":"GEN-API-9001",
                          "productName":"接口冲突产品",
                          "productType":"API",
                          "industryCategory":"卫生和社会工作",
                          "l3CategoryId":"cat-l3-emr-desense",
                          "typeSpecific":{
                            "api":{
                              "swaggerFileContent":"{\\"openapi\\":\\"3.0.3\\",\\"info\\":{\\"title\\":\\"t\\",\\"version\\":\\"1\\"},\\"paths\\":{\\"/from-swagger\\":{\\"get\\":{\\"summary\\":\\"FromSwagger\\",\\"responses\\":{\\"200\\":{\\"description\\":\\"ok\\"}}}}}}",
                              "endpoints":[
                                {
                                  "id":"client-ep",
                                  "method":"POST",
                                  "path":"/client-path",
                                  "summary":"ClientWins"
                                }
                              ]
                            }
                          }
                        }
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.typeSpecific.api.endpoints[0].method").value("POST"))
            .andExpect(jsonPath("$.data.typeSpecific.api.endpoints[0].path").value("/client-path"))
            .andExpect(jsonPath("$.data.typeSpecific.api.endpoints[0].summary").value("ClientWins"))
            .andExpect(jsonPath("$.data.typeSpecific.api.swaggerFileContent").exists())
            .andReturn();

    String productId =
        com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");
    // 回读：endpoints ≡ 客户端提交，≠ 对原文重解析的 /from-swagger
    assertThat(catalog.findProduct(productId).orElseThrow().typeSpecific()).isNotNull();
    @SuppressWarnings("unchecked")
    Map<String, Object> api =
        (Map<String, Object>) catalog.findProduct(productId).orElseThrow().typeSpecific().get("api");
    @SuppressWarnings("unchecked")
    java.util.List<Map<String, Object>> endpoints =
        (java.util.List<Map<String, Object>>) api.get("endpoints");
    assertThat(endpoints.get(0).get("path")).isEqualTo("/client-path");
    assertThat(endpoints.get(0).get("summary")).isEqualTo("ClientWins");
    assertThat(String.valueOf(api.get("swaggerFileContent"))).contains("/from-swagger");
  }

  @Test
  void create_dataset_report_roundTrip_andLegacyEndpointRead() throws Exception {
    stubAttest("sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee", "did:wsc:sim:ts");
    MockHttpSession session = login("admin", "demo");

    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"GEN-DS-9001",
                      "productName":"数据集扩展",
                      "productType":"DATASET",
                      "industryCategory":"卫生和社会工作",
                      "l3CategoryId":"cat-l3-emr-struct",
                      "typeSpecific":{
                        "dataset":{
                          "timeRange":"2025/01/01 - 2026/01/01",
                          "regionScope":"省级",
                          "dataScale":"1GB",
                          "dataForm":"表格",
                          "fieldDescription":"a\\tb\\tc",
                          "dataSample":"{}"
                        }
                      }
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.typeSpecific.dataset.dataScale").value("1GB"))
        .andExpect(jsonPath("$.data.typeSpecific.dataset.dataForm").value("表格"));

    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"GEN-RP-9001",
                      "productName":"报告扩展",
                      "productType":"REPORT",
                      "industryCategory":"卫生和社会工作",
                      "l3CategoryId":"cat-l3-credit-score",
                      "typeSpecific":{
                        "report":{
                          "contentDescription":"报告正文",
                          "timeRange":"2026/08/01 -",
                          "regionScope":"市级"
                        }
                      }
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.typeSpecific.report.contentDescription").value("报告正文"));

    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"GEN-API-9002",
                      "productName":"旧endpoint可读",
                      "productType":"API",
                      "industryCategory":"卫生和社会工作",
                      "l3CategoryId":"cat-l3-emr-desense",
                      "typeSpecific":{
                        "api":{
                          "endpoint":"GET /legacy"
                        }
                      }
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.typeSpecific.api.endpoint").value("GET /legacy"));
  }

  @Test
  void update_incrementsVersion_oldReadable_conflictAndFormat() throws Exception {
    stubAttest("sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "did:wsc:sim:edit");

    MockHttpSession session = login("provider", "demo");

    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/catalog/products")
                    .session(session)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "productCode":"MED-EDT-9002",
                          "productName":"可编辑产品",
                          "productType":"DATASET",
                          "industryCategory":"卫生和社会工作",
                          "l3CategoryId":"cat-l3-emr-desense",
                          "typeSpecific":{"dataset":{"recordCount":10}}
                        }
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.latestVersionNo").value(1))
            .andReturn();
    String productId =
        com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

    mockMvc
        .perform(
            put("/api/v1/catalog/products/" + productId)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"MED-EDT-9002",
                      "productName":"可编辑产品-v2",
                      "productType":"DATASET",
                      "industryCategory":"卫生和社会工作",
                      "l3CategoryId":"cat-l3-emr-struct",
                      "typeSpecific":{"dataset":{"recordCount":20}}
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.latestVersionNo").value(2))
        .andExpect(jsonPath("$.data.chainCount").value(2))
        .andExpect(jsonPath("$.data.l3CategoryId").value("cat-l3-emr-struct"))
        .andExpect(jsonPath("$.data.categoryPath").value("医疗卫生 / 电子病历 / 结构化病历"));

    mockMvc
        .perform(get("/api/v1/chain/products/" + productId + "/versions").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.items[0].versionNo").value(2))
        .andExpect(jsonPath("$.data.items[1].versionNo").value(1));

    String v1Id =
        chainStore.listByProductId(productId).stream()
            .filter(v -> v.versionNo() == 1)
            .findFirst()
            .orElseThrow()
            .versionId();
    mockMvc
        .perform(get("/api/v1/chain/versions/" + v1Id + "/snapshot").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.productCode").value("MED-EDT-9002"))
        .andExpect(jsonPath("$.data.categoryPathParts.l3").value("脱敏病历"));

    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"bad-code",
                      "productName":"坏编码",
                      "productType":"OTHER",
                      "industryCategory":"卫生和社会工作",
                      "l3CategoryId":"cat-l3-emr-desense"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ERR_PRODUCT_CODE_FORMAT"));

    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"MED-EDT-9002",
                      "productName":"冲突",
                      "productType":"OTHER",
                      "industryCategory":"卫生和社会工作",
                      "l3CategoryId":"cat-l3-emr-desense"
                    }
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ERR_PRODUCT_CODE_CONFLICT"));
  }

  @Test
  void attestationFailure_noHalfProduct_noOrphanChain() throws Exception {
    Mockito.when(attestationPort.attest(Mockito.any()))
        .thenThrow(new RuntimeException("simulated attestation failure"));

    MockHttpSession session = login("admin", "demo");
    int beforeProducts = catalog.products().size();

    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"GEN-FAIL-9003",
                      "productName":"应回滚",
                      "productType":"OTHER",
                      "industryCategory":"卫生和社会工作",
                      "l3CategoryId":"cat-l3-emr-desense"
                    }
                    """))
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.code").value("ERR_ATTESTATION"));

    assertThat(catalog.findProductByCode("GEN-FAIL-9003")).isEmpty();
    assertThat(catalog.products()).hasSize(beforeProducts);
    assertThat(chainStore.listByProductId("prod-should-not-exist")).isEmpty();
  }

  @Test
  void create_requiresIndustryCategory_andRejectsInvalidL3() throws Exception {
    stubAttest("sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc", "did:wsc:sim:leaf");
    MockHttpSession session = login("admin", "demo");

    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"GEN-LEAF-9004",
                      "productName":"缺行业分类",
                      "productType":"OTHER"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ERR_VALIDATION"));

    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"GEN-LEAF-9005",
                      "productName":"有门类无挂载",
                      "productType":"OTHER",
                      "industryCategory":"建筑业"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.industryCategory").value("建筑业"))
        .andExpect(jsonPath("$.data.l3CategoryId").doesNotExist());

    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"GEN-LEAF-9006",
                      "productName":"挂二级非法",
                      "productType":"OTHER",
                      "industryCategory":"建筑业",
                      "l3CategoryId":"cat-l2-emr"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ERR_CATEGORY_LEAF_REQUIRED"));
  }

  @Test
  void ordinaryUser_productWriteForbidden() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(
            post("/api/v1/catalog/products")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "productCode":"GEN-USR-9006",
                      "productName":"普通用户不可写",
                      "productType":"OTHER",
                      "industryCategory":"卫生和社会工作",
                      "l3CategoryId":"cat-l3-emr-desense"
                    }
                    """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
  }

  private void stubAttest(String hash, String did) {
    Mockito.when(attestationPort.attest(Mockito.any()))
        .thenAnswer(
            inv ->
                new ChainAttestationPort.AttestationResult(
                    hash,
                    did,
                    java.time.Instant.parse("2026-07-29T10:00:00Z"),
                    new ChainAttestationPort.AttestationResult.Certificate("模拟权属方-TEST")));
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
