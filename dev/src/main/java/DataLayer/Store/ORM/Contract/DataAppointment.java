package DataLayer.Store.ORM.Contract;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataOffer;
import DataLayer.Store.ORM.DataOfferConsent;
import DataLayer.Store.ORM.DataProduct;
import DataLayer.Store.ORM.DataStore;
import DataLayer.User.ORM.DataMember;
import Domain.Store.OwnerAppointmentContract;
import jakarta.persistence.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Table(name = "Contract")
public class DataAppointment {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    @Id
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private DataStore store;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner", nullable = false)
    private DataMember assigner;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee", nullable = false)
    private DataMember assignee;
    private String contract;
    @OneToMany(mappedBy = "key.appointment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @MapKey(name = "owner")
    private Map<String, DataAppointConsent> consents; //will be filled only if DbConfig.shouldPersist()


    public DataAppointment(){
    }

    public DataAppointment(int storeId, String assigningOwner, String newOwner, String contract, Set<String> ownersToConsent) {
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
        this.contract = contract;
        if (DbConfig.shouldPersist()) {
            try (Session session = DbConfig.getSessionFactory().openSession()){
                this.store = session.get(DataStore.class, storeId);
                this.assigner = session.get(DataMember.class, assigningOwner);
                this.assignee = session.get(DataMember.class, newOwner);
            }
        }
        this.consents = new ConcurrentHashMap<>();
        if (DbConfig.shouldPersist())
            persist();
        for (String owner: ownersToConsent){
            DataAppointConsent dataAppointConsent = new DataAppointConsent(this, owner);
            dataAppointConsent.persist();
            if (DbConfig.shouldPersist())
                consents.put(owner, dataAppointConsent);
        }
    }

    public int getId() {
        return id;
    }

    public DataStore getStore() {
        return store;
    }

    public DataMember getAssigner() {
        return assigner;
    }

    public DataMember getAssignee() {
        return assignee;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public Map<String, DataAppointConsent> getConsents() {
        return consents;
    }

    public DataAppointment persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataAppointment updated = session.get(DataAppointment.class, getId());
            if (updated == null) updated = this;
            updated.setContract(getContract());
            session.persist(updated);
            session.getTransaction().commit();
            return updated;
        }
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataAppointment toRemove = session.get(DataAppointment.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }

    public void updateConsent(String userName, boolean isConsent) {
        Optional<Map.Entry<String, DataAppointConsent>> optionalEntry =
                consents.entrySet().stream()
                        .filter(e -> Objects.equals(e.getKey(), userName))
                        .findFirst();
        if (optionalEntry.isPresent()){
            Map.Entry<String, DataAppointConsent> entry = optionalEntry.get();
            DataAppointConsent consent = entry.getValue();
            consent.setConsent(isConsent);
            entry.setValue(consent.persist());
        }
    }

    public void removeFromConsent(String ownerToRemove) {
        Optional<String> optionalMember =
                consents.keySet().stream()
                        .filter(o -> Objects.equals(o, ownerToRemove))
                        .findFirst();
        if (optionalMember.isPresent()){
            String member = optionalMember.get();
            DataAppointConsent consent = consents.remove(member);
            consent.remove();
        }
    }

    public void addToConsent(String ownerToAdd) {
        DataAppointConsent dataAppointConsent = new DataAppointConsent(this, ownerToAdd);
        dataAppointConsent.persist();
        if (DbConfig.shouldPersist())
            consents.put(ownerToAdd, dataAppointConsent);
    }
}
