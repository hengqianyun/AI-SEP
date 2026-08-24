package com.shdata.datachain.repository;

import com.shdata.datachain.entity.TOrderAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 订单附件 Repository。
 *
 * @author developer-wsc-916
 */
public interface TOrderAttachmentRepository extends JpaRepository<TOrderAttachmentEntity, Long> {

    /**
     * 按订单头 ID 查找所有附件。
     *
     * @param orderHeaderId 订单头 ID
     * @return 附件列表
     */
    List<TOrderAttachmentEntity> findByOrderHeaderId(Long orderHeaderId);

    /**
     * 按订单头 ID 与扫描结果查找附件。
     *
     * @param orderHeaderId 订单头 ID
     * @param scanResult 扫描结果码
     * @return 附件列表
     */
    List<TOrderAttachmentEntity> findByOrderHeaderIdAndScanResult(Long orderHeaderId, String scanResult);
}
