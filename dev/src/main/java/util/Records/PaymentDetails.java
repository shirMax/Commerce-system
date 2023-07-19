package util.Records;

import java.time.LocalDate;
import java.util.Objects;

public record PaymentDetails(String card_owner, String card_number, DateRecord expiry_date, String cvv, Double price, Long id) {
    public PaymentDetails{
        Objects.requireNonNull(card_owner);
        Objects.requireNonNull(card_number);
        Objects.requireNonNull(expiry_date);
        Objects.requireNonNull(cvv);
    }

    public PaymentDetails(String card_owner, String card_number, DateRecord expiry_date, String cvv, Double price){
        this(card_owner, card_number, expiry_date, cvv, price, null);
    }

    public PaymentDetails(String card_owner, String card_number, DateRecord expiry_date, String cvv){
        this(card_owner, card_number, expiry_date, cvv, null, null);
    }


    public PaymentDetails(String card_owner, String card_number, LocalDate expiry_date, String cvv){
        this(card_owner, card_number, new DateRecord(expiry_date.getYear(), expiry_date.getMonthValue()), cvv);
    }

    public PaymentDetails setPrice(double price){
        return new PaymentDetails(card_owner, card_number, expiry_date, cvv, price, id);
    }

    public PaymentDetails setID(Long id){
        return new PaymentDetails(card_owner, card_number, expiry_date, cvv, price, id);
    }
}
