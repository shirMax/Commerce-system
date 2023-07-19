package Domain.Store.Conditions;

import DataLayer.Store.ORM.DataCondition;
import Domain.Store.Category;
import Domain.User.IStoreBasket;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

public class NoAlcoholUnder18 extends Condition {

    //CTOR for data creation
    public NoAlcoholUnder18() {
    }

    //CTOR for recovery
    public NoAlcoholUnder18(DataCondition dataCondition) {
        super(dataCondition);
    }

    @Override
    public boolean checkCondition(IStoreBasket basket) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -18);
        LocalDate date18YearsAgo = calendar.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return (basket.getUserAge().isBefore(date18YearsAgo) ||
                basket.getProductsAsRecords()
                        .values().stream()
                        .noneMatch(record -> record.productCategory() == Category.ALCOHOL));

    }

    @Override
    public String toString(){
        return "NoAlcoholUnder18()";
    }
}
