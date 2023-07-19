package Domain.Store.Discount;

import DataLayer.Store.ORM.Discount.*;
import Domain.Store.Discount.DiscountTypes.Composite.*;
import Domain.Store.Discount.DiscountTypes.Simple.CategoryDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.ProductDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.StoreDiscount;
import util.Exceptions.DataError;

public class DiscountFactory {
    public static IDiscount recover(DataDiscount dataDiscount) {
        return dataDiscount.recover();
    }

    public static IDiscount recover(DataAddDiscount dataDiscount) {
        return new AddDiscount(dataDiscount);
    }
    public static IDiscount recover(DataAndDiscount dataDiscount) {
        return new AndDiscount(dataDiscount);
    }
    public static IDiscount recover(DataCategoryDiscount dataDiscount) {
        try {
            return new CategoryDiscount(dataDiscount);
        } catch (DataError e) {
            throw new RuntimeException(e);
        }
    }
    public static IDiscount recover(DataIfThenDiscount dataDiscount) {
        return new IfThenDiscount(dataDiscount);
    }
    public static IDiscount recover(DataMaxDiscount dataDiscount) {
        return new MaxDiscount(dataDiscount);
    }
    public static IDiscount recover(DataOrDiscount dataDiscount) {
        return new OrDiscount(dataDiscount);
    }
    public static IDiscount recover(DataProductDiscount dataDiscount) {
        try {
            return new ProductDiscount(dataDiscount);
        } catch (DataError e) {
            throw new RuntimeException(e);
        }
    }
    public static IDiscount recover(DataStoreDiscount dataDiscount) {
        try {
            return new StoreDiscount(dataDiscount);
        } catch (DataError e) {
            throw new RuntimeException(e);
        }
    }
    public static IDiscount recover(DataXorDiscount dataDiscount) {
        return new XorDiscount(dataDiscount);
    }
}
