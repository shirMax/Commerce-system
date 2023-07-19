package DataLayer.ORM;

import DataLayer.DbConfig;
import Domain.Store.Category;
import jakarta.persistence.*;
import org.hibernate.Session;
import util.Records.StoreRecords.ProductRecord;

import java.util.stream.Collectors;

@Entity
@Table(name = "Transacted_Product")
public class DataTransactedProduct {
    @EmbeddedId
    private DataTransactedProductKey key;

    private int storeID;
    private String name;
    private double price;
    @Enumerated(EnumType.STRING)
    private Category category;
    private int quantity;
    private double priceAfterDiscount;
    private double rating;

    public DataTransactedProduct(){
    }
    
    public DataTransactedProduct(DataTransaction dataTransaction, ProductRecord productRecord){
        key = new DataTransactedProductKey(productRecord.productId(), dataTransaction);
        storeID = productRecord.storeId();
        name = productRecord.productName();
        price = productRecord.productPrice();
        category = productRecord.productCategory();
        quantity = productRecord.quantity();
        priceAfterDiscount = productRecord.priceAfterDiscount();
        rating = productRecord.productRating();

        if (DbConfig.shouldPersist()){
            try (Session session = DbConfig.getSessionFactory().openSession()){
                session.beginTransaction();
                session.persist(this);
                session.getTransaction().commit();
            }
        }
    }

    public DataTransactedProductKey getKey() {
        return key;
    }

    public int getStoreID() {
        return storeID;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPriceAfterDiscount() {
        return priceAfterDiscount;
    }

    public double getRating() {
        return rating;
    }

    public ProductRecord getAsRecord() {
        return new ProductRecord(getStoreID(), getKey().getProductID(), getName(), getPrice(), getCategory(), getQuantity(), getPriceAfterDiscount(), getRating());
    }
}
