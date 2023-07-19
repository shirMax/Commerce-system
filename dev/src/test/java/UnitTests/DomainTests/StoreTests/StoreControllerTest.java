package UnitTests.DomainTests.StoreTests;

import DataLayer.Store.*;
import Domain.Services.NotificationService.NotificationService;
import Domain.Store.*;
import Domain.User.IUserController;
import Domain.User.UserController;
import UnitTests.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.ProductFilterAttributes;
import util.Records.StoreRecords.StoreRecord;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StoreControllerTest extends UnitTest {
    IProduct p1;
    IProduct p2;
    IProduct p3;
    IProduct p4;
    int storeId1;
    int storeId2;
    int storeId3;
    int productId1;
    int productId2;
    int productId3;
    int productId4;
    IStore store1;
    IStore store2;
    StoreController storeController;
    IUserController userController;
    String sessionId;
    IStoreRepo storeRepo;

    IStore realStore;
    StoreRecord storeRecord;
    String username1;


    @BeforeEach
    public void setUp() {
        username1 = "test user name";

        sessionId = "0";

        productId1 = 1;
        productId2 = 2;
        productId3 = 3;
        productId4 = 4;

        p1 = mock(Product.class);
        p2 = mock(Product.class);
        p3 = mock(Product.class);
        p4 = mock(Product.class);

        when(p1.getProductId()).thenReturn(productId1);
        when(p1.getProductName()).thenReturn("Product 1");
        when(p1.getProductPrice()).thenReturn(10.0);
        when(p1.getProductCategory()).thenReturn(Category.BABY);
        when(p1.getProductQuantity()).thenReturn(4);
        when(p1.getProductRating()).thenReturn(5.0);

        when(p2.getProductId()).thenReturn(productId2);
        when(p2.getProductName()).thenReturn("Product 2");
        when(p2.getProductPrice()).thenReturn(40.0);
        when(p2.getProductCategory()).thenReturn(Category.BEAUTY);
        when(p2.getProductQuantity()).thenReturn(3);
        when(p2.getProductRating()).thenReturn(2.0);

        when(p3.getProductId()).thenReturn(productId3);
        when(p3.getProductName()).thenReturn("Product 3");
        when(p3.getProductPrice()).thenReturn(15.0);
        when(p3.getProductCategory()).thenReturn(Category.BABY);
        when(p3.getProductQuantity()).thenReturn(4);
        when(p3.getProductRating()).thenReturn(4.0);

        when(p4.getProductId()).thenReturn(productId4);
        when(p4.getProductName()).thenReturn("Product 4");
        when(p4.getProductPrice()).thenReturn(25.0);
        when(p4.getProductCategory()).thenReturn(Category.AUTOMOTIVE);
        when(p4.getProductQuantity()).thenReturn(4);
        when(p4.getProductRating()).thenReturn(4.5);

        store1 = mock(IStore.class);
        store2 = mock(IStore.class);
        userController = mock(UserController.class);
        storeController = new StoreController(userController);
        storeRepo = mock(StoreRepo.class);


        storeRecord = new StoreRecord("store 3", "description 3");

        realStore = new Store(username1, storeRecord);
        storeId3 = realStore.getStoreId();
        storeId1 = storeId3 + 1;
        storeId2 = storeId1 + 1;
    }

    @Test
    void testGetFilteredProducts() throws NonExistentData {
        when(store1.getStoreId()).thenReturn(storeId1);
        when(store2.getStoreId()).thenReturn(storeId2);

        when(userController.isUserIsMember(sessionId)).thenReturn(true);
        storeController.addStore(store1);
        storeController.addStore(store2);

        // Create mock stores with products
        List<IProduct> products1 = Arrays.asList(p1, p2);
        List<IProduct> products2 = Arrays.asList(p3, p4);
        when(store1.getProducts()).thenReturn(products1);
        when(store2.getProducts()).thenReturn(products2);
        when(store1.isActive()).thenReturn(true);
        when(store2.isActive()).thenReturn(true);

        // Test case 1: filter by product name
        ProductFilterAttributes filter1 = new ProductFilterAttributes(null, null,"Product 1", null, null, null, null, null, null, null);
        List<IProduct> filteredProducts1 = storeController.getFilteredProducts(filter1);
        assertEquals(Arrays.asList(p1), filteredProducts1);

        // Test case 2: filter by product category
        ProductFilterAttributes filter2 = new ProductFilterAttributes(null, null,null, null, null, Arrays.asList(Category.BABY), null, null, null, null);
        List<IProduct> filteredProducts2 = storeController.getFilteredProducts(filter2);
        assertEquals(Arrays.asList(p1, p3), filteredProducts2);

        // Test case 3: filter by product rating
        ProductFilterAttributes filter3 = new ProductFilterAttributes(null, null,null, null, null, null, 4.0, null, null, null);
        List<IProduct> filteredProducts3 = storeController.getFilteredProducts(filter3);
        assertEquals(Arrays.asList(p1, p3, p4), filteredProducts3);

        // Test case 4: filter by product price
        ProductFilterAttributes filter4 = new ProductFilterAttributes(null, null,null, null, null, null, null, null,  10.0,25.0);
        List<IProduct> filteredProducts4 = storeController.getFilteredProducts(filter4);
        assertEquals(Arrays.asList(p1, p3, p4), filteredProducts4);

        // Test case 5: filter by store rating
        when(store1.getStoreRating()).thenReturn(4.0);
        when(store2.getStoreRating()).thenReturn(3.0);
        ProductFilterAttributes filter5 = new ProductFilterAttributes(null, null,null, 4.0, null, null, null, null, null, null);
        List<IProduct> filteredProducts5 = storeController.getFilteredProducts(filter5);
        assertEquals(Arrays.asList(p1, p2), filteredProducts5);
    }

    @Test
    public void testGetStoreInfo() throws NonExistentData {
        storeController.addStore(realStore);

        StoreRecord storeRecord = storeController.getStoreInfo(storeId3);

        assertEquals(storeId3, storeRecord.storeId());
        assertEquals("store 3", storeRecord.storeName());
        assertEquals(0, storeRecord.storeRating());
        assertEquals("description 3", storeRecord.storeDescription());
    }
}
