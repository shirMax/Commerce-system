package Domain;

import DataLayer.ITransactionRepo;
import util.Records.PaymentDetails;
import util.Records.StoreRecords.ProductRecord;
import util.Records.Transaction;
import Domain.Services.NotificationService.INotificationService;
import Domain.Services.PaymentService.IPaymentService;
import Domain.Services.SupplyService.ISupplyService;
import util.Exceptions.PurchaseLimitation;
import Domain.Store.IStoreController;
import Domain.User.IStoreBasket;
import Domain.User.IUser;
import Domain.User.IUserCart;
import Domain.User.IUserController;
import util.Exceptions.*;
import util.Records.AddressRecord;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * The IMarket interface represents the interactions between the market system
 * and its components, such as payment service, supply service, store
 * controller, and user controller. It defines the methods for purchasing
 * products, calculating cart prices, managing transactions, and initializing
 * controllers.
 */
public interface IMarket {

  /**
   * Updates the payment service used by the market.
   *
   * @param paymentService the new payment service to be used
   */
  void updatePaymentService(IPaymentService paymentService);

  /**
   * Updates the notification service used by the market.
   *
   * @param notificationService the new notification service to be used
   */
    void updateNotificationService(INotificationService notificationService);

    /**
   * Updates the supply service used by the market.
   *
   * @param supplyService the new supply service to be used
   */
  void updateSupplyService(ISupplyService supplyService);

  void updateTransactionRepo(ITransactionRepo transactionRepo);

  /**
   * Processes a purchase by the user for it's current cart.
   * Calculates user's cart price and tries to charge for calculated amount.
   * If the process payment failed after removing storage from stores,
   * there will be a rollback to the original storage quantities of the stores.
   *
   * @param paymentDetails the payment details for the purchase
   * @param deliveryAddress the delivery address for the purchased items
   * @throws DataError if the session ID belongs to a guest or member
   * @throws DataError if the provided username does not exist
   * @throws DataError if the user cart does not exist
   * @throws NonExistentData if the store ID does not exist
   */
  void purchase(util.Records.PaymentDetails paymentDetails,
                AddressRecord deliveryAddress, IUser user, IUserCart userCart)
          throws DataError, NonExistentData, PermissionError, PurchaseLimitation, PurchaseError;

  /**
   * Calculates the total price of a user cart.
   *
   * @param cart the user cart to calculate the price for
   * @return the total price of the cart
   * @throws DataError if the user cart does not exist
   */
  double calculateCartPrice(IUserCart cart) throws DataError, NonExistentData, PurchaseLimitation;

    void updatePaymentServiceURL(String url) throws IOException;

    /**
   * Retrieves a map of all transactions in the market.
   *
   * @return a map of transaction ID to Transaction objects
   */
  Map<Integer, Transaction> getTransactions();

  /**
   * Retrieves a specific transaction by its ID.
   *
   * @param id the transaction ID
   * @return the Transaction object for the specified ID
   */
  Transaction getTransaction(int id) throws NonExistentData;

  /**
   * Retrieves a list of transactions for a specific store.
   *
   * @param storeId the store ID
   * @return a list of Transaction objects for the specified store
   */
  List<Transaction> getStoreTransactions(int storeId)
      throws DataError;

  /**
   * Returns a list of user transactions that occurred between the specified
   * start and end date times.
   *
   * @param userName       The user name whose transactions are to be retrieved.
   * @param startDateTime  The starting date and time for the transaction range.
   * @param endDateTime    The ending date and time for the transaction range.
   * @return               A list of transactions that match the specified
   *     criteria.
   */
  List<Transaction>
  getUserTransactions(String userName, LocalDateTime startDateTime,
                             LocalDateTime endDateTime);

  /**
   * Returns a list of store transactions that occurred between the specified
   * start and end date times.
   *
   * @param storeId        The store ID whose transactions are to be retrieved.
   * @param startDateTime  The starting date and time for the transaction range.
   * @param endDateTime    The ending date and time for the transaction range.
   * @return               A list of transactions that match the specified
   *     criteria.
   */
  List<Transaction>
  getStoreTransactionsBetween(int storeId, LocalDateTime startDateTime,
                              LocalDateTime endDateTime);

  /**
   * Adds a new transaction to the market.
   *
   * @param basket the user cart involved in the transaction
   * @param userName the username of the user who made the transaction
   * @param price the total price of the transaction
   */
  void addTransaction(IStoreBasket basket, String userName, double price);

  /**
   * Initializes the user and store controllers used by the market.
   *
   * @param userController the user controller to be used
   * @param storeController the store controller to be used
   */
  void initControllers(IUserController userController,
                       IStoreController storeController);

  /**
   * Retrieves the total number of transactions in the market.
   *
   * @return the total number of transactions
   */
  int getTransactionCount();

  /**

   Checks if the products with the given IDs exist in the inventory of the store with the given ID.
   @param storeId the ID of the store to check for product existence
   @param productsId the list of product IDs to check for existence
   @throws NonExistentData if one of the given product IDs does not exist in the inventory of the store with the given ID
   */
  void checkProductsExist(int storeId, Map<Integer, ProductRecord> productsId) throws NonExistentData, DataError;

  INotificationService getNotificationService();

  IStoreController getStoreController();

  //********************************************************************Bid - Offers
  void purchaseBid(String sessionId, PaymentDetails paymentDetails, AddressRecord deliveryAddress,
                   int storeID, int offerID)
          throws DataError, NonExistentData, PermissionError, PurchaseLimitation, SessionError, PurchaseError;

  void memberPublishOffer(String sessionId, int storeId, int productId, double offerdPrice, int quantity) throws SessionError, NonExistentData;

  void memberRejectOffer(String sessionId, int offerId) throws NonExistentData, PermissionError, SessionError;

  void storeRejectOffer(String sessionId, int offerId, int storeId) throws NonExistentData, PermissionError, SessionError;

  List<Transaction> getMyTransactionHistory(String sessionId, LocalDateTime startDateTime, LocalDateTime endDateTime) throws SessionError;
}
