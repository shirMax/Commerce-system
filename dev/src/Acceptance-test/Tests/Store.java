package Tests;

import Domain.Store.Category;
import Domain.Store.Discount.DiscountTypes.Simple.ProductDiscount;
import Domain.Store.Conditions.ConditionFactory;
import Domain.Store.Offer;
import Domain.Store.Purchase.IfThenPurchaseRule;
import Exceptions.ATException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Enums.ErrorStatus;
import util.Enums.PermissionType;
import util.Records.DateRange;
import util.Records.DateRecord;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests all basic operations store founder/owner/manager may perform with their store
 * Tests Use-case II.4.1: Handle Storage
 * Tests Use-case II.4.3: Appoint owner
 * Tests Use-case II.4.4: Appoint Manager
 * Tests Use-case II.4.5: Modify manager's permissions or Remove store Owner
 * Tests Use-case II.4.7: Store officials information
 * Tests Use-case II.4.8: Get transaction history.
 */
public class Store extends TestBase{
    static String SESSION_SF1; //store founder 1
    static String SESSION_SO1; //store owner 1
    static String SESSION_SO2; //store owner 2
    static String SESSION_SM1; //store founder 1
    static String SESSION_M1; // member session id
    static final UserRecord STORE_FOUNDER1 = new UserRecord("test_founder1", "testfounder1@test.com", "0512345678", LocalDate.of(1997, 1, 19));
    static final UserRecord STORE_OWNER1 = new UserRecord("test_owner1", "testowner1@test.com", "0512345679", LocalDate.of(1997, 1, 19));
    static final UserRecord STORE_OWNER2 = new UserRecord("test_owner2", "testowner2@test.com", "0512345680", LocalDate.of(1997, 1, 19));
    static final UserRecord STORE_MANAGER1 = new UserRecord("test_manager1", "testmanager1@test.com", "0512345681", LocalDate.of(1997, 1, 19));
    static final UserRecord STORE_MEMBER1 = new UserRecord("test_member1", "testmember1@test.com", "0512345684", LocalDate.of(1997, 1, 19));

    static final String PASS_SF1 = "P2ssW0rd"; //store founder 1
    static final String PASS_SO1 = "P2ssW0rd"; //store owner 1
    static final String PASS_SO2 = "P2ssW0rd"; //store owner 1
    static final String PASS_SM1 = "P2ssW0rd"; //store manager 1
    static final String PASS_M1 = "P2ssW0rd"; //member 1

    static final StoreRecord STORE1 = new StoreRecord("test_store1", "Store for testing");
    static Integer STORE1_ID;
    static ProductRecord PROD1 = new ProductRecord("test_product1", 5.00, Category.PARTY, 5);
    static Integer PROD1_ID;
    @Override
    @BeforeEach
    protected void setUp() {
        inject();
        SESSION_SF1 = createSession(bridge);
        bridge.register(SESSION_SF1, STORE_FOUNDER1, PASS_SF1);
        Register.toDelete.add(STORE_FOUNDER1.username());
        bridge.login(SESSION_SF1, STORE_FOUNDER1.username(), PASS_SF1);

        SESSION_SM1 = createSession(bridge);
        bridge.register(SESSION_SM1, STORE_MANAGER1, PASS_SM1);
        Register.toDelete.add(STORE_MANAGER1.username());
        bridge.login(SESSION_SM1, STORE_MANAGER1.username(), PASS_SM1);

        SESSION_SO1 = createSession(bridge);
        bridge.register(SESSION_SO1, STORE_OWNER1, PASS_SO1);
        Register.toDelete.add(STORE_OWNER1.username());
        bridge.login(SESSION_SO1, STORE_OWNER1.username(), PASS_SO1);

        SESSION_SO2 = createSession(bridge);
        bridge.register(SESSION_SO2, STORE_OWNER2, PASS_SO2);
        Register.toDelete.add(STORE_OWNER2.username());
        bridge.login(SESSION_SO2, STORE_OWNER2.username(), PASS_SO2);

        SESSION_M1 = createSession(bridge);
        bridge.register(SESSION_M1, STORE_MEMBER1, PASS_M1);
        Register.toDelete.add(STORE_MEMBER1.username());
        bridge.login(SESSION_M1, STORE_MEMBER1.username(), PASS_M1);

        STORE1_ID = bridge.openStore(SESSION_SF1, STORE1);
        PROD1 = new ProductRecord(STORE1_ID, "test_product1", 5.00, Category.PARTY, 5);
        OpenStore.toDelete.add(STORE1_ID);
    }

