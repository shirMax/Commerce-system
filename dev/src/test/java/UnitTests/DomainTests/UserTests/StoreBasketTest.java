package UnitTests.DomainTests.UserTests;


import DataLayer.User.ORM.DataCart;
import DataLayer.User.ORM.DataMember;
import Domain.Store.Category;
import Domain.User.StoreBasket;
import UnitTests.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.ProductRecord;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StoreBasketTest extends UnitTest {
    private final DataMember dataMemberMock = mock(DataMember.class);
    private final DataCart dataCartMock = mock(DataCart.class);
    private StoreBasket storeBasket;
    private final int storeId = 1;
    private final int productId1 = 101;
    private final int productId2 = 102;
    private final int initialQuantity = 3;

    @BeforeEach
    void setUp() {
        when(dataMemberMock.getBirthday()).thenReturn(LocalDate.now());
        when(dataCartMock.getMember()).thenReturn(dataMemberMock);

        storeBasket = new StoreBasket(dataCartMock, storeId);
        storeBasket.addProduct(new ProductRecord(storeId, productId1, "test name", 10, Category.BEAUTY, initialQuantity, 10, 1));
    }

    @Test
    void testGetStoreId() {
        assertEquals(storeId, storeBasket.getStoreId());
    }

    @Test
    void testAddProduct() throws NonExistentData {
        int productId3 = 103;
        int quantity3 = 2;
        storeBasket.addProduct(new ProductRecord(storeId, productId3, "test", 10, Category.PETS, quantity3, 10, 1));

        assertEquals(quantity3, storeBasket.getProductRecord(productId3).quantity());
    }

    @Test
    void testUpdateProductQuantity() throws NonExistentData {
        int newQuantity = 5;
        storeBasket.updateProductRecord(new ProductRecord(storeId, productId1, "test", 10, Category.ART,newQuantity, 10, 1));
        assertEquals(newQuantity, storeBasket.getProductRecord(productId1).quantity());
    }

    @Test
    void testRemoveProduct() {
        try {
            storeBasket.removeProduct(productId1);
            assertFalse(storeBasket.isProductExists(productId1));
        } catch (NonExistentData e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    void testRemoveProductNonExistingProduct() {
        int nonExistingProductId = 999;
        assertThrows(NonExistentData.class, () -> storeBasket.removeProduct(nonExistingProductId));
    }

    @Test
    void testGetQuantityOfProduct() throws NonExistentData {
        assertEquals(initialQuantity, storeBasket.getProductRecord(productId1).quantity());
    }

    @Test
    void testGetQuantityOfProductNonExistingProduct() {
        int nonExistingProductId = 999;
        try{
            storeBasket.getProductRecord(nonExistingProductId).quantity();
            assertFalse(true);
        }
        catch (Exception e){
            assertTrue(true);
        }

    }

    @Test
    void testIsProductExists() {
        assertTrue(storeBasket.isProductExists(productId1));
        assertFalse(storeBasket.isProductExists(productId2));
    }
}