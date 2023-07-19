package Service;

import Domain.Services.NotificationService.NotificationObserver;
import Domain.Services.PaymentService.IPaymentService;
import org.checkerframework.org.apache.commons.lang3.NotImplementedException;
import util.Enums.PermissionType;
import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;
import util.Exceptions.PermissionError;
import util.Exceptions.SessionError;
import util.Records.Transaction;
import Domain.Store.Discount.IDiscount;
import Domain.Store.Offer;
import Domain.Store.Purchase.PurchaseRule;
import util.Enums.RoleType;
import util.Records.AddressRecord;
import util.Records.PaymentDetails;
import util.Records.StoreRecords.ProductFilterAttributes;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * API for system facade - should service all use-cases
 */
public interface ISystemFacade {

    /// Guest Actions - General

    /**
     * Initiates new session
     *
     * @return Session's ID
     */
    Result connect(String sessionId); //todo: add to the document the sessionId string

    /**
     * Terminates session.
     *
     * @param sessId session ID corresponding with the session to terminate.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     */
    Result terminate(String sessId);

    /**
     * Registers a new user to the system.
     *
     * @param sessId      session initiating the request
     * @param userDetails all the details the user wishes to register with.
     * @param pass        password.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>USERNAME_EXISTS
     * <br>INVALID_USERNAME
     * <br>INVALID_PASSWORD
     * <br>INVALID_EMAIL
     * <br>INVALID_PHONE_NUMBER
     */
    Result register(String sessId, UserRecord userDetails, String pass);

    /**
     * Logs the session to a signed-user.
     *
     * @param sessId session ID to log in.
     * @param uname  username.
     * @param pass   password.
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>ALREADY_LOGGED_IN
     * <br>USERNAME_PASSWORD_MISMATCH
     */
    Result login(String sessId, String uname, String pass);

    /**
     * Subscribes the given observer to notifications to the user identified with the given sessionID.
     *
     * @param sessId Session ID of the member making the request. Must be logged in.
     * @param observer  Observer to activate once the member receives a notification.
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     */
    Result subscribeToNotifications(String sessId, NotificationObserver observer);

    /**
     * Unsubscribes the given observer given from notifications of the user identified with the given sessionID.
     *
     * @param sessId Session ID of the member making the request. Must be logged in.
     * @param observerToRemove  Observer which was subscribed.
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     */
    Result unsubscribeFromNotifications(String sessId, NotificationObserver observerToRemove);

    /// Guest Actions - Buy

    /**
     * Returns stores which match given filters
     *
     * @param sessId     session ID performing the request.
     * @param name       filter for store name.
     * @param categories filter for store categories.
     * @param keywords   filter for store keywords.
     * @return List of store IDs.
     */
    Result<List<Integer>> getStoresBy(String sessId, String name, List<String> categories, List<String> keywords);

    /**
     * Returns information about a store.
     *
     * @param sessId  session ID performing the request.
     * @param storeId store ID to get info about.
     * @return Store info.
     * @implNote Possible expected failure codes:
     * <br>STORE_DOES_NOT_EXIST
     */
    Result<StoreRecord> getStoreInfo(String sessId, int storeId);

    /**
     * Returns products which match given filters
     *
     * @param sessId  session ID performing the request.
     * @param filters collection of attributes by why to filter the wanted products.
     * @return List of products.
     */
    //todo to return list of products info
    Result<List<ProductRecord>> getProductsBy(String sessId, ProductFilterAttributes filters);

    /**
     * Information of given product
     *
     * @param sessId    session ID performing the request.
     * @param productId product ID searching for.
     * @param storeId   store in which the product is sold.
     * @return Product information
     * @implNote Possible expected failure codes:
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     */
    Result<ProductRecord> getProductInfo(String sessId, int productId, int storeId);

    /**
     * Adds given product to user cart.
     *
     * @param sessId    session ID performing the request - adding the product to the cart of corresponding user.
     * @param productId product ID to add.
     * @param storeId   store in which the product is sold at.
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     */
    Result addProductToCart(String sessId, int productId, int storeId, int quantity);