    @Override
    @AfterEach
    protected void tearDown() {
        bridge.logout(SESSION_SF1);
        bridge.logout(SESSION_SO1);
        bridge.logout(SESSION_SO2);
        bridge.logout(SESSION_SM1);
        bridge.logout(SESSION_M1);
        bridge.closeSession(SESSION_SF1);
        bridge.closeSession(SESSION_SO1);
        bridge.closeSession(SESSION_SO2);
        bridge.closeSession(SESSION_SM1);
        bridge.closeSession(SESSION_M1);
        OpenStore.cleanToDelete(bridge);
        Register.cleanToDelete(bridge);
    }

    // Testing use-case II.4.1
    @Test
    void AddProduct_Founder_NullParameters_Failure(){
        try {
            ProductRecord nullPName = new ProductRecord(null, PROD1.productPrice(), PROD1.productCategory(), PROD1.quantity());
            bridge.addProduct(SESSION_SF1, STORE1_ID, nullPName);
            fail("Added product with null name.");
        } catch (Exception ignored) {
        }
        try {
            ProductRecord nullPCategory = new ProductRecord(PROD1.productName(), PROD1.productPrice(), null, PROD1.quantity());
            bridge.addProduct(SESSION_SF1, STORE1_ID, nullPCategory);
            fail("Added product with null category.");
        } catch (Exception ignored) {
        }
    }

