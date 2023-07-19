package UnitTests;

import DataLayer.User.ORM.DataMember;
import Domain.MarketImpl;
import Domain.Services.PaymentService.IPaymentService;
import Domain.Services.Response;
import Domain.Services.SupplyService.ISupplyService;
import Domain.Store.Category;
import util.Exceptions.PurchaseLimitation;
import Domain.Store.IStore;
import Domain.Store.IStoreController;
import Domain.Store.Store;
import Domain.User.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Exceptions.*;
import util.Records.PaymentDetails;
import util.Records.AddressRecord;
import util.Records.DateRecord;
import util.Records.StoreRecords.ProductRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;

// UnitTests.MultiThreadedMarketTests class contains multi-threaded tests for the
// MarketImpl class.
class MultiThreadedMarketTests extends UnitTest {
  private IUserController userController;
  // Helper method to create a mock IPaymentService
  private IPaymentService createMockPaymentService() {
    // Create a mock payment service
    IPaymentService paymentService = mock(IPaymentService.class);
    // Define a successful response
    Response successResponse = new Response("", false, 0);
    // Configure the mock payment service to always return a successful response
    when(paymentService.processPayment(any(PaymentDetails.class)))
        .thenReturn(successResponse);
    return paymentService;
  }

  // Helper method to create a mock ISupplyService
  private ISupplyService createMockSupplyService() {
    // Create a mock supply service
    ISupplyService supplyService = mock(ISupplyService.class);
    // Define a successful response
    Response successResponse = new Response("", false, 0);
    // Configure the mock supply service to always return a successful response
    when(supplyService.placeOrder(any(Store.class), any(AddressRecord.class),
                                  anyMap()))
        .thenReturn(successResponse);
    return supplyService;
  }

  // Helper method to create a mock IUserController
  private IUserController createUserController() throws SessionError {
    // Create a mock user controller
    IUserController userController = mock(IUserController.class);
    // Create a mock user
    IUser user = mock(User.class);
    // Create an initialized user cart
    DataMember dataMemberMock = mock(DataMember.class);
    when(dataMemberMock.getUsername()).thenReturn("John Doe");
    IUserCart cart = new UserCart(dataMemberMock);
    // Configure the mock user controller to return the mock user and user cart
    when(userController.getUser(anyString())).thenReturn(user);
    when(userController.getUserCart(anyString())).thenReturn(cart);
    return userController;
  }

  // Helper method to create a mock IStoreController
  private IStoreController createStoreController() {
    // Create a mock store controller
    IStoreController storeController = mock(IStoreController.class);
    // Create a mock store
    IStore store = mock(Store.class);
    // Create a store basket with a product
    IStoreBasket storeBasket = new DummyStoreBasket(1);
    storeBasket.addProduct(new ProductRecord(1, 1, "tst",10, Category.PETS, 10, 10, 1));
    // Configure the mock store controller to return the mock store and
    // calculated basket price
    try {
      when(storeController.getStore(anyInt())).thenReturn(store);
    } catch (NonExistentData e) {
      e.printStackTrace();
    }
    when(storeController.calculateBasketPrice(any())).thenReturn(50.0);
    // Configure the mock store controller to do nothing when removing and
    // adding cart quantities
    try {
      doNothing().when(storeController).removeCartQuantity(any());
    } catch (NonExistentData | DataError e) {
      e.printStackTrace();
    }
    try {
      doNothing().when(storeController).addCartQuantity(any());
    } catch (NonExistentData | DataError |PermissionError e) {
      e.printStackTrace();
    }

    return storeController;
  }

  // Set up method initializes the controllers and services before each test.
  @BeforeEach
  void setUp()
          throws SessionError{
    MarketImpl market = MarketImpl.getInstance();
    userController = createUserController();
    IStoreController storeController = createStoreController();
    // Initialize the controllers in the market instance
    market.initControllers(userController, storeController);
    // Update the payment and supply services in the market instance
    market.updatePaymentService(createMockPaymentService());
    market.updateSupplyService(createMockSupplyService());
  }

  // testConcurrentPurchase checks if the purchase method can handle concurrent
  // calls.
  @Test
  void testConcurrentPurchase() throws Exception {
    // Create a fixed thread pool with 10 threads
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<Future<?>> futures = new ArrayList<>();

    // Submit 10 concurrent purchase tasks to the executor
    for (int i = 0; i < 10; i++) {
      futures.add(executor.submit(() -> {
          // Execute a purchase in the MarketImpl instance
          try {
            MarketImpl.getInstance().purchase(
                    new util.Records.PaymentDetails("a", "1234567812345678", new DateRecord(LocalDate.of(1996, 12, 25)), "123"),
                    new AddressRecord("John Doe", "123 Main St", "New York", "NY",
                            "10001", "555-1234"),
                    userController.getUser("1"), userController.getUserCart("1"));
          } catch (NonExistentData | DataError | SessionError | PermissionError | PurchaseLimitation | PurchaseError e) {
            e.printStackTrace();
          }
      }));
    }

    // Wait for the completion of each task
    for (Future<?> future : futures) {
      future.get();
    }

    // If all tasks completed without errors, the test passed
  }
}
