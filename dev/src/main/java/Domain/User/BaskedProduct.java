package Domain.User;

import DataLayer.User.ORM.DataBaskedProduct;
import DataLayer.User.ORM.DataBasket;
import Domain.Store.Category;
import util.Records.StoreRecords.ProductRecord;

public class BaskedProduct implements IBaskedProduct {
    private ProductRecord currentState;
    private DataBaskedProduct dataBaskedProduct;

    // CTOR for data creation
    public BaskedProduct(DataBasket dataBasket, ProductRecord product) {
        this.currentState = product;
        this.dataBaskedProduct = new DataBaskedProduct(dataBasket, product);
        this.dataBaskedProduct = this.dataBaskedProduct.persist();
    }

    // Recover from DB
    public BaskedProduct(DataBaskedProduct dataBaskedProduct) {
        int storeId = dataBaskedProduct.getKey().getProduct().getKey().getStore().getId();
        int productId = dataBaskedProduct.getKey().getProduct().getKey().getId();
        String productName = dataBaskedProduct.getKey().getProduct().getName();
        double productPrice = dataBaskedProduct.getKey().getProduct().getPrice();
        Category productCategory = dataBaskedProduct.getKey().getProduct().getCategory();
        int quantity = dataBaskedProduct.getQuantity();
        double priceAfterDiscount = dataBaskedProduct.getPriceAfterDiscount();
        double productRating = dataBaskedProduct.getKey().getProduct().getRating();
        this.currentState = new ProductRecord(storeId, productId, productName, productPrice, productCategory, quantity, priceAfterDiscount, productRating);
        this.dataBaskedProduct = dataBaskedProduct;
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
        dataBaskedProduct.remove();
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

        dataBaskedProduct.setQuantity(currentState.quantity());
        dataBaskedProduct.setPriceAfterDiscount(newState.priceAfterDiscount());
        dataBaskedProduct = dataBaskedProduct.persist();
    }
}
