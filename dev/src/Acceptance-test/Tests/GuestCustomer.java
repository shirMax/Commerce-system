package Tests;

import org.junit.jupiter.api.RepeatedTest;
import util.Exceptions.DataError;
import util.Records.Transaction;
import Domain.Store.Category;
import Domain.Store.Discount.DiscountTypes.Simple.ProductDiscount;
import Domain.Store.Discount.IDiscount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Records.StoreRecords.ProductFilterAttributes;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests operations a guest can perform as a customer
 * Tests Use-case II.2.1: Gather information about stores and products.
 * Tests Use-case II.2.2: Search for products by filter.
 * Tests Use-case II.2.3: Adding items to cart
 * Tests Use-case II.2.4: Editing cart items
 * Tests Use-case II.2.5: Calculating cart price
 * Tests Use-case II.2.6: Performing payment
 */
public class GuestCustomer extends TestBase {

    static String SESSION; //Guest
    static String SESSION_SF1; //store founder 1
    static final UserRecord STORE_FOUNDER1 = new UserRecord("test_founder1", "testfounder1@test.com", "0512345678", LocalDate.of(1997, 1, 19));
    static final String PASS_SF1 = "P2ssW0rd"; //store founder 1
    static final StoreRecord STORE1 = new StoreRecord("test_store1", "Store for testing");
    static Integer STORE1_ID;
    static ProductRecord PROD1;
    static Integer PROD1_ID;
    static ProductRecord PROD2;
    static Integer PROD2_ID;
    static String SESSION_M1;

    @Override
    @BeforeEach
    protected void setUp() {
        inject();
        //Create founder 1
        SESSION_SF1 = createSession(bridge);
        bridge.register(SESSION_SF1, STORE_FOUNDER1, PASS_SF1);
        Register.toDelete.add(STORE_FOUNDER1.username());
        bridge.login(SESSION_SF1, STORE_FOUNDER1.username(), PASS_SF1);

        //Create store 1
        STORE1_ID = bridge.openStore(SESSION_SF1, STORE1);
        OpenStore.toDelete.add(STORE1_ID);

        //Add Item 1
        PROD1 = new ProductRecord(STORE1_ID, "test_product1", 5.00, Category.PARTY, 5);
        PROD2 = new ProductRecord(STORE1_ID, "test_product2", 5.00, Category.MUSIC, 5);
        PROD1_ID = bridge.addProduct(SESSION_SF1, STORE1_ID, PROD1);
        PROD2_ID = bridge.addProduct(SESSION_SF1, STORE1_ID, PROD2);

        PROD1 = bridge.getProduct(SESSION_SF1, STORE1_ID, PROD1_ID);
        PROD2 = bridge.getProduct(SESSION_SF1, STORE1_ID, PROD2_ID);

        bridge.logout(SESSION_SF1);
        bridge.closeSession(SESSION_SF1);

        SESSION = createSession(bridge);
    }

    @Override
    @AfterEach
    protected void tearDown() {
        bridge.closeSession(SESSION);
        OpenStore.cleanToDelete(bridge);
        Register.cleanToDelete(bridge);
    }

    // Testing use case II.2.1

    @Test
    void GetStores_Success() {
        Set<StoreRecord> stores = bridge.getStores(SESSION);
        assertNotNull(stores);
        Set<Integer> storeIDs = stores.stream().map(StoreRecord::storeId).collect(Collectors.toSet());
        assertTrue(storeIDs.contains(STORE1_ID), "Store 1 didn't return as part of the market stores.");
    }

    @Test
    void GetProducts_Success(){
        ProductFilterAttributes filter1 = new ProductFilterAttributes(null,null, null, null, null, null, null, null, null, null);
        Set<Integer> products1 = bridge.getProducts(SESSION, filter1).stream().map(ProductRecord::productId).collect(Collectors.toSet());
        assertTrue(products1.contains(PROD1.productId()), "Product list should contain all products.");
    }

    // Testing use case II.2.2
    @Test
    void FilterProducts_Success(){
        ProductFilterAttributes filter1 = new ProductFilterAttributes(STORE1_ID, null,null,null, null, null, null, null, null, null);
        Set<Integer> products1 = bridge.getProducts(SESSION, filter1).stream().map(ProductRecord::productId).collect(Collectors.toSet());
        assertTrue(products1.contains(PROD1.productId()), "Product list should contain all the products of the store.");

        ProductFilterAttributes filter2 = new ProductFilterAttributes(null, null, null, null, null, List.of(Category.AUTOMOTIVE, Category.OFFICE), null, null, null, null);
        Set<Integer> products2 = bridge.getProducts(SESSION, filter2).stream().map(ProductRecord::productId).collect(Collectors.toSet());
        assertFalse(products2.contains(PROD1.productId()), "Product list should filter out 'PARTY' products.");

        ProductFilterAttributes filter3 = new ProductFilterAttributes(null, null, null, null, null ,List.of(Category.AUTOMOTIVE, Category.OFFICE, Category.PARTY), null, null, 3., 6.);
        Set<Integer> products3 = bridge.getProducts(SESSION, filter3).stream().map(ProductRecord::productId).collect(Collectors.toSet());
        assertTrue(products3.contains(PROD1.productId()), "Product passes filter yet isn't returned.");
    }

    //Testing use case II.2.3

