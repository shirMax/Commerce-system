package util.Records;

import Domain.User.MemberAddress;

import java.util.Objects;

public record AddressRecord(String fullName, String street, String city, String country, String zip,
                            String phoneNumber) {
    public AddressRecord {
        Objects.requireNonNull(fullName);
        Objects.requireNonNull(street);
        Objects.requireNonNull(city);
        Objects.requireNonNull(country);
        Objects.requireNonNull(zip);
        Objects.requireNonNull(phoneNumber);
    }

    public AddressRecord(MemberAddress address) {
        this(address.getFullName(), address.getStreet(), address.getCity(), address.getCountry(), address.getZip(), address.getPhoneNumber());
    }

}