    /**
     * Retrieve user's current cart state
     *
     * @param sessId session ID performing the request - retrieving the cart of corresponding user.
     * @return Map from store ID to Map from product ID placed from that store to quantity.
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     */
    Result<Map<Integer, Map<Integer, ProductRecord>>> getCartContent(String sessId);

    /**
     * Removes product from user's cart
     *
     * @param sessId    session ID performing the request - removing from the cart of corresponding user.
     * @param storeId   store the product was placed from
     * @param productId product to remove
     * @param quantity
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>USERCART_DOES_NOT_EXISTS
     * <br>PRODUCT_DOES_NOT_EXIST
     */
    Result removeProductFromCart(String sessId, int storeId, int productId, int quantity);

    /**
     * Returns price of whole cart
     *
     * @param sessId session ID performing the request - calculating the cart of corresponding user.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     */
    Result<Double> getCartPrice(String sessId);

    /**
     * Pays for the user's cart.
     *
     * @param sessId          session ID performing the request - user to pay.
     * @param paymentDetails  pay info used to complete the payment.
     * @param deliveryAddress address of delivery.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>USERCART_DOES_NOT_EXISTS
     */
    Result pay(String sessId, PaymentDetails paymentDetails, AddressRecord deliveryAddress);

//    /**
//     * Places bid on an item placed for auction.
//     *
//     * @param sessId    session ID performing the request - user to bid.
//     * @param storeId   store in which the auction is taking place.
//     * @param productId product to bid on.
//     * @param bidVal    bid value.
//     * @return Result indicating success/failure
//     */
//    Result placeBid(String sessId, int storeId, int productId, int bidVal);
//
//    /**
//     * Places bet on raffled item. Immediately pays for the bet.
//     *
//     * @param sessId          session ID performing the request - user to bet.
//     * @param storeId         store in which the auction is taking place.
//     * @param productId       product to bet on.
//     * @param betVal          bet value.
//     * @param payInfo         pay info used to complete the payment.
//     * @param deliveryMethod  code for delivery method.
//     * @param deliveryAddress address to deliver to.
//     * @return Result indicating success/failure
//     */
//    Result placeBet(String sessId, int storeId, int productId, int betVal, PaymentDetails payInfo, int deliveryMethod, Address deliveryAddress);

    /// Logged-User Actions - General

    /**
     * Logs out from the user - degrading the session to guest.
     *
     * @param sessId
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>GUEST_SESSION
     */
    Result logout(String sessId);

    /**
     * Opens new store.
     *
     * @param sessId session ID performing the request - user to open the store.
     * @param store  information about the new store
     * @return New store's id
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>GUEST_SESSION
     */
    Result<Integer> openStore(String sessId, StoreRecord store);

    /// Logged-User Actions - Store-Owner

    /**
     * Updated product's price.
     *
     * @param sessId       session ID performing the request - store owner.
     * @param updatedStore updated state of the store.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     */
    Result updateStore(String sessId, StoreRecord updatedStore);

