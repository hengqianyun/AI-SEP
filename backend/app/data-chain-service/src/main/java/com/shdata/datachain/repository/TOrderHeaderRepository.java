package com.shdata.datachain.repository;

import com.shdata.datachain.entity.TOrderHeaderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * H2 兼容的 JPQL 分页查询（不依赖 MySQL JSON_EXTRACT 函数）。
 *
 * <p>用于测试环境 fallback：当原生 SQL 因 H2 不支持 JSON_EXTRACT 失败时，
 * 退化为 JPQL JOIN 查询（仅支持状态筛选 + 无关键字搜索）。
 */
// H2-compatible fallback methods are defined at the end of the interface

/**
 * 订单头表 Repository。
 *
 * <p>分页查询使用原生 SQL（MySQL），通过 LEFT JOIN sys_user / sys_enterprise
 * 支持跨表关键字搜索（订单号、产品名/编码、需求方姓名/单位、提供方企业名称）。
 *
 * @author developer-wsc-913
 */
public interface TOrderHeaderRepository
        extends JpaRepository<TOrderHeaderEntity, Long>,
                QuerydslPredicateExecutor<TOrderHeaderEntity> {

    // ================================================================
    // 基础查询（非分页，保留供其他场景使用）
    // ================================================================

    /**
     * 按订单号查找（UUID v4 唯一）。
     *
     * @param orderNo 订单号
     * @return 订单头实体
     */
    Optional<TOrderHeaderEntity> findByOrderNo(String orderNo);

    /**
     * 按需求方用户 ID 查找订单（USER 角色列表范围）。
     *
     * @param demandUserId 需求方用户 ID
     * @return 订单列表
     */
    List<TOrderHeaderEntity> findByDemandUserIdOrderByIdDesc(String demandUserId);

    /**
     * 按提供方企业 ID 查找订单（PROVIDER 角色列表范围）。
     *
     * @param providerEnterpriseId 提供方企业 ID
     * @return 订单列表
     */
    List<TOrderHeaderEntity> findByProviderEnterpriseIdOrderByIdDesc(String providerEnterpriseId);

    /**
     * 列出全部订单（ADMIN 角色列表范围）。
     *
     * @return 订单列表
     */
    List<TOrderHeaderEntity> findAllByOrderByIdDesc();

    /**
     * 统计需求方订单数。
     *
     * @param demandUserId 需求方用户 ID
     * @return 订单数
     */
    long countByDemandUserId(String demandUserId);

    /**
     * 统计提供方企业订单数。
     *
     * @param providerEnterpriseId 提供方企业 ID
     * @return 订单数
     */
    long countByProviderEnterpriseId(String providerEnterpriseId);

    /**
     * 按需求方用户 ID 与状态查找订单（USER 角色 + 状态筛选）。
     *
     * @param demandUserId 需求方用户 ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<TOrderHeaderEntity> findByDemandUserIdAndStatusOrderByIdDesc(String demandUserId, String status);

    /**
     * 按提供方企业 ID 与状态查找订单（PROVIDER 角色 + 状态筛选）。
     *
     * @param providerEnterpriseId 提供方企业 ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<TOrderHeaderEntity> findByProviderEnterpriseIdAndStatusOrderByIdDesc(String providerEnterpriseId, String status);

    /**
     * 按状态列出全部订单（ADMIN 角色 + 状态筛选）。
     *
     * @param status 订单状态
     * @return 订单列表
     */
    List<TOrderHeaderEntity> findByStatusOrderByIdDesc(String status);

    // ================================================================
    // 分页查询（P1-1 修复：数据库层分页 + 跨表关键字搜索）
    // ================================================================

    /**
     * ADMIN 分页查询全部订单（支持状态筛选 + 跨表关键字搜索）。
     *
     * <p>关键字搜索范围：订单号、产品编码、产品快照中的产品名称、
     * 需求方姓名/企业名称、提供方企业名称。
     *
     * @param status 状态筛选（null 或空字符串表示不限状态）
     * @param keyword 模糊关键字（null 或空字符串表示不筛选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query(value = "SELECT h.* FROM t_order_header h "
            + "LEFT JOIN sys_user u ON CAST(u.id AS CHAR) = h.demand_user_id "
            + "LEFT JOIN sys_enterprise e ON CAST(e.id AS CHAR) = h.provider_enterprise_id "
            + "WHERE h.del_flag = 0 "
            + "AND (:status IS NULL OR :status = '' OR h.status = :status) "
            + "AND (:kw IS NULL OR :kw = '' "
            + "  OR LOWER(h.order_no) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(h.product_code) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(JSON_UNQUOTE(JSON_EXTRACT(h.product_snapshot, '$.productName'))) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.display_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.enterprise_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(e.name) LIKE CONCAT('%', LOWER(:kw), '%')) "
            + "ORDER BY h.id DESC",
            countQuery = "SELECT COUNT(*) FROM t_order_header h "
            + "LEFT JOIN sys_user u ON CAST(u.id AS CHAR) = h.demand_user_id "
            + "LEFT JOIN sys_enterprise e ON CAST(e.id AS CHAR) = h.provider_enterprise_id "
            + "WHERE h.del_flag = 0 "
            + "AND (:status IS NULL OR :status = '' OR h.status = :status) "
            + "AND (:kw IS NULL OR :kw = '' "
            + "  OR LOWER(h.order_no) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(h.product_code) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(JSON_UNQUOTE(JSON_EXTRACT(h.product_snapshot, '$.productName'))) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.display_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.enterprise_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(e.name) LIKE CONCAT('%', LOWER(:kw), '%'))",
            nativeQuery = true)
    Page<TOrderHeaderEntity> searchAdminOrders(
            @Param("status") String status,
            @Param("kw") String keyword,
            Pageable pageable);

    /**
     * USER 分页查询本人需求方订单（支持状态筛选 + 跨表关键字搜索）。
     *
     * @param demandUserId 需求方用户 ID
     * @param status 状态筛选（null 或空字符串表示不限状态）
     * @param keyword 模糊关键字（null 或空字符串表示不筛选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query(value = "SELECT h.* FROM t_order_header h "
            + "LEFT JOIN sys_user u ON CAST(u.id AS CHAR) = h.demand_user_id "
            + "LEFT JOIN sys_enterprise e ON CAST(e.id AS CHAR) = h.provider_enterprise_id "
            + "WHERE h.del_flag = 0 "
            + "AND h.demand_user_id = :demandUserId "
            + "AND (:status IS NULL OR :status = '' OR h.status = :status) "
            + "AND (:kw IS NULL OR :kw = '' "
            + "  OR LOWER(h.order_no) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(h.product_code) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(JSON_UNQUOTE(JSON_EXTRACT(h.product_snapshot, '$.productName'))) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.display_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.enterprise_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(e.name) LIKE CONCAT('%', LOWER(:kw), '%')) "
            + "ORDER BY h.id DESC",
            countQuery = "SELECT COUNT(*) FROM t_order_header h "
            + "LEFT JOIN sys_user u ON CAST(u.id AS CHAR) = h.demand_user_id "
            + "LEFT JOIN sys_enterprise e ON CAST(e.id AS CHAR) = h.provider_enterprise_id "
            + "WHERE h.del_flag = 0 "
            + "AND h.demand_user_id = :demandUserId "
            + "AND (:status IS NULL OR :status = '' OR h.status = :status) "
            + "AND (:kw IS NULL OR :kw = '' "
            + "  OR LOWER(h.order_no) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(h.product_code) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(JSON_UNQUOTE(JSON_EXTRACT(h.product_snapshot, '$.productName'))) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.display_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.enterprise_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(e.name) LIKE CONCAT('%', LOWER(:kw), '%'))",
            nativeQuery = true)
    Page<TOrderHeaderEntity> searchUserOrders(
            @Param("demandUserId") String demandUserId,
            @Param("status") String status,
            @Param("kw") String keyword,
            Pageable pageable);

    /**
     * PROVIDER 分页查询本企业提供方订单（支持状态筛选 + 跨表关键字搜索）。
     *
     * @param providerEnterpriseId 提供方企业 ID
     * @param status 状态筛选（null 或空字符串表示不限状态）
     * @param keyword 模糊关键字（null 或空字符串表示不筛选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query(value = "SELECT h.* FROM t_order_header h "
            + "LEFT JOIN sys_user u ON CAST(u.id AS CHAR) = h.demand_user_id "
            + "LEFT JOIN sys_enterprise e ON CAST(e.id AS CHAR) = h.provider_enterprise_id "
            + "WHERE h.del_flag = 0 "
            + "AND h.provider_enterprise_id = :providerEnterpriseId "
            + "AND (:status IS NULL OR :status = '' OR h.status = :status) "
            + "AND (:kw IS NULL OR :kw = '' "
            + "  OR LOWER(h.order_no) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(h.product_code) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(JSON_UNQUOTE(JSON_EXTRACT(h.product_snapshot, '$.productName'))) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.display_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.enterprise_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(e.name) LIKE CONCAT('%', LOWER(:kw), '%')) "
            + "ORDER BY h.id DESC",
            countQuery = "SELECT COUNT(*) FROM t_order_header h "
            + "LEFT JOIN sys_user u ON CAST(u.id AS CHAR) = h.demand_user_id "
            + "LEFT JOIN sys_enterprise e ON CAST(e.id AS CHAR) = h.provider_enterprise_id "
            + "WHERE h.del_flag = 0 "
            + "AND h.provider_enterprise_id = :providerEnterpriseId "
            + "AND (:status IS NULL OR :status = '' OR h.status = :status) "
            + "AND (:kw IS NULL OR :kw = '' "
            + "  OR LOWER(h.order_no) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(h.product_code) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(JSON_UNQUOTE(JSON_EXTRACT(h.product_snapshot, '$.productName'))) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.display_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(u.enterprise_name) LIKE CONCAT('%', LOWER(:kw), '%') "
            + "  OR LOWER(e.name) LIKE CONCAT('%', LOWER(:kw), '%'))",
            nativeQuery = true)
    Page<TOrderHeaderEntity> searchProviderOrders(
            @Param("providerEnterpriseId") String providerEnterpriseId,
            @Param("status") String status,
            @Param("kw") String keyword,
            Pageable pageable);

    // ================================================================
    // H2 兼容 fallback（JPQL，无 JSON_EXTRACT；仅用于测试环境）
    // ================================================================

    /**
     * ADMIN JPQL 分页查询（H2 兼容，仅支持状态筛选，不支持关键字搜索）。
     *
     * @param status 状态筛选（null 表示不限状态）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT h FROM TOrderHeaderEntity h WHERE h.delFlag = false "
            + "AND (:status IS NULL OR :status = '' OR h.status = :status) "
            + "ORDER BY h.id DESC")
    Page<TOrderHeaderEntity> searchAdminOrdersFallback(
            @Param("status") String status,
            Pageable pageable);

    /**
     * USER JPQL 分页查询（H2 兼容，仅支持状态筛选，不支持关键字搜索）。
     *
     * @param demandUserId 需求方用户 ID
     * @param status 状态筛选（null 表示不限状态）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT h FROM TOrderHeaderEntity h WHERE h.delFlag = false "
            + "AND h.demandUserId = :demandUserId "
            + "AND (:status IS NULL OR :status = '' OR h.status = :status) "
            + "ORDER BY h.id DESC")
    Page<TOrderHeaderEntity> searchUserOrdersFallback(
            @Param("demandUserId") String demandUserId,
            @Param("status") String status,
            Pageable pageable);

    /**
     * PROVIDER JPQL 分页查询（H2 兼容，仅支持状态筛选，不支持关键字搜索）。
     *
     * @param providerEnterpriseId 提供方企业 ID
     * @param status 状态筛选（null 表示不限状态）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT h FROM TOrderHeaderEntity h WHERE h.delFlag = false "
            + "AND h.providerEnterpriseId = :providerEnterpriseId "
            + "AND (:status IS NULL OR :status = '' OR h.status = :status) "
            + "ORDER BY h.id DESC")
    Page<TOrderHeaderEntity> searchProviderOrdersFallback(
            @Param("providerEnterpriseId") String providerEnterpriseId,
            @Param("status") String status,
            Pageable pageable);
}
