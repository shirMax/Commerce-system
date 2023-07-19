package DataLayer.User.ORM;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataProduct;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.Session;
import util.Records.StoreRecords.ProductRecord;

@Entity
@Table(name = "Basked_Product")
public class DataBaskedProduct {

    @EmbeddedId
    private DataBaskedProductKey key;
    @Column(name = "product__id")
    private int productID;
    private double priceAfterDiscount;
    private int quantity;

    public DataBaskedProduct(){}

    public DataBaskedProduct(DataProduct product, DataBasket basket, int quantity){
        this.key = new DataBaskedProductKey(product, basket);
        this.productID = product.getKey().getId();
        this.quantity = quantity;
        this.priceAfterDiscount = product.getPrice();
    }

    public DataBaskedProduct(DataBasket dataBasket, ProductRecord product) {
        this.key = new DataBaskedProductKey(product, dataBasket);
        this.productID = product.productId();
        this.quantity = product.quantity();
        this.priceAfterDiscount = product.priceAfterDiscount();
    }

    public DataBaskedProductKey getKey() {
        return key;
    }

    public int getProductID() {
        return productID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPriceAfterDiscount() {
        return priceAfterDiscount;
    }

    public void setPriceAfterDiscount(double priceAfterDiscount) {
        this.priceAfterDiscount = priceAfterDiscount;
    }

    public Object getId(){
        return key;
    }

    public DataBaskedProduct persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataBaskedProduct updated = session.get(DataBaskedProduct.class, getId());
            if (updated == null) updated = this;
            updated.setQuantity(getQuantity());
            updated.setPriceAfterDiscount(getPriceAfterDiscount());
            session.persist(updated);
            session.getTransaction().commit();
            return updated;
        }
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataBaskedProduct toRemove = session.get(DataBaskedProduct.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }
}
