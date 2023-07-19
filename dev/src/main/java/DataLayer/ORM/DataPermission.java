package DataLayer.ORM;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataStore;
import DataLayer.User.ORM.DataBaskedProduct;
import DataLayer.User.ORM.DataMember;
import jakarta.persistence.*;
import org.hibernate.Session;
import util.Enums.PermissionType;
import util.Enums.RoleType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Permission")
public class DataPermission {
    @EmbeddedId
    private DataPermissionKey key;
    private String member;
    private String permission_Giver_Name;
    private RoleType role;
    private Set<PermissionType> permissions;

    public DataPermission() {
    }

    public DataPermission(DataStore store, DataMember member, String permission_Giver_Name){
        this.key = new DataPermissionKey(store, member);
        this.member = member.getUsername();
        this.permission_Giver_Name = permission_Giver_Name;
        role = RoleType.STORE_MANAGER;
        permissions = Set.of();
    }

    public DataPermission(DataStore store, String permission_Giver_Name, String member){
        this.key = new DataPermissionKey(store, member);
        this.member = member;
        this.permission_Giver_Name = permission_Giver_Name;
        role = RoleType.STORE_MANAGER;
        permissions = Set.of();
    }

    public DataPermissionKey getKey() {
        return key;
    }

    public String getMember() {
        return member;
    }

    public String getPermission_Giver_Name() {
        return permission_Giver_Name;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public Set<PermissionType> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionType> permissions) {
        this.permissions = permissions;
    }

    public Object getId(){
        return key;
    }

    public DataPermission persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataPermission updated = session.get(DataPermission.class, getId());
            if (updated == null) updated = this;
            updated.setRole(getRole());
            updated.setPermissions(getPermissions());
            session.persist(updated);
            session.getTransaction().commit();
            return updated;
        }
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataPermission toRemove = session.get(DataPermission.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }
}

