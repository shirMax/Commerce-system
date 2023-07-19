package UnitTests.DataLayerTests;

import DataLayer.DbConfig;
import DataLayer.Store.IStoreRepo;
import DataLayer.Store.ORM.*;
import DataLayer.Store.StoreRepo;
import Domain.Store.Category;
import Domain.Store.Conditions.*;
import Domain.Store.Discount.DiscountTypes.Composite.*;
import Domain.Store.Discount.DiscountTypes.Simple.CategoryDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.ProductDiscount;
import Domain.Store.Discount.DiscountTypes.Simple.StoreDiscount;
import Domain.Store.Discount.IDiscount;
import Domain.Store.IProduct;
import Domain.Store.IStore;
import Domain.Store.Purchase.AndPurchaseRule;
import Domain.Store.Purchase.IfThenPurchaseRule;
import Domain.Store.Purchase.OrPurchaseRule;
import Domain.Store.Purchase.PurchaseRule;
import Domain.User.Member;
import com.stripe.model.Discount;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class Store extends PersistenceTest {

    private static final UserRecord FOUNDER = new UserRecord("Founder", "founder@gmail.com", "0541234567", LocalDate.of(1997, 1, 1));
    private static final StoreRecord STORE_DATA = new StoreRecord("Store1", "Store1 desc");
    private static final ProductRecord PRODUCT_DATA = new ProductRecord("Prod1", 5, Category.BEAUTY, 10);
    private static IStoreRepo REPO;

    @BeforeEach
    void createFounder(){
        new Member("1", FOUNDER, "u12345678");
    }

    @BeforeEach
    void initRepo(){
        REPO = new StoreRepo();
    }

    @Test
    void Persist_Store(){
        try {
            // Normal behaviour
            assertTrue(REPO.getStoreMap().isEmpty(), "At the beginning the repo should be empty");
            int storeID = REPO.openNewStore(FOUNDER.username(), STORE_DATA);

            // Check existence of data
            assertEquals(1, REPO.getStoreMap().size(), "1 store added - 1 should exist");
            IStore store = REPO.getStore(storeID);
            assertEquals(STORE_DATA.storeName(), store.getStoreName());
            assertEquals(STORE_DATA.storeDescription(), store.getStoreDescription());

            // Check existence of data after reopen
            closeReopen();

            assertEquals(1, REPO.getStoreMap().size(), "1 store added - 1 should exist");
            store = REPO.getStore(storeID);
            assertEquals(STORE_DATA.storeName(), store.getStoreName());
            assertEquals(STORE_DATA.storeDescription(), store.getStoreDescription());

            // Test removal
            REPO.removeStore(storeID);
            closeReopen();

            assertThrows(NonExistentData.class, () -> REPO.getStore(storeID));
        } catch (Exception e) {
            e.printStackTrace();

            fail();
        }
    }

    @Test
    void Persist_Product(){
        try {
            // Data
            int storeID = REPO.openNewStore(FOUNDER.username(), STORE_DATA);
            IStore store = REPO.getStore(storeID);

            // Normal behaviour
            assertTrue(store.getProducts().isEmpty(), "At the beginning the store should be empty");
            int prodID = store.addNewProduct(FOUNDER.username(), PRODUCT_DATA);

            // Check existence of data
            assertEquals(1, store.getProducts().size(), "1 store added - 1 should exist");
            IProduct product = store.getProduct(prodID);
            assertEquals(PRODUCT_DATA.productName(), product.getProductName());
            assertEquals(PRODUCT_DATA.productPrice(), product.getProductPrice());
            assertEquals(PRODUCT_DATA.productCategory(), product.getProductCategory());
            assertEquals(PRODUCT_DATA.quantity(), product.getProductQuantity());

            // Check existence of data after reopen
            closeReopen();

            store = REPO.getStore(storeID);
            assertEquals(1, store.getProducts().size(), "1 store added - 1 should exist");
            product = store.getProduct(prodID);
            assertEquals(PRODUCT_DATA.productName(), product.getProductName());
            assertEquals(PRODUCT_DATA.productPrice(), product.getProductPrice());
            assertEquals(PRODUCT_DATA.productCategory(), product.getProductCategory());
            assertEquals(PRODUCT_DATA.quantity(), product.getProductQuantity());

            // Test removal
            store.removeProduct(FOUNDER.username(), prodID);
            closeReopen();

            store = REPO.getStore(storeID);
            IStore finalStore = store;
            assertThrows(NonExistentData.class, () -> finalStore.getProduct(prodID));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Product(){
        try {
            // Preparation
            int storeID = REPO.openNewStore(FOUNDER.username(), STORE_DATA);
            IStore store = REPO.getStore(storeID);
            store.addNewProduct(FOUNDER.username(), PRODUCT_DATA);

            // Test cascade
            closeReopen();
            REPO.removeStore(storeID);

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataProduct> query = session.createQuery("FROM DataProduct p WHERE p.key.store.id = :store_id", DataProduct.class);
                query.setParameter("store_id", storeID);
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Persist_PurchaseRule(){
        try {
            // Data
            int storeID = REPO.openNewStore(FOUNDER.username(), STORE_DATA);
            IStore store = REPO.getStore(storeID);
            PurchaseRule purchaseRule1 = new IfThenPurchaseRule(ConditionFactory.alcoholAge(), ConditionFactory.alcoholTime());
            PurchaseRule purchaseRule2 =
                    new AndPurchaseRule(
                            Set.of(
                                    ConditionFactory.atLeastQuantity(4),
                                    ConditionFactory.atLeastQuantity(6, 3),
                                    ConditionFactory.atLeastQuantity(7, Category.BEAUTY)
                            )
                    );
            PurchaseRule purchaseRule3 =
                    new OrPurchaseRule(
                            Set.of(
                                    ConditionFactory.limitQuantity(14),
                                    ConditionFactory.limitQuantity(16, 13),
                                    ConditionFactory.limitQuantity(17, Category.AUTOMOTIVE),
                                    ConditionFactory.minBasketPrice(7.1)
                            )
                    );

            // Normal behaviour
            assertTrue(store.getStorePurchaseRules().isEmpty(), "At the beginning the store should have no rule");
            int rule1ID = store.addPurchaseRule(FOUNDER.username(), purchaseRule1);
            int rule2ID = store.addPurchaseRule(FOUNDER.username(), purchaseRule2);
            int rule3ID = store.addPurchaseRule(FOUNDER.username(), purchaseRule3);

            // Check existence of data
            assertEquals(3, store.getStorePurchaseRules().size());
            PurchaseRule rule1 = store.getStorePurchaseRules().get(rule1ID);
            PurchaseRule rule2 = store.getStorePurchaseRules().get(rule2ID);
            PurchaseRule rule3 = store.getStorePurchaseRules().get(rule3ID);
            assertEquals(purchaseRule1, rule1);
            assertEquals(purchaseRule2, rule2);
            assertEquals(purchaseRule3, rule3);

            // Check existence of data after reopen
            closeReopen();
            store = REPO.getStore(storeID);

            assertEquals(3, store.getStorePurchaseRules().size());
            rule1 = store.getStorePurchaseRules().get(rule1ID);
            rule2 = store.getStorePurchaseRules().get(rule2ID);
            rule3 = store.getStorePurchaseRules().get(rule3ID);
            assertEquals(purchaseRule1, rule1);
            assertEquals(purchaseRule2, rule2);
            assertEquals(purchaseRule3, rule3);

            // Test removal
            store.removePurchaseRule(FOUNDER.username(), rule1ID);
            store.removePurchaseRule(FOUNDER.username(), rule2ID);
            store.removePurchaseRule(FOUNDER.username(), rule3ID);
            closeReopen();

            store = REPO.getStore(storeID);
            assertEquals(0, store.getStorePurchaseRules().size());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_PurchaseRule(){
        try {
            // Preparation
            int storeID = REPO.openNewStore(FOUNDER.username(), STORE_DATA);
            IStore store = REPO.getStore(storeID);
            PurchaseRule purchaseRule1 = new IfThenPurchaseRule(ConditionFactory.alcoholAge(), ConditionFactory.alcoholTime());
            PurchaseRule purchaseRule2 =
                    new AndPurchaseRule(
                            Set.of(
                                    ConditionFactory.atLeastQuantity(4),
                                    ConditionFactory.atLeastQuantity(6, 3),
                                    ConditionFactory.atLeastQuantity(7, Category.BEAUTY)
                            )
                    );
            PurchaseRule purchaseRule3 =
                    new OrPurchaseRule(
                            Set.of(
                                    ConditionFactory.limitQuantity(14),
                                    ConditionFactory.limitQuantity(16, 13),
                                    ConditionFactory.limitQuantity(17, Category.AUTOMOTIVE),
                                    ConditionFactory.minBasketPrice(7.1)
                            )
                    );
            int rule1ID = store.addPurchaseRule(FOUNDER.username(), purchaseRule1);
            int rule2ID = store.addPurchaseRule(FOUNDER.username(), purchaseRule2);
            int rule3ID = store.addPurchaseRule(FOUNDER.username(), purchaseRule3);
            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataPurchaseRule> query = session.createQuery("FROM DataPurchaseRule", DataPurchaseRule.class);
                assertEquals(3, query.list().size());
                Query<DataCondition> query2 = session.createQuery("FROM DataCondition", DataCondition.class);
                assertEquals(9, query2.list().size());
            }

            // Test cascade
            closeReopen();
            REPO.removeStore(storeID);

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataPurchaseRule> query = session.createQuery("FROM DataPurchaseRule", DataPurchaseRule.class);
                assertTrue(query.list().isEmpty());
                Query<DataCondition> query2 = session.createQuery("FROM DataCondition", DataCondition.class);
                assertTrue(query2.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Persist_Discount(){
        try {
            // Data
            int storeID = REPO.openNewStore(FOUNDER.username(), STORE_DATA);
            IStore store = REPO.getStore(storeID);
            int productID = store.addNewProduct(FOUNDER.username(), PRODUCT_DATA);
            IDiscount discount1 = new StoreDiscount(10);
            IDiscount discount2 = new CategoryDiscount(11, Category.AUTOMOTIVE);
            IDiscount discount3 = new ProductDiscount(12, productID);
            IDiscount discount4 = new XorDiscount(new StoreDiscount(13), new ProductDiscount(14, productID));
            IDiscount discount5 =
                    new OrDiscount(
                            Set.of(
                                    ConditionFactory.atLeastQuantity(4),
                                    ConditionFactory.atLeastQuantity(6, 3),
                                    ConditionFactory.atLeastQuantity(7, Category.BEAUTY)
                            ),
                            new XorDiscount(new StoreDiscount(15), new StoreDiscount(16))
                    );
            IDiscount discount6 =
                    new MaxDiscount(
                            Set.of(
                                    new StoreDiscount(17),
                                    new ProductDiscount(18, productID),
                                    new CategoryDiscount(19, Category.BEAUTY)
                            )
                    );
            IDiscount discount7 =
                    new IfThenDiscount(
                            ConditionFactory.limitQuantity(14),
                            new IfThenDiscount(
                                    ConditionFactory.limitQuantity(16, 13),
                                    new StoreDiscount(20)
                            )
                    );
            IDiscount discount8 =
                    new AndDiscount(
                            Set.of(
                                    ConditionFactory.limitQuantity(17, Category.AUTOMOTIVE),
                                    ConditionFactory.minBasketPrice(7.1)
                            ),
                            new StoreDiscount(21)
                    );
            IDiscount discount9 = new AddDiscount(Set.of(), Set.of(), Set.of());

            // Normal behaviour
            assertTrue(store.getStoreDiscounts().isEmpty(), "At the beginning the store should have no discount");
            int discount1ID = store.addDiscount(FOUNDER.username(), discount1);
            int discount2ID = store.addDiscount(FOUNDER.username(), discount2);
            int discount3ID = store.addDiscount(FOUNDER.username(), discount3);
            int discount4ID = store.addDiscount(FOUNDER.username(), discount4);
            int discount5ID = store.addDiscount(FOUNDER.username(), discount5);
            int discount6ID = store.addDiscount(FOUNDER.username(), discount6);
            int discount7ID = store.addDiscount(FOUNDER.username(), discount7);
            int discount8ID = store.addDiscount(FOUNDER.username(), discount8);
            int discount9ID = store.addDiscount(FOUNDER.username(), discount9);

            // Check existence of data
            assertEquals(9, store.getStoreDiscounts().size());
            IDiscount disc1 = store.getStoreDiscount(discount1ID);
            IDiscount disc2 = store.getStoreDiscount(discount2ID);
            IDiscount disc3 = store.getStoreDiscount(discount3ID);
            IDiscount disc4 = store.getStoreDiscount(discount4ID);
            IDiscount disc5 = store.getStoreDiscount(discount5ID);
            IDiscount disc6 = store.getStoreDiscount(discount6ID);
            IDiscount disc7 = store.getStoreDiscount(discount7ID);
            IDiscount disc8 = store.getStoreDiscount(discount8ID);
            IDiscount disc9 = store.getStoreDiscount(discount9ID);
            assertEquals(discount1, disc1);
            assertEquals(discount2, disc2);
            assertEquals(discount3, disc3);
            assertEquals(discount4, disc4);
            assertEquals(discount5, disc5);
            assertEquals(discount6, disc6);
            assertEquals(discount7, disc7);
            assertEquals(discount8, disc8);
            assertEquals(discount9, disc9);

            // Check existence of data after reopen
            closeReopen();
            store = REPO.getStore(storeID);

            assertEquals(9, store.getStoreDiscounts().size());
            disc1 = store.getStoreDiscount(discount1ID);
            disc2 = store.getStoreDiscount(discount2ID);
            disc3 = store.getStoreDiscount(discount3ID);
            disc4 = store.getStoreDiscount(discount4ID);
            disc5 = store.getStoreDiscount(discount5ID);
            disc6 = store.getStoreDiscount(discount6ID);
            disc7 = store.getStoreDiscount(discount7ID);
            disc8 = store.getStoreDiscount(discount8ID);
            disc9 = store.getStoreDiscount(discount9ID);
            assertEquals(discount1, disc1);
            assertEquals(discount2, disc2);
            assertEquals(discount3, disc3);
            assertEquals(discount4, disc4);
            assertEquals(discount5, disc5);
            assertEquals(discount6, disc6);
            assertEquals(discount7, disc7);
            assertEquals(discount8, disc8);
            assertEquals(discount9, disc9);

            // Test removal
            store.removeDiscount(FOUNDER.username(), discount1ID);
            store.removeDiscount(FOUNDER.username(), discount3ID);
            store.removeDiscount(FOUNDER.username(), discount5ID);
            store.removeDiscount(FOUNDER.username(), discount7ID);
            store.removeDiscount(FOUNDER.username(), discount9ID);
            closeReopen();

            store = REPO.getStore(storeID);
            assertEquals(4, store.getStoreDiscounts().size());
            disc2 = store.getStoreDiscount(discount2ID);
            disc4 = store.getStoreDiscount(discount4ID);
            disc6 = store.getStoreDiscount(discount6ID);
            disc8 = store.getStoreDiscount(discount8ID);
            assertEquals(discount2, disc2);
            assertEquals(discount4, disc4);
            assertEquals(discount6, disc6);
            assertEquals(discount8, disc8);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Discount_ByStore(){
        try {
            // Preparation
            int storeID = REPO.openNewStore(FOUNDER.username(), STORE_DATA);
            IStore store = REPO.getStore(storeID);
            int productID = store.addNewProduct(FOUNDER.username(), PRODUCT_DATA);
            IDiscount discount1 = new StoreDiscount(10);
            IDiscount discount2 = new CategoryDiscount(11, Category.AUTOMOTIVE);
            IDiscount discount3 = new ProductDiscount(12, productID);
            IDiscount discount4 = new XorDiscount(new StoreDiscount(13), new ProductDiscount(14, productID));
            IDiscount discount5 =
                    new OrDiscount(
                            Set.of(
                                    ConditionFactory.atLeastQuantity(4),
                                    ConditionFactory.atLeastQuantity(6, 3),
                                    ConditionFactory.atLeastQuantity(7, Category.BEAUTY)
                            ),
                            new XorDiscount(new StoreDiscount(15), new StoreDiscount(16))
                    );
            IDiscount discount6 =
                    new MaxDiscount(
                            Set.of(
                                    new StoreDiscount(17),
                                    new ProductDiscount(18, productID),
                                    new CategoryDiscount(19, Category.BEAUTY)
                            )
                    );
            IDiscount discount7 =
                    new IfThenDiscount(
                            ConditionFactory.limitQuantity(14),
                            new IfThenDiscount(
                                    ConditionFactory.limitQuantity(16, 13),
                                    new StoreDiscount(20)
                            )
                    );
            IDiscount discount8 =
                    new AndDiscount(
                            Set.of(
                                    ConditionFactory.limitQuantity(17, Category.AUTOMOTIVE),
                                    ConditionFactory.minBasketPrice(7.1)
                            ),
                            new StoreDiscount(21)
                    );
            IDiscount discount9 = new AddDiscount(Set.of(), Set.of(), Set.of());
            int discount1ID = store.addDiscount(FOUNDER.username(), discount1);
            int discount2ID = store.addDiscount(FOUNDER.username(), discount2);
            int discount3ID = store.addDiscount(FOUNDER.username(), discount3);
            int discount4ID = store.addDiscount(FOUNDER.username(), discount4);
            int discount5ID = store.addDiscount(FOUNDER.username(), discount5);
            int discount6ID = store.addDiscount(FOUNDER.username(), discount6);
            int discount7ID = store.addDiscount(FOUNDER.username(), discount7);
            int discount8ID = store.addDiscount(FOUNDER.username(), discount8);
            int discount9ID = store.addDiscount(FOUNDER.username(), discount9);
            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataPurchaseRule> query = session.createQuery("FROM DataDiscount", DataPurchaseRule.class);
                assertEquals(20, query.list().size());
                Query<DataCondition> query2 = session.createQuery("FROM DataCondition", DataCondition.class);
                assertEquals(7, query2.list().size());
            }

            // Test cascade
            closeReopen();
            REPO.removeStore(storeID);

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataPurchaseRule> query = session.createQuery("FROM DataDiscount", DataPurchaseRule.class);
                assertTrue(query.list().isEmpty());
                Query<DataCondition> query2 = session.createQuery("FROM DataCondition", DataCondition.class);
                assertTrue(query2.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Discount_ByDiscount(){
        try {
            // Preparation
            int storeID = REPO.openNewStore(FOUNDER.username(), STORE_DATA);
            IStore store = REPO.getStore(storeID);
            IDiscount discount1 = new StoreDiscount(10);
            IDiscount discount5 =
                    new OrDiscount(
                            Set.of(
                                    ConditionFactory.atLeastQuantity(4),
                                    ConditionFactory.atLeastQuantity(6, 3),
                                    ConditionFactory.atLeastQuantity(7, Category.BEAUTY)
                            ),
                            new XorDiscount(new StoreDiscount(15), new StoreDiscount(16))
                    );
            int discount1ID = store.addDiscount(FOUNDER.username(), discount1);
            int discount5ID = store.addDiscount(FOUNDER.username(), discount5);
            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataPurchaseRule> query = session.createQuery("FROM DataDiscount", DataPurchaseRule.class);
                assertEquals(5, query.list().size());
                Query<DataCondition> query2 = session.createQuery("FROM DataCondition", DataCondition.class);
                assertEquals(3, query2.list().size());
            }

            // Test cascade
            closeReopen();
            store = REPO.getStore(storeID);
            store.removeDiscount(FOUNDER.username(), discount5ID);

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataPurchaseRule> query = session.createQuery("FROM DataDiscount", DataPurchaseRule.class);
                assertEquals(1, query.list().size());
                Query<DataCondition> query2 = session.createQuery("FROM DataCondition", DataCondition.class);
                assertTrue(query2.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Discount_ByProduct(){
        try {
            // Preparation
            int storeID = REPO.openNewStore(FOUNDER.username(), STORE_DATA);
            IStore store = REPO.getStore(storeID);
            int productID = store.addNewProduct(FOUNDER.username(), PRODUCT_DATA);
            IDiscount discount1 = new StoreDiscount(10);
            IDiscount discount3 = new ProductDiscount(12, productID);
            IDiscount discount6 =
                    new MaxDiscount(
                            Set.of(
                                    new StoreDiscount(17),
                                    new ProductDiscount(18, productID),
                                    new CategoryDiscount(19, Category.BEAUTY)
                            )
                    );
            int discount1ID = store.addDiscount(FOUNDER.username(), discount1);
            int discount3ID = store.addDiscount(FOUNDER.username(), discount3);
            int discount6ID = store.addDiscount(FOUNDER.username(), discount6);
            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataPurchaseRule> query = session.createQuery("FROM DataDiscount", DataPurchaseRule.class);
                assertEquals(6, query.list().size());
                Query<DataCondition> query2 = session.createQuery("FROM DataCondition", DataCondition.class);
                assertTrue(query2.list().isEmpty());
            }

            // Test cascade
            closeReopen();
            store = REPO.getStore(storeID);
            store.removeProduct(FOUNDER.username(), productID);

            assertEquals(1, store.getStoreDiscounts().size());
            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataPurchaseRule> query = session.createQuery("FROM DataDiscount", DataPurchaseRule.class);
                assertEquals(1, query.list().size());
                Query<DataCondition> query2 = session.createQuery("FROM DataCondition", DataCondition.class);
                assertTrue(query2.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
