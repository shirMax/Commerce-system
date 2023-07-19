package DataLayer.Store.ORM.Contract;

import DataLayer.DbConfig;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.Session;

@Entity
@Table(name = "Appointment_Consent")
public class DataAppointConsent {
    @EmbeddedId
    private DataAppointConsentKey key;

    @Column(name = "owner__username")
    private String owner;

    @Column(name = "consent_state")
    private boolean isConsent;

    public DataAppointConsent() {
    }

    public DataAppointConsent(DataAppointment dataAppointment, String owner) {
        key = new DataAppointConsentKey(dataAppointment, owner);
        this.owner = owner;
        isConsent = false;
    }

    public DataAppointConsentKey getKey() {
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

    public DataAppointConsent persist(){
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()){
            session.beginTransaction();
            DataAppointConsent updated = session.get(DataAppointConsent.class, getId());
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
            DataAppointConsent toRemove = session.get(DataAppointConsent.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }
}
