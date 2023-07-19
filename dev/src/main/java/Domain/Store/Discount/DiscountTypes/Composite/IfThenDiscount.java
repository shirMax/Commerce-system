package Domain.Store.Discount.DiscountTypes.Composite;

import DataLayer.Store.ORM.Discount.DataIfThenDiscount;
import Domain.Store.Conditions.Condition;
import Domain.Store.Discount.DiscountTypes.CompositeDiscount;
import Domain.Store.Discount.IDiscount;
import Domain.User.IStoreBasket;

import java.util.Set;

public class IfThenDiscount extends CompositeDiscount {

    final Condition condition;
    final IDiscount discount;

    public IfThenDiscount(Condition test, IDiscount then){
        super(new DataIfThenDiscount(null), Set.of(test), Set.of(then));
        condition = test;
        discount = then;
    }

    public IfThenDiscount(DataIfThenDiscount dataDiscount) {
        super(dataDiscount);
        if (conditions.size() != 1)
            throw new RuntimeException("IfThenDiscount - failed at recovery, conditions.size()=" + conditions.size());
        if (discounts.size() != 1)
            throw new RuntimeException("IfThenDiscount - failed at recovery, discounts.size()=" + discounts.size());
        condition = conditions.stream().findFirst().get();
        discount = discounts.stream().findFirst().get();
    }

    public void applyDiscountOnBasket(IStoreBasket basket) {
        if(condition.checkCondition(basket))
            discount.applyDiscountOnBasket(basket);
    }

    @Override
    public String toString(){
        return "ID: "+getDiscountId()+", " +"if " + condition + " then " + discount;
    }
}
