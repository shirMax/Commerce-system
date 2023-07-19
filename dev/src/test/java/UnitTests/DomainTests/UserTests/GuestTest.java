package UnitTests.DomainTests.UserTests;

import Domain.Store.Category;
import Domain.User.Guest;
import UnitTests.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.ProductRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GuestTest extends UnitTest {
    private Guest guest;
    private String sessionId;

    @BeforeEach
    public void setUp() {
        sessionId = "12345";
        guest = new Guest(sessionId);
    }

    @Test
    public void testAddProductsToStoreBasket() {
        int storeId = 1;
        Map<Integer, ProductRecord> products = new HashMap<>();
        products.put(1, new ProductRecord(storeId, 1, "prod 1", 10, Category.AUTOMOTIVE, 2, 10, 1));
        products.put(2, new ProductRecord(storeId, 2, "prod 1", 10, Category.AUTOMOTIVE, 3, 10,1));

        guest.addProductsToStoreBasket(storeId, products.values().stream().toList());

        // Check if products were added to store basket correctly
        Map<Integer, ProductRecord> storeBasketProducts = null;
        storeBasketProducts = guest.getStoreBasketProducts(storeId);

        assertNotNull(storeBasketProducts);
        assertEquals(2, storeBasketProducts.size());
        assertEquals(Integer.valueOf(2), storeBasketProducts.get(1).quantity());
        assertEquals(Integer.valueOf(3), storeBasketProducts.get(2).quantity());
    }

    @Test
    public void testRemoveProductFromStoreBasket() {
        int storeId = 1;
        int productId = 1;

        // Add product to store basket
        Map<Integer, ProductRecord> products = new HashMap<>();
        products.put(productId, new ProductRecord(storeId, productId, "prod 1", 10, Category.AUTOMOTIVE, 3, 10, 1));
        guest.addProductsToStoreBasket(storeId, products.values().stream().toList());

        // Remove product from store basket
        try {
            guest.removeProductFromStoreBasket(storeId, productId, 3);
        } catch (NonExistentData e) {
            Assertions.fail("Failed to remove product from store basket");
        }

        // Check if product was removed from store basket
        Map<Integer, ProductRecord> storeBasketProducts = null;
        storeBasketProducts = guest.getStoreBasketProducts(storeId);
        assertNotNull(storeBasketProducts);
        assertEquals(0, storeBasketProducts.size());
    }

    @Test
    public void testUpdateProductQuantityInStoreBasket() {
        // data
        int storeId = 1;
        int productId = 1;
        int quantity = 5;
        ProductRecord product = new ProductRecord(storeId, productId, "prod 1", 10, Category.AUTOMOTIVE, 3, 10, 1);

        // Add product to store basket
        guest.addProductsToStoreBasket(storeId, List.of(product));

        // Update product quantity in store basket
        try {
            guest.updateProductQuantityInStoreBasket(storeId, productId, quantity);
        } catch (NonExistentData e) {
            fail(e.getMessage());
        }
        // Check if product quantity was updated in store basket
        Map<Integer, ProductRecord> storeBasketProducts = guest.getStoreBasketProducts(storeId);

        assertNotNull(storeBasketProducts);
        assertEquals(quantity, storeBasketProducts.get(productId).quantity());
    }

    @Test
    public void testGetStoreBasketProducts() {
        int storeId = 1;
        Map<Integer, ProductRecord> products = new HashMap<>();
        products.put(1, new ProductRecord(storeId, 1, "prod 1", 10, Category.AUTOMOTIVE, 2, 10, 1));
        products.put(2, new ProductRecord(storeId, 2, "prod 1", 10, Category.AUTOMOTIVE, 3, 10, 1));

        // Add products to store basket
        guest.addProductsToStoreBasket(storeId, products.values().stream().toList());

        // Get store basket products
        Map<Integer, ProductRecord> storeBasketProducts = null;
        storeBasketProducts = guest.getStoreBasketProducts(storeId);


        // Check if store basket products are returned correctly
        assertNotNull(storeBasketProducts);
        assertEquals(2, storeBasketProducts.size());
        assertEquals(Integer.valueOf(2), storeBasketProducts.get(1).quantity());
        assertEquals(Integer.valueOf(3), storeBasketProducts.get(2).quantity());
    }
}



