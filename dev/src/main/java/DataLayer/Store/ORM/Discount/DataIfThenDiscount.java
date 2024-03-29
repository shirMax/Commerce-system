package DataLayer.Store.ORM.Discount;

import DataLayer.Store.ORM.DataStore;
import Domain.Store.Discount.DiscountFactory;
import Domain.Store.Discount.IDiscount;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Discount_IfThen")
public class DataIfThenDiscount extends DataCompositeDiscount{
    public DataIfThenDiscount() {
    }

    public DataIfThenDiscount(DataStore store) {
        super(store);
    }

    @Override
    public IDiscount recover() {
        return DiscountFactory.recover(this);
    }
}