    @Test
    void AddToCart_LegalAmount_Success() {
        bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, 3);
        Map<Integer, Map<Integer, ProductRecord>> cart = bridge.getCart(SESSION);
        assertTrue(cart.containsKey(STORE1_ID));
        assertTrue(cart.get(STORE1_ID).containsKey(PROD1_ID));
        assertEquals(3, cart.get(STORE1_ID).get(PROD1_ID).quantity());
    }

    @Test
    void AddToCart_TooBigAmount_Success() {
        try {
            bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, 10);
            fail("Adding too many items succeeded.");
        } catch (Exception success) {
        }
    }

    @Test
    void AddToCart_NegativeAmount_Success() {
        try {
            bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, -10);
            fail("Adding negative amount succeeded.");
        } catch (Exception success) {
        }
    }

    //Testing use case II.2.4

    @Test
    void EditCart_Adding_Success() {
        bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, 3);
        bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, 2);
        Map<Integer, Map<Integer, ProductRecord>> cart = bridge.getCart(SESSION);
        assertEquals(5, cart.get(STORE1_ID).get(PROD1_ID).quantity());
    }

    @Test
    void EditCart_Subtracting_Success() {
        bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, 3);
        bridge.addToCart(SESSION, STORE1_ID, PROD2_ID, 3);

        // 1 removal
        bridge.removeFromCart(SESSION, STORE1_ID, PROD1_ID, 1);
        Map<Integer, Map<Integer, ProductRecord>> cart = bridge.getCart(SESSION);
        assertEquals(2, cart.get(STORE1_ID).get(PROD1_ID).quantity());

        // totally remove prod1
        bridge.removeFromCart(SESSION, STORE1_ID, PROD1_ID, 2);
        cart = bridge.getCart(SESSION);
        assertFalse(cart.get(STORE1_ID).containsKey(PROD1_ID), "After removing all instances of Prod1 - it shouldn't exist in the cart");

        // totally remove products from store1
        bridge.removeFromCart(SESSION, STORE1_ID, PROD2_ID, 3);
        cart = bridge.getCart(SESSION);
        assertFalse(cart.containsKey(STORE1_ID), "After removing all store1 products - it shouldn't exist in the cart");
    }

    @Test
    void EditCart_RemovingNonExistentProduct_Failure() {
        bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, 3);
        try {
            bridge.removeFromCart(SESSION, STORE1_ID, PROD2_ID, 1);
            fail("Removal of product which wasn't added to the cart shouldn't succeed.");
        } catch (Exception success) {
        }
    }

    //Testing use case II.2.5

    @Test
    void CalculateCartPrice_Success() {
        bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, 3);
        Double price = bridge.calculateCart(SESSION);
        assertEquals(3 * PROD1.productPrice(), price);
    }

    //Testing use case II.2.6

    @Test
    void BuyCart_1Customer_Success() {
        bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, 3);
        bridge.pay(SESSION);

        //check quantity dropped from store:
        ProductRecord product = bridge.getProduct(SESSION, STORE1_ID, PROD1_ID);
        assertEquals(PROD1.quantity() - 3, product.quantity(), "After successful purchase quantity should drop.");

        //check cart is empty:
        Map<Integer, Map<Integer, ProductRecord>> cart = bridge.getCart(SESSION);
        assertEquals(0, cart.size(), "Cart was not emptied after successful purchase.");

        //check transaction added
        List<Transaction> transactions = bridge.getStoreTransactionHistory(SESSION_SF1, STORE1_ID, null);
        boolean found = false;
        for (Transaction transaction : transactions)
            if (transaction.storeBasket().getProducts().get(PROD1_ID).getQuantity() == 3) {
                found = true;
                break;
            }
        assertTrue(found, "Transaction wasn't added after successful purchase.");
    }

    @Test
    void BuyCart_1Customer_QuantityTooHigh_Failure() {
        try {
            bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, PROD1.quantity() + 1);
            bridge.pay(SESSION);
            fail("Adding to cart or purchasing more than can be bought should fail.");
        } catch (Exception success) {
        }
    }

    @Test
    @RepeatedTest(20)
    void BuyCart_2CustomersRace_1Success() {
        String session2 = createSession(bridge);

        bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, 5);
        bridge.addToCart(session2, STORE1_ID, PROD1_ID, 5);


        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> firstPay = executor.submit(() -> bridge.pay(SESSION));
        Future<?> secondPay = executor.submit(() -> bridge.pay(session2));

        int successCounter = 0;
        for (Future<?> future : List.of(firstPay, secondPay)) {
            try {
                future.get();
                successCounter++;
            } catch (Exception ignored) {
            }
        }

        assertEquals(1, successCounter, "Only one thread should have succeeded in paying.");

    }

    @Test
    void BuyCart_Discount_1Customer_Success() throws DataError {
        SESSION_SF1 = createSession(bridge);

        bridge.addToCart(SESSION, STORE1_ID, PROD1_ID, 1);
        IDiscount discount = new ProductDiscount(20, PROD1_ID);
        bridge.login(SESSION_SF1, STORE_FOUNDER1.username(), PASS_SF1);
        bridge.addDiscount(SESSION_SF1, STORE1_ID, discount);
        bridge.logout(SESSION_SF1);
        bridge.pay(SESSION);

        //check quantity dropped from store:
        ProductRecord product = bridge.getProduct(SESSION, STORE1_ID, PROD1_ID);
        assertEquals(PROD1.quantity() - 1, product.quantity(), "After successful purchase quantity should drop.");

        //check cart is empty:
        Map<Integer, Map<Integer, ProductRecord>> cart = bridge.getCart(SESSION);
        assertEquals(0, cart.size(), "Cart was not emptied after successful purchase.");

        //check transaction added
        List<Transaction> transactions = bridge.getStoreTransactionHistory(SESSION_SF1, STORE1_ID, null);
        boolean found = false;
        for (Transaction transaction : transactions) {
            if (transaction.storeBasket().getProducts().get(PROD1_ID).getQuantity() == 1) {
                found = true;
            }
            if (transaction.price() == 4) {
                found = found && true;
            }
            assertTrue(found, "Transaction wasn't added after successful purchase.");
        }
    }
}
