/**
 * 上链存证适配与只读 API（TASK-WSC-006 / DEC-WSC-002）。
 *
 * <p>{@link ChainAttestationPort} 由 {@link SimulatedChainAttestationPort} 实现并注册为 Spring Bean，
 * 供后续 TASK-WSC-005 注入。业务层禁止直连真实链 SDK。
 */
package com.wsc.chain;
