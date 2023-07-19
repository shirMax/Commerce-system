package DataLayer;

import util.Records.Transaction;
import Domain.User.IStoreBasket;
import util.Exceptions.NonExistentData;
import util.Records.DateTimeRange;

import java.util.List;
import java.util.Map;

public interface ITransactionRepo {
  /**
   * Creates and stores a new transaction.
   * @param basket Products bought.
   * @param userName user making the transaction.
   * @param price price paid.
   * @return ID of the newly added transaction.
   */
  int addTransaction(IStoreBasket basket, String userName, double price);

  /**
   * @return All system transactions. Ever.
   */
  Map<Integer, Transaction> getTransactions();

  /**
   * @param range DateTime range.
   * @return All system transactions that were made in the given range of time.
   */
  List<Transaction> getTransactionsInRange(DateTimeRange range);

  /**
   * @param id ID of a transactions.
   * @return Transaction with matching id.
   */
  Transaction getTransaction(int id) throws NonExistentData;

  /**
   * @param userName a user.
   * @return All transactions made by given user.
   */
  List<Transaction> getUserTransactions(String userName);

  /**
   * @param userName a user.
   * @param range DateTime range.
   * @return all transactions made by the given user that were made in the given range of time.
   */
  List<Transaction> getUserTransactionsInRange(String userName, DateTimeRange range);

  /**
   * @param storeId ID of a store.
   * @return all transactions made with the given store.
   */
  List<Transaction> getStoreTransactions(int storeId);

  /**
   * @param storeId ID of a store.
   * @param range DateTime range.
   * @return all transactions made with the given store that were made in the given range of time.
   */
  List<Transaction> getStoreTransactionsInRange(int storeId, DateTimeRange range);

  void clean();

  int getTransactionCount();
}