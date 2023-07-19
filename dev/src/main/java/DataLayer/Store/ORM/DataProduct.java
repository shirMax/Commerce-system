package DataLayer.Store.ORM;

import DataLayer.DbConfig;
import DataLayer.User.ORM.DataBaskedProduct;
import Domain.Store.Category;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "Product")
public class DataProduct {
    @EmbeddedId
    private DataProductKey key;
    private String name;
    private double price;
    @Enumerated(EnumType.STRING)
    private Category category;
    private int quantity;
    private double rating;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "key.product")
    @MapKey(name = "key.id")
    private Map<Integer, DataOffer> offers;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "key.product")
    private List<DataBaskedProduct> products;

    public DataProduct() {
    }

    public DataProduct(DataStore dataStore, String name, double price, Category category, int quantity, double rating) {
        this.key = new DataProductKey(dataStore);
        this.name = name;
        this.price = price;
        this.category = category;
        this.quantity = quantity;
        this.rating = rating;
    }

    public DataProductKey getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Map<Integer, DataOffer> getOffers() {
        return offers;
    }

    public Object getId(){
        return key;
    }

    public DataProduct persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataProduct updated = session.get(DataProduct.class, getId());
            if (updated == null) updated = this;
            updated.setName(getName());
            updated.setPrice(getPrice());
            updated.setCategory(getCategory());
            updated.setQuantity(getQuantity());
            updated.setRating(getRating());
            session.persist(updated);
            session.getTransaction().commit();
            return updated;
        }
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataProduct toRemove = session.get(DataProduct.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }
}
