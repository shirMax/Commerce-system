package Domain.Store;

import DataLayer.Store.ORM.DataProduct;
import DataLayer.Store.ORM.DataStore;
import util.Enums.ErrorStatus;
import util.Exceptions.DataError;
import util.Records.StoreRecords.ProductRecord;

public class Product implements IProduct {
    private int storeId;
    private DataProduct dataProduct;

    // CTOR for data creation
    public Product(DataStore dataStore, ProductRecord productRecord) {
        dataProduct = new DataProduct(
                dataStore,
                productRecord.productName(),
                productRecord.productPrice(),
                productRecord.productCategory(),
                productRecord.quantity(),
                0
        );
        dataProduct = dataProduct.persist();
        storeId = dataStore.getId();
    }

    // CTOR for recovery from DB
    public Product(DataProduct dataProduct, int storeId) {
        this.dataProduct = dataProduct;
        this.storeId = storeId;
    }

    @Override
    public int getStoreId() {
        return storeId;
    }

    @Override
    public int getProductId() {
        return dataProduct.getKey().getId();
    }

    @Override
    public String getProductName() {
        return dataProduct.getName();
    }

    @Override
    public void setProductName(String name) {
        dataProduct.setName(name);
        dataProduct = dataProduct.persist();
    }

    @Override
    public double getProductPrice() {
        return dataProduct.getPrice();
    }

    @Override
    public void setProductPrice(double price) {
        dataProduct.setPrice(price);
        dataProduct = dataProduct.persist();
    }

    @Override
    public Category getProductCategory() {
        return dataProduct.getCategory();
    }

    public void setProductCategory(Category newCategory) {
        dataProduct.setCategory(newCategory);
        dataProduct = dataProduct.persist();
    }

    @Override
    public synchronized int getProductQuantity() {
        return dataProduct.getQuantity();
    }

    @Override
    public void reduceProductQuantity(int quantity) throws DataError {
        if (quantity < 0)
            throw new DataError(
                    "try to reduce quantity with negative value",
                    ErrorStatus.INVALID_PRODUCT_QUANTITY
            );
        int updatedQuantity;
        do {
            updatedQuantity = getProductQuantity() - quantity;
            if (updatedQuantity < 0)
                throw new DataError(
                        "Quantity to remove is greater than available quantity for product with ID " + getProductId(),
                        ErrorStatus.INVALID_PRODUCT_QUANTITY
                );
        } while (updatedQuantity != getProductQuantity() - quantity);

        dataProduct.setQuantity(updatedQuantity);
        dataProduct = dataProduct.persist();
    }

    @Override
    public synchronized void addingProductQuantity(int quantity) throws DataError {
        if (quantity < 0)
            throw new DataError(
                    "try to add quantity with negative value",
                    ErrorStatus.INVALID_PRODUCT_QUANTITY
            );

        dataProduct.setQuantity(getProductQuantity() + quantity);
        dataProduct = dataProduct.persist();
    }

    @Override
    public double getProductRating() {
        return dataProduct.getRating();
    }

    @Override
    public void setProductRating(double newRating) {
        dataProduct.setRating(newRating);
        dataProduct = dataProduct.persist();
    }

    @Override
    public synchronized void update(ProductRecord updated) {
        dataProduct.setName(updated.productName());
        dataProduct.setPrice(updated.productPrice());
        dataProduct.setCategory(updated.productCategory());
        dataProduct = dataProduct.persist();
    }

    @Override
    public void remove() {
        dataProduct.remove();
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + getProductId() +
                ", productName='" + getProductName() + '\'' +
                ", productPrice=" + getProductPrice() +
                ", productCategory='" + getProductCategory() + '\'' +
                ", quantity=" + getProductQuantity() +
                ", rating=" + getProductRating() +
                '}';
    }
}

