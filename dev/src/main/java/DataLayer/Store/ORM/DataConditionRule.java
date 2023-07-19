package DataLayer.Store.ORM;

import DataLayer.DbConfig;
import Domain.Store.Category;
import Domain.Store.Conditions.Condition;
import jakarta.persistence.*;
import org.hibernate.Session;

@Entity
@Table(name = "Rule_Condition")
public class DataConditionRule extends DataCondition {

    @ManyToOne(fetch = FetchType.LAZY)
    DataPurchaseRule purchaseRule;

    public DataConditionRule() {
    }

    public DataConditionRule(DataPurchaseRule purchaseRule, Condition.ConditionType conditionType, int quantity, double price, int productID, Category category) {
        super(conditionType, quantity, price, productID, category);
        this.purchaseRule = purchaseRule;
        if (DbConfig.shouldPersist())
            try (Session session = DbConfig.getSessionFactory().openSession()) {
                session.beginTransaction();
                session.persist(this);
                session.getTransaction().commit();
            }
    }

    public DataPurchaseRule getPurchaseRule() {
        return purchaseRule;
    }
}
