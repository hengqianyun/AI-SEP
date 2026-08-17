package com.shdata.datachain.rbac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shdata.datachain.common.security.RbacMatrix;
import com.shdata.datachain.common.security.WriteAuthorizationInterceptor;
import com.shdata.datachain.model.Role;
import org.junit.jupiter.api.Test;

class RbacMatrixTest {

  @Test
  void categoryWrite_onlyAdmin() {
    assertTrue(RbacMatrix.canWriteCategory(Role.ADMIN));
    assertFalse(RbacMatrix.canWriteCategory(Role.PROVIDER));
    assertFalse(RbacMatrix.canWriteCategory(Role.USER));
  }

  @Test
  void catalogMaintenance_defaultAllowsAdminAndProvider() {
    assertTrue(RbacMatrix.canMaintainCatalog(Role.ADMIN));
    assertTrue(RbacMatrix.canMaintainCatalog(Role.PROVIDER));
    assertFalse(RbacMatrix.canMaintainCatalog(Role.USER));
  }

  @Test
  void catalogMaintenance_scopeAware_matchesSection31() {
    assertTrue(RbacMatrix.canMaintainCatalog(Role.ADMIN, RbacMatrix.MAINTENANCE_SCOPE_FULL));
    assertTrue(RbacMatrix.canMaintainCatalog(Role.ADMIN, RbacMatrix.MAINTENANCE_SCOPE_MY_CATALOG));
    assertTrue(RbacMatrix.canMaintainCatalog(Role.ADMIN, null));

    assertFalse(RbacMatrix.canMaintainCatalog(Role.PROVIDER, RbacMatrix.MAINTENANCE_SCOPE_FULL));
    assertTrue(RbacMatrix.canMaintainCatalog(Role.PROVIDER, RbacMatrix.MAINTENANCE_SCOPE_MY_CATALOG));
    assertTrue(RbacMatrix.canMaintainCatalog(Role.PROVIDER, null));

    assertFalse(RbacMatrix.canMaintainCatalog(Role.USER, RbacMatrix.MAINTENANCE_SCOPE_FULL));
    assertFalse(RbacMatrix.canMaintainCatalog(Role.USER, RbacMatrix.MAINTENANCE_SCOPE_MY_CATALOG));
    assertFalse(RbacMatrix.canMaintainCatalog(Role.USER, "other"));
    assertFalse(RbacMatrix.canMaintainCatalog(Role.PROVIDER, "other"));
  }

  @Test
  void productWrite_adminAndProvider_userForbidden() {
    assertTrue(RbacMatrix.canWriteProduct(Role.ADMIN));
    assertTrue(RbacMatrix.canWriteProduct(Role.PROVIDER));
    assertFalse(RbacMatrix.canWriteProduct(Role.USER));
  }

  @Test
  void productImport_adminAndProvider_userForbidden() {
    assertTrue(RbacMatrix.canImportProduct(Role.ADMIN));
    assertTrue(RbacMatrix.canImportProduct(Role.PROVIDER));
    assertFalse(RbacMatrix.canImportProduct(Role.USER));
  }

  @Test
  void mineApi_adminAndProvider_userForbidden() {
    assertTrue(RbacMatrix.canAccessMineApi(Role.ADMIN));
    assertTrue(RbacMatrix.canAccessMineApi(Role.PROVIDER));
    assertFalse(RbacMatrix.canAccessMineApi(Role.USER));
  }

  @Test
  void userManage_onlyAdmin() {
    assertTrue(RbacMatrix.canManageUsers(Role.ADMIN));
    assertFalse(RbacMatrix.canManageUsers(Role.PROVIDER));
    assertFalse(RbacMatrix.canManageUsers(Role.USER));
  }

  @Test
  void importReportGet_adminAndProvider() {
    assertTrue(RbacMatrix.canGetImportReport(Role.ADMIN));
    assertTrue(RbacMatrix.canGetImportReport(Role.PROVIDER));
    assertFalse(RbacMatrix.canGetImportReport(Role.USER));
  }

  @Test
  void classify_importPathsPrecedeProducts() {
    assertEquals(
        WriteAuthorizationInterceptor.WriteResource.IMPORT,
        WriteAuthorizationInterceptor.classify("/api/v1/catalog/products/import"));
    assertEquals(
        WriteAuthorizationInterceptor.WriteResource.IMPORT_REPORT,
        WriteAuthorizationInterceptor.classify("/api/v1/catalog/products/import/reports/rep-1"));
    assertEquals(
        WriteAuthorizationInterceptor.WriteResource.MAINTENANCE,
        WriteAuthorizationInterceptor.classify("/api/v1/catalog/maintenance/entries"));
    assertEquals(
        WriteAuthorizationInterceptor.WriteResource.PRODUCT,
        WriteAuthorizationInterceptor.classify("/api/v1/catalog/products"));
  }
}
