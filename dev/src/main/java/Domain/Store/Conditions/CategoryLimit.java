package Domain.Store.Conditions;

import DataLayer.Store.ORM.DataCondition;
import Domain.Store.Category;
import Domain.User.IStoreBasket;
import util.Records.StoreRecords.ProductRecord;

public class CategoryLimit extends Condition {

    private final Category category;
    private final int quantity;

    // CTOR for data creation
    public CategoryLimit(Category category, int quantity) {
        this.category = category;
        this.quantity = quantity;
    }

    // CTOR for recovery
    public CategoryLimit(DataCondition dataCondition) {
        super(dataCondition);
        category = dataCondition.getCategory();
        quantity = dataCondition.getQuantity();
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean checkCondition(IStoreBasket basket) {
        return basket.getProductsAsRecords().values().stream()
                .filter(record -> record.productCategory() == category)
                .mapToInt(ProductRecord::quantity)
                .sum() <= quantity;
    }

    @Override
    public String toString(){
        return String.format("CategoryLimit(%s, %d)", getCategory(), getQuantity());
    }
}
