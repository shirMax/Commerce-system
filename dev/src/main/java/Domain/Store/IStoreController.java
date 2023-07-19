package Domain.Store;

import util.Records.Transaction;
import Domain.Permission;
import Domain.Services.NotificationService.INotificationService;
import Domain.Store.Discount.IDiscount;
import util.Exceptions.PurchaseLimitation;
import Domain.Store.Purchase.PurchaseRule;
import Domain.User.IStoreBasket;
import Domain.User.IUserCart;
import util.Enums.PermissionType;
import util.Enums.RoleType;
import util.Exceptions.*;
import util.Records.DateRange;
import util.Records.StoreRecords.ProductFilterAttributes;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IStoreController {

  Set<Integer> getStoresAccordingToRole(String sessionId,
                                        RoleType role)
      throws MarketException;

  /**
   * Opens a new store with the given name and description.
   *
   * @param sessionId         current sessionId
   * @param store             information about the new store
   * @return the unique identifier of the newly created store
   * @throws SessionError if the session does not exist.
   */

  int openNewStore(String sessionId, StoreRecord store) throws SessionError;

  /**
   * Update the fields of a specific store record in the system based on the provided StoreRecord object.
   * @param sessionId An integer representing the session ID of the member.
   * @param updatedStore A StoreRecord object containing the updated information for the store.
   *
   */
  void updateStoreFields(String sessionId, StoreRecord updatedStore) throws SessionError, NonExistentData, PermissionError;


  /**
   * Closes the store with the given identifier.
   *
   * @param sessionId         current sessionId
   * @param storeId the identifier of the store to be closed
   */
  void closeStore(String sessionId, int storeId) throws SessionError, NonExistentData, PermissionError;

  /**
   * Closes the store permanently.
   *
   *  @param sessionId         current sessionId
   * @param storeId the ID of the store to close.
   */
  void closeStorePermanently(String sessionId, int storeId) throws SessionError, NonExistentData, PermissionError;

  /**
   * Adds a new product to the specified store.
   *
   * @param sessionId       current sessionId
   * @param productRecord     the product record to add
   * @return          product id
   * @throws SessionError if the session does not exist
   * @throws NonExistentData if the store does not exist.
   * @throws DataError if there is an issue with the data provided in the product record
   * @throws PermissionError if the user does not have sufficient permissions to add a new product to the specified store.
   */

  int addNewProduct(String sessionId, ProductRecord productRecord) throws SessionError, NonExistentData, DataError, PermissionError;


  /**
   * Removes the specified product from the specified store.
   *
   *  @param sessionId         current sessionId
   * @param storeId the identifier of the store from which the product is to be
   *     removed
   * @param productId the identifier of the product to be removed
   * @throws SessionError if the session does not exist
   * @throws NonExistentData if the store or product specified does not exist.
   * @throws PermissionError if the user does not have sufficient permissions to remove the specified product from the specified store.
   */
  void removeProduct(String sessionId, int storeId,
                     int productId) throws SessionError, NonExistentData, PermissionError;


  /**
   * Updates the fields of an existing product.
   *
   * @param sessionId       the current session ID
   * @param productRecord   the product record containing the updated fields
   * @throws SessionError if the session does not exist
   * @throws NonExistentData if the product specified does not exist.
   * @throws DataError if the product record contains invalid or inconsistent data.
   * @throws PermissionError if the user does not have sufficient permissions to update the specified product.
   */

  void updateProductFields(String sessionId, ProductRecord productRecord) throws SessionError, NonExistentData, DataError, PermissionError;


  /**
   * Add quantity to a product in a store
   *
   *  @param sessionId         current sessionId
   * @param storeId   the ID of the store where the product is located
   * @param productId the ID of the product to add quantity to
   * @param quantity  the quantity to add
   * @throws SessionError if the session does not exist
   * @throws NonExistentData if the store or product specified does not exist.
   * @throws PermissionError if the user does not have sufficient permissions to update the specified product's quantity.
   *
   */
  void addProductQuantity(String sessionId, int storeId, int productId,
                          int quantity) throws SessionError, NonExistentData, PermissionError, DataError;

  /**
   * Remove quantity from a product in a store
   *
   * @param sessionId         current sessionId
   * @param storeId   the ID of the store where the product is located
   * @param productId the ID of the product to remove quantity from
   * @param quantity  the quantity to remove
   * @throws SessionError if the session does not exist
   * @throws NonExistentData if the store or product specified does not exist.
   * @throws DataError if the product's current quantity is less than the quantity to remove.
   * @throws PermissionError if the user does not have sufficient permissions to update the specified product's quantity.
   */
  void reduceProductQuantity(String sessionId, int storeId, int productId,
                             int quantity) throws SessionError, NonExistentData, DataError, PermissionError;

  /**
   * Adds the products in the given user cart to the store's inventory.
   *
   * @param userCart the user cart containing the products to add.
   *  @throws NonExistentData if one or more products in the user cart do not exist in the system.
   * @throws DataError exception can be thrown if there is an issue with the data being removed from the store's inventory, due to rollback.
   */
  void addCartQuantity(IUserCart userCart) throws NonExistentData, DataError, PermissionError;

  /**
   * Removes the products in the given user cart from the store's inventory.
   *
   * @param userCart the user cart containing the products to remove.
   * @throws NonExistentData if one or more products in the user cart do not exist in the system.
   */
  void removeCartQuantity(IUserCart userCart) throws NonExistentData, DataError;

  /**
   * Retrieves the store with the specified identifier.
   *
   * @param storeId the identifier of the store to retrieve
   * @return the store with the specified identifier
   * @throws NonExistentData if a store with the specified identifier does not exist in the system.
   *
   */
  IStore getStore(int storeId) throws NonExistentData;

  /**
   * Get the information of a specific store record in the system.
   * @param storeId An integer representing the ID of the store.
   * @return A StoreRecord object containing the information of the store.
   * @throws NonExistentData if a store with the specified identifier does not exist in the system.
   *
   */
  StoreRecord getStoreInfo(int storeId) throws NonExistentData;

  /**
   * Retrieves a list of all currently active stores.
   *
   * @return a list of all currently active stores
   */
  List<IStore> getActiveStores();

  /**
   * Retrieves a list of products from all stores that match the specified
   * filter criteria.
   *
   * @param productFilterAttributes the filter criteria to use when selecting products
   * @return a list of products from all stores that match the specified filter
   *     criteria
   */
  List<IProduct> getFilteredProducts(ProductFilterAttributes productFilterAttributes);

  /**
   * Calculates the total price of the user's basket from the specified store.
   *
   * @param storeBasket the basket for which to calculate the total price
   * @return the total price of the user's basket from the specified store or -1 if there was an error.
   */
  double calculateBasketPrice(IStoreBasket storeBasket);

  /**
   * Retrieves a list of products belonging to the specified store.
   *
   * @param storeId the ID of the store to retrieve products from
   * @precondition storeId is a valid store ID
   * @return a list of products belonging to the specified store
   * @throws NonExistentData if the store with the specified ID does not exist in the system.
   */
  List<IProduct> getStoreProducts(String sessionId, int storeId) throws NonExistentData, PermissionError, SessionError;

  /**
   * Appoints a new store owner to the given store.
   *
   *  @param sessionId         current sessionId
   * @param newOwnerUsername the username of the new store owner to be appointed
   * @param storeId the ID of the store to which the new owner will be appointed
   * @throws SessionError if the session does not exist
   * @throws NonExistentData if the store with the given storeId does not exist
   * @throws PermissionError if the user associated with the given sessionId does not have permission to appoint a new store owner
   * @throws DataExistentError if the user with the given username is already an owner of the store
   */
  void assignStoreOwner(String sessionId,
                        String newOwnerUsername, int storeId) throws SessionError, NonExistentData, PermissionError, DataExistentError;

  /**
   * Removes the appointment of a store owner from a given store.
   *
   *  @param sessionId         current sessionId
   * @param removedOwnerUsername the username of the store owner to be removed
   * @param storeId the ID of the store from which the owner's appointment is to
   *     be removed
   * @throws SessionError if the session ID does not exist
   * @throws NonExistentData if the store or the store owner does not exist
   */
  void removeStoreOwnerAppointment(String sessionId,
                                   String removedOwnerUsername, int storeId) throws SessionError, NonExistentData, PermissionError, DataExistentError;

  /**
   * Appoints a store manager to a store owned by the current owner.
   *
   *  @param sessionId         current sessionId
   * @param newManagerUsername the username of the user to be appointed as the
   *     new store manager
   * @param storeId the ID of the store to which the new manager will be
   *     appointed
   * @throws SessionError - if the session ID does not exist.
   * @throws NonExistentData - if the store with the given store ID does not exist.
   * @throws PermissionError - if the user with the given session ID is not the owner of the store.
   * @throws DataExistentError - if the user with the given username is already a manager of the store.
   */
  void assignStoreManager(String sessionId,
                          String newManagerUsername, int storeId) throws SessionError, NonExistentData, PermissionError, DataExistentError;

  /**
   * Removes the appointment of a store manager previously appointed by the
   * store owner.
   *
   *  @param sessionId         current sessionId
   * @param removedManagerUsername the username of the store manager to be
   *     removed
   * @param storeId the ID of the store where the appointment is being removed
   */
  void removeStoreManagerAppointment(String sessionId,
                                     String removedManagerUsername,
                                     int storeId) throws SessionError, NonExistentData, PermissionError;

  /**
   * Reopens a store that was previously closed.
   *
   *  @param sessionId         current sessionId
   * @param storeId          The ID of the store to reopen.
   */
  void reopenStore(String sessionId, int storeId);

  void checkPurchaseRules(IStoreBasket storeBasket) throws NonExistentData, PurchaseLimitation;

  /**
   * Retrieves the permissions of the roles for the given store.
   * @param sessionId the ID of the current user session.
   * @param storeId the ID of the store for which to retrieve the roles.
   * @return a map of the roles and their corresponding permissions for the store and user.
   * @throws SessionError: if the session does not exist
   * @throws  NonExistentData: if the store does not exist
   * @throws PermissionError: if the user does not have sufficient permission to access the store's roles.   */
  Map<String, Permission> getStoreRoles(String sessionId,
                                        int storeId) throws SessionError, NonExistentData, PermissionError;

  String getStoreFounder(String sessId, int storeId) throws NonExistentData, SessionError, PermissionError;
  /**
   * Retrieves the permissions of the managers for the given store.
   * @param sessionId the ID of the current user session.
   * @param storeId the ID of the store for which to retrieve the manager permissions.
   * @return a map of the managers and their corresponding permissions for the store.
   * @throws SessionError: if the provided session does not exist.
   * @throws NonExistentData: if the provided store ID does not match any existing store.
   * @throws PermissionError: if the current user does not have sufficient permissions to perform this operation.
   */
  Map<String, Permission> getManagersPermissions(String sessionId,
                                                 int storeId) throws SessionError, NonExistentData, PermissionError;

  /**
   * Sets the permissions of the given manager for the given store and user.
   * @param sessionId the ID of the current user session.
   * @param managerUserName the username of the manager for which to set the permissions.
   * @param storeId the ID of the store for which to set the manager permissions.
   * @param permissions the set of permissions to assign to the manager.
   * @throws SessionError - If the provided sessionId does not exist.
   * @throws NonExistentData - If the provided managerUserName or storeId is invalid.
   * @throws PermissionError - If the current user doesn't have the required permission to set the manager's permissions or the given username is not a manager.
   */
  void setManagerPermissions(String sessionId,
                             String managerUserName, int storeId,
                             Set<PermissionType> permissions) throws SessionError, NonExistentData, PermissionError;

  /**
   * Returns store records.
   *
   * @return store records
   */
  List<StoreRecord> getStores();

  /**
   * Returns a map of store IDs to lists of products in each store.
   *
   * @return a map of store IDs to lists of products in each store
   */
  Map<Integer, Map<Integer, IProduct>> getStoresProducts();

  /**

   Checks if the products with the given IDs exist in the inventory of the store with the given ID.
   @param storeId the ID of the store to check for product existence
   @param productsId the list of product IDs to check for existence
   @throws NonExistentData if one of the given product IDs does not exist in the inventory of the store with the given ID
   */
  void checkProductsExist(int storeId, Map<Integer, ProductRecord> products) throws NonExistentData, DataError;

  /**
   * update or replace the notification service
   * @param notificationService
   */
  void updateNotificationService(INotificationService notificationService);

  List<StoreRecord> getMyStores(String sessionId) throws NonExistentData, SessionError;

  /**
   * sync all the basket inside the cart with the stores
   * @param cart
   */
  void syncCart(IUserCart cart);

  Permission getMemberPermissions(String sessionId, Integer storeId) throws SessionError, NonExistentData;

  void removeDiscount(String session, int storeID, int discountId) throws MarketException;

  void checkCartPurchaseRules(String session) throws SessionError, NonExistentData, PurchaseLimitation;

  void removePurchaseRule(String session, int storeID, int purchaseRuleID) throws NonExistentData, SessionError, PermissionError;

  List<Transaction> getStoreTransactions(String sessionId, int storeId, DateRange range);

  void addDiscount(String session, int storeId, IDiscount discount) throws NonExistentData, SessionError, PermissionError;

  List<IDiscount> getDiscounts(String session, int storeID) throws NonExistentData;

  void addPurchaseRule(String session, int storeId, PurchaseRule rule) throws NonExistentData, SessionError, PermissionError;

  List<PurchaseRule> getStorePurchaseRules(String session, int storeID) throws NonExistentData;

  void removeOfferQuantity(Offer offer) throws NonExistentData, DataError, PermissionError;

  void restoreOfferQuantity(Offer offer) throws NonExistentData, PermissionError, DataError;

  void removeOffer(String sessionId, int storeId, int offerId) throws NonExistentData, PermissionError, SessionError;

  void consentOffer(String sessionId, int offerId, int storeId) throws NonExistentData, PermissionError, SessionError;

  Map<Integer, Offer> getOffers(String sessionId, int storeId) throws NonExistentData, PermissionError, SessionError;

  void counterOffer(String sessionId, int storeId, int offerId, int productQuantity, double productPrice) throws NonExistentData, PermissionError, SessionError, DataError;

  IDiscount getDiscount(String sessionId, Integer storeId, Integer discountId) throws NonExistentData;

  Set<PermissionType> getUserPermissionsTypes(String session, String userName, Integer storeID) throws NonExistentData;

    //**********************************************************Contract
    OwnerAppointmentContract publishMemberContract(String sessionId, int storeId, String newOwnerUserName, String contract) throws SessionError, NonExistentData, PermissionError, DataExistentError;

  void removeContract(String sessionId, int storeId, int contractId) throws NonExistentData, PermissionError, SessionError;

  void consentContract(String sessionId, int storeId, int contractId) throws NonExistentData, PermissionError, SessionError, DataExistentError;

  Map<Integer, OwnerAppointmentContract> getContracts(String sessionId, int storeId) throws NonExistentData, PermissionError, SessionError;
}