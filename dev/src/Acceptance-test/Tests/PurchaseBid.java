package Tests;

import Domain.Store.Offer;
import Exceptions.ATException;
import jakarta.persistence.criteria.CriteriaBuilder;
import util.Records.AddressRecord;
import util.Records.DateRecord;
import util.Records.PaymentDetails;
import util.Records.Transaction;
import Domain.Store.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

public class PurchaseBid extends TestBase{

    static String SESSION; //Member
    static String SESSION_SF1; //store founder 1
    static final UserRecord STORE_FOUNDER1 = new UserRecord("test_founder1", "testfounder1@test.com", "0512345678", LocalDate.of(1997, 1, 19));
    static final UserRecord MEMBER = new UserRecord("costumer1", "testcostumer1@test.com", "0512345678", LocalDate.of(1997, 1, 19));
    static final String PASS_MEMBER = "P2ssW0rd"; //store founder 1
    static final String PASS_SF1 = "P2ssW0rd"; //store founder 1
    static final StoreRecord STORE1 = new StoreRecord("test_store1", "Store for testing");
    static Integer STORE1_ID;
    static ProductRecord PROD1;
    static Integer PROD1_ID;
    static ProductRecord PROD2;
    static Integer PROD2_ID;
    static String PASS_O1 = "Aa123Bb123";


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

