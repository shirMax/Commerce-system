package DataLayer.Store.ORM;

import DataLayer.DbConfig;
import DataLayer.User.ORM.DataMember;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.Records.StoreRecords.ProductRecord;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

@Embeddable
public class DataOfferKey implements Serializable {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    @Column(name = "offer_id")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_member", nullable = false)
    private DataMember offeringMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "store_id", referencedColumnName = "store_id"),
            @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    })
    private DataProduct product;

    public DataOfferKey() {
    }

    public DataOfferKey(String offeringMember, ProductRecord product) {
        if (ID_COUNTER.get() == 0)
            synchronized (ID_COUNTER) {
                if (ID_COUNTER.get() == 0) {
                    if (!DbConfig.shouldPersist())
                        ID_COUNTER.incrementAndGet();
                    else
                        try (Session session = DbConfig.getSessionFactory().openSession()) {
                            Query<Integer> query = session.createQuery(
                                    "SELECT MAX(o.key.id) FROM DataOffer o",
                                    Integer.class
                            );
                            Integer maxID = query.uniqueResult();
                            ID_COUNTER.getAndSet(maxID == null ? 1 : maxID + 1);
                        }
                }
            }
        this.id = ID_COUNTER.getAndIncrement();
        if (DbConfig.shouldPersist()) {
            try (Session session = DbConfig.getSessionFactory().openSession()){
                this.offeringMember = session.get(DataMember.class, offeringMember);
                Query<DataProduct> query = session.createQuery("FROM DataProduct p WHERE p.key.store.id = :store_id AND p.key.id = :product_id", DataProduct.class);
                query.setParameter("store_id", product.storeId());
                query.setParameter("product_id", product.productId());
                this.product = query.getSingleResult();
            }
        }
    }

    public int getId() {
        return id;
    }

    public DataMember getOfferingMember() {
        return offeringMember;
    }

    public DataProduct getProduct() {
        return product;
    }
}
