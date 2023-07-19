package Domain.Store.Discount.DiscountTypes.Composite;

import DataLayer.Store.ORM.Discount.DataOrDiscount;
import Domain.Store.Conditions.Condition;
import Domain.Store.Discount.DiscountTypes.CompositeDiscount;
import Domain.Store.Discount.IDiscount;
import Domain.User.IStoreBasket;
import util.Enums.ErrorStatus;
import util.Exceptions.DataError;

import java.util.Set;
import java.util.StringJoiner;

public class OrDiscount extends CompositeDiscount {
    private final IDiscount discount;

    public OrDiscount(Set<Condition> conditions, IDiscount discount) throws DataError {
        super(new DataOrDiscount(null), conditions, Set.of(discount));
        this.discount = discount;
        if (this.conditions.isEmpty()) {
            throw new DataError(
                    "Condition Set cannot be empty",
                    ErrorStatus.EMPTY_COLLECTION
            );
        }
    }

    public OrDiscount(DataOrDiscount dataDiscount) {
        super(dataDiscount);
        if (conditions.isEmpty())
            throw new RuntimeException("OrDiscount - failed at recovery, conditions.isEmpty()");
        if (discounts.size() != 1)
            throw new RuntimeException("OrDiscount - failed at recovery, discounts.size()=" + discounts.size());
        discount = discounts.stream().findFirst().get();
    }

    public boolean checkCondition(IStoreBasket basket) {
        for(Condition condition : conditions){
            if(condition.checkCondition(basket))
                return true;
        }
        return false;
    }

    public void applyDiscountOnBasket(IStoreBasket basket) {
        if(checkCondition(basket))
            discount.applyDiscountOnBasket(basket);
    }

    public Set<Condition> getConditions() {
        return conditions;
    }

    @Override
    public String toString(){
        StringJoiner stringJoiner = new StringJoiner(" or ");
        for(Condition condition : conditions) {
            stringJoiner.add(condition.toString());
        }
        return "ID: "+getDiscountId()+", " +"If "+stringJoiner.toString()+" then apply "+discount.toString();
    }
}
