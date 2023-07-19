package util.Records;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record NotificationRecord(String sender, String recipient, LocalDateTime sendingTime, String message) implements Comparable<NotificationRecord>{
    public NotificationRecord{
        Objects.requireNonNull(sender);
        Objects.requireNonNull(recipient);
        Objects.requireNonNull(sendingTime);
        Objects.requireNonNull(message);
    }

    @Override
    public int compareTo(NotificationRecord o) {
        return sendingTime().compareTo(o.sendingTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationRecord that = (NotificationRecord) o;
        return Objects.equals(sender, that.sender) &&
                Objects.equals(recipient, that.recipient) &&
                Objects.equals(sendingTime.truncatedTo(ChronoUnit.SECONDS), that.sendingTime.truncatedTo(ChronoUnit.SECONDS)) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, recipient, sendingTime, message);
    }
}





