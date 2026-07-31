package com.shdata.datachain.rbac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shdata.datachain.security.Role;
import org.junit.jupiter.api.Test;

class RbacMatrixTest {

  @Test
  void categoryWrite_onlyAdmin() {
    assertTrue(RbacMatrix.canWriteCategory(Role.ADMIN));
    assertFalse(RbacMatrix.canWriteCategory(Role.PROVIDER));
    assertFalse(RbacMatrix.canWriteCategory(Role.USER));
  }

  @Test
  void catalogMaintenance_onlyAdmin() {
    assertTrue(RbacMatrix.canMaintainCatalog(Role.ADMIN));
    assertFalse(RbacMatrix.canMaintainCatalog(Role.PROVIDER));
    assertFalse(RbacMatrix.canMaintainCatalog(Role.USER));
  }

  @Test
  void productWrite_adminAndProvider() {
    assertTrue(RbacMatrix.canWriteProduct(Role.ADMIN));
    assertTrue(RbacMatrix.canWriteProduct(Role.PROVIDER));
    assertFalse(RbacMatrix.canWriteProduct(Role.USER));
  }

  @Test
  void productImport_adminAndProvider() {
    assertTrue(RbacMatrix.canImportProduct(Role.ADMIN));
    assertTrue(RbacMatrix.canImportProduct(Role.PROVIDER));
    assertFalse(RbacMatrix.canImportProduct(Role.USER));
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