    /**
     * Adds a new product to the specified store.
     *
     * @param sessId  session ID performing the request - store owner.
     * @param product product's data to be set.
     * @return Product ID of newly added product
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>INVALID_PRODUCT_NAME
     * <br>INVALID_PRODUCT_PRICE
     * <br>INVALID_PRODUCT_QUANTITY
     * <br>INVALID_PRODUCT_CATEGORY
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    Result<Integer> addNewProduct(String sessId, ProductRecord product);

    /**
     * Removes the specified product from the specified store.
     *
     * @param sessId    session ID performing the request - store owner.
     * @param storeId   store to remove product from.
     * @param productId product to remove.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    Result removeProduct(String sessId, int storeId,
                         int productId);

    /**
     * Updated product's price.
     *
     * @param sessId         session ID performing the request - store owner.
     * @param updatedProduct updated state of the product.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     * <br>INVALID_PRODUCT_NAME
     * <br>INVALID_PRODUCT_PRICE
     * <br>INVALID_PRODUCT_CATEGORY
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    Result updateProduct(String sessId, ProductRecord updatedProduct);

    /**
     * Add quantity to a product in a store
     *
     * @param sessId    session ID performing the request - store owner.
     * @param storeId   store where product exists.
     * @param productId product to update.
     * @param quantity  quantity to add.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     * <br>INVALID_PRODUCT_QUANTITY
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    Result addProductQuantity(String sessId, int storeId, int productId,
                              int quantity);

    /**
     * Remove quantity from a product in a store
     *
     * @param sessId    session ID performing the request - store owner.
     * @param storeId   store where product exists.
     * @param productId product to update.
     * @param quantity  quantity to remove.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     * <br>INVALID_PRODUCT_QUANTITY
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    Result removeProductQuantity(String sessId, int storeId, int productId,
                                 int quantity);

    /**
     * Appoint a user as store owner.
     *
     * @param sessId  session ID performing the request - store owner.
     * @param uname   username to be appointed.
     * @param storeId store to appoint to.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_ADD_OR_REMOVE_STORE_OWNER
     * <br>USERNAME_DOES_NOT_EXIST
     * <br>MEMBMER_ALREADY_HAS_ROLE
     */
    Result appointOwner(String sessId, String uname, int storeId);

    /**
     * Appoint a user as store manager.
     *
     * @param sessId  session ID performing the request - store owner.
     * @param uname   username to be appointed.
     * @param storeId store to appoint to.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_ADD_OR_REMOVE_STORE_MANAGER
     * <br>USERNAME_DOES_NOT_EXIST
     * <br>MEMBMER_ALREADY_HAS_ROLE
     */
    Result appointManager(String sessId, String uname, int storeId);

    /**
     * Get permissions of specified store manager of specified store
     *
     * @param sessId  session ID performing the request - store owner.
     * @param uname   specified manager.
     * @param storeId specified store.
     * @return bitmap detailing all permissions of the user
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_GET_EMPLOYEES_DATA
     * <br>USERNAME_DOES_NOT_EXIST
     */
    Result<Integer> getManagerPermissions(String sessId, String uname, int storeId);

    /**
     * Set permission of specified manager of specified store.
     *
     * @param sessId   session ID performing the request - store owner.
     * @param uname    specified manager.
     * @param storeId  specified store.
     * @param newPerms bitwise or composition of all new permissions.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>USERNAME_DOES_NOT_EXIST
     * <br>NO_PERMISSION_CHANGE_OWNER_PERMISSIONS
     */
    Result modifyPermissionsFor(String sessId, String uname, int storeId, int newPerms);

    /**
     * Closes specified store.
     *
     * @param sessId  session ID performing the request - store founder.
     * @param storeId store to close.
     * @return Result indicating success/failure
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_MAKE_STORE_INACTIVE
     */
    Result closeStore(String sessId, int storeId);

    /**
     * Returns all store owners.
     *
     * @param sessId  session ID performing the request - store owner.
     * @param storeId store to query.
     * @return List of usernames who are managers of specified store.
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_GET_EMPLOYEES_DATA
     */
    Result<List<String>> getStoreOwners(String sessId, int storeId);

    Result<String> getStoreFounder(String sessId, int storeId);

    /**
     * Returns all store managers.
     *
     * @param sessId  session ID performing the request - store owner.
     * @param storeId store to query.
     * @return List of usernames who are managers of specified store.
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_GET_EMPLOYEES_DATA
     */
    Result<List<String>> getStoreManagers(String sessId, int storeId);

