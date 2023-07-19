package Domain;

import DataLayer.ITransactionRepo;
import DataLayer.TransactionRepo;
import util.Records.Transaction;
import Domain.Services.NotificationService.INotificationService;
import Domain.Services.PaymentService.IPaymentService;
import Domain.Services.Response;
import Domain.Services.SupplyService.ISupplyService;
import util.Exceptions.PurchaseLimitation;
import Domain.Store.IStore;
import Domain.Store.IStoreController;
import Domain.Store.Offer;
import Domain.User.*;
import util.Exceptions.*;
import util.Records.AddressRecord;
import util.Records.DateTimeRange;
import util.Records.PaymentDetails;
import util.Records.StoreRecords.ProductRecord;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static util.Enums.ErrorStatus.*;

public class MarketImpl implements IMarket {
  private IPaymentService paymentService;
  private ISupplyService supplyService;
  private INotificationService notificationService;
  private IUserController userController;
  private IStoreController storeController;
  private ITransactionRepo transactionRepo;

  private AtomicLong paymentID = new AtomicLong(0);

  // TODO: find better lock name
  private ReadWriteLock marketOperationLock = new ReentrantReadWriteLock();
  public IPaymentService getPaymentService() { return paymentService; }

  public ISupplyService getSupplyService() { return supplyService; }

  public IUserController getUserController() { return userController; }

  public IStoreController getStoreController() { return storeController; }

  private MarketImpl() {}

  private static class Holder {

    private static final MarketImpl INSTANCE = new MarketImpl();
  }
  public synchronized static MarketImpl getInstance() {
    return Holder.INSTANCE;
  }

  public synchronized void initControllers(IUserController userController,
                                           IStoreController storeController) {
    this.storeController = storeController;
    this.userController = userController;
    this.transactionRepo = new TransactionRepo();
  }

  @Override
  public INotificationService getNotificationService() {
    return notificationService;
  }

  @Override
  public int getTransactionCount() {
    return transactionRepo.getTransactionCount();
  }

  @Override
  public void checkProductsExist(int storeId, Map<Integer, ProductRecord> products) throws NonExistentData, DataError {
    storeController.checkProductsExist(storeId, products);
  }

