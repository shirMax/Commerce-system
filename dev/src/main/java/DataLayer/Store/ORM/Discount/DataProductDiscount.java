package DataLayer.Store.ORM.Discount;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataProduct;
import DataLayer.Store.ORM.DataStore;
import Domain.Store.Discount.DiscountFactory;
import Domain.Store.Discount.IDiscount;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.Exceptions.DataError;

@Entity
@Table(name = "Discount_Product")
public class DataProductDiscount extends DataSimpleDiscount {
    @Column(name = "product_id")
    private int productID;

    public DataProductDiscount() {
    }

    public DataProductDiscount(double percentage, int productID) {
        super(percentage);
        this.productID = productID;
    }

    public int getProductID() {
        return productID;
    }

    @Override
    public IDiscount recover() {
        return DiscountFactory.recover(this);
    }
}
