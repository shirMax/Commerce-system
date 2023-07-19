package Domain.Store.Discount.DiscountTypes.Composite;

import DataLayer.Store.ORM.Discount.DataAndDiscount;
import Domain.Store.Conditions.Condition;
import Domain.Store.Discount.DiscountTypes.CompositeDiscount;
import Domain.Store.Discount.IDiscount;
import Domain.User.IStoreBasket;
import util.Enums.ErrorStatus;
import util.Exceptions.DataError;

import java.util.Set;
import java.util.StringJoiner;

public class AndDiscount extends CompositeDiscount {
    private final IDiscount discount;

    public AndDiscount(Set<Condition> conditions, IDiscount discount) throws DataError {
        super(new DataAndDiscount(null), conditions, Set.of(discount));
        if (this.conditions.isEmpty())
            throw new DataError("Condition Set cannot be empty", ErrorStatus.EMPTY_COLLECTION);
        this.discount = discount;
    }

    public AndDiscount(DataAndDiscount dataAndDiscount){
        super(dataAndDiscount);
        if (conditions.isEmpty())
            throw new RuntimeException("AndDiscount - failed at recovery, conditions.isEmpty()");
        if (discounts.size() != 1)
            throw new RuntimeException("AndDiscount - failed at recovery, discounts.size()=" + discounts.size());
        discount = discounts.stream().findFirst().get();
    }

    public boolean checkCondition(IStoreBasket basket) {
        for(Condition condition : conditions){
            if(!condition.checkCondition(basket))
                return false;
        }
        return true;
    }

    public void applyDiscountOnBasket(IStoreBasket basket) {
        if(checkCondition(basket))
            discount.applyDiscountOnBasket(basket);
    }

    @Override
    public String toString(){
        StringJoiner stringJoiner = new StringJoiner(" and ");
        for (Condition condition : conditions) {
            stringJoiner.add(condition.toString());
        }
        return "ID: "+getDiscountId()+", " +"If "+stringJoiner +" then apply " + discount;
    }
}
