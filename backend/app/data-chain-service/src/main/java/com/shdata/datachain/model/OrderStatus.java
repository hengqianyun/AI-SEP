package com.shdata.datachain.model;

/**
 * 订单状态枚举，对齐 contracts/openapi/openapi.yaml OrderStatus。
 *
 * <p>状态不可回退；无拒绝/退回/超时态。
 *
 * @author developer-wsc-913
 */
public enum OrderStatus {

    /** 待确认订单。 */
    PENDING_CONFIRM("待确认订单"),
    /** 待上传合约。 */
    PENDING_UPLOAD("待上传合约"),
    /** 待确认合约。 */
    PENDING_CONTRACT_CONFIRM("待确认合约"),
    /** 合约已达成。 */
    CONTRACT_REACHED("合约已达成"),
    /** 已取消。 */
    CANCELLED("已取消");

    /** 页面文案标签。 */
    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    /**
     * 获取页面文案标签。
     *
     * @return 状态对应的中文标签
     */
    public String getLabel() {
        return label;
    }
}