  @Override
  public synchronized void
  updatePaymentService(IPaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @Override
  public synchronized void updateNotificationService(INotificationService notificationService){
    this.notificationService = notificationService;
  }

  @Override
  public synchronized void updateSupplyService(ISupplyService supplyService) {
    this.supplyService = supplyService;
  }

  @Override
  public synchronized void
  updateTransactionRepo(ITransactionRepo transactionRepo) {
    this.transactionRepo = transactionRepo;
  }

  public void purchase(PaymentDetails paymentDetails, AddressRecord deliveryAddress, IUser user, IUserCart cart)
          throws DataError, NonExistentData, PermissionError, PurchaseLimitation, PurchaseError {
     String sessionId = user.getSessionId();
     String name = userController.isUserIsMember(sessionId) ? user.getUserName() : sessionId;
    double price = calculateCartPrice(cart);
    try {
      storeController.removeCartQuantity(cart);
    } catch (Exception e) {
      MarketLogger.logError("MarketImpl", "purchase",String.format(
          "Error msg: %s. could not remove %s user cart quantities ",
          e.getMessage(), name));
      throw e;
    }
    paymentDetails = paymentDetails.setID(paymentID.getAndIncrement());
    Response res = paymentService.processPayment(paymentDetails.setPrice(price));

    if (res.isErrorOccurred()) {
      MarketLogger.logError("MarketImpl", "purchase", String.format(
          "Error msg: %s. could not process payment for user: %s.",
          res.getMessage(), name));
      storeController.addCartQuantity(cart);
      throw new PurchaseError(res.getMessage(), PAYMENT_PROCESS_FAILED);
    }

    for (IStoreBasket basket : cart.getStoreBaskets()) {
      IStore store = storeController.getStore(basket.getStoreId());
      notificationService.broadcastMessage(store.getStoreRoles("System").keySet().stream().toList(),
              "System notification:\nThe user: "+name + " bought "+basket.getBasketPriceAfterDiscount()+
                      "$ from the store", "System notification from "+store.getStoreName());
      supplyService.placeOrder(store, deliveryAddress, basket.getProductsAsRecords());
      transactionRepo.addTransaction(basket, name, storeController.calculateBasketPrice(basket));
    }

    user.removeUserCart();
  }

  @Override
  public double calculateCartPrice(IUserCart cart) throws NonExistentData, PurchaseLimitation {
    double price = 0;
    for (IStoreBasket basket : cart.getStoreBaskets()) {
      try {
        storeController.checkPurchaseRules(basket);
        price += storeController.calculateBasketPrice(basket);
      } catch (Exception e) {
        MarketLogger.logError("MarketImpl", "calculateCartPrice",String.format(
                "Error msg: %s. could not calculate basket price", e.getMessage()));
        throw e;
      }
    }
    return price;
  }

  @Override
  public void updatePaymentServiceURL(String url) throws IOException {
    paymentService.updatePaymentServiceURL(url);
  }

  ////////////////////////////////////////////transactions

  @Override
  public Map<Integer, Transaction> getTransactions() {
    try {
      return transactionRepo.getTransactions();
    } catch (Exception e) {
      MarketLogger.logError("MarketImpl", "getTransactions",String.format(
          "Error msg: %s. could not return all transactions", e.getMessage()));
      throw e;
    }
  }
  @Override
  public Transaction getTransaction(int id) throws NonExistentData {
    try {
      return transactionRepo.getTransaction(id);
    } catch (Exception e) {
      MarketLogger.logError("MarketImpl", "getTransaction",
          String.format("Error msg: %s. could not return transactions %d",
                        e.getMessage(), id));
      throw e;
    }
  }

  @Override
  public List<Transaction> getStoreTransactions(int storeId)
      throws DataError {
    try {
      return transactionRepo.getStoreTransactions(storeId);
    } catch (Exception e) {
      MarketLogger.logError("MarketImpl", "getStoreTransactions",String.format(
          "Error msg: %s. could not receive %s store transactions",
          e.getMessage(), storeId));
      throw e;
    }
  }

  public List<Transaction>
  getStoreTransactionsBetween(int storeId, LocalDateTime startDateTime,
                              LocalDateTime endDateTime) {
    try {
      return transactionRepo.getStoreTransactionsInRange(
              storeId,
              new DateTimeRange(startDateTime, endDateTime)
      );
    } catch (Exception e) {
      MarketLogger.logError("MarketImpl", "getStoreTransactionsBetween",String.format(
              "Error msg: %s. could not receive %s store transactions",
              e.getMessage(), storeId));
      throw e;
    }
  }

  public List<Transaction>
  getUserTransactions(String userName, LocalDateTime optionalStartDateTime,
                             LocalDateTime optionalEndDateTime) {
    try {
      return transactionRepo.getUserTransactionsInRange(
              userName,
              new DateTimeRange(optionalStartDateTime, optionalEndDateTime)
      );
    } catch (Exception e) {
      MarketLogger.logError("MarketImpl", "getUserTransactionsBetween",
          String.format("Error msg: %s. could not receive %s user transactions",
                        e.getMessage(), userName));
      throw e;
    }
  }

  public List<Transaction>
  getMyTransactionHistory(String sessionId, LocalDateTime optionalStartDateTime,
                      LocalDateTime optionalEndDateTime) throws SessionError {
    String userName = userController.getMemberUserName(sessionId);
    try {
      return transactionRepo.getUserTransactionsInRange(
              userName,
              new DateTimeRange(optionalStartDateTime, optionalEndDateTime)
      );
    } catch (Exception e) {
      MarketLogger.logError("MarketImpl", "getUserTransactionsBetween",
              String.format("Error msg: %s. could not receive %s user transactions",
                      e.getMessage(), userName));
      throw e;
    }
  }

  @Override
  public void addTransaction(IStoreBasket basket, String userName,
                             double price) {
    try {
      transactionRepo.addTransaction(basket, userName, price);
    } catch (Exception e) {
      MarketLogger.logError("MarketImpl", "addTransaction", String.format(
          "Error msg: %s. could not add transaction", e.getMessage()));
      throw e;
    }
  }

  public synchronized void cleanTransactions() {
    try {
      transactionRepo.clean();
    } finally {
    }
  }

  //********************************************************************Bid - Offers
  @Override
  public void purchaseBid(String sessionId, PaymentDetails paymentDetails, AddressRecord deliveryAddress,
                          int storeID, int offerID)
          throws DataError, NonExistentData, PermissionError, SessionError, PurchaseError {
    //check the session exist
    String offeringMember = userController.getMemberUserName(sessionId);
    IStore store = storeController.getStore(storeID);
    Offer offer = store.getProductOffer(offerID);
    int productID = offer.getProduct().productId();

    //Check that there is agreement from the store for this offer
    if(!offer.isStoreConsent()) {
      MarketLogger.logError("MarketImpl", "purchaseBid", "store managers not fully consent to this bid offer", sessionId, storeID);
      throw new PurchaseError(
              "store managers not fully consent to this bid offer",
              OFFER_NOT_FULLY_CONSENTED
      );
    }
    if(!offer.getOfferingMember().equals(offeringMember)){
      MarketLogger.logError("MarketImpl", "purchaseBid", "user name offer and the buyer are not equals", sessionId, storeID);
      throw new PurchaseError(
              "user name offer and the buyer are not equals",
              INVALID_USERNAME
      );
    }

    try {
      storeController.removeOfferQuantity(offer);
    } catch (Exception e) {
      MarketLogger.logError("MarketImpl", "purchaseBid", String.format(
              "Error msg: %s. could not remove %s offer quantity ",
              e.getMessage(), offeringMember));
      throw e;
    }

    //calculate the offer price
    double price = offer.getOfferedPrice() * offer.getOfferedQuantity();
    //process payment
    Response res = paymentService.processPayment(paymentDetails.setPrice(price));

    if (res.isErrorOccurred()) {
      MarketLogger.logError("MarketImpl", "purchase", String.format(
              "Error msg: %s. could not process payment for user: %s.",
              res.getMessage(), offeringMember));
      storeController.restoreOfferQuantity(offer);
      throw new PurchaseError(
              res.getMessage(),
              PAYMENT_PROCESS_FAILED
      );
    }

    //supply the purchase
    Map<Integer, ProductRecord> products = new HashMap<>();
    products.put(productID, offer.getProduct());
    supplyService.placeOrder(store, deliveryAddress, products);

    //notify to store manager for purchase
    notificationService.broadcastMessage(store.getStoreRoles("System").keySet().stream().toList(),
            "System notification:\nThe user: "+offeringMember + " bought "+ price+
                    "$ from the store", "System notification from "+store.getStoreName());

    //add transaction to history
    IStoreBasket basket = new DummyStoreBasket(storeID, LocalDate.now(), products.values());
    transactionRepo.addTransaction(basket, offeringMember, price);
    store.removeOffer("System", offerID);
    userController.getMember(offeringMember).removeOffer(offerID);
  }

  @Override
  public void memberPublishOffer(String sessionId, int storeId, int productId, double offeredPrice, int quantity) throws SessionError, NonExistentData {
    String username = userController.getMemberUserName(sessionId);
    IStore store = storeController.getStore(storeId);
    Offer offer = store.publishMemberOffer(username, productId, offeredPrice, quantity);
    userController.getMember(username).addOffer(offer);
  }

  @Override
  public void memberRejectOffer(String sessionId, int offerId) throws NonExistentData, PermissionError, SessionError {
    Member member = userController.getMember(userController.getMemberUserName(sessionId));
    Offer offer = member.getOffers().get(offerId);
    member.removeOffer(offerId);
    storeController.getStore(offer.getProduct().storeId()).removeOffer("System", offerId);
  }

  @Override
  public void storeRejectOffer(String sessionId, int offerId, int storeId) throws NonExistentData, PermissionError, SessionError {
    String userName = userController.getMemberUserName(sessionId);
    IStore store = storeController.getStore(storeId);
    Offer offer = store.getOffers(userName).get(offerId);
    userController.getMember(offer.getOfferingMember()).removeOffer(offerId);
    store.removeOffer(userName, offerId);
  }
}