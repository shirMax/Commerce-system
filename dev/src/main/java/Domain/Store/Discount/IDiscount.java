package Domain.Store.Discount;

import DataLayer.Store.ORM.DataStore;
import Domain.Store.Discount.DiscountTypes.CompositeDiscount;
import Domain.User.IStoreBasket;

import java.util.Set;

public interface IDiscount {
    void applyDiscountOnBasket(IStoreBasket basket);

    int getDiscountId();

    void persist(DataStore dataStore); //give the discount a uniq per store ID

    void setFather(CompositeDiscount father);

    void remove();

    boolean isDependentOnProduct(int productID);

    Set<Integer> getDependentProducts();

    Set<Integer> getChildDiscountIds();
}
