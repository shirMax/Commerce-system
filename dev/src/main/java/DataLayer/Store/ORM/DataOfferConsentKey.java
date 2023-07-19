package DataLayer.Store.ORM;

import DataLayer.DbConfig;
import DataLayer.User.ORM.DataMember;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.io.Serializable;

@Embeddable
public class DataOfferConsentKey implements Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_username")
    private DataMember owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "offer_id", referencedColumnName = "offer_id"),
            @JoinColumn(name = "offering_member", referencedColumnName = "offering_member"),
            @JoinColumn(name = "product_id", referencedColumnName = "product_id"),
            @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    })
    private DataOffer offer;


    public DataOfferConsentKey(){
    }

    public DataOfferConsentKey(DataOffer dataOffer, String owner) {
        offer = dataOffer;
        if (DbConfig.shouldPersist()) {
            try (Session session = DbConfig.getSessionFactory().openSession()){
                this.owner = session.get(DataMember.class, owner);
            }
        }
    }

    public DataMember getOwner() {
        return owner;
    }

    public DataOffer getOffer() {
        return offer;
    }
}
