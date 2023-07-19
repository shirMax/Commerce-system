package UnitTests;

import Domain.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Enums.PermissionType;
import util.Enums.RoleType;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class PermissionTest extends UnitTest {

  private Permission permission;
  private static final String PERMISSION_GIVER = "test1";
  private static final String PERMISSION_OWNER = "test2";


  @BeforeEach
  void setUp() {
    permission = new Permission(null, PERMISSION_GIVER, PERMISSION_OWNER);
  }

  @Test
  void testInitStoreManager() {
    permission.initStoreManager();
    assertTrue(permission.isStoreManager());
    assertFalse(permission.isStoreOwner());
    assertFalse(permission.isStoreFounder());

    assertTrue(permission.hasPermission(
        PermissionType.GET_PURCHASE_HISTORY));
    assertFalse(permission.hasPermission(
        PermissionType.MANAGE_STORE_MANAGER));
    assertFalse(permission.hasPermission(
        PermissionType.ADD_OR_REMOVE_STORE_OWNER));
    assertFalse(permission.hasPermission(
        PermissionType.MAKE_STORE_INACTIVE));
    assertFalse(
        permission.hasPermission(PermissionType.STORAGE_MANAGEMENT));
    assertFalse(permission.hasPermission(
        PermissionType.CHANGE_STORE_POLICY));
    assertFalse(permission.hasPermission(
        PermissionType.CHANGE_OWNER_PERMISSIONS));
    assertFalse(
        permission.hasPermission(PermissionType.GET_EMPLOYEES_DATA));
    assertFalse(
        permission.hasPermission(PermissionType.REPLY_TO_MESSAGES));
  }

  @Test
  void testInitStoreOwner() {
    permission.initStoreOwner();
    assertFalse(permission.isStoreManager());
    assertTrue(permission.isStoreOwner());
    assertFalse(permission.isStoreFounder());

    assertTrue(permission.hasPermission(
        PermissionType.MANAGE_STORE_MANAGER));
    assertTrue(permission.hasPermission(
        PermissionType.ADD_OR_REMOVE_STORE_OWNER));
    assertTrue(
        permission.hasPermission(PermissionType.STORAGE_MANAGEMENT));
    assertTrue(permission.hasPermission(
        PermissionType.CHANGE_STORE_POLICY));
    assertTrue(
        permission.hasPermission(PermissionType.GET_EMPLOYEES_DATA));
    assertTrue(permission.hasPermission(
        PermissionType.GET_PURCHASE_HISTORY));

    assertFalse(permission.hasPermission(
        PermissionType.MAKE_STORE_INACTIVE));
    assertTrue(permission.hasPermission(
        PermissionType.CHANGE_OWNER_PERMISSIONS));
    assertFalse(
        permission.hasPermission(PermissionType.REPLY_TO_MESSAGES));
  }

  @Test
  void testInitStoreFounder() {
    permission.initStoreFounder();
    assertFalse(permission.isStoreManager());
    assertFalse(permission.isStoreOwner());
    assertTrue(permission.isStoreFounder());

    assertTrue(permission.hasPermission(
        PermissionType.MANAGE_STORE_MANAGER));
    assertTrue(permission.hasPermission(
        PermissionType.ADD_OR_REMOVE_STORE_OWNER));
    assertTrue(
        permission.hasPermission(PermissionType.STORAGE_MANAGEMENT));
    assertTrue(permission.hasPermission(
        PermissionType.CHANGE_STORE_POLICY));
    assertTrue(
        permission.hasPermission(PermissionType.GET_EMPLOYEES_DATA));
    assertTrue(permission.hasPermission(
        PermissionType.GET_PURCHASE_HISTORY));
    assertTrue(permission.hasPermission(
        PermissionType.MAKE_STORE_INACTIVE));
    assertTrue(permission.hasPermission(
        PermissionType.CHANGE_OWNER_PERMISSIONS));

    assertFalse(
        permission.hasPermission(PermissionType.REPLY_TO_MESSAGES));
  }

  @Test
  void testSetPermissions() {
    permission.setPermissions(Set.of(PermissionType.ADD_OR_REMOVE_STORE_OWNER,
                                     PermissionType.STORAGE_MANAGEMENT));
    assertTrue(
        permission.hasPermission(PermissionType.ADD_OR_REMOVE_STORE_OWNER));
    assertFalse(
        permission.hasPermission(PermissionType.MANAGE_STORE_MANAGER));
    assertTrue(permission.hasPermission(PermissionType.STORAGE_MANAGEMENT));

    permission.setPermissions(Set.of(PermissionType.MANAGE_STORE_MANAGER,
                                     PermissionType.MAKE_STORE_INACTIVE,
                                     PermissionType.CHANGE_OWNER_PERMISSIONS));
    assertFalse(permission.hasPermission(
        PermissionType.ADD_OR_REMOVE_STORE_OWNER));
    assertTrue(permission.hasPermission(
        PermissionType.MANAGE_STORE_MANAGER));
    assertTrue(permission.hasPermission(PermissionType.MAKE_STORE_INACTIVE));
    assertTrue(
        permission.hasPermission(PermissionType.CHANGE_OWNER_PERMISSIONS));

    permission.setPermissions(Set.of(PermissionType.ADD_OR_REMOVE_STORE_OWNER,
                                     PermissionType.MANAGE_STORE_MANAGER,
                                     PermissionType.CHANGE_STORE_POLICY,
                                     PermissionType.REPLY_TO_MESSAGES));
    assertTrue(
        permission.hasPermission(PermissionType.ADD_OR_REMOVE_STORE_OWNER));
    assertTrue(
        permission.hasPermission(PermissionType.MANAGE_STORE_MANAGER));
    assertTrue(permission.hasPermission(PermissionType.CHANGE_STORE_POLICY));
    assertTrue(permission.hasPermission(PermissionType.REPLY_TO_MESSAGES));
    assertFalse(permission.hasPermission(PermissionType.STORAGE_MANAGEMENT));
  }

  @Test
  public void testStoreManagerNoAddOrRemoveStoreOwnerPermission() {
    permission.initStoreManager();

    assertFalse(permission.hasPermission(
        PermissionType.ADD_OR_REMOVE_STORE_OWNER));
  }

  @Test
  public void testStoreManagerGetPurchaseHistoryPermission() {
    permission.initStoreManager();

    assertTrue(permission.hasPermission(
        PermissionType.GET_PURCHASE_HISTORY));
  }

  @Test
  public void testStoreFounderAllPermissions() {
    permission.initStoreFounder();

    assertTrue(permission.hasPermission(
        PermissionType.ADD_OR_REMOVE_STORE_OWNER));
    assertTrue(permission.hasPermission(
        PermissionType.MANAGE_STORE_MANAGER));
    assertTrue(permission.hasPermission(
        PermissionType.MAKE_STORE_INACTIVE));
    assertTrue(
        permission.hasPermission(PermissionType.STORAGE_MANAGEMENT));
    assertTrue(permission.hasPermission(
        PermissionType.CHANGE_STORE_POLICY));
    assertTrue(permission.hasPermission(
        PermissionType.CHANGE_OWNER_PERMISSIONS));
    assertTrue(
        permission.hasPermission(PermissionType.GET_EMPLOYEES_DATA));
    assertTrue(permission.hasPermission(
        PermissionType.GET_PURCHASE_HISTORY));
  }

  @Test
  public void testStoreFounderChangeOwnerPermissionsPermission() {
    permission.initStoreFounder();

    assertTrue(permission.hasPermission(
        PermissionType.CHANGE_OWNER_PERMISSIONS));
  }

  @Test
  public void testIsStoreOwner() {
    permission.setRole(RoleType.STORE_OWNER);
    assertTrue(permission.isStoreOwner());
  }

  @Test
  public void testHasPermission() {
    permission.initStoreOwner();
    assertTrue(permission.hasPermission(
        PermissionType.MANAGE_STORE_MANAGER));
    assertFalse(
        permission.hasPermission(PermissionType.REPLY_TO_MESSAGES));
  }
}
