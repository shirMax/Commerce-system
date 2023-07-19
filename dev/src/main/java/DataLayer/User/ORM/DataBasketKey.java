package DataLayer.User.ORM;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataStore;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.io.Serializable;

@Embeddable
public class DataBasketKey implements Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private DataStore store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private DataCart cart;

    public DataBasketKey() {
    }

    public DataBasketKey(DataStore store, DataCart cart) {
        this.store = store;
        this.cart = cart;
    }

    public DataBasketKey(Integer storeId, DataCart dataCart) {
        this.cart = dataCart;

        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            this.store = session.get(DataStore.class, storeId);
        }
    }

    public DataStore getStore() {
        return store;
    }

    public DataCart getCart() {
        return cart;
    }
}
