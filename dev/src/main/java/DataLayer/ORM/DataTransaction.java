package DataLayer.ORM;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataStore;
import DataLayer.User.ORM.DataMember;
import Domain.User.DummyStoreBasket;
import Domain.User.IStoreBasket;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.Records.StoreRecords.ProductRecord;
import util.Records.Transaction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Entity
@Table(name = "Transaction")
public class DataTransaction {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    @Id
    @Column(name = "transaction_id")
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private DataStore store;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_username")
    private DataMember member;
    private int storeID;
    private String username;
    private LocalDateTime time;
    private double price;
    @OneToMany(mappedBy = "key.transaction", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<DataTransactedProduct> products;

    public DataTransaction(){}

    public DataTransaction(IStoreBasket basket, String userName, double price) {
        if (ID_COUNTER.get() == 0)
            synchronized (ID_COUNTER) {
                if (ID_COUNTER.get() == 0) {
                    if (!DbConfig.shouldPersist())
                        ID_COUNTER.incrementAndGet();
                    else
                        try (Session session = DbConfig.getSessionFactory().openSession()) {
                            Query<Integer> query = session.createQuery(
                                    "SELECT MAX(t.id) FROM DataTransaction t",
                                    Integer.class
                            );
                            Integer maxID = query.uniqueResult();
                            ID_COUNTER.getAndSet(maxID == null ? 1 : maxID + 1);
                        }
                }
            }
        this.id = ID_COUNTER.getAndIncrement();
        storeID = basket.getStoreId();
        username = userName;
        time = LocalDateTime.now();
        this.price = price;
        if (DbConfig.shouldPersist()){
            try (Session session = DbConfig.getSessionFactory().openSession()){
                session.beginTransaction();
                store = session.get(DataStore.class, basket.getStoreId());
                member = session.get(DataMember.class, userName);
                session.persist(this);
                session.getTransaction().commit();
            }
        }
        products = basket.getProductsAsRecords()
                .values().stream()
                .map(p -> new DataTransactedProduct(this, p))
                .collect(Collectors.toSet());
    }

    public int getId() {
        return id;
    }

    public DataStore getStore() {
        return store;
    }

    public DataMember getMember() {
        return member;
    }

    public int getStoreID() {
        return storeID;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public double getPrice() {
        return price;
    }

    public Set<DataTransactedProduct> getProducts() {
        return products;
    }

    public Transaction getAsTransaction(){
        IStoreBasket basket =
                new DummyStoreBasket(
                        getStoreID(),
                        null,
                        products.stream().map(DataTransactedProduct::getAsRecord).toList()
                );
        return new Transaction(getId(), getStoreID(), getUsername(), basket, getPrice(), getTime());
    }
}
