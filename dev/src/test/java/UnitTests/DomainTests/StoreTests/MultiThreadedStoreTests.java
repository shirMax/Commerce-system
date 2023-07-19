package UnitTests.DomainTests.StoreTests;

import Domain.Permission;
import Domain.Store.Category;
import Domain.Store.Store;
import UnitTests.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import util.Exceptions.DataError;
import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;
import util.Exceptions.PermissionError;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class MultiThreadedStoreTests extends UnitTest {

    private final String storeFounder = "test";
    private Store store;
    private StoreRecord storeRecord;


    @BeforeEach
    public void setUp() {
        storeRecord = new StoreRecord("Test Store", "Test Store Description");
        store = new Store(storeFounder, storeRecord);
    }

    @Test
    public void productQuantity() throws NonExistentData, DataError, PermissionError, DataExistentError {
        int storeId = store.getStoreId();
        store.assignStoreOwner(storeFounder, "user1");
        ProductRecord productRecord = new ProductRecord("Test Product 1", 10.0, Category.PETS, 5);
        int productId = store.addNewProduct("user1", productRecord);


        ExecutorService executor = Executors.newFixedThreadPool(10);
        for(int i=0; i<10; i++){
            executor.submit(() ->{
                try{
                    store.reduceProductQuantity("System", productId,5);
                    store.addProductQuantity("System", productId,5);
                    store.reduceProductQuantity("System", productId,5);
                    store.addProductQuantity("System",productId,5);
                }
                catch (DataError | NonExistentData | PermissionError e){

                }
            });
        }
        executor.shutdown();
        try {
            // wait for all tasks to complete or until timeout occurs
            boolean terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
            if (!terminated) {
                assertTrue(false);
            }
        } catch (InterruptedException e) {
            // handle interruption if necessary
            assertTrue(false);
        }
        assertTrue(store.getProduct(productId).getProductQuantity() >= 0);

        executor = Executors.newFixedThreadPool(10);
        ProductRecord productRecord2 = new ProductRecord("Test Product 2", 10.0, Category.BEAUTY, 40);
        int product2ID = store.addNewProduct("user1", productRecord2);
        for(int i=0; i<10; i++){
            executor.submit(() ->{
                try{
                    store.reduceProductQuantity("System", product2ID,2);
                    store.addProductQuantity("System",product2ID,2);
                }
                catch (Exception e){
                    fail();
                }
            });
        }
        executor.shutdown();
        try {
            // wait for all tasks to complete or until timeout occurs
            boolean terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
            if (!terminated) {
                fail();
            }
        } catch (InterruptedException e) {
            // handle interruption if necessary
            fail();
        }
        assertEquals(40, store.getProduct(product2ID).getProductQuantity());
    }

    @Test
    @RepeatedTest(20)
    public void assignEmployee() throws NonExistentData {
        Store store2 = new Store("test", storeRecord);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i=0; i<10; i++){
            executor.submit(() -> {
                try {
                    try {
                        store2.assignStoreOwner("test", "manager1");
                    } catch (Exception e) {}
                    try {
                        store2.assignStoreOwner("manager1", "test");
                    } catch (Exception e) {}
                    try {
                        store2.assignStoreManager("manager1", "manager2");
                    } catch (Exception e) {}

                    store2.assignStoreManager("test", "manager2");

                } catch (DataExistentError | PermissionError ignored) {
                }
            });
        }
        executor.shutdown();
        try {
            // wait for all tasks to complete or until timeout occurs
            boolean terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
            if (!terminated) {
                fail();
            }
        } catch (InterruptedException e) {
            // handle interruption if necessary
            fail();
        }

        Map<String, Permission> map = store2.getPermissions();
        assertNotNull(map.get("test"));
        assertNotNull(map.get("manager1"));
        assertNotNull(map.get("manager2"));

        assertEquals("test", map.get("manager1").getPermissionGiverName());
        assertEquals("manager1", map.get("manager2").getPermissionGiverName());
    }

    @Test
    public void testAsyncAppointments() {
        Store store = new Store("user1", storeRecord);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            store.assignStoreOwner("user1", "user3");
        } catch (DataExistentError | NonExistentData | PermissionError e) {
            fail(e);
        }
        // Appoint User 2 by User 1
        executor.submit(() -> {
            try {
                store.assignStoreManager("user1", "user2");
            } catch (DataExistentError | PermissionError e) {
                fail(e);
            }
        });

        // Appoint User 2 by User 3
        executor.submit(() -> {
            try {
                store.assignStoreManager("user3", "user2");
            } catch (DataExistentError | PermissionError e) {
                fail(e);
            }
        });

        executor.shutdown();
        try {
            // Wait for all tasks to complete or until timeout occurs
            boolean terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
            if (!terminated) {
                fail("Timed out while waiting for appointments to complete");
            }
        } catch (InterruptedException e) {
            fail(e);
        }

        // Check that only one appointment succeeded
        Map<String, Permission> map = store.getPermissions();
        assertEquals(1, map.entrySet().stream().filter(entry -> entry.getKey().equals("user2")).count());
        assertTrue(map.get("user2").isStoreManager());
        // Check that User 2 is appointed
        assertTrue(map.get("user2").getPermissionGiverName().equals("user1") || map.get("user2").getPermissionGiverName().equals("user3"));
    }
}
