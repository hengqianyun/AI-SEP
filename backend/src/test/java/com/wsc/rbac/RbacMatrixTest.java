package com.wsc.rbac;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wsc.security.Role;
import org.junit.jupiter.api.Test;

class RbacMatrixTest {

  @Test
  void categoryWrite_onlyAdmin() {
    assertTrue(RbacMatrix.canWriteCategory(Role.ADMIN));
    assertFalse(RbacMatrix.canWriteCategory(Role.PROVIDER));
    assertFalse(RbacMatrix.canWriteCategory(Role.USER));
  }

  @Test
  void productWrite_adminAndProvider() {
    assertTrue(RbacMatrix.canWriteProduct(Role.ADMIN));
    assertTrue(RbacMatrix.canWriteProduct(Role.PROVIDER));
    assertFalse(RbacMatrix.canWriteProduct(Role.USER));
  }
}
