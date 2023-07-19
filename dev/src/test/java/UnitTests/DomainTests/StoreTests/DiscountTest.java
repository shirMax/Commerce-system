package UnitTests.DomainTests.StoreTests;

import Domain.Store.*;
import Domain.Store.Conditions.Condition;
import Domain.Store.Conditions.ConditionFactory;
import Domain.Store.Discount.DiscountTypes.Composite.AndDiscount;
import Domain.Store.Discount.DiscountTypes.Composite.IfThenDiscount;
import Domain.Store.Discount.DiscountTypes.Composite.MaxDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.CategoryDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.ProductDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.StoreDiscount;
import Domain.Store.Discount.IDiscount;
import Domain.User.DummyStoreBasket;
import Domain.User.IStoreBasket;
import UnitTests.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Exceptions.DataError;
import util.Exceptions.PermissionError;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class DiscountTest extends UnitTest {

    final StoreRecord storeData = new StoreRecord("test store", "store for tests");
    IStore store;
    IStoreBasket basket;

    final ProductRecord product1Data = new ProductRecord("product1", 10, Category.BEAUTY, 5);
    final ProductRecord product2Data = new ProductRecord("product2", 20, Category.BEAUTY, 2);
    final ProductRecord product3Data = new ProductRecord("product3", 5, Category.BEAUTY, 1);
    final ProductRecord product4Data = new ProductRecord("product4", 15, Category.AUTOMOTIVE, 2);
    final ProductRecord product5Data = new ProductRecord("product5", 10, Category.AUTOMOTIVE, 1);
    final ProductRecord product6Data = new ProductRecord("product6", 5, Category.HEALTH, 4);
    final ProductRecord product7Data = new ProductRecord("product7", 30, Category.ART, 2);

    int product1ID;
    int product2ID;
    int product3ID;
    int product4ID;
    int product5ID;
    int product6ID;
    int product7ID;

    @BeforeEach
    public void testSetUp(){
        // Init store
        store = new Store("SystemTest", storeData);

        // Insert products
        try {
            product1ID = store.addNewProduct("SystemTest", product1Data);
            product2ID = store.addNewProduct("SystemTest", product2Data);
            product3ID = store.addNewProduct("SystemTest", product3Data);
            product4ID = store.addNewProduct("SystemTest", product4Data);
            product5ID = store.addNewProduct("SystemTest", product5Data);
            product6ID = store.addNewProduct("SystemTest", product6Data);
            product7ID = store.addNewProduct("SystemTest", product7Data);
        } catch (DataError | PermissionError ignored) {
        }

        // Init basket
        basket = new DummyStoreBasket(store.getStoreId());
        for (IProduct product : store.getProducts())
            basket.addProduct(new ProductRecord(product));
    }

    @Test
    public void basketPriceTest(){
        Assertions.assertEquals( 215, basket.getBasketPrice());
        Assertions.assertEquals( 215, basket.getBasketPriceAfterDiscount()); //basket start with 0 discounts
        try{
            basket.removeProduct(product1ID);
            Assertions.assertEquals( 165, basket.getBasketPrice());
            Assertions.assertEquals( 165, basket.getBasketPriceAfterDiscount()); //basket start with 0 discounts
        }
        catch (Exception e){
            fail();
        }
    }

    @Test
    public void simpleProductDiscount() throws DataError {
        IDiscount discount = new ProductDiscount(10, 1);
        try {
            store.addDiscount("System", discount);
            store.calculateBasketPrice(basket);
            assertEquals(210, basket.getBasketPriceAfterDiscount());
            Assertions.assertEquals( 215, basket.getBasketPrice());
        }
        catch (Exception e){
            fail();
        }
    }

    @Test
    public void simpleCategoryDiscount() throws DataError {
        IDiscount discount = new CategoryDiscount(50, Category.BEAUTY);
        try {
            store.addDiscount("System", discount);
            store.calculateBasketPrice(basket);
            assertEquals(167.5, basket.getBasketPriceAfterDiscount());
            Assertions.assertEquals( 215, basket.getBasketPrice());
        }
        catch (Exception e){
            fail();
        }
    }

    @Test
    public void simpleStoreDiscount() throws DataError {
        IDiscount discount = new StoreDiscount(20);
        try {
            store.addDiscount("System", discount);
            store.calculateBasketPrice(basket);
            assertEquals(172, basket.getBasketPriceAfterDiscount());
            Assertions.assertEquals( 215, basket.getBasketPrice());
        }
        catch (Exception e){
            fail();
        }
    }

    @Test
    public void ifThenDiscount() throws DataError {
        Condition condition = ConditionFactory.minBasketPrice(200);
        IDiscount subDiscount = new StoreDiscount(20);
        IDiscount discount = new IfThenDiscount(condition, subDiscount);
        try {
            store.addDiscount("System", discount);
            store.calculateBasketPrice(basket);
            assertEquals(172, basket.getBasketPriceAfterDiscount());
            Assertions.assertEquals( 215, basket.getBasketPrice());
        }
        catch (Exception e){
            fail();
        }

        testSetUp();
        try{
            basket.removeProduct(product1ID);
            store.calculateBasketPrice(basket);
            Assertions.assertEquals( 165, basket.getBasketPrice());
            Assertions.assertEquals( 165, basket.getBasketPriceAfterDiscount());
        }
        catch (Exception e){
            fail();
        }
    }

    @Test
    public void andDiscount() throws DataError {
        Condition condition = ConditionFactory.minBasketPrice(200);
        Condition condition2 = ConditionFactory.atLeastQuantity(5, product1ID);
        IDiscount subDiscount = new StoreDiscount(20);
        Set<Condition> conditions = Set.of(condition, condition2);
        IDiscount discount = new AndDiscount(conditions, subDiscount);
        try {
            store.addDiscount("System", discount);
            store.calculateBasketPrice(basket);
            assertEquals(172, basket.getBasketPriceAfterDiscount());
            Assertions.assertEquals( 215, basket.getBasketPrice());
        }
        catch (Exception e){
            fail();
        }

        testSetUp();
        try {
            ProductRecord product1 = new ProductRecord(store.getProduct(product1ID));
            ProductRecord product1Updated = product1.updateQuantity(4);
            basket.updateProductRecord(product1Updated);
            store.calculateBasketPrice(basket);
            Assertions.assertEquals( 205, basket.getBasketPrice());
            Assertions.assertEquals( 205, basket.getBasketPriceAfterDiscount());
        }
        catch (Exception e){
            fail();
        }
    }

    @Test
    public void maxDiscount() throws DataError {
        IDiscount discount = new StoreDiscount(20);
        IDiscount discount2 = new CategoryDiscount(50, Category.BEAUTY);
        Set<IDiscount> discounts = Set.of(discount, discount2);
        IDiscount maxDiscount = new MaxDiscount(discounts);
        try {
            store.addDiscount("System", maxDiscount);
            store.calculateBasketPrice(basket);
            assertEquals(167.5, basket.getBasketPriceAfterDiscount());
            Assertions.assertEquals( 215, basket.getBasketPrice());
        }
        catch (Exception e){
            fail();
        }
    }

    @Test
    public void composeDiscount() throws DataError {
        IDiscount storeDiscount = new StoreDiscount(10);
        IDiscount categoryDiscountDiscount = new CategoryDiscount(50, Category.BEAUTY);
        Set<IDiscount> discounts = Set.of(storeDiscount, categoryDiscountDiscount);
        IDiscount maxDiscount = new MaxDiscount(discounts);
        Condition condition = ConditionFactory.minBasketPrice(200);
        Condition condition2 = ConditionFactory.atLeastQuantity(5, product1ID);
        Set<Condition> conditions = Set.of(condition, condition2);
        IDiscount andDiscount = new AndDiscount(conditions, maxDiscount);
        try {
            store.addDiscount("System", andDiscount);
            store.calculateBasketPrice(basket);
            assertEquals(167.5, basket.getBasketPriceAfterDiscount());
            Assertions.assertEquals( 215, basket.getBasketPrice());
        }
        catch (Exception e){
            fail();
        }
    }
}
