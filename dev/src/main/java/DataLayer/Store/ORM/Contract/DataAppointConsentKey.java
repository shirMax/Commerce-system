package DataLayer.Store.ORM.Contract;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataOffer;
import DataLayer.User.ORM.DataMember;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.io.Serializable;

@Embeddable
public class DataAppointConsentKey implements Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_username")
    private DataMember owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private DataAppointment appointment;

    public DataAppointConsentKey(){
    }

    public DataAppointConsentKey(DataAppointment appointment, String owner) {
        this.appointment = appointment;
        if (DbConfig.shouldPersist()) {
            try (Session session = DbConfig.getSessionFactory().openSession()){
                this.owner = session.get(DataMember.class, owner);
            }
        }
    }

    public DataMember getOwner() {
        return owner;
    }

    public DataAppointment getAppointment() {
        return appointment;
    }
}
