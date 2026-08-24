package com.shdata.datachain.repository;

import com.shdata.datachain.entity.TOrderLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 订单明细 Repository。
 *
 * @author developer-wsc-913
 */
public interface TOrderLineRepository extends JpaRepository<TOrderLineEntity, Long> {

    /**
     * 按订单头 ID 查找所有明细行。
     *
     * @param orderHeaderId 订单头 ID
     * @return 明细行列表
     */
    List<TOrderLineEntity> findByOrderHeaderId(Long orderHeaderId);
}
