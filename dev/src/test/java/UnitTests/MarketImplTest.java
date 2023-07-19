package UnitTests;

import DataLayer.ITransactionRepo;
import DataLayer.User.ORM.DataMember;
import util.Exceptions.NonExistentData;
import util.Records.Transaction;
import Domain.MarketImpl;
import Domain.Services.NotificationService.NotificationService;
import Domain.Services.PaymentService.IPaymentService;
import Domain.Services.Response;
import Domain.Services.SupplyService.ISupplyService;
import Domain.Store.*;
import Domain.User.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import util.Enums.ErrorStatus;
import util.Exceptions.PurchaseError;
import util.Records.PaymentDetails;
import util.Exceptions.DataError;
import util.Records.AddressRecord;
import util.Records.DateRecord;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MarketImplTest extends UnitTest {

  private MarketImpl market;
  private IPaymentService paymentService;
  private ISupplyService supplyService;
  private NotificationService notificationService;
  private IUserController userController;
  private IStoreController storeController;
  private IUser user;
  private IUserCart userCart;

  @BeforeEach
  public void setUp() {
    market = MarketImpl.getInstance();

    paymentService = mock(IPaymentService.class);
    supplyService = mock(ISupplyService.class);
    userController = mock(IUserController.class);
    storeController = mock(IStoreController.class);
    user = mock(IUser.class);
    userCart = mock(IUserCart.class);
    notificationService = new NotificationService(userController);

    market.initControllers(userController, storeController);
    market.updatePaymentService(paymentService);
    market.updateSupplyService(supplyService);
    market.updateNotificationService(notificationService);
  }

  @Test
  public void testGetTransactionCount() {
    market.cleanTransactions();
    assertEquals(0, market.getTransactionCount());
  }

  @Test
  public void testUpdatePaymentService() {
    IPaymentService newPaymentService = mock(IPaymentService.class);
    market.updatePaymentService(newPaymentService);
    assertSame(newPaymentService, market.getPaymentService());
  }

  @Test
  public void testUpdateSupplyService() {
    ISupplyService newSupplyService = mock(ISupplyService.class);
    market.updateSupplyService(newSupplyService);
    assertSame(newSupplyService, market.getSupplyService());
  }

  @Test
  public void testUpdateTransactionHistory() {
    ITransactionRepo newTransactionRepo = mock(ITransactionRepo.class);
    market.updateTransactionRepo(newTransactionRepo);
    assertEquals(newTransactionRepo.getTransactionCount(), market.getTransactionCount());
  }

  @Test
  public void testPurchase() throws Exception {
    // Data
    String sessionID = "1";
    String username = "test user";
    String founderName = "test user2";
    StoreRecord storeData = new StoreRecord("store name",  "asdf");
    PaymentDetails paymentDetails = new PaymentDetails(
            "a",
            "123456",
            new DateRecord(1997, 11),
            "777",
            100.,
            123L
    );
    PaymentDetails paymentDetails1 = new PaymentDetails(
            "a",
            "123456",
            new DateRecord(1997,11),
            "777"
    );
    AddressRecord deliveryAddress = new AddressRecord("a", "a", "a", "a", "a", "a");
    IStore store = new Store(founderName, storeData);
    ProductRecord product1 = new ProductRecord(store.getStoreId(),1, "product1", 10, Category.BEAUTY, 5, 10, 1);
    IStoreBasket basket = new DummyStoreBasket(store.getStoreId());
    Member founder = mock(Member.class);

    // set up mocks
    when(user.getUserName()).thenReturn(username);
    when(user.getSessionId()).thenReturn("1");
    when(userController.isMemberSession("1")).thenReturn(false);
    when(founder.getUserName()).thenReturn(founderName);
    when(userController.getUser(sessionID)).thenReturn(user);
    when(userController.getMember(founder.getUserName())).thenReturn(founder);
    when(userController.getUserCart(sessionID)).thenReturn(userCart);
    basket.addProduct(product1);
    List<IStoreBasket> baskets = new ArrayList<>();
    baskets.add(basket);
    when(userCart.getStoreBaskets()).thenReturn(baskets);
    when(storeController.getStore(store.getStoreId())).thenReturn(store);
    when(storeController.calculateBasketPrice(any())).thenReturn(100.0);
    when(paymentService.processPayment(any())).thenReturn(new Response("",false, 0));
    when(supplyService.placeOrder(any(), any(), any())).thenReturn(new Response("",false, 0));
    //validate purchase successes;
    assertDoesNotThrow(() -> market.purchase(paymentDetails1, deliveryAddress, userController.getUser(sessionID), userController.getUserCart(sessionID)));

    verify(user, times(1)).removeUserCart();

    //validate history
    assertEquals(1, market.getUserTransactions("1",null,null).size());
    assertEquals(1, market.getStoreTransactions(store.getStoreId()).size());

    //validate remove cart fail
    DataMember dataMemberMock = mock(DataMember.class);
    when(dataMemberMock.getUsername()).thenReturn("user1");
    UserCart cart = new UserCart(dataMemberMock);
    doThrow(RuntimeException.class).when(storeController).removeCartQuantity(cart);
    assertThrows(RuntimeException.class, () -> market.purchase(paymentDetails1, deliveryAddress, userController.getUser(sessionID), cart));

    //validate payment service fail
    Mockito.reset(storeController);
    when(paymentService.processPayment(any())).thenReturn(new Response("sdaf", true, 404));
    assertThrows(PurchaseError.class, () -> market.purchase(paymentDetails1, deliveryAddress, userController.getUser(sessionID), cart));

  }

  @Test
  public void testGetTransactions() {
    assertNotNull(market.getTransactions());
  }

  @Test
  public void testGetTransaction() {
    int transactionID = 1;
    assertThrows(NonExistentData.class, () -> market.getTransaction(transactionID));
  }

  @Test
  public void testGetUserTransactions() {
    String userName = "testUser";
    List<Transaction> transactions = market.getUserTransactions(userName, null, null);
    assertNotNull(transactions);
  }

  @Test
  public void testGetStoreTransactions(){
    int storeId = 1;
    List<Transaction> transactions = null;
    try {
      transactions = market.getStoreTransactions(storeId);
    } catch (DataError e) {
      e.printStackTrace();
    }
    assertNotNull(transactions);
  }

  @Test
  public void testCleanTransactions() {
    market.cleanTransactions();
    assertEquals(0, market.getTransactionCount());
  }

  @Test
  public void purhcaseBidTest() {
    try {
      // Data
      String sessionID = "1";
      String offeringMember = "test user";
      String founderName = "test user2";
      PaymentDetails paymentDetails = new PaymentDetails(
              "a",
              "123456",
              new DateRecord(1997,11),
              "777",
              100.,
              123L
      );
      PaymentDetails paymentDetails1 = new PaymentDetails(
              "a",
              "123456",
              new DateRecord(1997,11),
              "777"
      );
      AddressRecord deliveryAddress = new AddressRecord("a", "a", "a", "a", "a", "a");
      ProductRecord product1 = new ProductRecord("product1", 10, Category.BEAUTY, 5);
      IStore store = new Store(founderName, new StoreRecord("store name", "1"));
      int product1ID = store.addNewProduct(founderName, product1);

      // config mocks
      when(user.getUserName()).thenReturn(offeringMember);
      when(userController.getUser(sessionID)).thenReturn(user);
      when(storeController.getStore(store.getStoreId())).thenReturn(store);
      when(paymentService.processPayment(paymentDetails)).thenReturn(new Response("",false, 0));
      when(supplyService.placeOrder(any(), any(), any())).thenReturn(new Response("",false, 0));
      when(userController.getMember(offeringMember)).thenReturn(mock(Member.class));
      when(userController.getMember(founderName)).thenReturn(mock(Member.class));
      when(userController.getMemberUserName(sessionID)).thenReturn(offeringMember);
      when(userController.isSessionExists(sessionID)).thenReturn(true);
      when(userController.isUserIsMember(sessionID)).thenReturn(true);
      when(paymentService.processPayment(any())).thenReturn(new Response("test", false, 200));

      // set up
      Offer offer = store.publishMemberOffer(offeringMember, product1ID, 8, 3);
      store.consentOffer(founderName, offer.getId());

      //validate purchase successes;
      assertDoesNotThrow(() -> market.purchaseBid(sessionID, paymentDetails1, deliveryAddress, store.getStoreId(), offer.getId()));

      //validate history
      assertEquals(1, market.getUserTransactions("test user",null,null).size());
      assertEquals(1, market.getStoreTransactions(store.getStoreId()).size());

      //validate payment service fail
      Offer offer2 = store.publishMemberOffer(offeringMember, product1ID, 8, 3);
      store.consentOffer(founderName, offer2.getId());
      when(paymentService.processPayment(any())).thenReturn(new Response("sdaf", true, 404));
      try {
        market.purchaseBid(sessionID, paymentDetails1, deliveryAddress, store.getStoreId(), offer2.getId());
      } catch (PurchaseError e) {
        if (e.getStatus() != ErrorStatus.PAYMENT_PROCESS_FAILED)
          throw e;
      }
    }
    catch (Exception e){
      fail(e.getMessage());
    }
  }

  @Test
  public void purhcaseBidTest2() {
    try {
      // Data
      int numberOfDeletion = 0;
      String sessionID = "1";
      String username = "test user";
      String founderName = "test user2";
      PaymentDetails paymentDetails = new PaymentDetails(
              "a",
              "123456",
              new DateRecord(1997,11),
              "777",
              100.
      );
      PaymentDetails paymentDetails1 = new PaymentDetails(
              "a",
              "123456",
              new DateRecord(1997,11),
              "777"
      );
      AddressRecord deliveryAddress = new AddressRecord("a", "a", "a", "a", "a", "a");
      ProductRecord product1Data = new ProductRecord("product1", 10, Category.BEAUTY, 5);
      IStore store = new Store(founderName, new StoreRecord("store name", "1"));
      int product1ID = store.addNewProduct(founderName, product1Data);
      store.assignStoreOwner(founderName, "new Manager");

      // config mocks
      when(user.getUserName()).thenReturn(username);
      when(userController.getUser(sessionID)).thenReturn(user);
      when(storeController.getStore(store.getStoreId())).thenReturn(store);
      when(paymentService.processPayment(paymentDetails)).thenReturn(new Response("",false, 0));
      when(supplyService.placeOrder(any(), any(), any())).thenReturn(new Response("",false, 0));
      when(userController.getMember(user.getUserName())).thenReturn(mock(Member.class));
      when(userController.getMember(founderName)).thenReturn(mock(Member.class));
      when(userController.isSessionExists("1")).thenReturn(true);
      when(paymentService.processPayment(any())).thenReturn(new Response("test", false, 200));
      when(userController.getMemberUserName("5")).thenReturn(founderName);
      Member user2 = mock(Member.class);
      when(userController.getMember(username)).thenReturn(user2);

      // set up
      Offer offer = store.publishMemberOffer(username, product1ID, 8, 3);
      store.consentOffer(founderName, offer.getId());

      //validate purchase fail because not fully consent
      try {
        market.purchaseBid(sessionID, paymentDetails1, deliveryAddress, store.getStoreId(), offer.getId());
      } catch (PurchaseError e) {
        if (e.getStatus() != ErrorStatus.OFFER_NOT_FULLY_CONSENTED)
          throw e;
      }

      //validate when manager reject the offer
      market.storeRejectOffer("5", offer.getId(), store.getStoreId());
      verify(user2).removeOffer(offer.getId());
    }
    catch (Exception e){
      fail(e.getMessage());
    }
  }
}
