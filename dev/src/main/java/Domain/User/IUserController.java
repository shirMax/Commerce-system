package Domain.User;

import Domain.Services.NotificationService.NotificationObserver;
import util.Records.Transaction;
import Domain.Services.NotificationService.INotificationService;
import Domain.Store.Offer;
import util.Exceptions.*;
import util.Records.AddressRecord;
import util.Records.StoreRecords.ProductRecord;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IUserController {

  void updateNotificationService (INotificationService notificationService);

  /**
   * Opens a new session for a user and returns a unique session identifier.
   *
   * @return the session identifier
   */
  void openSession(String sessionId) throws SessionError;

  /**
   * Closes the session with the given session identifier. If it's a guest session,
   * the user cart associated with the session will be deleted.
   *
   * @param sessionId the identifier of the session to be closed
   * @throws SessionError if the session does not exist
   */
  void closeSession(String sessionId) throws SessionError;

  /**
   * Retrieves the user associated with the given session identifier.
   *
   * @param sessionId the identifier of the session
   * @return the user associated with the session
   * @throws SessionError     if the session is not a member or guest session
   */
  IUser getUser(String sessionId) throws SessionError;

  /**
   * Retrieves the user associated with the userName.
   *
   * @param userName the userName of the member
   * @return the user associated with the userName
   */
  Member getMember(String userName) throws NonExistentData;

  /**
   * Performs a login operation using the provided username and password.
   *
   * @param sessionId the identifier of the session
   * @param userName the username of the user to be logged in
   * @param password the password of the user to be logged in
   * @throws SessionError if the provided username does not exist
   * @throws SessionError if the user is already logged in
   * @throws SessionError if the provided password is incorrect
   * @throws SessionError if the sessionId does not exist
   */
  void login(String sessionId, String userName, String password) throws SessionError;

  /**
   * Subscribes the given observer to notifications to the user identified with the given sessionID.
   *
   * @param sessionID Session ID of the member making the request. Must be logged in.
   * @param observer  Observer to activate once the member receives a notification.
   * @throws SessionError if the sessionID doesn't correspond to a logged in member.
   */
  void subscribeToNotifications(String sessionID, NotificationObserver observer) throws SessionError;

  /**
   * Unsubscribes the given observer given from notifications of the user identified with the given sessionID.
   *
   * @param sessionID Session ID of the member making the request. Must be logged in.
   * @param observerToRemove  Observer which was subscribed.
   * @throws SessionError if the sessionID doesn't correspond to a logged in member.
   */
  void unsubscribeFromNotifications(String sessionID, NotificationObserver observerToRemove) throws SessionError;

  /**
   * Performs a logout operation for the user associated with the given session
   * identifier.
   *
   * @param sessionId the identifier of the session
   * @throws SessionError if the user is already disconnected
   * @throws SessionError if the session does not exist
   * @throws SessionError if the user is a guest and tries to perform a logout operation
   */
  void logout(String sessionId) throws SessionError;

    boolean isMemberIsSystemManager(String sessionId);

    /**
     * Registers a new user with the system.
     *
     * @param sessionId The session ID of the user.
     * @param userData  The username of the user.
     * @param password  The password of the user.
     * @throws DataExistentError If the username already exists in the system.
     * @throws DataError         If the username is invalid.
     * @throws DataError         If the password is invalid.
     * @throws DataError         If the email is invalid.
     * @throws DataError         If the phone number is invalid.
     * @throws SessionError      If the session ID does not exist.
     */
  void register(String sessionId, UserRecord userData, String password) throws DataError, SessionError, DataExistentError;

  /**
   * Changes the password of a user.
   *
   * @param sessionId    The session ID of the user.
   * @param oldPassword  The old password of the user.
   * @param newPassword  The new password of the user.
   * @throws DataError   If the new password is illegal.
   * @throws SessionError If the session is not a system manager or member session.
   */
  void changePassword(String sessionId, String oldPassword, String newPassword) throws DataError, SessionError;

    void sendMessage(String msg, String receiver, String sessionIdSender) throws SessionError, NonExistentData;

    /**
   * Gets the user's cart for a given session.
   *
   * @param sessionId The session ID of the user.
   * @return The user's cart.
   * @throws SessionError   If the session is not a guest or member session.
   */
  IUserCart getUserCart(String sessionId) throws SessionError;

  /**
   * Adds products to the store basket for a given session.
   *
   * @param sessionId             The session ID of the user.
   * @param storeId               The ID of the store.
   * @param products A map containing the product IDs and their corresponding quantities.
   * @throws SessionError     If the session is not a guest or member session.
   * @throws NonExistentData  If store/product don't exist
   */
  void addProductsToStoreBasket(String sessionId, int storeId,
                                Map<Integer, ProductRecord> products) throws SessionError, NonExistentData, DataError;
  /**
   * Removes a product from the store basket for a given session ID, store ID, and product ID.
   *
   * @param sessionId The ID of the user session.
   * @param storeId   The ID of the store.
   * @param productId The ID of the product to be removed.
   * @param quantity
   * @throws NonExistentData If the product does not exist in the store basket.
   * @throws NonExistentData If the user's cart does not exist.
   * @throws SessionError    If the session is not a guest or member session.
   */
  void removeProductFromStoreBasket(String sessionId, int storeId, int productId, int quantity) throws SessionError, NonExistentData;

  /**
   * Updates the quantity of a product in the store basket for a given session ID, store ID, product ID, and quantity.
   *
   * @param sessionId  The ID of the user session.
   * @param storeId    The ID of the store.
   * @param productId  The ID of the product to be updated.
   * @param quantity   The updated quantity of the product.
   * @throws NonExistentData If the product does not exist in the store basket.
   * @throws SessionError If the session is not a guest or member session.
   */
  void updateProductQuantityInStoreBasket(String sessionId, int storeId, int productId, int quantity) throws SessionError, NonExistentData;

  /**
   * Checks if the user associated with the given session ID is a system manager.
   *
   * @param sessionId The ID of the user session.
   * @return True if the user is a system manager, false otherwise.
   */
  boolean isUserIsSystemManager(String sessionId);

  /**
   * Checks if the user associated with the given session ID is a member.
   *
   * @param sessionId The ID of the user session.
   * @return True if the user is a member, false otherwise.
   */
  boolean isUserIsMember(String sessionId);

  /**
   * Retrieves the username of the member associated with the given session ID.
   *
   * @param sessionId The ID of the user session.
   * @return The username of the member.
   * @throws SessionError If the session is not a member session.
   */
  String getMemberUserName(String sessionId) throws SessionError;

  /**
   * Checks if a session with the given session ID exists.
   *
   * @param sessionId The ID of the user session.
   * @return True if the session exists, false otherwise.
   */
  boolean isSessionExists(String sessionId);

  /**
   * Adds a role to a member identified by their username for a given store ID.
   *
   * @param userName The username of the member.
   * @param storeId  The ID of the store.
   * @throws DataExistentError If the store already has a role assigned to the member.
   */
  void addRoleToMember(String userName, int storeId) throws DataExistentError, NonExistentData;

  /**
   * Removes a role from a member.
   *
   * @param userName The username of the member.
   * @param storeId The store ID from which the role should be removed.
   * @throws NonExistentData If the member role does not exist.
   */
  void removeRoleFromMember(String userName, int storeId) throws NonExistentData;

  /**
   * Creates a system manager.
   *
   * @param sessionId The session ID of the current session.
   * @param userName The username of the system manager to be created.
   * @param password The password of the system manager to be created.
   * @throws DataExistentError If the username already exists.
   * @throws DataError If the username is invalid.
   * @throws DataError If the password is invalid.
   */
  void createSystemManager(String sessionId, String userName, String password) throws DataError, DataExistentError;

  /**
   * Changes the password of a system manager.
   *
   * @param sessionId The session ID of the current session.
   * @param oldPassword The old password of the system manager.
   * @param newPassword The new password of the system manager.
   * @throws DataError If the old password is incorrect.
   * @throws DataError If the username does not exist.
   * @throws SessionError If the session is not a system manager session.
   */
  void changeSystemManagerPassword(String sessionId, String oldPassword, String newPassword) throws DataError, SessionError;

  /**
   * Retrieves the stores associated with a member.
   *
   * @param sessionId The username of the member.
   * @return A list of store IDs associated with the member.
   */
  List<Integer> getMemberStores(String sessionId) throws SessionError;

  /**
   * Retrieves the primary address of a member.
   *
   * @param sessionId The session ID of the current session.
   * @return The primary address of the member.
   * @throws NonExistentData If the primary address does not exist.
   * @throws SessionError If the session is not a member session.
   */
  MemberAddress getMemberPrimaryAddress(String sessionId) throws NonExistentData, SessionError;

  /**
   * Sets the primary address for a member.
   *
   * @param sessionId The session ID of the current session.
   * @param addressId The address ID to be set as the primary address.
   * @throws NonExistentData If the address does not exist.
   * @throws SessionError If the session is not a member session.
   */
  void setPrimaryAddress(String sessionId, int addressId) throws NonExistentData, SessionError;

  /**
   * Adds a new address to the addresses associated with a member.
   *
   * @param sessionId   The session ID of the current session.
   * @param addressData The full name associated with the new address.
   * @return ID of added address
   * @throws SessionError If the session is not a member session.
   */
  int addAddress(String sessionId, AddressRecord addressData) throws SessionError;

  /**
   * Removes an address associated with a member.
   *
   * @param sessionId The session ID of the current session.
   * @param addressId The address ID to be removed.
   * @throws NonExistentData If the address does not exist.
   * @throws SessionError If the session is not a member session.
   */
  void removeAddress(String sessionId, int addressId) throws NonExistentData, SessionError;

  /**
   * Updates an existing address associated with a member.
   *
   * @param sessionId   The session ID of the current session.
   * @param addressId   The address ID to be updated.
   * @param addressData The updated full name associated with the address.
   * @throws NonExistentData If the address does not exist.
   * @throws SessionError    If the session is not a member session.
   */
  void updateAddress(String sessionId, int addressId, AddressRecord addressData) throws NonExistentData, SessionError;

  /**
   * Removes the user's cart for a given session.
   *
   * @param sessionId The session ID of the user.
   * @throws SessionError If the session is not a guest or member session.
   * @throws SessionError If the session does not exist.
   */
  void removeUserCart(String sessionId) throws SessionError;


  /**
   * remove a user from the system if the user not taking role in any store
   * @param managerSessionId
   * @param userName
   * @throws SessionError If the session does not exist or not for manager
   * @throws PermissionError If the sessionId is not a system manager seesion
   * @throws NonExistentData if the member with the given username doesn't exist
   */
  void removeMember(String managerSessionId, String userName) throws SessionError, PermissionError, NonExistentData;

  boolean isGuestSession(String sessionId);

  boolean isMemberSession(String sessionId);

  boolean isSystemManagerSession(String sessionId);

  boolean isMemberExists(String newOwnerUsername);

  Integer getAmountOfConnectedMembers(String sessionId) throws SessionError;

  Integer getAmountOfConnectedGuests(String sessionId) throws SessionError;

  UserRecord getMemberDetails(String sessionId) throws NonExistentData, SessionError;

  List<Transaction> getUserTransactions(String sessionId, LocalDateTime optionalStart, LocalDateTime optionalEnd) throws SessionError;

  Map<Integer, Transaction> getAllTransactions(String sessionId) throws SessionError;

  Map<Integer, Offer> getMemberOffers(String sessionID) throws NonExistentData, SessionError;

  void getAllPendingMessage(String sessionId) throws SessionError;

  Set<String> getUsers(String session) throws PermissionError;

  Set<String> getLoggedUsers(String session) throws PermissionError;

  Set<String> getDisconnectedUsers(String session) throws PermissionError;
}