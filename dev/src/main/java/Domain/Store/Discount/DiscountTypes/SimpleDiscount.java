package Domain.Store.Discount.DiscountTypes;

import DataLayer.Store.ORM.DataStore;
import DataLayer.Store.ORM.Discount.DataSimpleDiscount;
import Domain.MarketLogger;
import Domain.Store.Discount.IDiscount;
import util.Enums.ErrorStatus;
import util.Exceptions.DataError;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class SimpleDiscount implements IDiscount {
    protected DataSimpleDiscount dataSimpleDiscount;

    // CTOR for data creation
    protected SimpleDiscount(DataSimpleDiscount dataSimpleDiscount) throws DataError {
        checkPercentage(dataSimpleDiscount.getPercentage());
        this.dataSimpleDiscount = dataSimpleDiscount;
    }

    private void checkPercentage (double percentage) throws DataError {
        if(percentage < 0 || percentage >1){
            MarketLogger.logError("SimpleDiscount", "checkPercentage", String.format("The discount percentage - %f, are not in range", percentage));
            throw new DataError(
                    "can't add discount with illegal percentage:" + percentage,
                    ErrorStatus.INVALID_PERCENTAGE
            );
        }
    }

    public double getPercentage() {
        return dataSimpleDiscount.getPercentage();
    }

    @Override
    public int getDiscountId() {
        return dataSimpleDiscount.getId();
    }

    @Override
    public void setFather(CompositeDiscount father) {
        dataSimpleDiscount.setFather(father.dataCompositeDiscount);
    }

    @Override
    public void persist(DataStore dataStore) {
        dataSimpleDiscount.setStore(dataStore);
        dataSimpleDiscount = (DataSimpleDiscount) dataSimpleDiscount.persist();
    }

    @Override
    public void remove() {
        dataSimpleDiscount.remove();
    }

    @Override
    public boolean isDependentOnProduct(int productID) {
        return false;
    }

    @Override
    public Set<Integer> getDependentProducts() {
        return Set.of();
    }

    @Override
    public Set<Integer> getChildDiscountIds() {
        return Set.of();
    }
}
