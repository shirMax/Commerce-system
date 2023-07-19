package UnitTests.DomainTests.ServicesTests;

import Domain.MarketImpl;
import Domain.Permission;
import Domain.Services.NotificationService.INotificationService;
import Domain.Services.NotificationService.NotificationService;
import Domain.Services.PaymentService.IPaymentService;
import Domain.Services.Response;
import Domain.Services.SupplyService.ISupplyService;
import Domain.Store.IStore;
import Domain.Store.IStoreController;
import Domain.User.IStoreBasket;
import Domain.User.IUserCart;
import Domain.User.IUserController;
import Domain.User.Member;
import UnitTests.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Records.AddressRecord;
import util.Records.DateRecord;
import util.Records.NotificationRecord;
import util.Records.PaymentDetails;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NotificationTests extends UnitTest {
    private MarketImpl market;
    private IPaymentService paymentService;
    private ISupplyService supplyService;
    private IUserController userController;
    private IStoreController storeController;
    private Member user1;
    private Member user2;
    private Member user3;
    private IUserCart userCart;
    private IStore store;

    @BeforeEach
    public void setUp() throws NoSuchAlgorithmException {
        market = MarketImpl.getInstance();

        paymentService = mock(IPaymentService.class);
        supplyService = mock(ISupplyService.class);
        userController = mock(IUserController.class);
        storeController = mock(IStoreController.class);
        INotificationService notificationService = new NotificationService(userController);
        UserRecord user1Data = new UserRecord("Amit", "amit@gamil.com", "05242462384", LocalDate.of(2000,1,1));
        UserRecord user2Data = new UserRecord("Amit2", "amit2@gamil.com", "05242542384", LocalDate.of(2000,1,1));
        user1 = new Member("1", user1Data, "123");
        user2 = new Member("2", user2Data, "1233");
        user3 = mock(Member.class);
        userCart = mock(IUserCart.class);

        market.initControllers(userController, storeController);
        market.updatePaymentService(paymentService);
        market.updateSupplyService(supplyService);
        market.updateNotificationService(notificationService);
        StoreRecord record = new StoreRecord(1, "test store", 5, "store for tests", true);
        store = mock(IStore.class);
    }

    /**
     * user1: not log in, the notification added to the queue
     * user2: not log in, the notification added to the queue
     * user3: login, notification not added to the service queue
     *
     * after check, user1 login, and we check that the queue now in empty.
     * @throws Exception
     */
    @Test
    void purchaseNotificationBroadCast() throws Exception {
        String sessionID = "1";
        PaymentDetails paymentDetails = new PaymentDetails(
                "a",
                "123456",
                new DateRecord(1996,11),
                "777"
        );
        PaymentDetails paymentDetails1 = new PaymentDetails("a", "123456", new DateRecord(1997,11), "777");
        AddressRecord deliveryAddress = new AddressRecord("a", "a", "a", "a", "a", "a");
        Queue<NotificationRecord> amitNots = new LinkedList<>();
        Queue<NotificationRecord> amit2Nots = new LinkedList<>();
        market.getNotificationService().subscribe("Amit", amitNots::add);
        market.getNotificationService().subscribe("Amit2", amit2Nots::add);
        when(userController.getUser(sessionID)).thenReturn(user1);
        when(userController.getUserCart(sessionID)).thenReturn(userCart);
        IStoreBasket basket = mock(IStoreBasket.class);
        when(basket.getStoreId()).thenReturn(1);
        when(userCart.getStoreBaskets()).thenReturn(Collections.singletonList(basket));
        when(storeController.calculateBasketPrice(any())).thenReturn(100.0);
        when(storeController.getStore(1)).thenReturn(store);
        when(paymentService.processPayment(paymentDetails)).thenReturn(new Response("",false, 0));
        when(supplyService.placeOrder(any(), any(), any())).thenReturn(new Response("",false, 0));
        Map<String, Permission> rules = new HashMap<>();
        rules.put("Amit", mock(Permission.class));
        rules.put("Amit2", mock(Permission.class));
        rules.put("Amit3", mock(Permission.class));// todo - Amit, after implementation of notify add this line.
        when(store.getStoreRoles(any())).thenReturn(rules);
        when(store.getStoreName()).thenReturn("store name for testing");
        when(userController.getMember("Amit")).thenReturn(user1);
        when(userController.getMember("Amit2")).thenReturn(user2);
        when(userController.getMember("Amit3")).thenReturn(user3);
        when(userController.isMemberExists("Amit")).thenReturn(true);
        when(userController.isMemberExists("Amit2")).thenReturn(true);
        when(userController.isMemberExists("Amit3")).thenReturn(true);
        when(user3.isLoggedIn()).thenReturn(true);
        when(user3.getUserName()).thenReturn("Amit3");
        when(paymentService.processPayment(any())).thenReturn(new Response("test", false, 200));
        market.purchase(paymentDetails1, deliveryAddress, userController.getUser(sessionID), userController.getUserCart(sessionID));

        assertEquals(1, amitNots.size());
        assertEquals(1, amit2Nots.size());
        assertEquals(
                1,
                market.getNotificationService().sendAllMemberNotifications("Amit3"),
                "since nothing subscribed to receive notifications for Amit3, there should still be 1 message left for Amit3"
        );

        // subscribing in delay, mimics late login
        Queue<NotificationRecord> amit3Nots = new LinkedList<>();
        market.getNotificationService().subscribe("Amit3", amit3Nots::add);
        assertEquals(0, market.getNotificationService().sendAllMemberNotifications("Amit3"));
        assertEquals(1, amit3Nots.size());
    }
}
