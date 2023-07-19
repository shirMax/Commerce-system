package Domain.Store.Discount.DiscountTypes.Simple;

import DataLayer.Store.ORM.Discount.DataSimpleDiscount;
import DataLayer.Store.ORM.Discount.DataStoreDiscount;
import Domain.Store.Discount.DiscountTypes.SimpleDiscount;
import Domain.User.IStoreBasket;
import util.Exceptions.DataError;
import util.Records.StoreRecords.ProductRecord;

import java.util.Objects;

public class StoreDiscount extends SimpleDiscount {

    public StoreDiscount(double percentage) throws DataError {
        super(new DataStoreDiscount(percentage/100.));
    }

    public StoreDiscount(DataStoreDiscount dataStoreDiscount) throws DataError {
        super(dataStoreDiscount);
    }

    @Override
    public void applyDiscountOnBasket(IStoreBasket basket) {
        for (ProductRecord record : basket.getProductsAsRecords().values()) {
            ProductRecord discountRecord =
                    record.updatePriceAfterDiscount(
                            (1-getPercentage()) * record.priceAfterDiscount()
                    );
            basket.updateProductRecord(discountRecord);
        }
    }

    @Override
    public String toString(){
        return "ID: "+getDiscountId()+", " +"Store discount: -> " + getPercentage() + "%";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreDiscount that = (StoreDiscount) o;
        return Objects.equals(getDiscountId(), that.getDiscountId()) &&
                Objects.equals(getPercentage(), that.getPercentage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDiscountId(), getPercentage());
    }
}
