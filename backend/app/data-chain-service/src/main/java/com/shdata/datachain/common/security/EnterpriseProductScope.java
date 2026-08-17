package com.shdata.datachain.common.security;

import com.shdata.datachain.entity.SysUserEntity;
import com.shdata.datachain.model.Role;
import com.shdata.datachain.model.SessionPrincipal;
import com.shdata.datachain.repository.CatalogBrowseSeedStore;
import com.shdata.datachain.repository.SysUserRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * 本企业 / 本人产品 scope 判定（REQ-CAT-016 / REQ-RBAC-002 / PLAN-WSC-8.3 §3.2）。
 *
 * <p>产品经 {@code create_by} 关联 {@code sys_user}，取该用户 {@code enterprise_id} 与 {@link
 * SessionPrincipal#enterpriseId()} 比较；同企业即本企业（不要求 create_by=当前用户）。
 * 授权<strong>仅</strong>信会话主体，忽略客户端 enterpriseId。
 */
@Component
public class EnterpriseProductScope {

  private final SysUserRepository userRepo;
  private final CatalogBrowseSeedStore catalog;

  /**
   * @param userRepo 用户仓储（按 create_by 解析 enterprise_id）
   * @param catalog 产品归属（create_by）读取
   */
  public EnterpriseProductScope(SysUserRepository userRepo, CatalogBrowseSeedStore catalog) {
    this.userRepo = userRepo;
    this.catalog = catalog;
  }

  /**
   * 我的数据产品可见：create_by 对应用户与会话同 enterpriseId。
   *
   * <p>空/缺失 create_by、找不到用户、enterprise_id 无效 → 不可见。
   *
   * @param createBy 产品 create_by（通常为用户主键字符串）
   * @param sessionEnterpriseId 会话企业 id
   * @return true 当且仅当同企业
   */
  public boolean matchesMineEnterprise(String createBy, String sessionEnterpriseId) {
    if (sessionEnterpriseId == null || sessionEnterpriseId.isBlank()) {
      return false;
    }
    Optional<SysUserEntity> owner = findOwnerUser(createBy);
    if (owner.isEmpty()) {
      return false;
    }
    Long enterpriseId = owner.get().getEnterpriseId();
    if (enterpriseId == null || enterpriseId <= 0L) {
      return false;
    }
    return sessionEnterpriseId.equals(String.valueOf(enterpriseId));
  }

  /**
   * 产品是否属于会话企业（create_by → sys_user.enterprise_id）。
   *
   * @param productId 产品 id 或 product_code
   * @param sessionEnterpriseId 会话企业 id
   * @return true 当且仅当同企业
   */
  public boolean isProductInEnterprise(String productId, String sessionEnterpriseId) {
    String createBy = catalog.findProductOwner(productId).orElse("");
    return matchesMineEnterprise(createBy, sessionEnterpriseId);
  }

  /**
   * 产品写权限：ADMIN 本企业；PROVIDER 本人 create_by；空/缺失 create_by → false（调用方 403）。
   *
   * @param productId 产品 id 或 product_code
   * @param principal 会话主体（仅信此对象，不读客户端企业参数）
   * @return 是否允许写
   */
  public boolean canWriteProduct(String productId, SessionPrincipal principal) {
    if (principal == null || principal.role() == null) {
      return false;
    }
    Optional<String> ownerOpt = catalog.findProductOwner(productId);
    if (ownerOpt.isEmpty()) {
      return false;
    }
    String createBy = ownerOpt.get();
    if (createBy == null || createBy.isBlank()) {
      return false;
    }
    if (principal.role() == Role.PROVIDER) {
      return principal.userId() != null && principal.userId().equals(createBy);
    }
    if (principal.role() == Role.ADMIN) {
      return matchesMineEnterprise(createBy, principal.enterpriseId());
    }
    return false;
  }

  /**
   * 按 create_by 解析未删除用户：优先主键，其次用户名（兼容历史写入）。
   *
   * @param createBy 产品 create_by
   * @return 用户；空/找不到则 empty
   */
  Optional<SysUserEntity> findOwnerUser(String createBy) {
    if (createBy == null || createBy.isBlank()) {
      return Optional.empty();
    }
    String token = createBy.trim();
    try {
      long id = Long.parseLong(token);
      Optional<SysUserEntity> byId = userRepo.findById(id);
      if (byId.isPresent() && !Boolean.TRUE.equals(byId.get().getDelFlag())) {
        return byId;
      }
    } catch (NumberFormatException ignored) {
      // 非数字则按用户名解析
    }
    return userRepo.findByUsernameAndDelFlag(token.toLowerCase(), false);
  }
}
