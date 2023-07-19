package Domain.User;

import util.Records.StoreRecords.ProductRecord;

public interface IBaskedProduct {
    int getProductId();

    double getPrice();

    int getQuantity();

    double getPriceAfterDiscount();

    void remove();

    ProductRecord getAsRecord();

    void update(ProductRecord newState);
}
