package com.shdata.datachain.repository;

import com.shdata.datachain.entity.TOrderChainLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 订单上链日志 Repository。
 *
 * @author developer-wsc-913
 */
public interface TOrderChainLogRepository extends JpaRepository<TOrderChainLogEntity, Long> {

    /**
     * 按订单头 ID 查找所有上链日志（按 ID 升序）。
     *
     * @param orderHeaderId 订单头 ID
     * @return 上链日志列表
     */
    List<TOrderChainLogEntity> findByOrderHeaderIdOrderByIdAsc(Long orderHeaderId);
}
