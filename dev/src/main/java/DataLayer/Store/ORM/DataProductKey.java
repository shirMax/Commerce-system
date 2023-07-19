package DataLayer.Store.ORM;

import DataLayer.DbConfig;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Embeddable
public class DataProductKey implements Serializable {
    private static final Map<Integer, Integer> STORES_ID_COUNTERS = new ConcurrentHashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private DataStore store;

    @Column(name = "product_id")
    private Integer id;

    public DataProductKey() {}

    public DataProductKey(DataStore store){
        this.store = store;
        Integer storeId = store.getId();
        synchronized (STORES_ID_COUNTERS){
            if (!STORES_ID_COUNTERS.containsKey(storeId)) {
                if (!DbConfig.shouldPersist())
                    STORES_ID_COUNTERS.put(storeId, 1);
                else
                    try (Session session = DbConfig.getSessionFactory().openSession()) {
                        Query<Integer> query = session.createQuery(
                                "SELECT MAX(p.key.id) FROM DataProduct p WHERE p.key.store.id = :store_id",
                                Integer.class
                        );
                        query.setParameter("store_id", storeId);
                        Integer maxID = query.uniqueResult();
                        STORES_ID_COUNTERS.put(storeId, maxID == null ? 1 : maxID + 1);
                    }
            }
            this.id = STORES_ID_COUNTERS.get(storeId);
            STORES_ID_COUNTERS.put(storeId, id + 1);
        }
    }

    public DataStore getStore() {
        return store;
    }

    public Integer getId() {
        return id;
    }

}
