package Domain.Store.Conditions;

import DataLayer.Store.ORM.DataCondition;
import Domain.Store.Category;
import Domain.User.IStoreBasket;
import java.time.LocalTime;

public class NoAlcoholAtNight extends Condition{
    private final static LocalTime START_TIME = LocalTime.of(6, 0); // 6:00 AM
    private final static LocalTime END_TIME = LocalTime.of(23, 0); // 11:00 PM

    // CTOR for data creation
    public NoAlcoholAtNight() {
    }

    // CTOR for recovery
    public NoAlcoholAtNight(DataCondition dataCondition) {
        super(dataCondition);
    }

    @Override
    public boolean checkCondition(IStoreBasket basket) {
        LocalTime currentTime = LocalTime.now();
        return ((currentTime.isAfter(START_TIME) && currentTime.isBefore(END_TIME)) ||
                basket.getProductsAsRecords()
                        .values().stream()
                        .noneMatch(record -> record.productCategory() == Category.ALCOHOL));
    }

    @Override
    public String toString(){
        return "NoAlcoholAtNight()";
    }
}
