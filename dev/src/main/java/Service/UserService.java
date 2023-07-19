package Service;

import Domain.Services.NotificationService.NotificationObserver;
import Domain.Store.Offer;
import Domain.User.IStoreBasket;
import Domain.User.IUserController;
import util.Exceptions.*;
import util.Records.AddressRecord;
import util.Records.StoreRecords.ProductRecord;
import util.Records.UserRecords.UserRecord;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapter between service layer to IUserController
 */
public class UserService {

    private final IUserController controller;

    public UserService(IUserController controller){
        this.controller = controller;
    }

    public Result connect(String sessionId){
        try {
            controller.openSession(sessionId);
        }
        catch (SessionError e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     */
    public Result terminate(String sessId){
        try {
            controller.closeSession(sessId);
        } catch (SessionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>USERNAME_EXISTS
     * <br>INVALID_USERNAME
     * <br>INVALID_PASSWORD
     * <br>INVALID_EMAIL
     * <br>INVALID_PHONE_NUMBER
     */
    public Result register(String sessId, UserRecord userDetails, String pass) {
        try {
            controller.register(sessId, userDetails, pass);
        }
        catch (DataError | SessionError | DataExistentError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>ALREADY_LOGGED_IN
     * <br>USERNAME_PASSWORD_MISMATCH
     */
    public Result login(String sessId, String uname, String pass){
        try {
            controller.login(sessId, uname, pass);
        } catch (SessionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result sub(String sessId, NotificationObserver observer){
        try {
            controller.subscribeToNotifications(sessId, observer);
        } catch (SessionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result unsub(String sessId, NotificationObserver observer) {
        try {
            controller.unsubscribeFromNotifications(sessId, observer);
        } catch (SessionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     */
    public Result addProductToCart(String sessId, ProductRecord product) {
        try {
            controller.addProductsToStoreBasket(sessId, product.storeId(), Collections.singletonMap(product.productId(), product));
        } catch (SessionError | NonExistentData |DataError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     */
    public Result<Map<Integer, Map<Integer, ProductRecord>>> getCartContent(String sessId) {
        try {
            return  Result.makeGood(
                    controller.getUserCart(sessId)
                    .getStoreBaskets().stream()
                    .collect(Collectors.toMap(IStoreBasket::getStoreId, IStoreBasket::getProductsAsRecords)));
        } catch (SessionError e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>USERCART_DOES_NOT_EXISTS
     * <br>PRODUCT_DOES_NOT_EXIST
     */
    public Result removeProductFromCart(String sessId, int storeId, int productId, int quantity) {
        try {
            controller.removeProductFromStoreBasket(sessId, storeId, productId, quantity);
        } catch (NonExistentData | SessionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>GUEST_SESSION
     */
    public Result logout(String sessId) {
        try {
            controller.logout(sessId);
        } catch (SessionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     */
    public Result<Double> getCartPrice(String sessId) {
        try {
            return Result.makeGood(controller.getUserCart(sessId).getStoreBaskets().stream()
                    .mapToDouble(IStoreBasket::getBasketPriceAfterDiscount)
                    .sum());
        } catch (SessionError e) {
            return Result.makeBad(e);
        }
    }

    public Result<Boolean> isGuestSession(String sessionId) {
        return Result.makeGood(controller.isGuestSession(sessionId));
    }

    public Result<Boolean> isMemberSession(String sessionId) {
        return Result.makeGood(controller.isMemberSession(sessionId));
    }

    public Result<Boolean> isSystemManagerSession(String sessionId) {
        return Result.makeGood(controller.isSystemManagerSession(sessionId));
    }

    public Result removeMember(String session, String username) {
        try {
            controller.removeMember(session, username);
        } catch (MarketException e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result changePassword(String sessionId, String oldPassword, String newPassword) {
        try {
            controller.changePassword(sessionId, oldPassword, newPassword);
        } catch (MarketException e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result<Integer> addMemberAddress(String sessionId, AddressRecord addressData) {
        try {
            return Result.makeGood(controller.addAddress(sessionId, addressData));
        } catch (MarketException e) {
            return Result.makeBad(e);
        }
    }

    public Result<Integer> getAmountOfConnectedMembers(String sessionId) {
        try {
            return Result.makeGood(controller.getAmountOfConnectedMembers(sessionId));
        } catch (MarketException e) {
            return Result.makeBad(e);
        }
    }

    public Result<Integer> getAmountOfConnectedGuests(String sessionId) {
        try {
            return Result.makeGood(controller.getAmountOfConnectedGuests(sessionId));
        } catch (MarketException e) {
            return Result.makeBad(e);
        }
    }

    public Result<UserRecord> getMemberDetails(String sessionId) {
        try {
            return Result.makeGood(controller.getMemberDetails(sessionId));
        } catch (NonExistentData | SessionError e) {
            return Result.makeBad(e);
        }
    }

    public Result createSystemManager(String sessId, String userName, String password) {
        try {
            controller.createSystemManager(sessId, userName, password);
        }
        catch (DataError | DataExistentError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result<Map<Integer, Offer>> getMemberOffers(String sessionID) {
        try {
            return Result.makeGood(controller.getMemberOffers(sessionID));
        } catch (NonExistentData | SessionError e) {
            return Result.makeBad(e);
        }
    }

    public Result sendMessage(String sessionId, String msg, String receiver) {
        try {
            controller.sendMessage(msg, receiver, sessionId);
        } catch (MarketException e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result getAllPendingMessages(String sessionId) {
        try {
            controller.getAllPendingMessage(sessionId);
        } catch (MarketException e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result<Set<String>> getUsers(String session) {
        try {
            return Result.makeGood(controller.getUsers(session));
        }catch (PermissionError e){
            return Result.makeBad(e);
        }
    }

    public Result<Set<String>> getLoggedUsers(String session) {
        try {
            return Result.makeGood(controller.getLoggedUsers(session));
        }catch (PermissionError e){
            return Result.makeBad(e);
        }
    }

    public Result<Set<String>> getDisconnectedUsers(String session) {
        try {
            return Result.makeGood(controller.getDisconnectedUsers(session));
        }catch (PermissionError e){
            return Result.makeBad(e);
        }
    }
}

