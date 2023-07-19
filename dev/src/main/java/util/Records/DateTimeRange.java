package util.Records;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DateTimeRange (LocalDateTime earliest, LocalDateTime latest) {
    public DateTimeRange{
        if (earliest != null && latest != null && earliest.isAfter(latest))
            throw new IllegalArgumentException("First date has to come before second date");
    }

    public boolean isInRange(LocalDateTime time) {
        return (earliest == null || earliest.isBefore(time)) && (latest == null || time.isBefore(latest));
    }
}
