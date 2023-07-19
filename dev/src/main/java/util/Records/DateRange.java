package util.Records;

import java.time.LocalDate;

public record DateRange(DateRecord earliest, DateRecord latest) {
    public DateRange{
        if (earliest != null && latest != null && earliest.toLocalDate().isAfter(latest.toLocalDate()))
            throw new IllegalArgumentException("First date has to come before second date");
    }

    public DateRange(LocalDate earliest, LocalDate latest){
        this(   earliest == null ? null : new DateRecord(earliest),
                latest == null ? null : new DateRecord(latest));
    }
}
