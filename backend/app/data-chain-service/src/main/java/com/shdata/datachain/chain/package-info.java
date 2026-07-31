/**
 * 上链存证适配与只读 API（TASK-WSC-105 / DEC-WSC-002 / REQ-CHAIN-001）。
 *
 * <p>{@link ChainAttestationPort} 由 {@link SimulatedChainAttestationPort} 实现并注册为 Spring Bean。
 * 目录快照含三级 {@code categoryPath} / {@code categoryPathParts}。业务层禁止直连真实链 SDK。
 */
package com.shdata.datachain.chain;
