/**
 * Toast 辅助 — 封装 ant-design-vue message，统一订单模块成功/失败提示。
 * RULE-GLOBAL-FRONTEND §1：写接口成功须 toast；失败走既有错误提示，不出现成功 toast。
 * REQ-WSC-ORDER-FE-001：创建/确认订单/提交合约/确认合约/取消成功均 toast。
 */
import { message } from 'ant-design-vue'

/** 创建订单成功 */
export function showCreateSuccessToast(msg = '订单创建成功') {
  message.success(msg)
}

/** 确认订单成功 */
export function showConfirmOrderToast(msg = '订单已确认') {
  message.success(msg)
}

/** 提交合约附件成功 */
export function showSubmitContractToast(msg = '合约已提交') {
  message.success(msg)
}

/** 确认合约成功 */
export function showConfirmContractToast(msg = '合约已确认') {
  message.success(msg)
}

/** 取消订单成功 */
export function showCancelOrderToast(msg = '订单已取消') {
  message.success(msg)
}
