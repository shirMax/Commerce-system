package DataLayer.Store.ORM.Discount;

import DataLayer.Store.ORM.DataStore;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "Discount_Simple")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataSimpleDiscount extends DataDiscount{

    private double percentage;

    public DataSimpleDiscount() {
    }

    public DataSimpleDiscount(double percentage) {
        super(null);
        this.percentage = percentage;
    }

    public double getPercentage() {
        return percentage;
    }
}
