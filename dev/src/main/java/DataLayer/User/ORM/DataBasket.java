package DataLayer.User.ORM;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataProduct;
import DataLayer.Store.ORM.DataStore;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.util.Map;

@Entity
@Table(name = "Basket")
public class DataBasket {

    @EmbeddedId
    private DataBasketKey key;

    @Column(name = "store__id")
    private int storeID;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "key.basket", fetch = FetchType.LAZY)
    @MapKey(name = "productID")
    private Map<Integer, DataBaskedProduct> products;

    public DataBasket() {
    }

    public DataBasket(DataCart cart, DataStore store){
        this.key = new DataBasketKey(store, cart);
        this.storeID = store.getId();
    }

    public DataBasket(DataCart dataCart, Integer storeId) {
        this.key = new DataBasketKey(storeId, dataCart);
        this.storeID = storeId;
    }

    public DataBasketKey getKey() {
        return key;
    }

    public int getStoreID() {
        return storeID;
    }

    public Map<Integer, DataBaskedProduct> getProducts() {
        return products;
    }

    public Object getId(){
        return key;
    }

    public DataBasket persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataBasket updated = session.get(DataBasket.class, getId());
            if (updated == null) updated = this;
            session.persist(updated);
            session.getTransaction().commit();
            return updated;
        }
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataBasket toRemove = session.get(DataBasket.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }
}