        SESSION = createSession(bridge);
        bridge.register(SESSION, MEMBER, PASS_MEMBER);
        Register.toDelete.add(MEMBER.username());
        bridge.login(SESSION, MEMBER.username(), PASS_MEMBER);
    }

    @Override
    @AfterEach
    protected void tearDown() {
        bridge.closeSession(SESSION);
        bridge.logout(SESSION_SF1);
        bridge.closeSession(SESSION_SF1);
        OpenStore.cleanToDelete(bridge);
        Register.cleanToDelete(bridge);
    }

    @Test
    void BuyPurchaseBid_1Customer_Success(){
        bridge.memberPublishOffer(SESSION, STORE1_ID, PROD1_ID, 3, 1);
        Map<Integer, Offer> res = bridge.getMemberOffers(SESSION);
        assertNotNull(res);
        Offer offer = res.values().stream().toList().get(0);
        assertNotNull(offer);
        assertEquals(3, offer.getOfferedPrice());
        assertEquals(1, offer.getOfferedQuantity());

        bridge.consentOffer(SESSION_SF1, offer.getId(), STORE1_ID);

        PaymentDetails paymentDetails = new PaymentDetails("a", "1234567812345678", new DateRecord(LocalDate.of(2025, 10, 1)), "777");
        AddressRecord deliveryAddress = new AddressRecord("a", "a", "a", "a", "a", "a");

        bridge.purchaseBid(SESSION, paymentDetails, deliveryAddress, offer);

        boolean found = false;
        List<Transaction> transactions = bridge.getStoreTransactionHistory(SESSION_SF1, STORE1_ID, null);
        for (Transaction transaction : transactions) {
            if (transaction.storeBasket().getProducts().get(PROD1_ID).getQuantity() == 1) {
                found = true;
            }
            if (transaction.price() == 3) {
                found = found && true;
            }
            assertTrue(found, "Transaction wasn't added after successful purchase.");
        }
    }

    @Test
    void BuyPurchaseBid_1Customer_Failure(){
        bridge.memberPublishOffer(SESSION, STORE1_ID, PROD1_ID, 3, 1);
        Map<Integer, Offer> res = bridge.getMemberOffers(SESSION);
        assertNotNull(res);
        Offer offer = res.values().stream().toList().get(0);
        assertNotNull(offer);
        assertEquals(3, offer.getOfferedPrice());
        assertEquals(1, offer.getOfferedQuantity());

        bridge.storeRejectOffer(SESSION_SF1, offer.getId(), STORE1_ID);

        PaymentDetails paymentDetails = new PaymentDetails("a", "1234567812345678", new DateRecord(LocalDate.of(2025, 10, 1)), "777");
        AddressRecord deliveryAddress = new AddressRecord("a", "a", "a", "a", "a", "a");

        assertThrows(ATException.class, () -> bridge.purchaseBid(SESSION_SF1, paymentDetails, deliveryAddress, offer));

        List<Transaction> transactions = bridge.getStoreTransactionHistory(SESSION_SF1, STORE1_ID, null);
        assertEquals(transactions.size(), 0);
    }

    @Test
    void BuyPurchaseBid_1Customer_2Approvals_Success(){
        //register and assign storeManager
        String SESSION_SM1 = createSession(bridge);
        UserRecord SO1 = new UserRecord("test_owner1", "testmanager1@test.com", "0512345678", LocalDate.of(1997, 1, 19));
        bridge.register(SESSION_SM1, SO1, PASS_O1);
        Register.toDelete.add(SO1.username());
        bridge.appointOwner(SESSION_SF1, STORE1_ID, SO1.username());

        bridge.memberPublishOffer(SESSION, STORE1_ID, PROD1_ID, 3, 1);
        Map<Integer, Offer> res = bridge.getMemberOffers(SESSION);
        assertNotNull(res);
        Offer offer = res.values().stream().toList().get(0);
        assertNotNull(offer);
        assertEquals(3, offer.getOfferedPrice());
        assertEquals(1, offer.getOfferedQuantity());

        bridge.consentOffer(SESSION_SF1, offer.getId(), STORE1_ID);
        bridge.login(SESSION_SM1, SO1.username(), PASS_O1);
        bridge.consentOffer(SESSION_SM1, offer.getId(), STORE1_ID);
        bridge.logout(SESSION_SM1);
        bridge.closeSession(SESSION_SM1);

        PaymentDetails paymentDetails = new PaymentDetails("a", "1234567812345678", new DateRecord(LocalDate.of(2025, 10, 1)), "777");
        AddressRecord deliveryAddress = new AddressRecord("a", "a", "a", "a", "a", "a");
        bridge.purchaseBid(SESSION, paymentDetails, deliveryAddress, offer);

        boolean found = false;
        List<Transaction> transactions = bridge.getStoreTransactionHistory(SESSION_SF1, STORE1_ID, null);
        for (Transaction transaction : transactions) {
            if (transaction.storeBasket().getProducts().get(PROD1_ID).getQuantity() == 1) {
                found = true;
            }
            if (transaction.price() == 3) {
                found = found && true;
            }
            assertTrue(found, "Transaction wasn't added after successful purchase.");
        }
    }

    @Test
    void BuyPurchaseBid_1Customer_1Approval_1Reject_Failure(){
        //register and assign storeManager
        String SESSION_SO1 = createSession(bridge);
        UserRecord SO1 = new UserRecord("test_owner1", "testmanager1@test.com", "0512345678", LocalDate.of(1997, 1, 19));
        bridge.register(SESSION_SO1, SO1, PASS_O1);
        Register.toDelete.add( SO1.username());
        bridge.appointOwner(SESSION_SF1, STORE1_ID, SO1.username());

        bridge.memberPublishOffer(SESSION, STORE1_ID, PROD1_ID, 3, 1);
        Map<Integer, Offer> res = bridge.getMemberOffers(SESSION);
        assertNotNull(res);
        Offer offer = res.values().stream().toList().get(0);
        assertNotNull(offer);
        assertEquals(3, offer.getOfferedPrice());
        assertEquals(1, offer.getOfferedQuantity());

        bridge.login(SESSION_SO1, SO1.username(), PASS_O1);
        bridge.consentOffer(SESSION_SO1, offer.getId(), STORE1_ID);
        bridge.logout(SESSION_SO1);
        bridge.closeSession(SESSION_SO1);
        bridge.storeRejectOffer(SESSION_SF1, offer.getId(), STORE1_ID);

        PaymentDetails paymentDetails = new PaymentDetails("a", "1234567812345678", new DateRecord(LocalDate.of(2025, 10, 1)), "777");
        AddressRecord deliveryAddress = new AddressRecord("a", "a", "a", "a", "a", "a");

        assertThrows(ATException.class, () -> bridge.purchaseBid(SESSION_SF1, paymentDetails, deliveryAddress, offer));

        List<Transaction> transactions = bridge.getStoreTransactionHistory(SESSION_SF1, STORE1_ID, null);
        assertEquals(transactions.size(), 0);
    }
}
