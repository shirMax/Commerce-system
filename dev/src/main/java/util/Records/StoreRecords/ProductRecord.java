package util.Records.StoreRecords;

import Domain.Store.Category;
import Domain.Store.IProduct;
import Domain.User.IBaskedProduct;

import java.util.Objects;

public record ProductRecord(int storeId, int productId, String productName,
                            double productPrice, Category productCategory, int quantity, double priceAfterDiscount, double productRating) {

    public ProductRecord{
        Objects.requireNonNull(productName);
        //Objects.requireNonNull(productCategory);
    }

    /**
     * Constructor only for information needed to create the product.
     */
    public ProductRecord(String productName, double productPrice, Category productCategory, int quantity){
        this(-1, -1, productName, productPrice, productCategory, quantity, productPrice, 0);
    }

    public ProductRecord(int storeId, int productId, String productName, double productPrice, Category productCategory){
        this(storeId, productId, productName, productPrice, productCategory, 0, productPrice, 0);
    }

    public ProductRecord(IProduct product) {
        this(product.getStoreId(), product.getProductId(), product.getProductName(), product.getProductPrice(), product.getProductCategory(), product.getProductQuantity(), product.getProductPrice(), product.getProductRating());
    }

    public ProductRecord(IProduct product, int quantity) {
        this(product.getStoreId(), product.getProductId(), product.getProductName(), product.getProductPrice(), product.getProductCategory(), quantity, product.getProductPrice(), product.getProductRating());
    }

    public ProductRecord(int storeId, String productName, Double productPrice,Category productCategory,int productQuantity) {
        this(storeId, -1, productName, productPrice, productCategory, productQuantity, productPrice, 0);
    }

    public ProductRecord updateName(String newName){
        return new ProductRecord(
                storeId(),
                productId(),
                newName,
                productPrice(),
                productCategory(),
                quantity(),
                priceAfterDiscount(),
                productRating()
        );
    }

    public ProductRecord updatePrice(double newPrice){
        return new ProductRecord(
                storeId(),
                productId(),
                productName(),
                newPrice,
                productCategory(),
                quantity(),
                priceAfterDiscount(),
                productRating()
        );
    }

    public ProductRecord updateCategory(Category newCategory){
        return new ProductRecord(
                storeId(),
                productId(),
                productName(),
                productPrice(),
                newCategory,
                quantity(),
                priceAfterDiscount(),
                productRating()
        );
    }

    public ProductRecord updateQuantity(int newQuantity){
        return new ProductRecord(
                storeId(),
                productId(),
                productName(),
                productPrice(),
                productCategory(),
                newQuantity,
                priceAfterDiscount(),
                productRating()
        );
    }

    public ProductRecord updatePriceAfterDiscount(double newPriceAfterDiscount) {
        return new ProductRecord(
                storeId(),
                productId(),
                productName(),
                productPrice(),
                productCategory(),
                quantity(),
                newPriceAfterDiscount,
                productRating()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductRecord that = (ProductRecord) o;
        return storeId == that.storeId && productId == that.productId && Double.compare(that.productPrice, productPrice) == 0 && quantity == that.quantity && Double.compare(that.priceAfterDiscount, priceAfterDiscount) == 0 && Double.compare(that.productRating, productRating) == 0 && Objects.equals(productName, that.productName) && productCategory == that.productCategory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, productId, productName, productPrice, productCategory, quantity, priceAfterDiscount, productRating);
    }

}