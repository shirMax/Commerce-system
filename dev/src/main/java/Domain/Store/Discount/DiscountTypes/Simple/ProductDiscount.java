package Domain.Store.Discount.DiscountTypes.Simple;

import DataLayer.Store.ORM.Discount.DataProductDiscount;
import DataLayer.Store.ORM.Discount.DataSimpleDiscount;
import Domain.Store.Discount.DiscountTypes.SimpleDiscount;
import Domain.User.IStoreBasket;
import util.Exceptions.DataError;
import util.Records.StoreRecords.ProductRecord;

import java.util.Objects;
import java.util.Set;

public class ProductDiscount extends SimpleDiscount {


    public ProductDiscount(double percentage, int productId) throws DataError {
        super(new DataProductDiscount(percentage/100.0, productId));
    }

    public ProductDiscount(DataProductDiscount dataProductDiscount) throws DataError {
        super(dataProductDiscount);
    }

    @Override
    public void applyDiscountOnBasket(IStoreBasket basket) {
        try {
            for (ProductRecord record : basket.getProductsAsRecords().values()) {
                if (record.productId() == getProductId()) {
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

    public int getProductId() {
        return ((DataProductDiscount)dataSimpleDiscount).getProductID();
    }

    @Override
    public String toString(){
        return "ID: "+getDiscountId()+", " +"Product discount: productID - "+ getProductId() + " -> "+ getPercentage() + "%";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDiscount that = (ProductDiscount) o;
        return Objects.equals(getDiscountId(), that.getDiscountId()) &&
                Objects.equals(getPercentage(), that.getPercentage()) &&
                Objects.equals(getProductId(), that.getProductId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDiscountId(), getPercentage(), getProductId());
    }

    @Override
    public boolean isDependentOnProduct(int productID) {
        return getProductId() == productID;
    }

    @Override
    public Set<Integer> getDependentProducts() {
        return Set.of(getProductId());
    }
}
