package util.Records;
import java.time.LocalDate;
import java.util.Objects;

public record DateRecord (int year, int month, Integer day){


    public DateRecord{
        if (month < 1 || month > 12)
            throw new RuntimeException("Month out of range");
        if (day != null && (day < 1 || day > 31))
            throw new RuntimeException("Day out of range");
    }

    public DateRecord(LocalDate date){
        this(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    public DateRecord(int year, int month) {
        this(year, month, null);
    }

    @Override
    public String toString() {
        return day != null ? String.format("%d/%d/%d", day, month, year)
                : String.format("%d/%d", month, year);
    }

    public LocalDate toLocalDate(){
        return day != null ? LocalDate.of(year, month, day)
                : LocalDate.of(year, month, 1);
    }
}