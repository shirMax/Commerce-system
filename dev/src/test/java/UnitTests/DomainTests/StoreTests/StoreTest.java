package UnitTests.DomainTests.StoreTests;

import DataLayer.Store.*;
import Domain.Permission;
import Domain.Services.NotificationService.NotificationService;
import Domain.Store.Category;
import Domain.Store.IProduct;
import Domain.Store.Product;
import Domain.Store.Store;
import Domain.User.DummyStoreBasket;
import Domain.User.IStoreBasket;
import Domain.User.StoreBasket;
import UnitTests.UnitTest;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Enums.PermissionType;
import util.Exceptions.DataError;
import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;
import util.Exceptions.PermissionError;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StoreTest extends UnitTest {
    private final String storeFounder = "test";
    private Store store;


    @BeforeEach
    public void setUp() {
        StoreRecord storeRecord = new StoreRecord("Test Store", "Test Store Description");
        store = new Store(storeFounder, storeRecord);
    }

    @Test
    public void testAddNewProduct() {
        try {
            int storeId = 1;
            ProductRecord productData = new ProductRecord(storeId, 1, "Test Product 1", 10.0, Category.BABY, 5, 10, 0);

            // Test
            int productID = store.addNewProduct("System", productData);
            assertEquals(productData.productName(), store.getProduct(productID).getProductName());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAddNewProductNoPermission() {
        try {
            // Data
            String noPermissionUser = "user1";

            // Set manager with no permissions
            store.assignStoreManager(storeFounder, noPermissionUser);
            store.setManagerPermissions(storeFounder, noPermissionUser, new HashSet<>());

            // Test
            assertThrows(PermissionError.class, () -> {
                store.addNewProduct(noPermissionUser, null);
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRemoveProduct() {
        try {
            // Data
            ProductRecord productData = new ProductRecord("Test Product 1", 10.0, Category.BEAUTY, 5);
            String storeOwner = "user1";

            // Set up store owner and a product
            store.assignStoreOwner(storeFounder, storeOwner);
            int productID = store.addNewProduct(storeOwner, productData);
            int storeProductsAmount = store.getProducts().size();

            // Test
            store.removeProduct(storeOwner, productID);
            assertEquals(storeProductsAmount - 1, store.getProducts().size());
            assertThrows(NonExistentData.class, () -> store.getProduct(productID));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRemoveProductNoPermission() {
        try {
            // Data
            String storeManager = "user1";

            // Set up store manager
            store.assignStoreManager(storeFounder, storeManager);

            // Test
            assertThrows(PermissionError.class, () -> {
                store.removeProduct(storeManager, 1);
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetProductName() {
        try {
            // Data
            String productName = "Test Product 1";
            ProductRecord productData = new ProductRecord(productName, 10.0, Category.AUTOMOTIVE, 5);
            String storeOwner = "user1";

            // Set up owner and a product
            store.assignStoreOwner(storeFounder, storeOwner);
            int productID = store.addNewProduct(storeOwner, productData);

            // get updated info and change name
            ProductRecord updated = new ProductRecord(store.getProduct(productID));
            final ProductRecord newName = updated.updateName("product name changed");

            // Test
            store.updateProductFields("user1", newName);
            assertEquals(newName.productName(), store.getProduct(productID).getProductName());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetProductNameNoPermission() {
        try {
            // Data
            String storeManager = "user1";

            // Set up store manager
            store.assignStoreManager(storeFounder, storeManager);

            // Test
            assertThrows(PermissionError.class, () -> {
                store.updateProductFields(storeManager, null);
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetProductPrice() {
        try {
            // Data
            String productName = "Test Product 1";
            ProductRecord productData = new ProductRecord(productName, 10.0, Category.AUTOMOTIVE, 5);
            String storeOwner = "user1";

            // Set up owner and a product
            store.assignStoreOwner(storeFounder, storeOwner);
            int productID = store.addNewProduct(storeOwner, productData);

            // get updated info and change price
            ProductRecord updated = new ProductRecord(store.getProduct(productID));
            final ProductRecord newPrice = updated.updatePrice(15);

            // Test
            store.updateProductFields("user1", newPrice);
            assertEquals(15.0, store.getProduct(productID).getProductPrice());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetProductPriceNoPermission() {
        try {
            // Data
            String storeManager = "user1";

            // Set up store manager
            store.assignStoreManager(storeFounder, storeManager);

            // Test
            assertThrows(PermissionError.class, () -> {
                store.updateProductFields("user2", null);
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetProductQuantity() {
        try {
            // Data
            String productName = "Test Product 1";
            ProductRecord productData = new ProductRecord(productName, 10.0, Category.AUTOMOTIVE, 5);
            String storeOwner = "user1";

            // Set up owner and a product
            store.assignStoreOwner(storeFounder, storeOwner);
            int productID = store.addNewProduct(storeOwner, productData);

            // Test
            store.addProductQuantity("user1", productID, 5);
            assertEquals(10, store.getProduct(productID).getProductQuantity());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetProductQuantityNoPermission() {
        try {
            // Data
            String storeManager = "user1";

            // Set up store manager
            store.assignStoreManager(storeFounder, storeManager);

            // Test
            assertThrows(PermissionError.class, () -> {
                store.addProductQuantity("user2", 1, 10);
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCloseStore() {
        try {
            // Call the method being tested
            store.closeStore(storeFounder);

            // Verify that the store is now inactive
            assertFalse(store.isActive());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCloseStoreNoPermission() {
        try {
            // Data
            String storeOwner = "user1";

            // Set up owner
            store.assignStoreOwner(storeFounder, storeOwner);

            // Test
            assertThrows(PermissionError.class, () -> {
                store.closeStore(storeOwner);
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAddNewProductDuplicateProductId() {
        try {
            // Data
            String productName = "Test Product 1";
            ProductRecord productData = new ProductRecord(productName, 10.0, Category.AUTOMOTIVE, 5);

            // Test
            Integer product1ID = store.addNewProduct(storeFounder, productData);
            Integer product2ID = store.addNewProduct(storeFounder, productData);
            assertNotEquals(product1ID, product2ID);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRemoveProductNonexistentProductId() {
        try {
            // Test
            store.removeProduct(storeFounder, 1);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetProductPriceNegativeValue() {
        try {
            // Data
            String productName = "Test Product 1";
            ProductRecord productData = new ProductRecord(productName, 10.0, Category.AUTOMOTIVE, 5);

            // Set up product
            Integer productID = store.addNewProduct(storeFounder, productData);

            // get updated info and change price to negative
            ProductRecord updated = new ProductRecord(store.getProduct(productID));
            final ProductRecord negativeValue = updated.updatePrice(-10.0);

            // Test
            assertThrows(Exception.class, () -> {
                store.updateProductFields(storeFounder, negativeValue);
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetProductNameEmptyString() {
        try {
            //Data
            ProductRecord data = new ProductRecord("Test Product 1", 10.0, Category.AUTOMOTIVE, 5);

            // Set up product
            Integer productID = store.addNewProduct(storeFounder, data);

            // get updated info and change name to empty
            ProductRecord updated = new ProductRecord(store.getProduct(productID));
            final ProductRecord emptyName = updated.updateName("");

            //Test
            assertThrows(DataError.class, () -> store.updateProductFields(storeFounder, emptyName));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetProductQuantityNegativeValue() {
        try {
            // Data
            ProductRecord productRecord = new ProductRecord("Test Product 1", 10.0, Category.AUTOMOTIVE, 5);

            // Set up product
            int productID = store.addNewProduct("System", productRecord);

            assertThrows(DataError.class, () -> {
                store.reduceProductQuantity("System", productID, 8);
            });
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testCalculateBasketPrice() {
        try {
            // inserted products data
            ProductRecord productRecord1 = new ProductRecord("Test Product 1", 10.0, Category.AUTOMOTIVE, 2);
            ProductRecord productRecord2 = new ProductRecord("Duplicate Product", 5.0, Category.AUTOMOTIVE, 1);

            //insert and get ids
            Integer product1_id = store.addNewProduct("test", productRecord1);
            Integer product2_id = store.addNewProduct("test", productRecord2);

            //get full records
            productRecord1 = new ProductRecord(store.getProduct(product1_id));
            productRecord2 = new ProductRecord(store.getProduct(product2_id));

            //add to basket
            IStoreBasket storeBasket = new DummyStoreBasket(1);
            storeBasket.addProduct(productRecord1);
            storeBasket.addProduct(productRecord2);


            double totalPrice = store.calculateBasketPrice(storeBasket);

            assertEquals(25.0, totalPrice);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    void testAssignStoreOwner() {
        try {
            // Arrange
            StoreRecord storeRecord1 = new StoreRecord("Test Store2", "Test Store Description");
            Store store2 = new Store("teest", storeRecord1);
            String currentOwnerUsername = "teest";
            String newOwnerUsername = "newOwner";

            // Act
            store2.assignStoreOwner(currentOwnerUsername, newOwnerUsername);

            // Assert
            assertTrue(store2.hasPermission(newOwnerUsername));
            assertTrue(store2.getMemberPermissions(newOwnerUsername).isStoreOwner());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testAssignStoreManager() {
        try {
            // Arrange
            StoreRecord storeRecord1 = new StoreRecord("Test Store3", "Test Store Description");
            Store store3 = new Store("currentOwner", storeRecord1);
            String currentOwnerUsername = "currentOwner";
            String newOwnerUsername = "newOwner";

            // Act
            store3.assignStoreManager(currentOwnerUsername, newOwnerUsername);

            // Assert
            assertTrue(store3.hasPermission(newOwnerUsername));
            assertTrue(store3.getMemberPermissions(newOwnerUsername).isStoreManager());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetManagerPermissions() {
        try {
            // Data
            String storeOwner = "user1";
            String storeManager = "user2";
            Set<PermissionType> permissions = Set.of(PermissionType.STORAGE_MANAGEMENT);

            // Set up owner and manager
            store.assignStoreOwner(storeFounder, storeOwner);
            store.assignStoreManager(storeFounder, storeManager);

            // Test
            store.setManagerPermissions(storeOwner, storeManager, permissions);
            assertTrue(store.getMemberPermissions(storeManager).hasPermission(PermissionType.STORAGE_MANAGEMENT));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetManagersPermissions() {
        try {
            // Arrange
            StoreRecord storeRecord1 = new StoreRecord("Test Store2", "Test Store Description");

            Store store2 = new Store("currentOwner", storeRecord1);
            String currentOwnerUsername = "currentOwner";
            String newOwnerUsername = "newOwner";

            // Act
            store2.assignStoreManager(currentOwnerUsername, newOwnerUsername);
            Permission managerPermission = store2.getMemberPermissions(newOwnerUsername);
            Map<String, Permission> result = store2.getManagersPermissions(currentOwnerUsername);
            // Assert
            Assertions.assertEquals(managerPermission, result.get(newOwnerUsername));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}