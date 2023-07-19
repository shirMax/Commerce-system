package DataLayer.Store.ORM;


import DataLayer.DbConfig;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.Session;

@Entity
@Table(name = "Offer_Consent")
public class DataOfferConsent {
    @EmbeddedId
    private DataOfferConsentKey key;

    @Column(name = "owner__username")
    private String owner;

    @Column(name = "consent_state")
    private boolean isConsent;

    public DataOfferConsent() {
    }

    public DataOfferConsent(DataOffer dataOffer, String owner) {
        key = new DataOfferConsentKey(dataOffer, owner);
        this.owner = owner;
        isConsent = false;
    }

    public DataOfferConsentKey getKey() {
        return key;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isConsent() {
        return isConsent;
    }

    public void setConsent(boolean consent) {
        isConsent = consent;
    }

    public Object getId(){
        return key;
    }

    public DataOfferConsent persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataOfferConsent updated = session.get(DataOfferConsent.class, getId());
            if (updated == null) updated = this;
            updated.setConsent(isConsent());
            session.persist(updated);
            session.getTransaction().commit();
            return updated;
        }
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataOfferConsent toRemove = session.get(DataOfferConsent.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }
}
