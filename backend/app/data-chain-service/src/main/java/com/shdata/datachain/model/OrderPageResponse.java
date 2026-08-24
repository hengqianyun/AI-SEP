package com.shdata.datachain.model;

import java.util.List;

/**
 * 订单分页响应，对齐 contracts/openapi/openapi.yaml OrderPage。
 *
 * @param total 总条数
 * @param list 订单列表
 * @param page 当前页码
 * @param pageSize 页大小
 * @author developer-wsc-913
 */
public record OrderPageResponse(
    long total,
    List<OrderListItemResponse> list,
    int page,
    int pageSize) {}
