package DataLayer.Store.ORM.Discount;

import DataLayer.Store.ORM.DataStore;
import Domain.Store.Discount.DiscountFactory;
import Domain.Store.Discount.IDiscount;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "Discount_Xor")
public class DataXorDiscount extends DataCompositeDiscount{
    public DataXorDiscount() {
    }

    public DataXorDiscount(DataStore store) {
        super(store);
    }

    @Override
    public IDiscount recover() {
        return DiscountFactory.recover(this);
    }
}
