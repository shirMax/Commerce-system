package DataLayer.Store.ORM;

import DataLayer.DbConfig;
import Domain.Store.Conditions.Condition;
import Domain.Store.Purchase.PurchaseRule;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "Purchase_Rule")
public class DataPurchaseRule {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    @Id
    private int id;
    @Enumerated(EnumType.STRING)
    private PurchaseRule.PurchaseType purchaseType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private DataStore store;
    @OneToMany(mappedBy = "purchaseRule", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<DataConditionRule> conditions;

    public DataPurchaseRule() {
    }

    public DataPurchaseRule(DataStore store, PurchaseRule.PurchaseType purchaseType) {
        if (ID_COUNTER.get() == 0)
            synchronized (ID_COUNTER) {
                if (ID_COUNTER.get() == 0) {
                    if (!DbConfig.shouldPersist())
                        ID_COUNTER.incrementAndGet();
                    else
                        try (Session session = DbConfig.getSessionFactory().openSession()) {
                            Query<Integer> query = session.createQuery(
                                    "SELECT MAX(o.id) FROM DataPurchaseRule o",
                                    Integer.class
                            );
                            Integer maxID = query.uniqueResult();
                            ID_COUNTER.getAndSet(maxID == null ? 1 : maxID + 1);
                        }
                }
            }
        this.id = ID_COUNTER.getAndIncrement();
        this.purchaseType = purchaseType;
        this.store = store;

        if (DbConfig.shouldPersist())
            try (Session session = DbConfig.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.persist(this);
                session.getTransaction().commit();
            }
    }

    public int getId() {
        return id;
    }

    public PurchaseRule.PurchaseType getPurchaseType() {
        return purchaseType;
    }

    public DataStore getStore() {
        return store;
    }

    public Set<DataConditionRule> getConditions() {
        return conditions;
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataPurchaseRule toRemove = session.get(DataPurchaseRule.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }
}
