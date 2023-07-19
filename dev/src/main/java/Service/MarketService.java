package Service;

import Domain.IMarket;
import util.Exceptions.SessionError;
import util.Records.PaymentDetails;
import util.Records.Transaction;
import Domain.Services.PaymentService.IPaymentService;
import Domain.User.IUser;
import Domain.User.IUserController;
import util.Enums.ErrorStatus;
import util.Exceptions.MarketException;
import util.Records.AddressRecord;

import java.time.LocalDateTime;
import java.util.List;

public class MarketService {

  private final IMarket controller;
  private final IUserController userController;

  public MarketService(IMarket market, IUserController userController) {
    this.controller = market;
    this.userController = userController;
  }

  /**
   * @implNote Possible expected failure codes:
   * <br>SESSION_ID_DOES_NOT_EXIST
   * <br>USERCART_DOES_NOT_EXISTS
   */
  public Result pay(String sessId, util.Records.PaymentDetails paymentDetails, AddressRecord deliveryAddress) {
    try {
      IUser user = userController.getUser(sessId);
      controller.purchase(paymentDetails, deliveryAddress, user, user.getUserCart());
    }
    catch (Exception e){
      return Result.makeBad(ErrorStatus.MAX, e.getMessage());
    }
    return Result.makeGood();
  }

  public Result<List<Transaction>>
  getStoreTransactionHistory(String sessId, int storeId,
                             LocalDateTime startDateTime,
                             LocalDateTime endDateTime) {
    return Result.makeGood(controller.getStoreTransactionsBetween(storeId, startDateTime, endDateTime));
  }

  public Result<List<Transaction>>
  getUserTransactionHistory(String sessId, String uname,
                            LocalDateTime startDateTime,
                            LocalDateTime endDateTime) {
    return Result.makeGood(controller.getUserTransactions(uname, startDateTime, endDateTime));
  }

  public Result memberPublishOffer(String sessionId, int storeId, int productId, double offerdPrice, int quantity) {
    try {
      controller.memberPublishOffer(sessionId, storeId, productId, offerdPrice, quantity);
    }
    catch (MarketException e){
      return Result.makeBad(e);
    }
    return Result.makeGood();
  }

  public Result memberRejectOffer(String sessionId, int offerId) {
    try {
      controller.memberRejectOffer(sessionId, offerId);
    }
    catch (MarketException e){
      return Result.makeBad(e);
    }
    return Result.makeGood();
  }

  public Result storeRejectOffer(String sessionId, int offerId, int storeId) {
    try {
      controller.storeRejectOffer(sessionId, offerId, storeId);
    }
    catch (MarketException e){
      return Result.makeBad(e);
    }
    return Result.makeGood();
  }

  public Result purchaseBid(String sessionId, PaymentDetails paymentDetails, AddressRecord deliveryAddress, int storeID, int offerID) {
    try {
      controller.purchaseBid(sessionId, paymentDetails, deliveryAddress, storeID, offerID);
    }catch (Exception e){
      return Result.makeBad(ErrorStatus.MAX, e.getMessage());
    }
    return Result.makeGood();
  }

  public Result<List<Transaction>> getTransactionHistory(String sessionId) {
    try {
      return Result.makeGood(controller.getTransactions().values().stream().toList());
    }catch (Exception e) {
      return Result.makeBad(ErrorStatus.NO_MANAGER_PERMISSION, e.getMessage());
    }
  }

  public Result updatePaymentService(IPaymentService paymentService) {
    try {
      controller.updatePaymentService(paymentService);
    }
    catch (Exception e){
      return Result.makeBad(ErrorStatus.MAX, e.getMessage());
    }
    return Result.makeGood();
  }

  public Result updatePaymentServiceURL(String url) {
    try {
      controller.updatePaymentServiceURL(url);
    }
    catch (Exception e){
      return Result.makeBad(ErrorStatus.MAX, e.getMessage());
    }
    return Result.makeGood();
  }

  public Result<List<Transaction>> getMyTransactionHistory(String sessionId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
    try {
      return Result.makeGood(controller.getMyTransactionHistory(sessionId, startDateTime, endDateTime));
    }
    catch (SessionError e) {
      return Result.makeBad(e);
    }
  }
}
