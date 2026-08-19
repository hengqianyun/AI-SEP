package com.shdata.datachain.config;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * ChainMP 接口配置。凭证只从服务端配置或环境变量读取，禁止在业务代码中硬编码。
 */
@Component
@Validated
@ConfigurationProperties(prefix = "chainmp")
public class ChainMpProperties {

  private boolean enabled;

  private boolean mockEnabled = true;

  @NotBlank private String baseUrl = "https://www.chainmp.com";

  private String appId = "";

  private String appSecret = "";

  @Positive private int connectTimeoutSeconds = 5;

  @Positive private int readTimeoutSeconds = 10;

  @Positive private int submitMaxAttempts = 3;

  @Positive private int queryMaxAttempts = 5;

  @Positive private long retryDelayMillis = 2000;

  /**
   * 开启真实上链时校验 AK/SK 是否完整配置。
   *
   * @return 未开启或凭证完整时返回 {@code true}
   */
  @AssertTrue(message = "chainmp.enabled=true 时必须配置 chainmp.app-id 和 chainmp.app-secret")
  public boolean isCredentialsValid() {
    return !enabled || (!appId.isBlank() && !appSecret.isBlank());
  }

  /** @return 是否启用 ChainMP 真实上链 */
  public boolean isEnabled() {
    return enabled;
  }

  /** @param enabled 是否启用 ChainMP 真实上链 */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /** @return 是否允许未启用真实链或重试耗尽时使用 mock 结果 */
  public boolean isMockEnabled() {
    return mockEnabled;
  }

  /** @param mockEnabled 是否允许使用 mock 上链结果 */
  public void setMockEnabled(boolean mockEnabled) {
    this.mockEnabled = mockEnabled;
  }

  /** @return ChainMP 服务域名 */
  public String getBaseUrl() {
    return baseUrl;
  }

  /** @param baseUrl ChainMP 服务域名 */
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = trimTrailingSlash(baseUrl);
  }

  /** @return ChainMP app_id（AK） */
  public String getAppId() {
    return appId;
  }

  /** @param appId ChainMP app_id（AK） */
  public void setAppId(String appId) {
    this.appId = appId == null ? "" : appId.trim();
  }

  /** @return ChainMP app_secret（SK） */
  public String getAppSecret() {
    return appSecret;
  }

  /** @param appSecret ChainMP app_secret（SK） */
  public void setAppSecret(String appSecret) {
    this.appSecret = appSecret == null ? "" : appSecret;
  }

  /** @return 建连超时秒数 */
  public int getConnectTimeoutSeconds() {
    return connectTimeoutSeconds;
  }

  /** @param connectTimeoutSeconds 建连超时秒数 */
  public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
    this.connectTimeoutSeconds = connectTimeoutSeconds;
  }

  /** @return 请求读取超时秒数 */
  public int getReadTimeoutSeconds() {
    return readTimeoutSeconds;
  }

  /** @param readTimeoutSeconds 请求读取超时秒数 */
  public void setReadTimeoutSeconds(int readTimeoutSeconds) {
    this.readTimeoutSeconds = readTimeoutSeconds;
  }

  /** @return 提交存证最大尝试次数 */
  public int getSubmitMaxAttempts() {
    return submitMaxAttempts;
  }

  /** @param submitMaxAttempts 提交存证最大尝试次数 */
  public void setSubmitMaxAttempts(int submitMaxAttempts) {
    this.submitMaxAttempts = submitMaxAttempts;
  }

  /** @return 查询确认最大尝试次数 */
  public int getQueryMaxAttempts() {
    return queryMaxAttempts;
  }

  /** @param queryMaxAttempts 查询确认最大尝试次数 */
  public void setQueryMaxAttempts(int queryMaxAttempts) {
    this.queryMaxAttempts = queryMaxAttempts;
  }

  /** @return 提交和查询的重试间隔毫秒数 */
  public long getRetryDelayMillis() {
    return retryDelayMillis;
  }

  /** @param retryDelayMillis 提交和查询的重试间隔毫秒数 */
  public void setRetryDelayMillis(long retryDelayMillis) {
    this.retryDelayMillis = retryDelayMillis;
  }

  /**
   * 去掉域名尾部斜杠，避免拼接 API 路径时出现双斜杠。
   *
   * @param value 原始域名
   * @return 规范化域名
   */
  private static String trimTrailingSlash(String value) {
    if (value == null) {
      return "";
    }
    String normalized = value.trim();
    while (normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }
}
