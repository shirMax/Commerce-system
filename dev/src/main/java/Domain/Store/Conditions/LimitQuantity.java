package Domain.Store.Conditions;

import DataLayer.Store.ORM.DataCondition;
import Domain.User.IStoreBasket;
import util.Records.StoreRecords.ProductRecord;

public class LimitQuantity extends Condition{

    private final int quantity;

    // CTOR for data creation
    public LimitQuantity(int quantity) {
        this.quantity = quantity;
    }

    // CTOR for recovery
    public LimitQuantity(DataCondition dataCondition) {
        super(dataCondition);
        this.quantity = dataCondition.getQuantity();
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean checkCondition(IStoreBasket basket) {
        return basket.getProductsAsRecords().values().stream()
                .mapToInt(ProductRecord::quantity)
                .sum() <= quantity;
    }

    @Override
    public String toString(){
        return String.format("LimitQuantity(%d)", getQuantity());
    }
}
