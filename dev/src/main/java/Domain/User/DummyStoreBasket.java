package Domain.User;

import DataLayer.Store.ORM.DataProduct;
import DataLayer.User.ORM.DataBaskedProduct;
import DataLayer.User.ORM.DataBasket;
import DataLayer.User.ORM.DataCart;
import org.checkerframework.com.google.common.util.concurrent.AtomicDouble;
import util.Enums.ErrorStatus;
import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.ProductRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DummyStoreBasket implements IStoreBasket{
    private final Map<Integer, IBaskedProduct> products; // <product ID, product record>
    private final int storeId;
    private AtomicDouble basketRegularPrice = new AtomicDouble(0);
    private AtomicDouble basketDiscountPrice = new AtomicDouble(0);
    private LocalDate userAge;

    public DummyStoreBasket(int storeId) {
        this.storeId = storeId;
        this.userAge = LocalDate.now();
        this.products = new ConcurrentHashMap<>();
    }

    public DummyStoreBasket(int storeId, LocalDate userAge) {
        this.storeId = storeId;
        this.userAge = userAge;
        this.products = new ConcurrentHashMap<>();
    }

    public DummyStoreBasket(int storeId, LocalDate userAge, Collection<ProductRecord> products) {
        this.storeId = storeId;
        this.userAge = userAge;
        this.products = products.stream()
                .map(DummyBaskedProduct::new)
                .collect(Collectors.toMap(IBaskedProduct::getProductId, Function.identity()));
    }

    public DummyStoreBasket(IStoreBasket toCopy){
        this.storeId = toCopy.getStoreId();
        this.userAge = toCopy.getUserAge();
        this.products = toCopy.getProductsAsRecords().values().stream()
                .map(DummyBaskedProduct::new)
                .collect(Collectors.toMap(IBaskedProduct::getProductId, Function.identity()));
    }

    @Override
    public int getStoreId() {
        return storeId;
    }

    @Override
    public Map<Integer, IBaskedProduct> getProducts() {
        return products;
    }

    @Override
    public Map<Integer, ProductRecord> getProductsAsRecords() {
        return products
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsRecord()));
    }

    @Override
    public void addProduct(ProductRecord product) {
        if (products.containsKey(product.productId())) {
            product = product.updateQuantity(product.quantity() + this.products.get(product.productId()).getQuantity());
            updateProductRecord(product);
        }
        else {
            products.put(product.productId(), new DummyBaskedProduct(product));
            basketRegularPrice.addAndGet(product.productPrice() * product.quantity());
            basketDiscountPrice.addAndGet(product.priceAfterDiscount() * product.quantity());
        }
    }

    @Override
    public void updateProductRecord(ProductRecord newState) {
        if(products.containsKey(newState.productId())) {
            IBaskedProduct product = products.get(newState.productId());
            ProductRecord oldState = product.getAsRecord();
            product.update(newState);

            double priceDif = (newState.productPrice() * newState.quantity()) - (oldState.productPrice() * oldState.quantity());
            double priceDiscountDif = newState.priceAfterDiscount() * newState.quantity() - oldState.priceAfterDiscount() * oldState.quantity();
            basketRegularPrice.addAndGet(priceDif);
            basketDiscountPrice.addAndGet(priceDiscountDif);
        }
    }

    @Override
    public void removeProduct(int productId) throws NonExistentData {
        if (!isProductExists(productId))
            throw new NonExistentData("can't remove the product because he is not in the store basket!", ErrorStatus.PRODUCT_DOES_NOT_EXIST);

        ProductRecord oldRecord = products.remove(productId).getAsRecord();
        basketRegularPrice.addAndGet(-oldRecord.productPrice() * oldRecord.quantity());
        basketDiscountPrice.addAndGet(-oldRecord.priceAfterDiscount() * oldRecord.quantity());
    }

    public ProductRecord getProductRecord(int prodctId) throws NonExistentData {
        if (!isProductExists(prodctId))
            throw new NonExistentData("product is not in the store basket!", ErrorStatus.PRODUCT_DOES_NOT_EXIST);

        return products.get(prodctId).getAsRecord();
    }

    @Override
    public IStoreBasket clone() {
        // Deep copy the products map
        return new DummyStoreBasket(this);
    }

    @Override
    public boolean isProductExists(int productId) {
        return products.containsKey(productId);
    }

    @Override
    public double getBasketPrice() {
        return basketRegularPrice.doubleValue();
    }

    @Override
    public double getBasketPriceAfterDiscount() {
        return basketDiscountPrice.doubleValue();
    }

    @Override
    public LocalDate getUserAge() {
        return userAge;
    }

    @Override
    public void setUserAge(LocalDate userAge) {
        this.userAge = userAge;
    }

    @Override
    public String toString() {
        return "StoreBasket"+storeId;
    }

    @Override
    public void updateProductQuantity(int productId, int quantity) throws NonExistentData {
        ProductRecord updated = getProductRecord(productId).updateQuantity(quantity);
        updateProductRecord(updated);
    }

    @Override
    public boolean isEmpty() {
        return products.isEmpty();
    }

    @Override
    public void remove(){
    }

    @Override
    public boolean productsAreEqual(IStoreBasket basket) {
        Set<ProductRecord> thisProducts = new HashSet<>(getProductsAsRecords().values());;
        Set<ProductRecord> otherProducts = new HashSet<>(basket.getProductsAsRecords().values());;

        return thisProducts.equals(otherProducts);
    }
}