    /**
     * Returns all of the store's transactions between dates.
     *
     * @param sessId    session ID performing the request - store owner.
     * @param storeId   store to query.
     * @param dateRange date range - returns transactions made between dateRange.getLeft() and dateRange.getRange() or all if null.
     * @return List of transactions.
     * @apiNote this query can be performed by a system admin as well
     */
    Result<List<Transaction>> getStoreTransactionHistory(String sessId, int storeId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /// Logged-User Actions - System-Admin

    /**
     * Returns all of the user's transactions between dates.
     *
     * @param sessId    session ID performing the request - store owner.
     * @param uname     user.
     * @param dateRange date range - returns transactions made between dateRange.getLeft() and dateRange.getRange() or all if null.
     * @return List of transactions.
     * @apiNote this query can be performed by a system admin as well
     */
    Result<List<Transaction>> getUserTransactionHistory(String sessId, String uname, LocalDateTime startDateTime, LocalDateTime endDateTime);

    Result reopenStore(String sessionId, int storeId);

    Result<List<StoreRecord>> getMyStores(String sessionId);

    Result removeStoreOwner(String sessionId, String username, Integer storeId);

    Result removeStoreManager(String sessionId, String username, Integer storeId);

    Result closeStorePermanently(String sessionId, int storeId);

    Result<Set<Integer>>
    getStoresAccordingToRole(String sessionId, RoleType role);

    Result removeMember(String session, String username);

    Result<Integer> getMemberPermissions(String sessionId, Integer storeId);

    Result changePassword(String sessionId, String oldPassword, String newPassword);

    Result<Integer> addMemberAddress(String sessionId, AddressRecord addressData);

    Result<List<ProductRecord>> getStoreProducts(String sessionId, int storeId);

    Result<Map<String, Integer>> getManagersPermissions(String sessionId, int storeId);

    Result<Set<StoreRecord>> getStores();

    Result<Integer> getAmountOfConnectedMembers(String sessionId);

    Result<Integer> getAmountOfConnectedGuests(String sessionId);

    Result<UserRecord> getMemberDetails(String sessionId);

    Result<List<Transaction>> getTransactionHistory(String sessionId);

    Result addDiscount(String session, int storeId, IDiscount discount);

    Result removeDiscount(String session, int storeID, int discountId);

    Result<List<IDiscount>> getStoreDiscounts(String session, int storeID);

    Result checkCartPurchaseRules(String session);

    Result addPurchaseRule(String session, int storeId, PurchaseRule rule);

    Result removePurchaseRule(String session, int storeID, int purchaseRuleID);

    Result<List<PurchaseRule>> getStorePurchaseRules(String session, int storeID);

    Result memberPublishOffer(String sessionId, int storeId, int productId, double offerdPrice, int quantity);

    Result memberRejectOffer(String sessionId, int offerId);

    Result storeRejectOffer(String sessionId, int offerId, int storeId);

    Result purchaseBid(String sessionId, PaymentDetails paymentDetails, AddressRecord deliveryAddress, int storeID, int offerID);

    Result<Map<Integer, Offer>> getMemberOffers(String sessionID);

    Result consentOffer(String sessionId, int offerId, int storeId);

    Result<Map<Integer, Offer>> getOffers(String sessionId, int storeId);

    Result createSystemManager(String sessId, String username, String password);

    Result<IDiscount> getStoreDiscount(String sessionId, Integer storeId, Integer discountId);

    Result counterOffer(String sessionId, int storeId, int offerId, int productQuantity, double productPrice);

    Result sendMessage(String msg, String receiver, String sessionIdSender);

    Result updatePaymentService(IPaymentService paymentService);

    Result getAllPendingMessages(String sessionId);

    Result updatePaymentServiceURL(String url);

    Result<Set<String>> getUsers(String session);

    Result<Set<String>> getLoggedUsers(String session);

    Result<Set<String>> getDisconnectedUsers(String session);

    Result<Set<PermissionType>> getUserPermissionsTypes(String session, String userName,Integer storeID);

    //****************************************************************** Contracts
    Result publishMemberContract(String sessionId, int storeId, String newOwnerUserName, String contract) throws SessionError, NonExistentData, PermissionError, DataExistentError;

    Result removeContract(String sessionId, int storeId, int contractId) throws NonExistentData, PermissionError, SessionError;

    Result consentContract(String sessionId, int contractId, int storeId) throws NonExistentData, PermissionError, SessionError, DataExistentError;

    Result getContracts(String sessionId, int storeId) throws NonExistentData, PermissionError, SessionError;
}
