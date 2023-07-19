package UnitTests;

import DataLayer.ITransactionRepo;
import DataLayer.TransactionRepo;
import Domain.Store.Category;
import Domain.User.DummyStoreBasket;
import util.Records.StoreRecords.ProductRecord;
import util.Records.Transaction;
import Domain.User.IStoreBasket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Exceptions.DataError;
import util.Exceptions.NonExistentData;
import util.Records.DateTimeRange;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionHistoryTest extends UnitTest {

  private ITransactionRepo transactionRepo;
  private IStoreBasket storeBasket;
  private String userName;
  private int storeId;
  private double price;

  @BeforeEach
  public void setUp() {
    userName = "TestUser";
    storeId = 1;
    price = 100.0;
    transactionRepo = new TransactionRepo();
    storeBasket = new DummyStoreBasket(storeId);

  }

  @Test
  public void testAddTransaction() {
    // Data
    ProductRecord product = new ProductRecord(storeId, 1, "name", 6, Category.BEAUTY, 6, 4, 2);
    storeBasket.addProduct(product);

    int transactionId =
        transactionRepo.addTransaction(storeBasket, userName, price);
    Transaction transaction =
            null;
    try {
      transaction = transactionRepo.getTransaction(transactionId);
    } catch (NonExistentData e) {
      fail(e.getMessage());
    }

    assertNotNull(transaction);
    assertEquals(transactionId, transaction.id());
    assertEquals(storeId, transaction.storeId());
    assertEquals(userName, transaction.userName());
    assertTrue(storeBasket.productsAreEqual(transaction.storeBasket()));
    assertEquals(price, transaction.price(), 0.001);
  }

  @Test
  public void testUserTransactions() {
    int transactionId =
        transactionRepo.addTransaction(storeBasket, userName, price);
    List<Transaction> userTransactions =
        transactionRepo.getUserTransactions(userName);

    assertNotNull(userTransactions);
    assertEquals(1, userTransactions.size());
    assertEquals(transactionId, userTransactions.get(0).id());
  }

  @Test
  public void testStoreTransactions() throws DataError {
    int transactionId =
        transactionRepo.addTransaction(storeBasket, userName, price);
    List<Transaction> storeTransactions =
        transactionRepo.getStoreTransactions(storeId);

    assertNotNull(storeTransactions);
    assertEquals(1, storeTransactions.size());
    assertEquals(transactionId, storeTransactions.get(0).id());
  }

  @Test
  public void testTransactionsBetween() {
    LocalDateTime startDateTime = LocalDateTime.now().minusDays(1);
    LocalDateTime endDateTime = LocalDateTime.now().plusDays(1);

    int transactionId =
        transactionRepo.addTransaction(storeBasket, userName, price);
    List<Transaction> transactionsBetween =
        transactionRepo.getTransactionsInRange(new DateTimeRange(startDateTime, endDateTime));

    assertNotNull(transactionsBetween);
    assertEquals(1, transactionsBetween.size());
    assertEquals(transactionId, transactionsBetween.get(0).id());
  }

  @Test
  public void testCleanTransactions() {
    int transactionId =
        transactionRepo.addTransaction(storeBasket, userName, price);
    transactionRepo.clean();

    Map<Integer, Transaction> transactions =
        transactionRepo.getTransactions();
    assertNotNull(transactions);
    assertEquals(0, transactions.size());
  }
}
