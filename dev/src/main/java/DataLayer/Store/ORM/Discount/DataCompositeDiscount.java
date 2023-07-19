package DataLayer.Store.ORM.Discount;

import DataLayer.Store.ORM.DataStore;
import jakarta.persistence.*;

import java.util.Map;

@Entity
@Table(name = "Discount_Composite")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class DataCompositeDiscount extends DataDiscount{
    @OneToMany(mappedBy = "father", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @MapKey(name = "id")
    Map<Integer, DataDiscount> discounts;

    public DataCompositeDiscount() {
    }

    public DataCompositeDiscount(DataStore store) {
        super(store);
    }

    public Map<Integer, DataDiscount> getDiscounts() {
        return discounts;
    }
}
