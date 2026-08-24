package com.shdata.datachain.entity;

import com.shdata.datachain.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 订单附件实体，对应 {@code t_order_attachment}。
 *
 * <p>白名单：Word (.doc/.docx)、PDF (.pdf)；单文件 20MB 限制。
 * 扫描结果由 {@code OrderAttachmentScanPort} 填充。
 *
 * @author developer-wsc-916
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_order_attachment")
@Where(clause = "del_flag = 0")
public class TOrderAttachmentEntity extends BaseEntity {

    /** 所属订单头 ID（业务列，非物理 FK）。 */
    @Column(name = "order_header_id", nullable = false)
    private Long orderHeaderId;

    /** 文件名。 */
    @Column(name = "file_name", nullable = false, length = 256)
    private String fileName;

    /** 文件类型（MIME 类型或后缀）。 */
    @Column(name = "file_type", nullable = false, length = 64)
    private String fileType;

    /** 文件大小（字节）。 */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /** 存储键（对象存储 key 或文件路径）。 */
    @Column(name = "storage_key", length = 512)
    private String storageKey;

    /** 扫描结果码（PASS / FAIL / ERROR / PENDING）。 */
    @Column(name = "scan_result", nullable = false, length = 32)
    private String scanResult;
}
