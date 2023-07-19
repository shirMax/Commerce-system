package DataLayer.Store.ORM.Discount;

import DataLayer.Store.ORM.DataConditionDiscount;
import DataLayer.Store.ORM.DataStore;
import Domain.Store.Discount.DiscountFactory;
import Domain.Store.Discount.IDiscount;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "Discount_Or")
public class DataOrDiscount extends DataCompositeDiscount{
    public DataOrDiscount() {
    }

    public DataOrDiscount(DataStore store) {
        super(store);
    }

    @Override
    public IDiscount recover() {
        return DiscountFactory.recover(this);
    }
}
