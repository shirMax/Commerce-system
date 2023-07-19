package UnitTests.DomainTests.StoreTests;

import Domain.Store.*;
import Domain.Store.Conditions.Condition;
import Domain.Store.Conditions.ConditionFactory;
import util.Exceptions.PurchaseLimitation;
import Domain.Store.Purchase.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

public class PurchaseRulesTests extends UnitTest {
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
    public void testSetUp() {
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
    public void andPurchaseRule() {
        Condition cond1 = ConditionFactory.atLeastQuantity(10);
        Condition cond2 = ConditionFactory.atLeastQuantity(5, product1ID);
        Set<Condition> conditions = Set.of(cond1, cond2);
        PurchaseRule rule = new AndPurchaseRule(conditions);
        try {
            store.addPurchaseRule("System", rule);
            store.checkPurchaseRules(basket);
            basket.removeProduct(product1ID);
        } catch (Exception e) {
            fail();
        }
        Assertions.assertThrows(PurchaseLimitation.class, () -> {
            store.checkPurchaseRules(basket);
        });
    }

    @Test
    public void ifThenPurchaseRule() {
        Condition cond1 = ConditionFactory.atLeastQuantity(18);
        Condition cond2 = ConditionFactory.atLeastQuantity(6, product1ID);
        PurchaseRule rule = new IfThenPurchaseRule(cond1, cond2);
        try {
            store.addPurchaseRule("System", rule);
            store.checkPurchaseRules(basket);
            //What do you mean add product 8? it doesn't exist! talk to me so we can fix this - Roi
            basket.addProduct(new ProductRecord(1, 8, "product8", 30, Category.ART, 10, 30, 1));
        } catch (Exception e) {
            fail();
        }
        Assertions.assertThrows(PurchaseLimitation.class, () -> {
            store.checkPurchaseRules(basket);
        });
    }

    @Test
    public void orPurchaseRule() {
        // Data
        Condition cond1 = ConditionFactory.atLeastQuantity(15);
        Condition cond2 = ConditionFactory.atLeastQuantity(6, product1ID);
        Set<Condition> conditions = Set.of(cond1, cond2);
        PurchaseRule rule = new OrPurchaseRule(conditions);

        // Test
        try {
            store.addPurchaseRule("System", rule);
            store.checkPurchaseRules(basket);
            ProductRecord product1 = new ProductRecord(store.getProduct(product1ID));
            ProductRecord updatedQuantity = product1.updateQuantity(1);
            basket.updateProductRecord(updatedQuantity);
        } catch (Exception e) {
            fail();
        }

        // Assert
        Assertions.assertThrows(PurchaseLimitation.class, () -> store.checkPurchaseRules(basket));
    }
}
