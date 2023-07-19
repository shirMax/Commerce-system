package DataLayer.Store.ORM;

import DataLayer.DbConfig;
import DataLayer.ORM.DataPermission;
import DataLayer.ORM.DataTransaction;
import DataLayer.Store.ORM.Contract.DataAppointment;
import DataLayer.Store.ORM.Discount.DataDiscount;
import DataLayer.User.ORM.DataBasket;
import DataLayer.User.ORM.DataMember;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "Store")
public class DataStore {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    @Id
    private Integer id;
    private String name;
    private String description;
    private double rating;
    private boolean active_state;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "key.store", fetch = FetchType.LAZY)
    @MapKey(name = "key.id")
    private Map<Integer, DataProduct> products;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "key.store", fetch = FetchType.LAZY)
    private List<DataBasket> baskets;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "key.store", fetch = FetchType.LAZY)
    @MapKey(name = "member")
    private Map<String, DataPermission> permissions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "store", fetch = FetchType.LAZY)
    @MapKey(name = "id")
    private Map<Integer, DataAppointment> contracts;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DataTransaction> transactions;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MapKey(name = "id")
    private Map<Integer, DataPurchaseRule> purchaseRules;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MapKey(name = "id")
    private Map<Integer, DataDiscount> discounts;

    public DataStore(){
    }

    public DataStore(String name, String description, double rating, boolean active_state) {
        this.name = name;
        this.description = description;
        this.rating = rating;
        this.active_state = active_state;
        if (ID_COUNTER.get() == 0)
            synchronized (ID_COUNTER) {
                if (ID_COUNTER.get() == 0) {
                    if (!DbConfig.shouldPersist())
                        ID_COUNTER.incrementAndGet();
                    else
                        try (Session session = DbConfig.getSessionFactory().openSession()) {
                            Query<Integer> query = session.createQuery(
                                    "SELECT MAX(s.id) FROM DataStore s",
                                    Integer.class
                            );
                            Integer maxID = query.uniqueResult();
                            ID_COUNTER.getAndSet(maxID == null ? 1 : maxID + 1);
                        }
                }
            }
        this.id = ID_COUNTER.getAndIncrement();
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isActive_state() {
        return active_state;
    }

    public void setActive_state(boolean active_state) {
        this.active_state = active_state;
    }

    public Map<Integer, DataProduct> getProducts() {
        return products;
    }

    public Map<String, DataPermission> getPermissions() {
        return permissions;
    }

    public Map<Integer, DataAppointment> getContracts() {
        return contracts;
    }

    public Map<Integer, DataPurchaseRule> getPurchaseRules() {
        return purchaseRules;
    }

    public Map<Integer, DataDiscount> getDiscounts() {
        return discounts;
    }

    public DataStore persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataStore updated = session.get(DataStore.class, getId());
            if (updated == null) updated = this;
            updated.setName(getName());
            updated.setDescription(getDescription());
            updated.setRating(getRating());
            updated.setActive_state(isActive_state());
            session.persist(updated);
            session.getTransaction().commit();
            return updated;
        }
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataStore toRemove = session.get(DataStore.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }
}