    @Test
    void AddProduct_Founder_CorrectParameters_Success(){
        PROD1_ID = -1;
        try {
            PROD1_ID = bridge.addProduct(SESSION_SF1, STORE1_ID, PROD1);
            if (PROD1_ID == -1){
                fail("Adding the product didn't return ID.");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
        //checking added product details:

        ProductRecord productRecord = bridge.getProduct(SESSION_SF1, STORE1_ID, PROD1_ID);
        assertEquals(STORE1_ID, productRecord.storeId(), "Bridge didn't return product with the correct store ID.");
        assertEquals(PROD1_ID, productRecord.productId(), "Bridge didn't return product with the correct product ID.");
        assertEquals(PROD1.productName(), productRecord.productName(), "Added product's name don't match given name.");
        assertEquals(PROD1.productPrice(), productRecord.productPrice(), "Added product's price don't match given price.");
        assertEquals(PROD1.productCategory(), productRecord.productCategory(), "Added product's category don't match given category.");
        assertEquals(PROD1.quantity(), productRecord.quantity(), "Added product's quantity don't match given quantity.");
    }

    @Test
    void RemoveProduct_Founder_CorrectParameters_Success(){
        PROD1_ID = bridge.addProduct(SESSION_SF1, STORE1_ID, PROD1);
        try {
            bridge.removeProduct(SESSION_SF1, STORE1_ID, PROD1_ID);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        //checking product was removed:

        try {
            ProductRecord productRecord = bridge.getProduct(SESSION_SF1, STORE1_ID, PROD1_ID);
        } catch (ATException e) {
            assertEquals(ErrorStatus.PRODUCT_DOES_NOT_EXIST, e.status, e.getMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    // Testing use-case II.4.7
    @Test
    void OfficialsInfo_Founder_Success(){
        String founder = null;
        try {
            founder = bridge.getStoreFounder(SESSION_SF1, STORE1_ID);
            if (founder == null)
                fail("Store owners query returned null.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        //checking the founder is the only owner:
        assertEquals("test_founder1", founder, "Founder don't match expected.");

        Set<String> managers = null;
        try {
            managers = bridge.getStoreManagers(SESSION_SF1, STORE1_ID);
            if (managers == null)
                fail("Store owners query returned null.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        //checking the founder is the only owner:
        assertTrue(managers.isEmpty(), "Managers count don't match expected.");
    }

    // Testing use-case II.4.3
    @Test
    void AppointOwner_Founder_NewOwner_Success(){
        Set<String> owners = bridge.getStoreOwners(SESSION_SF1, STORE1_ID);
        Set<String> managers = bridge.getStoreManagers(SESSION_SF1, STORE1_ID);
        try {
            bridge.appointOwner(SESSION_SF1, STORE1_ID, STORE_OWNER1.username());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        //check owner appointed - store's side:
        Set<String> updatedOwners = bridge.getStoreOwners(SESSION_SF1, STORE1_ID);
        Set<String> updatedManagers = bridge.getStoreManagers(SESSION_SF1, STORE1_ID);
        assertEquals(managers, updatedManagers, "Managers set shouldn't change.");
        assertEquals(owners.size() + 1, updatedOwners.size(), "Owners set should grow by 1.");

        owners.add(STORE_OWNER1.username());
        assertEquals(owners, updatedOwners, "Owners set should be new owner's name away from original set.");

        //check owner appointed - owner's side:
        Set<Integer> ownedStores = bridge.getOwnedStores(SESSION_SO1);
        assertTrue(ownedStores.contains(STORE1_ID), "Owner's set of owned stores should now contain the store.");
    }

    @Test
    void AppointOwner_Founder_AlreadyOwner_Failure(){
        bridge.appointOwner(SESSION_SF1, STORE1_ID, STORE_OWNER1.username());
        try {
            bridge.appointOwner(SESSION_SF1, STORE1_ID, STORE_OWNER1.username());
            fail("Appointing an already appointed owner should not succeed.");
        } catch (Exception ignored) {
        }
    }

    @Test
    void AppointManager_Founder_NewManager_Success(){
        Set<String> owners = bridge.getStoreOwners(SESSION_SF1, STORE1_ID);
        Set<String> managers = bridge.getStoreManagers(SESSION_SF1, STORE1_ID);
        try {
            bridge.appointManager(SESSION_SF1, STORE1_ID, STORE_MANAGER1.username());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        //check manager appointed - store's side:
        Set<String> updatedOwners = bridge.getStoreOwners(SESSION_SF1, STORE1_ID);
        Set<String> updatedManagers = bridge.getStoreManagers(SESSION_SF1, STORE1_ID);
        assertEquals(owners, updatedOwners, "Owners set shouldn't change.");
        assertEquals(managers.size() + 1, updatedManagers.size(), "Owners set should grow by 1.");

        managers.add(STORE_MANAGER1.username());
        assertEquals(owners, updatedOwners, "Managers set should be new manager's name away from original set.");

        //check manager appointed - manager's side:
        Set<Integer> managedStores = bridge.getManagedStores(SESSION_SM1);
        assertTrue(managedStores.contains(STORE1_ID), "Manager's set of managed stores should now contain the store.");
    }

    // Testing use-case II.4.5
    @Test
    void SetManagerPermissions_Founder_Success(){
        bridge.appointManager(SESSION_SF1, STORE1_ID, STORE_MANAGER1.username());

        //1st try:
        Set<PermissionType> permissions1 = Set.of(PermissionType.STORAGE_MANAGEMENT);
        try {
            bridge.setManagerPermissions(SESSION_SF1, STORE1_ID, STORE_MANAGER1.username(), permissions1);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        //check manager's permissions - store's side:
        Set<PermissionType> updatedPermissions1 = bridge.getManagerPermissions(SESSION_SF1, STORE1_ID, STORE_MANAGER1.username());
        if (updatedPermissions1 == null)
            fail("getManagerPermissions returned null");
        assertEquals(permissions1, updatedPermissions1, "Updated permissions don't match set.");

        //check manager's permissions - manager's side:
        updatedPermissions1 = bridge.getPermissions(SESSION_SM1, STORE_MANAGER1.username(), STORE1_ID);
        if (updatedPermissions1 == null)
            fail("getPermissions returned null");
        assertEquals(permissions1, updatedPermissions1, "Updated permissions don't match set.");

        //2nd try:
        Set<PermissionType> permissions2 = Set.of(PermissionType.GET_PURCHASE_HISTORY);
        try {
            bridge.setManagerPermissions(SESSION_SF1, STORE1_ID, STORE_MANAGER1.username(), permissions2);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        //check manager's permissions - store's side:
        Set<PermissionType> updatedPermissions2 = bridge.getManagerPermissions(SESSION_SF1, STORE1_ID, STORE_MANAGER1.username());
        if (updatedPermissions2 == null)
            fail("getManagerPermissions returned null");
        assertEquals(permissions2, updatedPermissions2, "Updated permissions don't match set.");

        //check manager's permissions - manager's side:
        updatedPermissions2 = bridge.getPermissions(SESSION_SM1, STORE_MANAGER1.username(), STORE1_ID);
        if (updatedPermissions2 == null)
            fail("getPermissions returned null");
        assertEquals(permissions2, updatedPermissions2, "Updated permissions don't match set.");
    }

    @Test
    void RemoveOwner_ByAppointer_Success(){
        bridge.appointOwner(SESSION_SF1, STORE1_ID, STORE_OWNER1.username());

        bridge.removeOwner(SESSION_SF1, STORE1_ID, STORE_OWNER1.username());

        //check removal was successful:
        Set<String> owners = bridge.getStoreOwners(SESSION_SF1, STORE1_ID);
        assertFalse(owners.contains(STORE_OWNER1.username()), "Owner was not removed from store.");
    }

    @Test
    void RemoveOwner_NotByAppointer_Success(){
        bridge.appointOwner(SESSION_SF1, STORE1_ID, STORE_OWNER1.username());

        try {
            bridge.removeOwner(SESSION_SO2 , STORE1_ID, STORE_OWNER1.username());
            fail("Removing a store owner cannot be done by someone other than the original appointer.");
        } catch (Exception success){
        }
    }

    // Testing use-case II.4.8
    @Test
    void GetStoreTransactions_Founder_Success(){
        try {
            bridge.getStoreTransactionHistory(SESSION_SF1, STORE1_ID, new DateRange((DateRecord) null, null));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void AddIfThenPurchaseRule_Founder_Success(){
        try {
            bridge.addPurchaseRule(SESSION_SF1, STORE1_ID, new IfThenPurchaseRule(ConditionFactory.atLeastQuantity(1, 1), ConditionFactory.minBasketPrice(100)));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void AddDiscount_Founder_Success(){
        try {
            bridge.addProduct(SESSION_SF1, STORE1_ID, PROD1);
            bridge.addDiscount(SESSION_SF1, STORE1_ID, new ProductDiscount(10, 1));
        } catch (Exception ex) {
            fail(ex.getMessage() + "Added product discount for product that doesn't exist!.");
        }
    }
  
    @Test
    void AddDiscount_Founder_ProductDoesntExist_Failure(){
        try {
            bridge.addDiscount(SESSION_SF1, STORE1_ID, new ProductDiscount(10, 1));
            fail("Added product discount for product that doesn't exist!.");
        } catch (Exception ignored) {
        }
    }

    @Test
    void Offer_Member_Success() {
        try {
            bridge.addProduct(SESSION_SF1, STORE1_ID, PROD1);
            bridge.memberPublishOffer(SESSION_M1, STORE1_ID, 1, 100.0, 1);
            Map<Integer, Offer> membersOffers = bridge.getMemberOffers(SESSION_M1);
            assertEquals(1, membersOffers.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void Offer_Member_ProductDoesntExist_Failure() {
        try {
            bridge.memberPublishOffer(SESSION_M1, STORE1_ID, 1, 100.0, 1);
            fail("Published offered for product that doesn't exists!");
        } catch (Exception ignored) {
        }
    }
}
