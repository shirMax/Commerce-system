package DataLayer.ORM;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataStore;
import DataLayer.User.ORM.DataMember;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.io.Serializable;

@Embeddable
public class DataPermissionKey implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private DataStore store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private DataMember member;

    public DataPermissionKey() {
    }

    public DataPermissionKey(DataStore store, DataMember member) {
        this.store = store;
        this.member = member;
    }

    public DataPermissionKey(DataStore store, String member) {
        this.store = store;

        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()) {
            this.member = session.get(DataMember.class, member);
        }
    }

    public DataStore getStore() {
        return store;
    }

    public DataMember getMember() {
        return member;
    }
}
