package com.wsc.catalog.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wsc.catalog.browse.CatalogBrowseSeedStore;
import com.wsc.chain.ChainAttestationPort;
import com.wsc.chain.InMemoryChainStore;
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
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=none",
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
    })
@AutoConfigureMockMvc
class ProductEditorIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CatalogBrowseSeedStore catalog;
  @Autowired private InMemoryChainStore chainStore;

  @MockBean private ChainAttestationPort attestationPort;

  @Test
  void create_other_version1_fourFields_andRejectExtraTypeSpecific() throws Exception {
    Mockito.when(attestationPort.attest(Mockito.any()))
        .thenAnswer(
            inv -> {
              ChainAttestationPort.AttestationRequest req = inv.getArgument(0);
              return new ChainAttestationPort.AttestationResult(
                  "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                  "did:wsc:sim:test",
                  java.time.Instant.parse("2026-07-29T10:00:00Z"),
                  new ChainAttestationPort.AttestationResult.Certificate("模拟权属方-TEST"));
            });

    MockHttpSession session = login("admin", "demo");

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
                      "l2CategoryId":"cat-l2-emr",
                      "typeSpecific":{"dataset":{"recordCount":1}}
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("ERR_PRODUCT_CODE_FORMAT"));

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
                          "l2CategoryId":"cat-l2-emr",
                          "summary":"OQ-004"
                        }
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.latestVersionNo").value(1))
            .andExpect(jsonPath("$.data.chainCount").value(1))
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
  }

  @Test
  void update_incrementsVersion_oldReadable_conflictAndFormat() throws Exception {
    Mockito.when(attestationPort.attest(Mockito.any()))
        .thenAnswer(
            inv -> {
              ChainAttestationPort.AttestationRequest req = inv.getArgument(0);
              return new ChainAttestationPort.AttestationResult(
                  "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                  "did:wsc:sim:edit",
                  java.time.Instant.parse("2026-07-29T11:00:00Z"),
                  new ChainAttestationPort.AttestationResult.Certificate("模拟权属方-EDIT"));
            });

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
                          "l2CategoryId":"cat-l2-emr",
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
                      "l2CategoryId":"cat-l2-emr",
                      "typeSpecific":{"dataset":{"recordCount":20}}
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.latestVersionNo").value(2))
        .andExpect(jsonPath("$.data.chainCount").value(2));

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
        .andExpect(jsonPath("$.data.productCode").value("MED-EDT-9002"));

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
                      "l2CategoryId":"cat-l2-emr"
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
                      "l2CategoryId":"cat-l2-emr"
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
                      "l2CategoryId":"cat-l2-emr"
                    }
                    """))
        .andExpect(status().is5xxServerError())
        .andExpect(jsonPath("$.code").value("ERR_ATTESTATION"));

    assertThat(catalog.findProductByCode("GEN-FAIL-9003")).isEmpty();
    assertThat(catalog.products()).hasSize(beforeProducts);
    assertThat(chainStore.listByProductId("prod-should-not-exist")).isEmpty();
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
