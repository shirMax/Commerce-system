package Domain.User;

import DataLayer.User.ORM.DataMember;
import DataLayer.User.ORM.DataMemberAddress;
import util.Records.AddressRecord;

public class MemberAddress {

  DataMemberAddress dataMemberAddress;

  //CTOR for data creation
  public MemberAddress(DataMember dataMember, AddressRecord addressData) {
    this.dataMemberAddress = new DataMemberAddress(dataMember, addressData, false);
    dataMemberAddress = dataMemberAddress.persist();
  }

  //CTOR for data pulled from DB
  public MemberAddress(DataMemberAddress dataMemberAddress) {
    this.dataMemberAddress = dataMemberAddress;
  }

  @Override
  public String toString() {
    return "MemberAddress{" +
            "fullName='" + getFullName() + '\'' +
            ", street='" + getStreet() + '\'' +
            ", city='" + getCity() + '\'' +
            ", country='" + getCountry() + '\'' +
            ", zip='" + getZip() + '\'' +
            ", phoneNumber='" + getPhoneNumber() + '\'' +
            ", primary=" + getPrimary() +
            '}';
  }

  public int getId() { return dataMemberAddress.getKey().getId(); }
  public String getFullName() { return dataMemberAddress.getFullName(); }

  public void setFullName(String fullName) {
    dataMemberAddress.setFullName(fullName);
    dataMemberAddress = dataMemberAddress.persist();
  }

  public String getStreet() { return dataMemberAddress.getStreet(); }

  public void setStreet(String street) {
    dataMemberAddress.setStreet(street);
    dataMemberAddress = dataMemberAddress.persist();
  }

  public String getCity() { return dataMemberAddress.getCity(); }

  public void setCity(String city) {
    dataMemberAddress.setCity(city);
    dataMemberAddress = dataMemberAddress.persist();
  }

  public String getCountry() { return dataMemberAddress.getCountry(); }

  public void setCountry(String country) {
    dataMemberAddress.setCountry(country);
    dataMemberAddress = dataMemberAddress.persist();
  }

  public String getZip() { return dataMemberAddress.getZip(); }

  public void setZip(String zip) {
    dataMemberAddress.setZip(zip);
    dataMemberAddress = dataMemberAddress.persist();
  }

  public String getPhoneNumber() { return dataMemberAddress.getPhoneNumber(); }

  public void setPhoneNumber(String phoneNumber) {
    dataMemberAddress.setPhoneNumber(phoneNumber);
    dataMemberAddress = dataMemberAddress.persist();
  }

  public Boolean getPrimary() { return dataMemberAddress.isPrimary(); }

  public void setPrimary(boolean primary) {
    dataMemberAddress.setPrimary(primary);
    dataMemberAddress = dataMemberAddress.persist();
  }

  public void update(AddressRecord addressData) {
    dataMemberAddress.update(addressData);
    dataMemberAddress = dataMemberAddress.persist();
  }

  public void remove() {
    dataMemberAddress.remove();
  }
}
