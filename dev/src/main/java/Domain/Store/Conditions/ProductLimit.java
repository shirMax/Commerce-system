package Domain.Store.Conditions;

import DataLayer.Store.ORM.DataCondition;
import Domain.User.IStoreBasket;
import util.Records.StoreRecords.ProductRecord;

public class ProductLimit extends Condition{

    private final int productID;
    private final int quantity;

    // CTOR for data creation
    public ProductLimit(int productID, int quantity) {
        this.productID = productID;
        this.quantity = quantity;
    }

    // CTOR for recovery
    public ProductLimit(DataCondition dataCondition) {
        super(dataCondition);
        productID = dataCondition.getProductID();
        quantity = dataCondition.getQuantity();
    }

    @Override
    public int getProductID() {
        return productID;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean checkCondition(IStoreBasket basket) {
        return basket.getProductsAsRecords().values().stream()
                .filter(record -> record.productId() == productID)
                .mapToInt(ProductRecord::quantity)
                .sum() <= quantity;
    }

    @Override
    public String toString(){
        return String.format("ProductLimit(%d, %d)", getProductID(), getQuantity());
    }
}
