package Domain.Store.Discount.DiscountTypes.Simple;

import DataLayer.Store.ORM.Discount.DataCategoryDiscount;
import Domain.Store.Category;
import Domain.Store.Discount.DiscountTypes.SimpleDiscount;
import Domain.User.IStoreBasket;
import util.Exceptions.DataError;
import util.Records.StoreRecords.ProductRecord;

import java.util.Objects;

public class CategoryDiscount extends SimpleDiscount {

    public CategoryDiscount(double percentage, Category category) throws DataError {
        super(new DataCategoryDiscount(percentage/100.0, category));
    }

    public CategoryDiscount(DataCategoryDiscount dataDiscount) throws DataError {
        super(dataDiscount);
    }

    @Override
    public void applyDiscountOnBasket(IStoreBasket basket) {
        try {
            for (ProductRecord record : basket.getProductsAsRecords().values()) {
                if (record.productCategory() == getCategory()) {
                    ProductRecord discountRecord =
                            record.updatePriceAfterDiscount(
                                    (1-getPercentage()) * record.priceAfterDiscount()
                            );
                    basket.updateProductRecord(discountRecord);
                }
            }
        }
        catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public Category getCategory() {
        return ((DataCategoryDiscount)dataSimpleDiscount).getCategory();
    }

    @Override
    public String toString(){
        return "ID: "+getDiscountId()+", " +"Category discount: "+ getCategory().toString() + "-> "+ getPercentage() + "%";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryDiscount that = (CategoryDiscount) o;
        return Objects.equals(getDiscountId(), that.getDiscountId()) &&
                Objects.equals(getPercentage(), that.getPercentage()) &&
                Objects.equals(getCategory(), that.getCategory());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDiscountId(), getPercentage(), getCategory());
    }
}
