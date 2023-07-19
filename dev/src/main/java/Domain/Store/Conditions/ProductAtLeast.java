package Domain.Store.Conditions;

import DataLayer.Store.ORM.DataCondition;
import Domain.User.IStoreBasket;
import util.Records.StoreRecords.ProductRecord;

public class ProductAtLeast extends Condition {

    private final int productId;
    private final int quantity;

    // CTOR for data creation
    public ProductAtLeast(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // CTOR for recovery
    public ProductAtLeast(DataCondition dataCondition) {
        super(dataCondition);
        this.productId = dataCondition.getProductID();
        this.quantity = dataCondition.getQuantity();
    }

    @Override
    public int getProductID() {
        return productId;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean checkCondition(IStoreBasket basket) {
        return basket.getProductsAsRecords().values().stream()
                .filter(record -> record.productId() == productId)
                .mapToInt(ProductRecord::quantity)
                .sum() >= quantity;
    }

    @Override
    public String toString(){
        return String.format("ProductAtLeast(%d, %d)", getProductID(), getQuantity());
    }
}
