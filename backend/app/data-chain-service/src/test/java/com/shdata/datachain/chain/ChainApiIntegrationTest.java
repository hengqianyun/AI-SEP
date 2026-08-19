package com.shdata.datachain.repository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shdata.datachain.common.port.ChainAttestationPort;
import com.shdata.datachain.common.port.SimulatedChainAttestationPort;
import com.shdata.datachain.entity.ChainCatalogSnapshotEntity;
import com.shdata.datachain.entity.ChainVersionEntity;
import com.shdata.datachain.repository.ChainCatalogSnapshotRepository;
import com.shdata.datachain.repository.ChainVersionRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_chain_105;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
      "spring.flyway.enabled=false",
      "chainmp.enabled=false",
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
    })
@AutoConfigureMockMvc
class ChainApiIntegrationTest {

  private static final String SEED_PRODUCT_CODE = "WSC-DEMO-001";

  @Autowired private MockMvc mockMvc;
  @Autowired private ChainAttestationPort chainAttestationPort;
  @Autowired private ChainVersionRepository versionRepo;
  @Autowired private ChainCatalogSnapshotRepository snapshotRepo;

  @BeforeEach
  void seed() {
    // 清除旧数据
    snapshotRepo.deleteAll();
    versionRepo.deleteAll();

    Instant t1 = Instant.parse("2026-07-20T08:00:00Z");
    Instant t2 = Instant.parse("2026-07-22T10:30:00Z");
    Instant t3 = Instant.parse("2026-07-25T14:15:00Z");

    seedVersion("cv-seed-001-v1", SEED_PRODUCT_CODE, 1,
        "sha256:1111111111111111111111111111111111111111111111111111111111111111",
        "did:wsc:sim:seed000000000001", "模拟权属方-WSC-DEMO-001", t1,
        snapshotJson("cv-seed-001-v1", 1, t1));
    seedVersion("cv-seed-001-v2", SEED_PRODUCT_CODE, 2,
        "sha256:2222222222222222222222222222222222222222222222222222222222222222",
        "did:wsc:sim:seed000000000001", "模拟权属方-WSC-DEMO-001", t2,
        snapshotJson("cv-seed-001-v2", 2, t2));
    seedVersion("cv-seed-001-v3", SEED_PRODUCT_CODE, 3,
        "sha256:3333333333333333333333333333333333333333333333333333333333333333",
        "did:wsc:sim:seed000000000001", "模拟权属方-WSC-DEMO-001", t3,
        snapshotJson("cv-seed-001-v3", 3, t3));
  }

  @Test
  void portIsExposedAsSpringBean() {
    org.junit.jupiter.api.Assertions.assertNotNull(chainAttestationPort);
    org.junit.jupiter.api.Assertions.assertTrue(
        chainAttestationPort instanceof SimulatedChainAttestationPort);
  }

  @Test
  void listVersions_requiresLogin() throws Exception {
    mockMvc
        .perform(get("/api/v1/chain/products/" + SEED_PRODUCT_CODE + "/versions"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("401"));
  }

  @Test
  void listVersions_seedProduct_latestFirst() throws Exception {
    MockHttpSession session = login("admin", "demo");
    mockMvc
        .perform(get("/api/v1/chain/products/" + SEED_PRODUCT_CODE + "/versions").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items.length()").value(3))
        .andExpect(jsonPath("$.data.items[0].versionNo").value(3))
        .andExpect(jsonPath("$.data.items[1].versionNo").value(2))
        .andExpect(jsonPath("$.data.items[2].versionNo").value(1));
  }

  @Test
  void listVersions_unknownProduct_returnsEmptyList() throws Exception {
    MockHttpSession session = login("user", "demo");
    mockMvc
        .perform(get("/api/v1/chain/products/prod-does-not-exist/versions").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.items.length()").value(0));
  }

  @Test
  void getSnapshot_byVersionId() throws Exception {
    MockHttpSession session = login("provider", "demo");
    mockMvc
        .perform(get("/api/v1/chain/versions/cv-seed-001-v2/snapshot").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.versionId").value("cv-seed-001-v2"))
        .andExpect(jsonPath("$.data.productCode").value("WSC-DEMO-001"));
  }

  @Test
  void getSnapshot_unknownVersion_returns404() throws Exception {
    MockHttpSession session = login("admin", "demo");
    mockMvc
        .perform(get("/api/v1/chain/versions/cv-missing/snapshot").session(session))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("404"));
  }

  private void seedVersion(String versionCode, String productCode, int versionNo,
      String hash, String did, String certOwner, Instant ts, String snapJson) {
    ChainVersionEntity v = ChainVersionEntity.builder()
        .versionCode(versionCode).productCode(productCode).versionNo(versionNo)
        .metadataHash(hash).ownerDid(did).certOwner(certOwner).ts(ts).build();
    versionRepo.save(v);
    ChainCatalogSnapshotEntity s = ChainCatalogSnapshotEntity.builder()
        .versionCode(versionCode).snapshotJson(snapJson).build();
    snapshotRepo.save(s);
  }

  private String snapshotJson(String versionId, int versionNo, Instant capturedAt) {
    return "{"
        + "\"versionId\":\"" + versionId + "\","
        + "\"productCode\":\"WSC-DEMO-001\","
        + "\"productName\":\"演示数据产品（上链）\","
        + "\"productType\":\"DATASET\","
        + "\"categoryPath\":\"金融服务 / 征信评估 / 征信评分\","
        + "\"categoryPathParts\":{\"l1\":\"金融服务\",\"l2\":\"征信评估\",\"l3\":\"征信评分\"},"
        + "\"basicInfo\":{\"dataSource\":\"内部系统\",\"updateFrequency\":\"日更\",\"deliveryMethod\":\"API\"},"
        + "\"supplierInfo\":{\"supplierName\":\"演示供应商\",\"supplierCreditCode\":\"91310000MA1KXXXX0X\"},"
        + "\"propertyRights\":{\"propertyRightsType\":\"数据使用权\"},"
        + "\"typeSpecific\":{\"datasetFormat\":\"CSV\",\"recordCount\":" + (1000 + versionNo) + "},"
        + "\"tags\":[\"演示\",\"上链\",\"v" + versionNo + "\"],"
        + "\"summary\":\"TASK-WSC-006 内存种子快照 v" + versionNo + "\","
        + "\"scenario\":\"接入端工作台试点验收\","
        + "\"versionNo\":" + versionNo + ","
        + "\"capturedAt\":\"" + capturedAt + "\","
        + "\"attestation\":{"
        + "\"metadataHash\":\"sha256:1111111111111111111111111111111111111111111111111111111111111111\","
        + "\"ownerDID\":\"did:wsc:sim:seed000000000001\","
        + "\"timestamp\":\"" + capturedAt + "\","
        + "\"certificate\":{\"owner\":\"模拟权属方-WSC-DEMO-001\"}"
        + "}}";
  }

  private MockHttpSession login(String username, String password) throws Exception {
    var result = mockMvc
        .perform(post("/api/v1/auth/session")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk())
        .andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }
}
