package DataLayer.User.ORM;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataProduct;
import DataLayer.Store.ORM.DataStore;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.Records.StoreRecords.ProductRecord;

import java.io.Serializable;

@Embeddable
public class DataBaskedProductKey implements Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "store_id", referencedColumnName = "store_id"),
            @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    })
    private DataProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "basket_store_id", referencedColumnName = "store_id"),
            @JoinColumn(name = "username", referencedColumnName = "username")
    })
    private DataBasket basket;

    public DataBaskedProductKey() {
    }

    public DataBaskedProductKey(DataProduct product, DataBasket basket) {
        this.product = product;
        this.basket = basket;
    }

    public DataBaskedProductKey(ProductRecord product, DataBasket dataBasket) {
        this.basket = dataBasket;

        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            Query<DataProduct> query = session.createQuery("FROM DataProduct p WHERE p.key.store.id = :store_id AND p.key.id = :product_id", DataProduct.class);
            query.setParameter("store_id", product.storeId());
            query.setParameter("product_id", product.productId());
            this.product = query.getSingleResult();
        }
    }

    public DataProduct getProduct() {
        return product;
    }

    public DataBasket getBasket() {
        return basket;
    }
}
