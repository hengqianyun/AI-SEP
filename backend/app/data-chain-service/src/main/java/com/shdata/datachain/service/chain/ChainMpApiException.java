package com.shdata.datachain.service.chain;

/** ChainMP 返回非成功 HTTP 状态时抛出的异常。 */
public class ChainMpApiException extends RuntimeException {

  private final int statusCode;
  private final String responseBody;

  /**
   * @param statusCode HTTP 状态码
   * @param responseBody 平台原始错误响应
   */
  public ChainMpApiException(int statusCode, String responseBody) {
    super("ChainMP API 调用失败: HTTP " + statusCode);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }

  /** @return HTTP 状态码 */
  public int statusCode() {
    return statusCode;
  }

  /** @return 平台原始错误响应 */
  public String responseBody() {
    return responseBody;
  }
}
