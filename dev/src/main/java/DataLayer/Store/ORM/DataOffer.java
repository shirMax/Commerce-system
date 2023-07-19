package DataLayer.Store.ORM;

import DataLayer.DbConfig;
import DataLayer.User.ORM.DataMember;
import jakarta.persistence.*;
import org.hibernate.Session;
import util.Records.StoreRecords.ProductRecord;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Table(name = "Offer")
public class DataOffer {

    @EmbeddedId
    private DataOfferKey key;

    @Column(name = "offered_price")
    private double offeredPrice;
    @Column(name = "offered_quantity")
    private int offeredQuantity;
    @Column(name = "time_of_offer", columnDefinition = "timestamp(9)")
    private LocalDateTime timeOfOffer;
    @OneToMany(mappedBy = "key.offer", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @MapKey(name = "owner")
    private Map<String, DataOfferConsent> consents; //will be filled only if DbConfig.shouldPersist()

    public  DataOffer(){
    }

    public DataOffer(String offeringMember, ProductRecord product, double offeredPrice, int offeredQuantity, Set<String> ownersToConsent) {
        key = new DataOfferKey(offeringMember, product);
        this.offeredPrice = offeredPrice;
        this.offeredQuantity = offeredQuantity;
        timeOfOffer = LocalDateTime.now();
        consents = new ConcurrentHashMap<>();
        persist();

        for (String owner: ownersToConsent){
            DataOfferConsent dataOfferConsent = new DataOfferConsent(this, owner);
            dataOfferConsent.persist();
            if (DbConfig.shouldPersist())
                consents.put(owner, dataOfferConsent);
        }
    }

    public DataOfferKey getKey() {
        return key;
    }

    public double getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(double offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public int getOfferedQuantity() {
        return offeredQuantity;
    }

    public void setOfferedQuantity(int offeredQuantity) {
        this.offeredQuantity = offeredQuantity;
    }

    public LocalDateTime getTimeOfOffer() {
        return timeOfOffer;
    }

    public void setTimeOfOffer(LocalDateTime timeOfOffer) {
        this.timeOfOffer = timeOfOffer;
    }

    public Map<String, DataOfferConsent> getConsents() {
        return consents;
    }

    public Object getId(){
        return key;
    }

    public DataOffer persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataOffer updated = session.get(DataOffer.class, getId());
            if (updated == null) updated = this;
            updated.setOfferedPrice(getOfferedPrice());
            updated.setOfferedQuantity(getOfferedQuantity());
            updated.setTimeOfOffer(getTimeOfOffer());
            session.persist(updated);
            session.getTransaction().commit();
            return updated;
        }
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataOffer toRemove = session.get(DataOffer.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }

    public void update(double newPrice, int newQuantity) {
        setOfferedPrice(newPrice);
        setOfferedQuantity(newQuantity);
        setTimeOfOffer(LocalDateTime.now());
        for (Map.Entry<String, DataOfferConsent> entry : consents.entrySet()){
            DataOfferConsent dataOfferConsent = entry.getValue();
            dataOfferConsent.setConsent(false);
            entry.setValue(dataOfferConsent.persist());
        }
    }


    public void updateConsent(String userName, boolean isConsent) {
        Optional<Map.Entry<String, DataOfferConsent>> optionalEntry =
                consents.entrySet().stream()
                        .filter(e -> Objects.equals(e.getKey(), userName))
                        .findFirst();
        if (optionalEntry.isPresent()){
            Map.Entry<String, DataOfferConsent> entry = optionalEntry.get();
            DataOfferConsent consent = entry.getValue();
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
            DataOfferConsent consent = consents.remove(member);
            consent.remove();
        }
    }

    public void addToConsent(String ownerToAdd) {
        DataOfferConsent dataOfferConsent = new DataOfferConsent(this, ownerToAdd);
        dataOfferConsent.persist();
        if (DbConfig.shouldPersist())
            consents.put(ownerToAdd, dataOfferConsent);
    }
}
