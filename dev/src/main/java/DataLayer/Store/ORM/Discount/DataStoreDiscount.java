package DataLayer.Store.ORM.Discount;

import Domain.Store.Discount.DiscountFactory;
import Domain.Store.Discount.IDiscount;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import util.Exceptions.DataError;

@Entity
@Table(name = "Discount_Store")
public class DataStoreDiscount extends DataSimpleDiscount {
    public DataStoreDiscount() {
    }

    public DataStoreDiscount(double percentage) {
        super(percentage);
    }

    @Override
    public IDiscount recover() {
        return DiscountFactory.recover(this);
    }
}
