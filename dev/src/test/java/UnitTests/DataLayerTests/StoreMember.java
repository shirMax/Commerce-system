package UnitTests.DataLayerTests;

import DataLayer.DbConfig;
import DataLayer.ITransactionRepo;
import DataLayer.ORM.DataPermission;
import DataLayer.ORM.DataTransaction;
import DataLayer.Store.IStoreRepo;
import DataLayer.Store.ORM.DataOffer;
import DataLayer.Store.ORM.DataOfferConsent;
import DataLayer.Store.StoreRepo;
import DataLayer.TransactionRepo;
import DataLayer.User.IUserRepo;
import DataLayer.User.ORM.DataBaskedProduct;
import DataLayer.User.ORM.DataBasket;
import DataLayer.User.UserRepo;
import Domain.Permission;
import Domain.Store.Category;
import Domain.Store.IProduct;
import Domain.Store.IStore;
import Domain.Store.Offer;
import Domain.User.Member;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import util.Enums.PermissionType;
import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.Transaction;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class StoreMember extends PersistenceTest{
    private static final UserRecord FOUNDER = new UserRecord("Founder1", "founder@gmail.com", "0501234567", LocalDate.of(1997,1,1));
    private static final UserRecord OWNER = new UserRecord("Owner", "owner@email.com", "0501234567", LocalDate.of(1997, 1, 1));
    private static final UserRecord MANAGER1 = new UserRecord("Manager1", "manager1@email.com", "0501234567", LocalDate.of(1997, 1, 1));
    private static final UserRecord MANAGER2 = new UserRecord("Manager2", "manager2@email.com", "0501234567", LocalDate.of(1997, 1, 1));
    private static final UserRecord USER1 = new UserRecord("User1", "user1@email.com", "0501234567", LocalDate.of(1997, 1, 1));
    private static final String FOUNDER_SESSION = "founderSess";
    private static final String OWNER_SESSION = "ownerSess";
    private static final String MANAGER1_SESSION = "manager1Sess";
    private static final String MANAGER2_SESSION = "manager2Sess";
    private static final String USER1_SESSION = "user1Sess";
    private static final String FOUNDER_PASSWORD = "u1234567";
    private static final String OWNER_PASSWORD = "u1234567";
    private static final String MANAGER1_PASSWORD = "u1234567";
    private static final String MANAGER2_PASSWORD = "u1234567";
    private static final String USER1_PASSWORD = "u1234567";
    private static final StoreRecord STORE1_DATA = new StoreRecord("Store1", "Store1 desc");
    private static final StoreRecord STORE2_DATA = new StoreRecord("Store2", "Store2 desc");
    private static final ProductRecord PRODUCT1_DATA = new ProductRecord("Prod1", 5, Category.BEAUTY, 10);
    private static final ProductRecord PRODUCT2_DATA = new ProductRecord("Prod2", 7, Category.AUTOMOTIVE, 15);
    private static final ProductRecord PRODUCT3_DATA = new ProductRecord("Prod3", 8, Category.MUSIC, 20);
    private static IStoreRepo STORE_REPO;
    private static IUserRepo USER_REPO;
    private static ITransactionRepo TRANSACTION_REPO;

    @BeforeEach
    void initRepo(){
        STORE_REPO = new StoreRepo();
        USER_REPO = new UserRepo();
        TRANSACTION_REPO = new TransactionRepo();
    }

    @Test
    void Persist_Permission(){
        try {
            // Data
            Set<PermissionType> manager2Perms = Set.of(PermissionType.CHANGE_OWNER_PERMISSIONS, PermissionType.GET_EMPLOYEES_DATA, PermissionType.MANAGE_STORE_MANAGER);

            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(OWNER_SESSION, OWNER, OWNER_PASSWORD);
            USER_REPO.addMember(MANAGER1_SESSION, MANAGER1, MANAGER1_PASSWORD);
            USER_REPO.addMember(MANAGER2_SESSION, MANAGER2, MANAGER2_PASSWORD);
            int storeID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            IStore store = STORE_REPO.getStore(storeID);

            // Normal behaviour
            Map<String, Permission> permissions = store.getPermissions();
            assertEquals(1, permissions.size(), "At the beginning only the founder permissions should exist");
            assertTrue(permissions.containsKey(FOUNDER.username()));
            store.assignStoreOwner(FOUNDER.username(), OWNER.username());
            store.assignStoreManager(OWNER.username(), MANAGER1.username());
            store.assignStoreManager(OWNER.username(), MANAGER2.username());
            store.setManagerPermissions(OWNER.username(), MANAGER2.username(), manager2Perms);

            // Check data
            assertEquals(4, permissions.size());
            assertTrue(
                    permissions.containsKey(FOUNDER.username()) &&
                            permissions.containsKey(OWNER.username()) &&
                            permissions.containsKey(MANAGER1.username()) &&
                            permissions.containsKey(MANAGER2.username())
            );
            assertEquals(FOUNDER.username(), permissions.get(FOUNDER.username()).getPermissionGiverName());
            assertTrue(permissions.get(FOUNDER.username()).isStoreFounder());
            assertEquals(
                    PermissionType.collectionToBitmap(Permission.FOUNDER_PERMISSIONS),
                    permissions.get(FOUNDER.username()).permissionsBitMap()
            );
            assertEquals(FOUNDER.username(), permissions.get(OWNER.username()).getPermissionGiverName());
            assertTrue(permissions.get(OWNER.username()).isStoreOwner());
            assertEquals(
                    PermissionType.collectionToBitmap(Permission.OWNER_PERMISSIONS),
                    permissions.get(OWNER.username()).permissionsBitMap()
            );
            assertEquals(OWNER.username(), permissions.get(MANAGER1.username()).getPermissionGiverName());
            assertTrue(permissions.get(MANAGER1.username()).isStoreManager());
            assertEquals(
                    PermissionType.collectionToBitmap(Permission.MANAGER_PERMISSIONS),
                    permissions.get(MANAGER1.username()).permissionsBitMap()
            );
            assertEquals(OWNER.username(), permissions.get(MANAGER2.username()).getPermissionGiverName());
            assertTrue(permissions.get(MANAGER2.username()).isStoreManager());
            assertEquals(
                    PermissionType.collectionToBitmap(manager2Perms),
                    permissions.get(MANAGER2.username()).permissionsBitMap()
            );

            // Check data after reopen
            closeReopen();
            store = STORE_REPO.getStore(storeID);
            permissions = store.getPermissions();

            assertEquals(4, permissions.size());
            assertTrue(
                    permissions.containsKey(FOUNDER.username()) &&
                            permissions.containsKey(OWNER.username()) &&
                            permissions.containsKey(MANAGER1.username()) &&
                            permissions.containsKey(MANAGER2.username())
            );
            assertEquals(FOUNDER.username(), permissions.get(FOUNDER.username()).getPermissionGiverName());
            assertTrue(permissions.get(FOUNDER.username()).isStoreFounder());
            assertEquals(
                    PermissionType.collectionToBitmap(Permission.FOUNDER_PERMISSIONS),
                    permissions.get(FOUNDER.username()).permissionsBitMap()
            );
            assertEquals(FOUNDER.username(), permissions.get(OWNER.username()).getPermissionGiverName());
            assertTrue(permissions.get(OWNER.username()).isStoreOwner());
            assertEquals(
                    PermissionType.collectionToBitmap(Permission.OWNER_PERMISSIONS),
                    permissions.get(OWNER.username()).permissionsBitMap()
            );
            assertEquals(OWNER.username(), permissions.get(MANAGER1.username()).getPermissionGiverName());
            assertTrue(permissions.get(MANAGER1.username()).isStoreManager());
            assertEquals(
                    PermissionType.collectionToBitmap(Permission.MANAGER_PERMISSIONS),
                    permissions.get(MANAGER1.username()).permissionsBitMap()
            );
            assertEquals(OWNER.username(), permissions.get(MANAGER2.username()).getPermissionGiverName());
            assertTrue(permissions.get(MANAGER2.username()).isStoreManager());
            assertEquals(
                    PermissionType.collectionToBitmap(manager2Perms),
                    permissions.get(MANAGER2.username()).permissionsBitMap()
            );

            // check removal
            store.removeStoreOwnerAppointment(FOUNDER.username(), OWNER.username());
            closeReopen();

            store = STORE_REPO.getStore(storeID);
            permissions = store.getPermissions();
            assertEquals(1, permissions.size(), "At the beginning only the founder permissions should exist");
            assertTrue(permissions.containsKey(FOUNDER.username()));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Permission_ByStore(){
        try {
            // Data
            Set<PermissionType> manager2Perms = Set.of(PermissionType.CHANGE_OWNER_PERMISSIONS, PermissionType.GET_EMPLOYEES_DATA, PermissionType.MANAGE_STORE_MANAGER);

            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(OWNER_SESSION, OWNER, OWNER_PASSWORD);
            USER_REPO.addMember(MANAGER1_SESSION, MANAGER1, MANAGER1_PASSWORD);
            USER_REPO.addMember(MANAGER2_SESSION, MANAGER2, MANAGER2_PASSWORD);
            int storeID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            IStore store = STORE_REPO.getStore(storeID);
            store.assignStoreOwner(FOUNDER.username(), OWNER.username());
            store.assignStoreManager(OWNER.username(), MANAGER1.username());
            store.assignStoreManager(OWNER.username(), MANAGER2.username());
            store.setManagerPermissions(OWNER.username(), MANAGER2.username(), manager2Perms);

            // Test Cascade
            closeReopen();
            STORE_REPO.removeStore(storeID);

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataPermission> query = session.createQuery("FROM DataPermission p WHERE p.key.store.id = :store_id", DataPermission.class);
                query.setParameter("store_id", storeID);
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Permission_ByMember(){
        try {
            // Data
            Set<PermissionType> manager2Perms = Set.of(PermissionType.CHANGE_OWNER_PERMISSIONS, PermissionType.GET_EMPLOYEES_DATA, PermissionType.MANAGE_STORE_MANAGER);

            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(OWNER_SESSION, OWNER, OWNER_PASSWORD);
            USER_REPO.addMember(MANAGER1_SESSION, MANAGER1, MANAGER1_PASSWORD);
            USER_REPO.addMember(MANAGER2_SESSION, MANAGER2, MANAGER2_PASSWORD);
            int storeID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            IStore store = STORE_REPO.getStore(storeID);
            store.assignStoreOwner(FOUNDER.username(), OWNER.username());
            store.assignStoreManager(OWNER.username(), MANAGER1.username());
            store.assignStoreManager(OWNER.username(), MANAGER2.username());
            store.setManagerPermissions(OWNER.username(), MANAGER2.username(), manager2Perms);

            /// Test Cascade
            // Cascade non-appointing manager
            closeReopen();
            USER_REPO.removeMember(MANAGER2.username());

            store = STORE_REPO.getStore(storeID);
            Map<String, Permission> permissions = store.getPermissions();
            assertFalse(permissions.containsKey(MANAGER2.username()));

            // Cascade appointing owner.
            /* TODO: make sure this needs to be reinforced.
            closeReopen();
            USER_REPO.removeMember(OWNER.username());

            store = STORE_REPO.getStore(storeID);
            permissions = store.getPermissions();
            assertFalse(permissions.containsKey(OWNER.username()));
            assertFalse(permissions.containsKey(MANAGER1.username()));
             */
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Persist_Basket(){
        try {
            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            Member member = USER_REPO.getMember(USER1.username());
            int store1ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            int store2ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE2_DATA);
            IStore store1 = STORE_REPO.getStore(store1ID);
            IStore store2 = STORE_REPO.getStore(store2ID);
            int prod1ID = store1.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            int prod2ID = store2.addNewProduct(FOUNDER.username(), PRODUCT2_DATA);
            int prod3ID = store2.addNewProduct(FOUNDER.username(), PRODUCT3_DATA);
            IProduct product1 = store1.getProduct(prod1ID);
            IProduct product2 = store2.getProduct(prod2ID);
            IProduct product3 = store2.getProduct(prod3ID);

            // Add products to userCart
            member.addProductsToStoreBasket(store1ID, List.of(new ProductRecord(product1)));
            member.addProductsToStoreBasket(store2ID, List.of(new ProductRecord(product2), new ProductRecord(product3)));

            // Check data existence
            ProductRecord basked1 = member.getStoreBasketProducts(store1ID).get(prod1ID);
            ProductRecord basked2 = member.getStoreBasketProducts(store2ID).get(prod2ID);
            ProductRecord basked3 = member.getStoreBasketProducts(store2ID).get(prod3ID);
            assertEquals(PRODUCT1_DATA.productName(), basked1.productName());
            assertEquals(PRODUCT1_DATA.productPrice(), basked1.productPrice());
            assertEquals(PRODUCT1_DATA.productCategory(), basked1.productCategory());

            assertEquals(PRODUCT2_DATA.productName(), basked2.productName());
            assertEquals(PRODUCT2_DATA.productPrice(), basked2.productPrice());
            assertEquals(PRODUCT2_DATA.productCategory(), basked2.productCategory());

            assertEquals(PRODUCT3_DATA.productName(), basked3.productName());
            assertEquals(PRODUCT3_DATA.productPrice(), basked3.productPrice());
            assertEquals(PRODUCT3_DATA.productCategory(), basked3.productCategory());

            // Check data existence after reopen
            closeReopen();
            member = USER_REPO.getMember(USER1.username());
            basked1 = member.getStoreBasketProducts(store1ID).get(prod1ID);
            basked2 = member.getStoreBasketProducts(store2ID).get(prod2ID);
            basked3 = member.getStoreBasketProducts(store2ID).get(prod3ID);

            assertEquals(PRODUCT1_DATA.productName(), basked1.productName());
            assertEquals(PRODUCT1_DATA.productPrice(), basked1.productPrice());
            assertEquals(PRODUCT1_DATA.productCategory(), basked1.productCategory());

            assertEquals(PRODUCT2_DATA.productName(), basked2.productName());
            assertEquals(PRODUCT2_DATA.productPrice(), basked2.productPrice());
            assertEquals(PRODUCT2_DATA.productCategory(), basked2.productCategory());

            assertEquals(PRODUCT3_DATA.productName(), basked3.productName());
            assertEquals(PRODUCT3_DATA.productPrice(), basked3.productPrice());
            assertEquals(PRODUCT3_DATA.productCategory(), basked3.productCategory());

            // Test removal persistence
            member.removeProductFromStoreBasket(store1ID, prod1ID, PRODUCT1_DATA.quantity());
            member.removeProductFromStoreBasket(store2ID, prod2ID, PRODUCT2_DATA.quantity());
            closeReopen();

            member = USER_REPO.getMember(USER1.username());
            Member finalMember = member;
            assertThrows(NonExistentData.class, () -> finalMember.getStoreBasket(store1ID).getProductRecord(prod1ID));
            assertThrows(NonExistentData.class, () -> finalMember.getStoreBasket(store2ID).getProductRecord(prod2ID));
            assertDoesNotThrow(() -> finalMember.getStoreBasket(store2ID).getProductRecord(prod3ID));


        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void Cascade_BaskedProduct_ByStore(){
        try {
            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            Member member = USER_REPO.getMember(USER1.username());
            int store1ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            int store2ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE2_DATA);
            IStore store1 = STORE_REPO.getStore(store1ID);
            IStore store2 = STORE_REPO.getStore(store2ID);
            int prod1ID = store1.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            int prod2ID = store2.addNewProduct(FOUNDER.username(), PRODUCT2_DATA);
            int prod3ID = store2.addNewProduct(FOUNDER.username(), PRODUCT3_DATA);
            IProduct product1 = store1.getProduct(prod1ID);
            IProduct product2 = store2.getProduct(prod2ID);
            IProduct product3 = store2.getProduct(prod3ID);
            member.addProductsToStoreBasket(store1ID, List.of(new ProductRecord(product1)));
            member.addProductsToStoreBasket(store2ID, List.of(new ProductRecord(product2), new ProductRecord(product3)));

            // Test Cascade
            closeReopen();
            STORE_REPO.removeStore(store1ID);

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataBaskedProduct> query = session.createQuery("FROM DataBaskedProduct p WHERE p.key.product.key.store.id = :store1_id", DataBaskedProduct.class);
                query.setParameter("store1_id", store1ID);
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_BaskedProduct_ByProduct(){
        try {
            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            Member member = USER_REPO.getMember(USER1.username());
            int store1ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            int store2ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE2_DATA);
            IStore store1 = STORE_REPO.getStore(store1ID);
            IStore store2 = STORE_REPO.getStore(store2ID);
            int prod1ID = store1.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            int prod2ID = store2.addNewProduct(FOUNDER.username(), PRODUCT2_DATA);
            int prod3ID = store2.addNewProduct(FOUNDER.username(), PRODUCT3_DATA);
            IProduct product1 = store1.getProduct(prod1ID);
            IProduct product2 = store2.getProduct(prod2ID);
            IProduct product3 = store2.getProduct(prod3ID);
            member.addProductsToStoreBasket(store1ID, List.of(new ProductRecord(product1)));
            member.addProductsToStoreBasket(store2ID, List.of(new ProductRecord(product2), new ProductRecord(product3)));

            // Test Cascade
            closeReopen();
            store1 = STORE_REPO.getStore(store1ID);
            store2 = STORE_REPO.getStore(store2ID);
            store1.removeProduct(FOUNDER.username(), prod1ID);
            store2.removeProduct(FOUNDER.username(), prod2ID);

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataBaskedProduct> query = session.createQuery("FROM DataBaskedProduct p WHERE p.key.product.key.id = :product1_id OR p.key.product.key.id = :product2_id", DataBaskedProduct.class);
                query.setParameter("product1_id", prod1ID);
                query.setParameter("product2_id", prod2ID);
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_BaskedProduct_ByMember(){
        try {
            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            Member member = USER_REPO.getMember(USER1.username());
            int store1ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            int store2ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE2_DATA);
            IStore store1 = STORE_REPO.getStore(store1ID);
            IStore store2 = STORE_REPO.getStore(store2ID);
            int prod1ID = store1.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            int prod2ID = store2.addNewProduct(FOUNDER.username(), PRODUCT2_DATA);
            int prod3ID = store2.addNewProduct(FOUNDER.username(), PRODUCT3_DATA);
            IProduct product1 = store1.getProduct(prod1ID);
            IProduct product2 = store2.getProduct(prod2ID);
            IProduct product3 = store2.getProduct(prod3ID);
            member.addProductsToStoreBasket(store1ID, List.of(new ProductRecord(product1)));
            member.addProductsToStoreBasket(store2ID, List.of(new ProductRecord(product2), new ProductRecord(product3)));

            // Test Cascade
            closeReopen();
            USER_REPO.removeMember(USER1.username());

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataBaskedProduct> query = session.createQuery("FROM DataBaskedProduct", DataBaskedProduct.class);
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Basket_ByStore(){
        try {
            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            Member member = USER_REPO.getMember(USER1.username());
            int store1ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            int store2ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE2_DATA);
            IStore store1 = STORE_REPO.getStore(store1ID);
            IStore store2 = STORE_REPO.getStore(store2ID);
            int prod1ID = store1.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            int prod2ID = store2.addNewProduct(FOUNDER.username(), PRODUCT2_DATA);
            int prod3ID = store2.addNewProduct(FOUNDER.username(), PRODUCT3_DATA);
            IProduct product1 = store1.getProduct(prod1ID);
            IProduct product2 = store2.getProduct(prod2ID);
            IProduct product3 = store2.getProduct(prod3ID);
            member.addProductsToStoreBasket(store1ID, List.of(new ProductRecord(product1)));
            member.addProductsToStoreBasket(store2ID, List.of(new ProductRecord(product2), new ProductRecord(product3)));

            // Test Cascade
            closeReopen();
            STORE_REPO.removeStore(store1ID);

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataBasket> query = session.createQuery("FROM DataBasket b WHERE b.key.store.id = :store1_id", DataBasket.class);
                query.setParameter("store1_id", store1ID);
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Basket_ByMember(){
        try {
            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            Member member = USER_REPO.getMember(USER1.username());
            int store1ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            int store2ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE2_DATA);
            IStore store1 = STORE_REPO.getStore(store1ID);
            IStore store2 = STORE_REPO.getStore(store2ID);
            int prod1ID = store1.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            int prod2ID = store2.addNewProduct(FOUNDER.username(), PRODUCT2_DATA);
            int prod3ID = store2.addNewProduct(FOUNDER.username(), PRODUCT3_DATA);
            IProduct product1 = store1.getProduct(prod1ID);
            IProduct product2 = store2.getProduct(prod2ID);
            IProduct product3 = store2.getProduct(prod3ID);
            member.addProductsToStoreBasket(store1ID, List.of(new ProductRecord(product1)));
            member.addProductsToStoreBasket(store2ID, List.of(new ProductRecord(product2), new ProductRecord(product3)));

            // Test Cascade
            closeReopen();
            USER_REPO.removeMember(USER1.username());

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataBasket> query = session.createQuery("FROM DataBasket", DataBasket.class);
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Persist_Transaction(){
        try {
            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            Member member = USER_REPO.getMember(USER1.username());
            int store1ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            int store2ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE2_DATA);
            IStore store1 = STORE_REPO.getStore(store1ID);
            IStore store2 = STORE_REPO.getStore(store2ID);
            int prod1ID = store1.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            int prod2ID = store2.addNewProduct(FOUNDER.username(), PRODUCT2_DATA);
            int prod3ID = store2.addNewProduct(FOUNDER.username(), PRODUCT3_DATA);
            IProduct product1 = store1.getProduct(prod1ID);
            IProduct product2 = store2.getProduct(prod2ID);
            IProduct product3 = store2.getProduct(prod3ID);
            member.addProductsToStoreBasket(store1ID, List.of(new ProductRecord(product1)));
            member.addProductsToStoreBasket(store2ID, List.of(new ProductRecord(product2), new ProductRecord(product3)));
            double price1 = 500;
            double price2 = 600;

            // Normal behaviour
            assertTrue(TRANSACTION_REPO.getTransactions().isEmpty(), "At the beginning only the founder permissions should exist");
            int transaction1ID = TRANSACTION_REPO.addTransaction(member.getStoreBasket(store2ID), USER1.username(), price1);
            int transaction2ID = TRANSACTION_REPO.addTransaction(member.getStoreBasket(store1ID), USER1.username(), price2);

            // Check data
            Map<Integer, Transaction> transactions = TRANSACTION_REPO.getTransactions();
            assertEquals(2, transactions.size());
            assertTrue(transactions.containsKey(transaction1ID));
            assertTrue(transactions.containsKey(transaction2ID));
            assertEquals(
                    new Transaction(transaction1ID, store2ID, USER1.username(), member.getStoreBasket(store2ID).clone(), price1, transactions.get(transaction1ID).timeStamp()),
                    transactions.get(transaction1ID)
            );
            assertEquals(
                    new Transaction(transaction2ID, store1ID, USER1.username(), member.getStoreBasket(store1ID).clone(), price2, transactions.get(transaction2ID).timeStamp()),
                    transactions.get(transaction2ID)
            );


            // Check data after reopen
            closeReopen();

            transactions = TRANSACTION_REPO.getTransactions();
            assertEquals(2, transactions.size());
            assertTrue(transactions.containsKey(transaction1ID));
            assertTrue(transactions.containsKey(transaction2ID));
            assertEquals(
                    new Transaction(transaction1ID, store2ID, USER1.username(), member.getStoreBasket(store2ID).clone(), price1, transactions.get(transaction1ID).timeStamp()),
                    transactions.get(transaction1ID)
            );
            assertEquals(
                    new Transaction(transaction2ID, store1ID, USER1.username(), member.getStoreBasket(store1ID).clone(), price2, transactions.get(transaction2ID).timeStamp()),
                    transactions.get(transaction2ID)
            );

            // check removal
            TRANSACTION_REPO.clean();
            closeReopen();

            assertTrue(TRANSACTION_REPO.getTransactions().isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Transaction_ByStore(){
        try {
            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            Member member = USER_REPO.getMember(USER1.username());
            int store1ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            int store2ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE2_DATA);
            IStore store1 = STORE_REPO.getStore(store1ID);
            IStore store2 = STORE_REPO.getStore(store2ID);
            int prod1ID = store1.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            int prod2ID = store2.addNewProduct(FOUNDER.username(), PRODUCT2_DATA);
            int prod3ID = store2.addNewProduct(FOUNDER.username(), PRODUCT3_DATA);
            IProduct product1 = store1.getProduct(prod1ID);
            IProduct product2 = store2.getProduct(prod2ID);
            IProduct product3 = store2.getProduct(prod3ID);
            member.addProductsToStoreBasket(store1ID, List.of(new ProductRecord(product1)));
            member.addProductsToStoreBasket(store2ID, List.of(new ProductRecord(product2), new ProductRecord(product3)));
            double price1 = 500;
            double price2 = 600;
            int transaction1ID = TRANSACTION_REPO.addTransaction(member.getStoreBasket(store1ID), USER1.username(), price1);
            int transaction2ID = TRANSACTION_REPO.addTransaction(member.getStoreBasket(store2ID), USER1.username(), price2);

            // Test Cascade
            closeReopen();
            STORE_REPO.removeStore(store1ID);

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataTransaction> query = session.createQuery("FROM DataTransaction t WHERE t.key.store.id = :store_id", DataTransaction.class);
                query.setParameter("store_id", store1ID);
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Transaction_ByMember(){
        try {
            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            Member member = USER_REPO.getMember(USER1.username());
            int store1ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            int store2ID = STORE_REPO.openNewStore(FOUNDER.username(), STORE2_DATA);
            IStore store1 = STORE_REPO.getStore(store1ID);
            IStore store2 = STORE_REPO.getStore(store2ID);
            int prod1ID = store1.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            int prod2ID = store2.addNewProduct(FOUNDER.username(), PRODUCT2_DATA);
            int prod3ID = store2.addNewProduct(FOUNDER.username(), PRODUCT3_DATA);
            IProduct product1 = store1.getProduct(prod1ID);
            IProduct product2 = store2.getProduct(prod2ID);
            IProduct product3 = store2.getProduct(prod3ID);
            member.addProductsToStoreBasket(store1ID, List.of(new ProductRecord(product1)));
            member.addProductsToStoreBasket(store2ID, List.of(new ProductRecord(product2), new ProductRecord(product3)));
            double price1 = 500;
            double price2 = 600;
            int transaction1ID = TRANSACTION_REPO.addTransaction(member.getStoreBasket(store1ID), USER1.username(), price1);
            int transaction2ID = TRANSACTION_REPO.addTransaction(member.getStoreBasket(store2ID), USER1.username(), price2);

            // Test Cascade
            closeReopen();
            USER_REPO.removeMember(USER1.username());

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataTransaction> query = session.createQuery("FROM DataTransaction", DataTransaction.class);
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Persist_Offer(){
        try {
            // Data
            double offeredPrice = 59.4;
            int offeredQuantity = 69;

            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            int storeID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            IStore store = STORE_REPO.getStore(storeID);
            int product1ID = store.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);

            // Normal behaviour
            Map<Integer, Offer> offers = store.getOffers();
            assertEquals(0, offers.size(), "At the beginning no offers should exist");
            Offer offer = store.publishMemberOffer(USER1.username(), product1ID, offeredPrice, offeredQuantity);


            // Check data
            assertEquals(1, offers.size());
            assertTrue(offers.containsKey(offer.getId()));
            assertEquals(offer, offers.get(offer.getId()));
            assertEquals(USER1.username(), offer.getOfferingMember());
            assertEquals(offeredPrice, offer.getOfferedPrice());
            assertEquals(offeredQuantity, offer.getOfferedQuantity());
            ProductRecord offeredProduct = offer.getProduct();
            assertEquals(storeID, offeredProduct.storeId());
            assertEquals(product1ID, offeredProduct.productId());
            assertEquals(offeredPrice, offeredProduct.productPrice());
            assertEquals(offeredQuantity, offeredProduct.quantity());


            // Check data after reopen
            closeReopen();
            store = STORE_REPO.getStore(storeID);
            offers = store.getOffers();

            assertEquals(1, offers.size());
            assertTrue(offers.containsKey(offer.getId()));
            assertEquals(offer, offers.get(offer.getId()));
            assertEquals(USER1.username(), offer.getOfferingMember());
            assertEquals(offeredPrice, offer.getOfferedPrice());
            assertEquals(offeredQuantity, offer.getOfferedQuantity());
            offeredProduct = offer.getProduct();
            assertEquals(storeID, offeredProduct.storeId());
            assertEquals(product1ID, offeredProduct.productId());
            assertEquals(offeredPrice, offeredProduct.productPrice());
            assertEquals(offeredQuantity, offeredProduct.quantity());

            // check removal
            store.removeOffer(FOUNDER.username(), offer.getId());
            closeReopen();

            store = STORE_REPO.getStore(storeID);
            offers = store.getOffers();
            assertEquals(0, offers.size());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Offer_ByProduct(){
        try {
            // Data
            double offeredPrice = 59.4;
            int offeredQuantity = 69;

            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            int storeID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            IStore store = STORE_REPO.getStore(storeID);
            int product1ID = store.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            store.publishMemberOffer(USER1.username(), product1ID, offeredPrice, offeredQuantity);
            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataOffer> query =
                        session.createQuery(
                                "FROM DataOffer o WHERE " +
                                        "o.key.product.key.store.id = :store_id AND " +
                                        "o.key.product.key.id = :product_id AND " +
                                        "o.key.offeringMember.username = :offering_username",
                                DataOffer.class
                        );
                query.setParameter("store_id", storeID);
                query.setParameter("product_id", product1ID);
                query.setParameter("offering_username", USER1.username());
                assertEquals(1, query.list().size());
            }

            // Test Cascade
            closeReopen();
            store = STORE_REPO.getStore(storeID);
            store.removeProduct(FOUNDER.username(), product1ID);

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataOffer> query =
                        session.createQuery(
                                "FROM DataOffer o WHERE " +
                                        "o.key.product.key.store.id = :store_id AND " +
                                        "o.key.product.key.id = :product_id AND " +
                                        "o.key.offeringMember.username = :offering_username",
                                DataOffer.class
                        );
                query.setParameter("store_id", storeID);
                query.setParameter("product_id", product1ID);
                query.setParameter("offering_username", USER1.username());
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_Offer_ByOfferingMember(){
        try {
            // Data
            double offeredPrice = 59.4;
            int offeredQuantity = 69;

            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            int storeID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            IStore store = STORE_REPO.getStore(storeID);
            int product1ID = store.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            store.publishMemberOffer(USER1.username(), product1ID, offeredPrice, offeredQuantity);
            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataOffer> query =
                        session.createQuery(
                                "FROM DataOffer o WHERE " +
                                        "o.key.product.key.store.id = :store_id AND " +
                                        "o.key.product.key.id = :product_id AND " +
                                        "o.key.offeringMember.username = :offering_username",
                                DataOffer.class
                        );
                query.setParameter("store_id", storeID);
                query.setParameter("product_id", product1ID);
                query.setParameter("offering_username", USER1.username());
                assertEquals(1, query.list().size());
            }

            // Test Cascade
            closeReopen();
            USER_REPO.removeMember(USER1.username());

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataOffer> query =
                        session.createQuery(
                                "FROM DataOffer o WHERE " +
                                        "o.key.product.key.store.id = :store_id AND " +
                                        "o.key.product.key.id = :product_id AND " +
                                        "o.key.offeringMember.username = :offering_username",
                                DataOffer.class
                        );
                query.setParameter("store_id", storeID);
                query.setParameter("product_id", product1ID);
                query.setParameter("offering_username", USER1.username());
                assertTrue(query.list().isEmpty());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Persist_OfferConsent(){
        try {
            // Data
            double offeredPrice = 59.4;
            int offeredQuantity = 69;

            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(OWNER_SESSION, OWNER, OWNER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            int storeID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            IStore store = STORE_REPO.getStore(storeID);
            int product1ID = store.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            Offer offer = store.publishMemberOffer(USER1.username(), product1ID, offeredPrice, offeredQuantity);
            int offerId = offer.getId();

            // Normal behaviour
            Map<String, Boolean> consents = offer.getStoreConsent();
            assertEquals(1, consents.size(), "At the beginning only the founder's consent should exist");
            assertTrue(consents.containsKey(FOUNDER.username()));
            assertFalse(consents.get(FOUNDER.username()));
            assertFalse(offer.isStoreConsent());
            store.consentOffer(FOUNDER.username(), offer.getId());
            store.assignStoreOwner(FOUNDER.username(), OWNER.username());


            // Check data
            assertEquals(2, consents.size());
            assertTrue(consents.containsKey(FOUNDER.username()));
            assertTrue(consents.containsKey(OWNER.username()));
            assertTrue(consents.get(FOUNDER.username()));
            assertFalse(consents.get(OWNER.username()));
            assertFalse(offer.isStoreConsent());

            // Check data after reopen
            closeReopen();
            store = STORE_REPO.getStore(storeID);
            offer = store.getProductOffer(offerId);
            consents = offer.getStoreConsent();

            assertEquals(2, consents.size());
            assertTrue(consents.containsKey(FOUNDER.username()));
            assertTrue(consents.containsKey(OWNER.username()));
            assertTrue(consents.get(FOUNDER.username()));
            assertFalse(consents.get(OWNER.username()));
            assertFalse(offer.isStoreConsent());

            // check removal
            store.removeStoreOwnerAppointment(FOUNDER.username(), OWNER.username());
            closeReopen();

            store = STORE_REPO.getStore(storeID);
            offer = store.getProductOffer(offerId);
            consents = offer.getStoreConsent();
            assertTrue(consents.containsKey(FOUNDER.username()));
            assertTrue(consents.get(FOUNDER.username()));
            assertTrue(offer.isStoreConsent());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_OfferConsent_ByOffer(){
        try {
            // Data
            double offeredPrice = 59.4;
            int offeredQuantity = 69;

            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(OWNER_SESSION, OWNER, OWNER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            int storeID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            IStore store = STORE_REPO.getStore(storeID);
            int product1ID = store.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            store.assignStoreOwner(FOUNDER.username(), OWNER.username());
            Offer offer = store.publishMemberOffer(USER1.username(), product1ID, offeredPrice, offeredQuantity);
            int offerID = offer.getId();
            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataOfferConsent> query =
                        session.createQuery(
                                "FROM DataOfferConsent o WHERE o.key.offer.key.id = :offer_id",
                                DataOfferConsent.class
                        );
                query.setParameter("offer_id", offerID);
                assertEquals(2, query.list().size());
            }

            // Test Cascade
            closeReopen();
            store = STORE_REPO.getStore(storeID);
            store.removeOffer(FOUNDER.username(), offerID);

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataOfferConsent> query =
                        session.createQuery(
                                "FROM DataOfferConsent o WHERE o.key.offer.key.id = :offer_id",
                                DataOfferConsent.class
                        );
                query.setParameter("offer_id", offerID);
                assertEquals(0, query.list().size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void Cascade_OfferConsent_ByOwner(){
        try {
            // Data
            double offeredPrice = 59.4;
            int offeredQuantity = 69;

            // Preparation
            USER_REPO.addMember(FOUNDER_SESSION, FOUNDER, FOUNDER_PASSWORD);
            USER_REPO.addMember(OWNER_SESSION, OWNER, OWNER_PASSWORD);
            USER_REPO.addMember(USER1_SESSION, USER1, USER1_PASSWORD);
            int storeID = STORE_REPO.openNewStore(FOUNDER.username(), STORE1_DATA);
            IStore store = STORE_REPO.getStore(storeID);
            int product1ID = store.addNewProduct(FOUNDER.username(), PRODUCT1_DATA);
            store.assignStoreOwner(FOUNDER.username(), OWNER.username());
            Offer offer = store.publishMemberOffer(USER1.username(), product1ID, offeredPrice, offeredQuantity);
            int offerID = offer.getId();
            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataOfferConsent> query =
                        session.createQuery(
                                "FROM DataOfferConsent o WHERE o.key.offer.key.id = :offer_id",
                                DataOfferConsent.class
                        );
                query.setParameter("offer_id", offerID);
                assertEquals(2, query.list().size());
            }

            // Test Cascade
            closeReopen();
            USER_REPO.removeMember(OWNER.username());

            try (Session session = DbConfig.getSessionFactory().openSession()){
                Query<DataOfferConsent> query =
                        session.createQuery(
                                "FROM DataOfferConsent o WHERE o.key.offer.key.id = :offer_id",
                                DataOfferConsent.class
                        );
                query.setParameter("offer_id", offerID);
                assertEquals(1, query.list().size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
