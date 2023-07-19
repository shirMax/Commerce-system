package DataLayer.User.ORM;

import DataLayer.DbConfig;
import DataLayer.ORM.DataPermission;
import DataLayer.ORM.DataTransaction;
import DataLayer.Services.NotificationService.ORM.DataNotification;
import DataLayer.Store.ORM.Contract.DataAppointConsent;
import DataLayer.Store.ORM.Contract.DataAppointment;
import DataLayer.Store.ORM.DataOffer;
import DataLayer.Store.ORM.DataOfferConsent;
import jakarta.persistence.*;
import org.hibernate.Session;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "Member")
public class DataMember {
    @Id
    private String username;
    private String password;
    private String email;
    private String phone_no;
    private LocalDate birthday;
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DataCart cart;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "key.member", fetch = FetchType.LAZY)
    @MapKey(name = "key.id")
    private Map<Integer, DataMemberAddress> addresses;

    @OneToMany(mappedBy = "key.member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DataPermission> permissions;

    @OneToMany(mappedBy = "key.member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DataNotification> notifications;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DataTransaction> transactions;
    @OneToMany(mappedBy = "key.offeringMember", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DataOffer> offers;
    @OneToMany(mappedBy = "key.owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DataOfferConsent> offerConsents;
    @OneToMany(mappedBy = "assigner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DataAppointment> createdContracts;
    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DataAppointment> appointingContracts;
    @OneToMany(mappedBy = "key.owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DataAppointConsent> appointConsents;

    public DataMember() {
    }

    public DataMember(UserRecord userData, String password) {
        this.username = userData.username();
        this.password = password;
        this.email = userData.email();
        this.phone_no = userData.phoneNumber();
        this.birthday = userData.dateOfBirth();
    }

    public String getUsername() {
        return username;
    }

    public DataCart getCart() {
        return cart;
    }

    public void setCart(DataCart cart) {
        this.cart = cart;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Map<Integer, DataMemberAddress> getAddresses() {
        return addresses;
    }

    public Set<DataPermission> getPermissions() {
        return permissions;
    }

    public Object getId() {
        return username;
    }

    @Override
    public String toString() {
        return "Member{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", phone_no='" + phone_no + '\'' +
                ", birthday=" + birthday +
                '}';
    }

    public DataMember persist() {
        if (!DbConfig.shouldPersist()) return this;

        try (Session session = DbConfig.getSessionFactory().openSession()) {
            session.beginTransaction();
            DataMember updated = session.get(DataMember.class, getId());
            if (updated == null) updated = this;
            updated.setPassword(getPassword());
            updated.setEmail(getEmail());
            updated.setPhone_no(getPhone_no());
            updated.setBirthday(getBirthday());
            session.persist(updated);
            session.getTransaction().commit();
            return updated;
        }
    }

    public void remove() {
        if (!DbConfig.shouldPersist()) return;

        try (Session session = DbConfig.getSessionFactory().openSession()) {
            session.beginTransaction();
            DataMember toRemove = session.get(DataMember.class, getId());
            if (toRemove == null) return;
            session.remove(toRemove);
            session.getTransaction().commit();
        }
    }

    public DataMember cleanCart() {
        if (!DbConfig.shouldPersist()) {
            setCart(new DataCart(this));
            return this;
        }

        try (Session session = DbConfig.getSessionFactory().openSession()) {
            session.beginTransaction();
            DataMember clean = session.get(DataMember.class, getId());
            DataCart toRemove = clean.getCart();
            clean.setCart(null);
            session.remove(toRemove);
            DataCart newCart = new DataCart(clean);
            session.persist(newCart);
            session.getTransaction().commit();
            clean.setCart(newCart);
            return clean;
        }
    }
}
