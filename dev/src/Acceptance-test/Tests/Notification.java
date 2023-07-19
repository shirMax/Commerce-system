package Tests;

import Domain.Store.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Records.NotificationRecord;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Notification extends TestBase{

    static String SESSION_SF1; //store founder 1
    static String SESSION_SO1; //store owner 1
    static String SESSION_SM1; //store founder 1
    static String SESSION_USER; //buyer
    static final UserRecord STORE_FOUNDER1 = new UserRecord("test_founder1", "testfounder1@test.com", "0512345678", LocalDate.of(1997, 1, 19));
    static final UserRecord STORE_OWNER1 = new UserRecord("test_owner1", "testowner1@test.com", "0512345679", LocalDate.of(1997, 1, 19));
    static final UserRecord STORE_MANAGER1 = new UserRecord("test_manager1", "testmanager1@test.com", "0512345680", LocalDate.of(1997, 1, 19));
    static final UserRecord USER = new UserRecord("test_user1", "testuser1@test.com", "0512345681", LocalDate.of(1997, 1, 19));
    static final String PASS_SF1 = "P2ssW0rd"; //store founder 1
    static final String PASS_SO1 = "P2ssW0rd"; //store owner 1
    static final String PASS_SM1 = "P2ssW0rd"; //store manager 1
    static final String PASS_USER = "P2ssW0rd"; //store owner 1
    static final StoreRecord STORE1 = new StoreRecord("test_store1", "Store for testing");
    static Integer STORE1_ID;
    static ProductRecord PROD1 = new ProductRecord("test_product1", 5.00, Category.PARTY, 5);
    static Integer PROD1_ID;
    static List<NotificationRecord> NOTIFICATIONS_FOUNDER;
    static List<NotificationRecord> NOTIFICATIONS_OWNER;
    static List<NotificationRecord> NOTIFICATIONS_MANAGER;
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

        SESSION_USER = createSession(bridge);
        bridge.register(SESSION_USER, USER, PASS_USER);
        Register.toDelete.add(USER.username());
        bridge.login(SESSION_USER, USER.username(), PASS_USER);

        STORE1_ID = bridge.openStore(SESSION_SF1, STORE1);
        PROD1 = new ProductRecord(STORE1_ID, PROD1.productId(), PROD1.productName(), PROD1.productPrice(), PROD1.productCategory(), 5, PROD1.priceAfterDiscount(), 5);
        PROD1_ID = bridge.addProduct(SESSION_SF1, STORE1_ID, PROD1);
        OpenStore.toDelete.add(STORE1_ID);

        bridge.appointOwner(SESSION_SF1, STORE1_ID, STORE_OWNER1.username());
        bridge.appointManager(SESSION_SO1, STORE1_ID, STORE_MANAGER1.username());

        NOTIFICATIONS_FOUNDER = new ArrayList<>();
        NOTIFICATIONS_OWNER = new ArrayList<>();
        NOTIFICATIONS_MANAGER = new ArrayList<>();

        // TODO: somehow subscribe to the notification service so that the list gets is added with the new notification
    }

    @Override
    @AfterEach
    protected void tearDown() {
        bridge.logout(SESSION_SF1);
        bridge.logout(SESSION_SO1);
        bridge.logout(SESSION_USER);
        bridge.logout(SESSION_SM1);
        bridge.closeSession(SESSION_SF1);
        bridge.closeSession(SESSION_SO1);
        bridge.closeSession(SESSION_USER);
        bridge.closeSession(SESSION_SM1);
        OpenStore.cleanToDelete(bridge);
        Register.cleanToDelete(bridge);

        //TODO: somehow remove subscription of the lists
    }

    @Test
    void ManagerOwnerRemovedNotification_Success(){
        bridge.removeManager(SESSION_SO1, STORE1_ID, STORE_MANAGER1.username());
        //assertEquals(1, NOTIFICATIONS_MANAGER.size(), "Removed Manager should be notified"); //todo - we cant monitor notification like this

        //check no change with founder and owner
        //assertEquals(0, NOTIFICATIONS_OWNER.size()); //todo - we cant monitor notification like this
        //assertEquals(0, NOTIFICATIONS_FOUNDER.size()); //todo - we cant monitor notification like this

        bridge.removeOwner(SESSION_SF1, STORE1_ID, STORE_OWNER1.username());
        //assertEquals(1, NOTIFICATIONS_OWNER.size(), "Removed Owner should be notified"); //todo - we cant monitor notification like this

        //check no change with founder
        //assertEquals(0, NOTIFICATIONS_FOUNDER.size()); //todo - we cant monitor notification like this
    }

    @Test
    void PurchaseNotification_Success(){
        bridge.addToCart(SESSION_USER, STORE1_ID, PROD1_ID, 3);
        bridge.pay(SESSION_USER);

        //assertEquals(1, NOTIFICATIONS_FOUNDER.size(), "Founder should be notified with a purchase."); //todo - we cant monitor notification like this
        //assertEquals(1, NOTIFICATIONS_OWNER.size(), "Owner should be notified with a purchase."); //todo - we cant monitor notification like this
        //assertEquals(1, NOTIFICATIONS_MANAGER.size(), "Manager should be notified with a purchase."); //todo - we cant monitor notification like this
    }

}
