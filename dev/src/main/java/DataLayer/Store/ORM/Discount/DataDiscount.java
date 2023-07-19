package DataLayer.Store.ORM.Discount;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataConditionDiscount;
import DataLayer.Store.ORM.DataStore;
import Domain.Store.Discount.IDiscount;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "Discount")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataDiscount {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    @Id
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private DataStore store;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "father_id")
    private DataCompositeDiscount father;

    @OneToMany(mappedBy = "discount", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<DataConditionDiscount> conditions;

    public DataDiscount() {
    }

    public DataDiscount(DataStore store) {
        if (ID_COUNTER.get() == 0)
            synchronized (ID_COUNTER) {
                if (ID_COUNTER.get() == 0) {
                    if (!DbConfig.shouldPersist())
                        ID_COUNTER.incrementAndGet();
                    else
                        try (Session session = DbConfig.getSessionFactory().openSession()) {
                            Query<Integer> query = session.createQuery(
                                    "SELECT MAX(o.id) FROM DataDiscount o",
                                    Integer.class
                            );
                            Integer maxID = query.uniqueResult();
                            ID_COUNTER.getAndSet(maxID == null ? 1 : maxID + 1);
                        }
                }
            }
        this.id = ID_COUNTER.getAndIncrement();
        this.store = store;
    }

    public int getId() {
        return id;
    }

    public DataStore getStore() {
        return store;
    }

    public void setStore(DataStore store) {
        this.store = store;
    }

    public DataCompositeDiscount getFather() {
        return father;
    }

    public void setFather(DataCompositeDiscount father) {
        this.father = father;
    }

    public Set<DataConditionDiscount> getConditions() {
        return conditions;
    }

    public DataDiscount persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataDiscount updated = session.get(DataDiscount.class, getId());
            if (updated == null) updated = this;
            if (father != null)
                updated.setFather(session.get(DataCompositeDiscount.class, father.getId()));
            updated.setStore(getStore());
            session.persist(updated);
            session.getTransaction().commit();
            return updated;
        }
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataDiscount toRemove = session.get(DataDiscount.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }

    public abstract IDiscount recover();
}
