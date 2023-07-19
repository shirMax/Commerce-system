package DataLayer.User.ORM;

import DataLayer.DbConfig;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.Session;
import util.Records.AddressRecord;

@Entity
@Table(name = "Address")
public class DataMemberAddress {
        @EmbeddedId
        private DataMemberAddressKey key;
        private String fullName;
        private String street;
        private String city;
        private String country;
        private String zip;
        private String phoneNumber;
        private boolean isPrimary;

        public DataMemberAddress(){}

        public DataMemberAddress(DataMember member, AddressRecord addressData, boolean primary) {
                this.key = new DataMemberAddressKey(member);
                this.fullName = addressData.fullName();
                this.street = addressData.street();
                this.city = addressData.city();
                this.country = addressData.country();
                this.zip = addressData.zip();
                this.phoneNumber = addressData.phoneNumber();
                this.isPrimary = primary;
        }

        public DataMemberAddressKey getKey() {
                return key;
        }

        public String getFullName() {
                return fullName;
        }

        public void setFullName(String fullName) {
                this.fullName = fullName;
        }

        public String getStreet() {
                return street;
        }

        public void setStreet(String street) {
                this.street = street;
        }

        public String getCity() {
                return city;
        }

        public void setCity(String city) {
                this.city = city;
        }

        public String getCountry() {
                return country;
        }

        public void setCountry(String country) {
                this.country = country;
        }

        public String getZip() {
                return zip;
        }

        public void setZip(String zip) {
                this.zip = zip;
        }

        public String getPhoneNumber() {
                return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
        }

        public boolean isPrimary() {
                return isPrimary;
        }

        public void setPrimary(boolean primary) {
                isPrimary = primary;
        }

        public Object getId(){
                return key;
        }

        public void update(AddressRecord addressData) {
                this.fullName = addressData.fullName();
                this.street = addressData.street();
                this.city = addressData.city();
                this.country = addressData.country();
                this.zip = addressData.zip();
                this.phoneNumber = addressData.phoneNumber();
        }

        public DataMemberAddress persist(){
                if (!DbConfig.shouldPersist()) return this;

                try (Session session = DbConfig.getSessionFactory().openSession()){
                        session.beginTransaction();
                        DataMemberAddress updated = session.get(DataMemberAddress.class, getId());
                        if (updated == null) updated = this;
                        updated.setFullName(getFullName());
                        updated.setStreet(getStreet());
                        updated.setCity(getCity());
                        updated.setCountry(getCountry());
                        updated.setZip(getZip());
                        updated.setPhoneNumber(getPhoneNumber());
                        updated.setPrimary(isPrimary());
                        session.persist(updated);
                        session.getTransaction().commit();
                        return updated;
                }
        }

        public void remove() {
                if (!DbConfig.shouldPersist()) return;

                try (Session session = DbConfig.getSessionFactory().openSession()){
                        session.beginTransaction();
                        DataMemberAddress toRemove = session.get(DataMemberAddress.class, getId());
                        if (toRemove == null) return;
                        session.remove(toRemove);
                        session.getTransaction().commit();
                }
        }
}
