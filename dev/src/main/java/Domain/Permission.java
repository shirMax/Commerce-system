package Domain;

import DataLayer.ORM.DataPermission;
import DataLayer.Store.ORM.DataStore;
import org.checkerframework.checker.nullness.qual.NonNull;
import util.Enums.PermissionType;
import util.Enums.RoleType;

import java.util.Set;

//TODO: this class needs some serious refactor
public class Permission {

    public static final Set<PermissionType> MANAGER_PERMISSIONS = Set.of(PermissionType.GET_PURCHASE_HISTORY);
    public static final Set<PermissionType> OWNER_PERMISSIONS =
            Set.of(
                    PermissionType.MANAGE_STORE_MANAGER,
                    PermissionType.ADD_OR_REMOVE_STORE_OWNER,
                    PermissionType.CHANGE_OWNER_PERMISSIONS,
                    PermissionType.STORAGE_MANAGEMENT,
                    PermissionType.CHANGE_STORE_POLICY,
                    PermissionType.GET_EMPLOYEES_DATA,
                    PermissionType.GET_PURCHASE_HISTORY,
                    PermissionType.STORE_MANAGEMENT,
                    PermissionType.MANAGE_OFFERS,
                    PermissionType.MANAGE_CONTRACTS
            );
    public static final Set<PermissionType> FOUNDER_PERMISSIONS =
            Set.of(
                    PermissionType.ADD_OR_REMOVE_STORE_OWNER,
                    PermissionType.MANAGE_STORE_MANAGER,
                    PermissionType.MAKE_STORE_INACTIVE,
                    PermissionType.STORAGE_MANAGEMENT,
                    PermissionType.CHANGE_STORE_POLICY,
                    PermissionType.CHANGE_OWNER_PERMISSIONS,
                    PermissionType.GET_EMPLOYEES_DATA,
                    PermissionType.GET_PURCHASE_HISTORY,
                    PermissionType.STORE_MANAGEMENT,
                    PermissionType.MANAGE_OFFERS,
                    PermissionType.MANAGE_CONTRACTS
            );

    private DataPermission dataPermission;

    // CTOR for data creation
    public Permission(DataStore dataStore, @NonNull String permissionGiverName, String member) {
        dataPermission = new DataPermission(dataStore, permissionGiverName, member);
        dataPermission = dataPermission.persist();
    }

    //CTOR for recovery from DB
    public Permission(DataPermission dataPermission) {
        this.dataPermission = dataPermission;
    }

    public void initStoreManager() {
        dataPermission.setRole(RoleType.STORE_MANAGER);
        dataPermission.setPermissions(MANAGER_PERMISSIONS);
        dataPermission = dataPermission.persist();
    }

    public void initStoreOwner() {
        dataPermission.setRole(RoleType.STORE_OWNER);
        dataPermission.setPermissions(OWNER_PERMISSIONS);
        dataPermission = dataPermission.persist();
    }

    public void initStoreFounder() {
        dataPermission.setRole(RoleType.STORE_FOUNDER);
        dataPermission.setPermissions(FOUNDER_PERMISSIONS);
        dataPermission = dataPermission.persist();
    }

    public void setPermissions(Set<PermissionType> permissionTypes) {
        dataPermission.setPermissions(Set.copyOf(permissionTypes));
        dataPermission = dataPermission.persist();
    }

    public RoleType getRoleType() {
        return dataPermission.getRole();
    }

    public void setRole(RoleType role) {
        dataPermission.setRole(role);
        dataPermission = dataPermission.persist();
    }

    public boolean hasPermission(PermissionType permissionType) {
        return dataPermission.getPermissions().contains(permissionType);
    }

    /**
     * Returns all activated permissions
     *
     * @return bitmap composed of all permissions with bitwise or
     */
    public int permissionsBitMap() {
        int bitmap = 0;
        for (PermissionType perm : PermissionType.values())
            if (hasPermission(perm))
                bitmap = bitmap | perm.getNumVal();
        return bitmap;
    }

    public boolean isStoreOwner() {
        return getRoleType() == RoleType.STORE_OWNER;
    }

    public boolean isStoreManager() {
        return getRoleType() == RoleType.STORE_MANAGER;
    }

    public boolean isStoreFounder() {
        return getRoleType() == RoleType.STORE_FOUNDER;
    }

    public String getPermissionGiverName() {
        return dataPermission.getPermission_Giver_Name();
    }

    public void remove() {
        dataPermission.remove();
    }
}
