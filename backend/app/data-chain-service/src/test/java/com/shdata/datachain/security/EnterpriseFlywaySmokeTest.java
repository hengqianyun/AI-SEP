package com.shdata.datachain.security;

import com.shdata.datachain.entity.EnterpriseEntity;
import com.shdata.datachain.repository.EnterpriseRepository;
import com.shdata.datachain.repository.SysUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TASK-WSC-904 空库 Flyway smoke：V7 企业表 + sys_user.enterprise_id 列存在、种子 DEMO≠UNASSIGNED。
 * <p>本测试跑 H2 MySQL 模式，不能用生产同款 {@code ddl-auto=validate}：H2 会把 V1 TEXT
 * 映射成 VARCHAR，Hibernate 期望 CLOB，校验会在无关表（如 t_chain_catalog_snapshot）失败。
 * 生产 MySQL 仍为 {@code validate}。本 smoke 只证明 Flyway 已应用到 V7 且企业/用户种子可查询。
 */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:wsc_flyway_904;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=none",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
      "spring.flyway.enabled=true",
      "spring.flyway.locations=classpath:sql/migration",
      "spring.flyway.baseline-on-migrate=true",
      "spring.flyway.baseline-version=0",
      "spring.autoconfigure.exclude="
          + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
    })
class EnterpriseFlywaySmokeTest {

  @Autowired private EnterpriseRepository enterpriseRepo;
  @Autowired private SysUserRepository userRepo;
  @Autowired private JdbcTemplate jdbc;

  @Test
  void emptyDatabase_flywayCreatesEnterpriseAndUserFk() {
    assertTrue(enterpriseRepo.findByCodeAndDelFlag(EnterpriseEntity.CODE_DEMO, false).isPresent());
    assertTrue(
        enterpriseRepo.findByCodeAndDelFlag(EnterpriseEntity.CODE_UNASSIGNED, false).isPresent());
    Long demoId =
        enterpriseRepo.findByCodeAndDelFlag(EnterpriseEntity.CODE_DEMO, false).orElseThrow().getId();
    Long unassignedId =
        enterpriseRepo
            .findByCodeAndDelFlag(EnterpriseEntity.CODE_UNASSIGNED, false)
            .orElseThrow()
            .getId();
    assertNotEquals(demoId, unassignedId);

    Integer colCount =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='sys_user' AND column_name='enterprise_id'",
            Integer.class);
    assertEquals(1, colCount);

    assertTrue(
        userRepo.findByUsernameAndDelFlag("admin", false).isPresent(),
        "empty db seed should create demo users after Flyway");
    assertEquals(
        demoId, userRepo.findByUsernameAndDelFlag("admin", false).orElseThrow().getEnterpriseId());
  }
}
