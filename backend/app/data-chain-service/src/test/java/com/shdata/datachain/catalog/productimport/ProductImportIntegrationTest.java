package com.shdata.datachain.catalog.productimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import com.shdata.datachain.model.CatalogProduct;
import com.shdata.datachain.service.catalog.productimport.ProductImportService;
import com.shdata.datachain.common.port.ChainAttestationPort;
import com.shdata.datachain.common.constant.ImportTemplateColumns;
import com.shdata.datachain.config.ImportMultipartConfig;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_cat_import_301;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
      "spring.flyway.enabled=false",
      "spring.servlet.multipart.max-file-size=12MB",
      "spring.servlet.multipart.max-request-size=12MB",
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
    })
@AutoConfigureMockMvc
class ProductImportIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CatalogBrowseSeedStore catalog;
  @Autowired private ProductImportService importService;

  @MockBean private ChainAttestationPort attestationPort;

  private static Path resolveFixtures() {
    Path cwd = Path.of("").toAbsolutePath();
    Path[] candidates =
        new Path[] {
          cwd.resolve("tests/fixtures/import"),
          cwd.resolve("../../tests/fixtures/import").normalize(),
          cwd.resolve("../../../tests/fixtures/import").normalize(),
          Path.of("C:/WorkSpace/AI-SEP/tests/fixtures/import")
        };
    for (Path p : candidates) {
      if (Files.isDirectory(p) && Files.exists(p.resolve("full-success.csv"))) {
        return p;
      }
    }
    return cwd.resolve("tests/fixtures/import");
  }

  @Test
  void template_matchesV0729Columns_andAuthoritativeXlsx() throws Exception {
    MockHttpSession session = login("provider", "demo");
    MvcResult csv =
        mockMvc
            .perform(get("/api/v1/catalog/products/import/template?format=csv").session(session))
            .andExpect(status().isOk())
            .andReturn();
    String body = new String(csv.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
    for (String col : ImportTemplateColumns.COLUMNS) {
      assertThat(body).contains(col);
    }
    assertThat(body).doesNotContain("产品编码");

    MvcResult xlsx =
        mockMvc
            .perform(get("/api/v1/catalog/products/import/template?format=xlsx").session(session))
            .andExpect(status().isOk())
            .andReturn();
    byte[] downloaded = xlsx.getResponse().getContentAsByteArray();
    assertThat(downloaded.length).isGreaterThan(100);

    Path asset = resolveFixtures().resolve("product-import-template-v0729.xlsx");
    if (Files.exists(asset)) {
      byte[] authoritative = Files.readAllBytes(asset);
      assertThat(downloaded).isEqualTo(authoritative);
    }
  }

  @Test
  void fullSuccess_autoCode_searchableAndChained() throws Exception {
    stubAttest();
    MockHttpSession session = login("provider", "demo");
    int cleanedBefore = importService.tempFilesCleanedCount();
    int beforeCount = catalog.products().size();

    MvcResult result =
        mockMvc
            .perform(
                multipart("/api/v1/catalog/products/import")
                    .file(fixture("full-success.csv"))
                    .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0"))
            .andExpect(jsonPath("$.data.successCount").value(2))
            .andExpect(jsonPath("$.data.failureCount").value(0))
            .andReturn();

    Object reportId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.reportId");
    assertThat(reportId).isNull();
    assertThat(importService.tempFilesCleanedCount()).isGreaterThan(cleanedBefore);
    assertThat(catalog.products().size()).isEqualTo(beforeCount + 2);

    CatalogProduct created =
        catalog.products().stream()
            .filter(p -> "导入成功数据集甲".equals(p.productName()))
            .findFirst()
            .orElseThrow();
    assertThat(created.productCode()).matches("^[A-Z0-9]+-[A-Z0-9]+-[0-9]{4,}$");
    assertThat(created.industryCategory()).isEqualTo("建筑业");
    assertThat(created.l3CategoryId()).isNull();
    assertThat(created.updateFrequency()).isEqualTo("NO_UPDATE");
    assertThat(created.typeSpecific()).isNotNull();
    assertThat(created.typeSpecific().get("dataset")).isInstanceOf(Map.class);

    mockMvc
        .perform(get("/api/v1/catalog/products").param("q", created.productCode()).session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1));

    mockMvc
        .perform(
            get("/api/v1/chain/products/" + created.productCode() + "/versions").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(1));
  }

  @Test
  void partialSuccess_andAllFail_rowLevel() throws Exception {
    stubAttest();
    MockHttpSession session = login("provider", "demo");

    MvcResult partial =
        mockMvc
            .perform(
                multipart("/api/v1/catalog/products/import")
                    .file(fixture("partial-success.csv"))
                    .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successCount").value(1))
            .andExpect(jsonPath("$.data.failureCount").value(1))
            .andExpect(jsonPath("$.data.reportId").isNotEmpty())
            .andReturn();

    String reportId =
        JsonPath.read(partial.getResponse().getContentAsString(), "$.data.reportId");
    MvcResult report =
        mockMvc
            .perform(get("/api/v1/catalog/products/import/reports/" + reportId).session(session))
            .andExpect(status().isOk())
            .andReturn();
    @SuppressWarnings("unchecked")
    Map<String, Object> row0 =
        JsonPath.read(report.getResponse().getContentAsString(), "$.data.rows[0]");
    assertThat(row0.keySet())
        .containsExactlyInAnyOrder("rowNumber", "productCode", "reasonCode", "reasonMessage");

    mockMvc
        .perform(
            multipart("/api/v1/catalog/products/import")
                .file(fixture("all-fail.csv"))
                .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.successCount").value(0))
        .andExpect(jsonPath("$.data.failureCount").value(2))
        .andExpect(jsonPath("$.data.reportId").isNotEmpty());
  }

  @Test
  void legacyTemplate_requestLevelReject_zeroWrites() throws Exception {
    stubAttest();
    MockHttpSession session = login("provider", "demo");
    int before = catalog.products().size();

    mockMvc
        .perform(
            multipart("/api/v1/catalog/products/import")
                .file(fixture("legacy-template.csv"))
                .session(session))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ERR_IMPORT_TEMPLATE_UNSUPPORTED"));

    assertThat(catalog.products().size()).isEqualTo(before);
  }

  @Test
  void formatInvalid_notMappedToTemplateUnsupported() throws Exception {
    MockHttpSession session = login("provider", "demo");
    byte[] bytes = Files.readAllBytes(resolveFixtures().resolve("format-invalid.bin"));
    MockMultipartFile file =
        new MockMultipartFile("file", "bad.bin", "application/octet-stream", bytes);

    mockMvc
        .perform(multipart("/api/v1/catalog/products/import").file(file).session(session))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ERR_IMPORT_FORMAT_INVALID"));
  }

  @Test
  void oversize_requestLevelReject() throws Exception {
    MockHttpSession session = login("provider", "demo");
    byte[] big = new byte[(int) ImportMultipartConfig.MAX_BYTES + 1];
    MockMultipartFile file = new MockMultipartFile("file", "oversize.csv", "text/csv", big);

    mockMvc
        .perform(multipart("/api/v1/catalog/products/import").file(file).session(session))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ERR_IMPORT_FILE_TOO_LARGE"));
  }

  @Test
  void openapiCell_badText_rowFail_sharedFixture_endpoints() throws Exception {
    stubAttest();
    MockHttpSession session = login("provider", "demo");

    MvcResult bad =
        mockMvc
            .perform(
                multipart("/api/v1/catalog/products/import")
                    .file(fixture("openapi-bad-text.csv"))
                    .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successCount").value(0))
            .andExpect(jsonPath("$.data.failureCount").value(1))
            .andReturn();
    String badReport =
        JsonPath.read(bad.getResponse().getContentAsString(), "$.data.reportId");
    mockMvc
        .perform(get("/api/v1/catalog/products/import/reports/" + badReport).session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.rows[0].reasonMessage").exists());

    mockMvc
        .perform(
            multipart("/api/v1/catalog/products/import")
                .file(fixture("openapi-shared-import.csv"))
                .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.successCount").value(1))
        .andExpect(jsonPath("$.data.failureCount").value(0));

    CatalogProduct apiProduct =
        catalog.products().stream()
            .filter(p -> "共享OpenAPI导入".equals(p.productName()))
            .findFirst()
            .orElseThrow();
    @SuppressWarnings("unchecked")
    Map<String, Object> api = (Map<String, Object>) apiProduct.typeSpecific().get("api");
    assertThat(api.get("swaggerFileContent")).asString().contains("openapi");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> endpoints = (List<Map<String, Object>>) api.get("endpoints");
    assertThat(endpoints).isNotEmpty();
    assertThat(endpoints.get(0).get("method")).isEqualTo("POST");
    assertThat(endpoints.get(0).get("path")).isEqualTo("/enterprise/security/verify");
    assertThat(endpoints.get(0).get("summary")).isEqualTo("企业安全信息核验");
  }

  @Test
  void categoryMismatch_andRequiredMissing_rowLevel() throws Exception {
    stubAttest();
    MockHttpSession session = login("provider", "demo");

    MvcResult cat =
        mockMvc
            .perform(
                multipart("/api/v1/catalog/products/import")
                    .file(fixture("category-mismatch.csv"))
                    .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.failureCount").value(2))
            .andReturn();
    String catReport = JsonPath.read(cat.getResponse().getContentAsString(), "$.data.reportId");
    mockMvc
        .perform(get("/api/v1/catalog/products/import/reports/" + catReport).session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.rows[0].reasonCode").value("ERR_IMPORT_ROW_INVALID"))
        .andExpect(
            jsonPath(
                "$.data.rows[0].reasonMessage",
                org.hamcrest.Matchers.containsString("行业分类非法")));

    MvcResult req =
        mockMvc
            .perform(
                multipart("/api/v1/catalog/products/import")
                    .file(fixture("required-field-missing.csv"))
                    .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.failureCount").value(2))
            .andReturn();
    String reqReport = JsonPath.read(req.getResponse().getContentAsString(), "$.data.reportId");
    String reqJson =
        mockMvc
            .perform(get("/api/v1/catalog/products/import/reports/" + reqReport).session(session))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(reqJson).contains("ERR_IMPORT_ROW_INVALID");
  }

  @Test
  void reportGet_forbiddenForUserAndUnauthenticated() throws Exception {
    stubAttest();
    // V1.4：导入仅 PROVIDER；ADMIN 仍可 GET 报告（matrix importReportGet）
    MockHttpSession provider = login("provider", "demo");
    MvcResult imported =
        mockMvc
            .perform(
                multipart("/api/v1/catalog/products/import")
                    .file(fixture("all-fail.csv"))
                    .session(provider))
            .andExpect(status().isOk())
            .andReturn();
    String reportId =
        JsonPath.read(imported.getResponse().getContentAsString(), "$.data.reportId");

    MockHttpSession user = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/products/import/reports/" + reportId).session(user))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));

    mockMvc
        .perform(get("/api/v1/catalog/products/import/reports/" + reportId))
        .andExpect(status().isUnauthorized());

    MockHttpSession admin = login("admin", "demo");
    mockMvc
        .perform(get("/api/v1/catalog/products/import/reports/rep-unknown").session(admin))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ERR_IMPORT_REPORT_NOT_FOUND"));
  }

  @Test
  void admin_importOwnEnterprise_200() throws Exception {
    stubAttest();
    MockHttpSession admin = login("admin", "demo");
    mockMvc
        .perform(
            multipart("/api/v1/catalog/products/import")
                .file(fixture("full-success.csv"))
                .session(admin)
                .param("enterpriseId", "999999"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.successCount").value(2));
  }

  @Test
  void ordinaryUser_importForbidden() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(
            multipart("/api/v1/catalog/products/import")
                .file(fixture("full-success.csv"))
                .session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ERR_FORBIDDEN"));
  }

  private MockMultipartFile fixture(String name) throws Exception {
    byte[] bytes = Files.readAllBytes(resolveFixtures().resolve(name));
    String contentType = name.endsWith(".csv") ? "text/csv" : "application/octet-stream";
    return new MockMultipartFile("file", name, contentType, bytes);
  }

  private void stubAttest() {
    Mockito.when(attestationPort.attest(Mockito.any()))
        .thenAnswer(
            inv ->
                new ChainAttestationPort.AttestationResult(
                    "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc",
                    "did:wsc:sim:import",
                    java.time.Instant.parse("2026-07-31T12:00:00Z"),
                    new ChainAttestationPort.AttestationResult.Certificate("模拟权属方-IMPORT")));
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
