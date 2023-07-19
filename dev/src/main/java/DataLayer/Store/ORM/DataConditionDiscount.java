package DataLayer.Store.ORM;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.Discount.DataDiscount;
import Domain.Store.Category;
import Domain.Store.Conditions.Condition;
import jakarta.persistence.*;
import org.hibernate.Session;

@Entity
@Table(name = "Discount_Condition")
public class DataConditionDiscount extends DataCondition {
    @ManyToOne(fetch = FetchType.LAZY)
    private DataDiscount discount;

    public DataConditionDiscount() {
    }

    public DataConditionDiscount(DataDiscount discount, Condition.ConditionType conditionType, int quantity, double price, int productID, Category category) {
        super(conditionType, quantity, price, productID, category);
        this.discount = discount;
        if (DbConfig.shouldPersist())
            try (Session session = DbConfig.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.persist(this);
                session.getTransaction().commit();
            }
    }

    public DataDiscount getDiscount() {
        return discount;
    }
}
