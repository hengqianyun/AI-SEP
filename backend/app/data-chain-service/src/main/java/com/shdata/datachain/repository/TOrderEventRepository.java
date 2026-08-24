package com.shdata.datachain.repository;

import com.shdata.datachain.entity.TOrderEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 订单时间线事件 Repository。
 *
 * @author developer-wsc-913
 */
public interface TOrderEventRepository extends JpaRepository<TOrderEventEntity, Long> {

    /**
     * 按订单头 ID 查找所有时间线事件（按 ID 升序）。
     *
     * @param orderHeaderId 订单头 ID
     * @return 事件列表
     */
    List<TOrderEventEntity> findByOrderHeaderIdOrderByIdAsc(Long orderHeaderId);
}
