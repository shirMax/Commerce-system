package util.Records.UserRecords;

import Domain.User.Member;
import Domain.User.MemberAddress;
import util.Records.AddressRecord;
import util.Records.DateRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record UserRecord (String username, String email, String phoneNumber, DateRecord dobRecord, List<AddressRecord> addresses){
    public UserRecord{
        Objects.requireNonNull(username);
        Objects.requireNonNull(email);
        Objects.requireNonNull(phoneNumber);
        Objects.requireNonNull(dobRecord);
    }

    public UserRecord(String username, String email, String phoneNumber, DateRecord date){
        this(username, email, phoneNumber, date, null);
    }
    public UserRecord(String username, String email, String phoneNumber, LocalDate date){
        this(username, email, phoneNumber, new DateRecord(date), null);
    }

    public UserRecord(Member member){
        this(member.getUserName(), member.getEmail(), member.getPhoneNumber(), new DateRecord(member.getBirthday()),
                member.getAllMemberAddresses().stream().map(AddressRecord::new).collect(Collectors.toList()));
    }

    public LocalDate dateOfBirth() {
        return dobRecord.toLocalDate();
    }

    @Override
    public String toString() {
        String info =  "User[username=" + username +", email=" + email + ", phone no.=" + phoneNumber + ", DoB=" + dobRecord +"]";
        StringBuilder fullInfo = new StringBuilder(info);
        if(addresses != null){
            for(AddressRecord add : addresses)
                fullInfo.append(add.toString());
        }
        return fullInfo.toString();
    }
}
