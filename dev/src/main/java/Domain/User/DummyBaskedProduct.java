package Domain.User;

import util.Records.StoreRecords.ProductRecord;

/**
 * BaskedProduct that doesn't persist to DB
 */
public class DummyBaskedProduct implements IBaskedProduct {
    private ProductRecord currentState;

    // New data
    public DummyBaskedProduct(ProductRecord product) {
        this.currentState = product;
    }

    // Deep copy CTOR
    public DummyBaskedProduct(IBaskedProduct toCopy) {
        this.currentState = toCopy.getAsRecord();
    }

    @Override
    public int getProductId() {
        return currentState.productId();
    }

    @Override
    public double getPrice() {
        return currentState.productPrice();
    }

    @Override
    public int getQuantity() {
        return currentState.quantity();
    }

    @Override
    public double getPriceAfterDiscount() {
        return currentState.priceAfterDiscount();
    }

    @Override
    public void remove() {
    }

    @Override
    public ProductRecord getAsRecord() {
        return currentState;
    }

    @Override
    public void update(ProductRecord newState) {
        currentState = new ProductRecord(currentState.storeId(), currentState.productId(), currentState.productName(),
                currentState.productPrice(), currentState.productCategory(), newState.quantity(),
                newState.priceAfterDiscount(), currentState.productRating());
    }
}
