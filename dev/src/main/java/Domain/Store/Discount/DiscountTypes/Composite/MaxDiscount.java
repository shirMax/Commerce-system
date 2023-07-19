package Domain.Store.Discount.DiscountTypes.Composite;

import DataLayer.Store.ORM.Discount.DataMaxDiscount;
import Domain.Store.Discount.DiscountTypes.CompositeDiscount;
import Domain.Store.Discount.IDiscount;
import Domain.User.IStoreBasket;
import util.Enums.ErrorStatus;
import util.Exceptions.DataError;

import java.util.Set;

public class MaxDiscount extends CompositeDiscount {

    public MaxDiscount(Set<IDiscount> discounts) throws DataError {
        super(new DataMaxDiscount(null), Set.of(), discounts);
        if (this.discounts.isEmpty()) {
            throw new DataError("Discount set cannot be empty", ErrorStatus.EMPTY_COLLECTION);
        }
    }

    public MaxDiscount(DataMaxDiscount dataDiscount) {
        super(dataDiscount);
        if (this.discounts.isEmpty()) {
            throw new RuntimeException("MaxDiscount - failed at recovery, discounts.isEmpty()");
        }
    }

    @Override
    public void applyDiscountOnBasket(IStoreBasket basket) {
        IDiscount maxDiscount = null;
        double basketPriceAfterDiscount = Double.MAX_VALUE;
        for(IDiscount discount : discounts){
            IStoreBasket clonedBasket = basket.clone();
            discount.applyDiscountOnBasket(clonedBasket);
            if(basketPriceAfterDiscount > clonedBasket.getBasketPriceAfterDiscount()){
                basketPriceAfterDiscount = clonedBasket.getBasketPriceAfterDiscount();
                maxDiscount = discount;
            }
        }
        if (maxDiscount != null) maxDiscount.applyDiscountOnBasket(basket);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Max {");
        for (IDiscount predicate : discounts) {
            stringBuilder.append(predicate.toString()).append(", ");
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());  // Remove the extra ", "
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
