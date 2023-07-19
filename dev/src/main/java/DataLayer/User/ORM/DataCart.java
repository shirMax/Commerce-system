package DataLayer.User.ORM;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataStore;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "Cart")
public class DataCart {
    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username")
    private DataMember member;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "key.cart")
    @MapKey(name = "storeID")
    private Map<Integer, DataBasket> baskets;

    public DataCart(){}

    public DataCart(DataMember member) {
        this.member = member;
        baskets = new HashMap<>();
    }

    public DataMember getMember() {
        return member;
    }

    public Map<Integer, DataBasket> getBaskets() {
        return baskets;
    }

    public Object getId(){
        return member.getId();
    }

    public DataCart persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataCart updated = session.get(DataCart.class, getId());
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
            DataCart toRemove = session.get(DataCart.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }
}
