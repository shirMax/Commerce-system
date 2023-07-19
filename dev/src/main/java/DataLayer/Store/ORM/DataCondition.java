package DataLayer.Store.ORM;

import DataLayer.DbConfig;
import Domain.Store.Category;
import Domain.Store.Conditions.Condition;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "Condition")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataCondition {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    @Id
    private int id;
    @Enumerated(EnumType.STRING)
    private Condition.ConditionType conditionType;
    private int quantity;
    private double price;
    @Column(name = "product_id")
    private int productID;
    @Enumerated(EnumType.STRING)
    private Category category;

    public DataCondition() {
    }

    public DataCondition(Condition.ConditionType conditionType, int quantity, double price, int productID, Category category) {
        if (ID_COUNTER.get() == 0)
            synchronized (ID_COUNTER) {
                if (ID_COUNTER.get() == 0) {
                    if (!DbConfig.shouldPersist())
                        ID_COUNTER.incrementAndGet();
                    else
                        try (Session session = DbConfig.getSessionFactory().openSession()) {
                            Query<Integer> query = session.createQuery(
                                    "SELECT MAX(o.id) FROM DataCondition o",
                                    Integer.class
                            );
                            Integer maxID = query.uniqueResult();
                            ID_COUNTER.getAndSet(maxID == null ? 1 : maxID + 1);
                        }
                }
            }
        this.id = ID_COUNTER.getAndIncrement();
        this.conditionType = conditionType;
        this.quantity = quantity;
        this.price = price;
        this.productID = productID;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public Condition.ConditionType getConditionType() {
        return conditionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getProductID() {
        return productID;
    }

    public double getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataCondition toRemove = session.get(DataCondition.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }
}
