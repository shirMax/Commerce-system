package Domain.Store;

import util.Records.Transaction;
import Domain.Permission;
import Domain.Store.Discount.IDiscount;
import util.Exceptions.PurchaseLimitation;
import Domain.Store.Purchase.PurchaseRule;
import Domain.User.IStoreBasket;
import util.Enums.PermissionType;
import util.Exceptions.DataError;
import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;
import util.Exceptions.PermissionError;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IStore {

    /**
     * Adding permmisions to the founder of the store
     *
     * @param userName the name of the user opens the store.
     */
    void setFounderPermissions(String userName);

    /**
     * Adds a new product to the store's inventory with the given details.
     *
     * @param userName      the name of the user adding the product.
     * @param productRecord Data about the new product
     * @return Product ID of the added product
     */
    Integer addNewProduct(String userName, ProductRecord productRecord) throws DataError, PermissionError;

    /**
     * Removes the product with the given ID from the store's inventory.
     *
     * @param userName  the name of the user removing the product.
     * @param productId the ID of the product to remove.
     */
    void removeProduct(String userName, int productId) throws NonExistentData, PermissionError;

    /**
     * Updates the fields of an existing product.
     *
     * @param userName      the name of the user updating the product.
     * @param productRecord the product record containing the updated fields
     */
    void updateProductFields(String userName, ProductRecord productRecord) throws DataError, NonExistentData, PermissionError;

    /**
     * Adds the products in the given store basket to the store's inventory.
     *
     * @param storeBasket   the store basket containing the products to add.
     * @param productsCache
     */
    void addBasketQuantity(IStoreBasket storeBasket, Map<Integer, Integer> productsCache) throws NonExistentData, DataError;

    /**
     * Removes the products in the given store basket from the store's inventory.
     *
     * @param storeBasket the store basket containing the products to remove.
     */
    void removeBasketQuantity(IStoreBasket storeBasket, Map<Integer, Integer> productsCache) throws NonExistentData, DataError;

    /**
     * Returns a list of all the products in the store's inventory.
     *
     * @return a list of all the products in the store's inventory.
     */
    List<IProduct> getProducts();

    /**
     * Calculates and returns the average rating of the store.
     *
     * @return the average rating of the store.
     */
    double getStoreRating();

    /**
     * Calculates the total price of a store basket.
     *
     * @param storeBasket the store basket containing the products to calculate
     *                    the price for.
     * @return the total price of the products in the store basket.
     */
    double calculateBasketPrice(IStoreBasket storeBasket) throws NonExistentData;

    /**
     * sync all basket product with the store, update all prices with discounts and category.
     *
     * @param basket
     */
    void syncBasket(IStoreBasket basket);

    /**
     * Checks whether the store is currently active.
     *
     * @return true if the store is active, false otherwise
     */
    boolean isActive();

    /**
     * Closes the store.
     *
     * @param userName the name of the user closing the store
     */
    void closeStore(String userName) throws NonExistentData, PermissionError;

    /**
     * Appoints a new store owner to the given store.
     *
     * @param currentOwnerUsername the username of the current store owner
     * @param newOwnerUsername     the username of the new store owner to be appointed
     */
    void assignStoreOwner(String currentOwnerUsername, String newOwnerUsername) throws NonExistentData, PermissionError, DataExistentError;

    /**
     * Removes the appointment of a store owner from a given store.
     *
     * @param currentOwnerUsername the username of the current store owner who
     *                             initiates the removal
     * @param removedOwnerUsername the username of the store owner to be removed
     */
    void removeStoreOwnerAppointment(String currentOwnerUsername,
                                     String removedOwnerUsername) throws PermissionError, DataExistentError, NonExistentData;

    /**
     * Appoints a store manager to a store owned by the current owner.
     *
     * @param currentOwnerUsername the username of the current store owner
     * @param newManagerUsername   the username of the user to be appointed as the
     *                             new store manager
     */
    void assignStoreManager(String currentOwnerUsername,
                            String newManagerUsername) throws DataExistentError, PermissionError;

    /**
     * Removes the appointment of a store manager previously appointed by the
     * store owner.
     *
     * @param currentOwnerUsername   the username of the current store owner
     * @param removedManagerUsername the username of the store manager to be
     *                               removed
     */
    void removeStoreManagerAppointment(String currentOwnerUsername,
                                       String removedManagerUsername) throws NonExistentData, PermissionError;

    /**
     * Add quantity to a product in a store
     *
     * @param userName  the name of the user performing the action
     * @param productId the ID of the product to add quantity to
     * @param quantity  the quantity to add
     */
    void addProductQuantity(String userName, int productId, int quantity) throws NonExistentData, PermissionError, DataError;

    /**
     * Remove quantity from a product in a store
     *
     * @param userName  the name of the user performing the action
     * @param productId the ID of the product to remove quantity from
     * @param quantity  the quantity to remove
     */
    void reduceProductQuantity(String userName, int productId, int quantity) throws NonExistentData, DataError, PermissionError;

    /**
     * Retrieves the permissions of the roles for the given store.
     *
     * @param userName the username of the user requesting the store roles.
     * @return a map of the roles and their corresponding permissions for the store.
     */
    Map<String, Permission> getStoreRoles(String userName) throws PermissionError;

    /**
     * Retrieves the permissions of the managers for the given store.
     *
     * @param userName the username of the user requesting the manager permissions.
     * @return a map of the managers and their corresponding permissions for the store.
     */
    Map<String, Permission> getManagersPermissions(String userName) throws NonExistentData, PermissionError;

    /**
     * Sets the permissions of the given manager for the given store and user.
     *
     * @param userName        the username of the user requesting to set the manager permissions.
     * @param managerUserName the username of the manager for which to set the permissions.
     * @param permissions     the set of permissions to assign to the manager.
     */
    void setManagerPermissions(String userName, String managerUserName,
                               Set<PermissionType> permissions) throws NonExistentData, PermissionError;

    /**
     * Returns the ID of a store.
     *
     * @return An integer representing the ID of a store.
     */
    int getStoreId();

    String getStoreName();

    String getStoreDescription();

    IProduct getProduct(int productId) throws NonExistentData;

    void setStoreName(String storeName);

    void setStoreDescription(String storeDescription);

    void areProductsExist(Map <Integer, ProductRecord> products) throws NonExistentData, DataError;

    //***********************************************Discount
    List<Transaction> getStoreTransactionsByDate(String userName, LocalDateTime startDateTime,
                                                 LocalDateTime endDateTime) throws PermissionError, NonExistentData;

  int addDiscount(String userName, IDiscount discount) throws PermissionError, NonExistentData;

    void removeDiscount(String userName, int discountId) throws PermissionError;

    Map<Integer, IDiscount> getStoreDiscounts();

    //***********************************************PurchaseRules

    /**
     * Checks if the given basket violates any of the purchase rules.
     *
     * @param basket The basket to be checked.
     * @throws PurchaseLimitation If the basket violates any of the purchase rules.
     */
    void checkPurchaseRules(IStoreBasket basket) throws PurchaseLimitation;

    /**
     * Adds a new purchase rule to the store.
     *
     * @param userName The name of the user adding the purchase rule.
     * @param rule     The purchase rule to be added.
     * @return
     * @throws PermissionError If the user does not have permission to add purchase rules.
     */
    int addPurchaseRule(String userName, PurchaseRule rule) throws PermissionError;

    /**
     * Removes the purchase rule with the given ID from the store.
     *
     * @param userName       The name of the user removing the purchase rule.
     * @param purchaseRuleId The ID of the purchase rule to be removed.
     * @throws PermissionError If the user does not have permission to remove purchase rules.
     */
    void removePurchaseRule(String userName, int purchaseRuleId) throws PermissionError;

    /**
     * Gets a list of all the purchase rules in the store.
     *
     * @return A list of all the purchase rules in the store.
     */
    Map<Integer, PurchaseRule> getStorePurchaseRules();

    Permission getMemberPermissions(String userName) throws NonExistentData;

    Offer publishMemberOffer(String offeringMember, int productID, double offeredPrice, int offeredQuantity) throws NonExistentData;

    void removeOffer(String userName, int offerId) throws PermissionError;

    void consentOffer(String userName, int offerId) throws PermissionError;

    Map<Integer, Offer> getOffers(String userName) throws PermissionError;

    void updateFields(String userName, StoreRecord updatedStore) throws PermissionError;

    Map<String, Permission> getPermissions();

    List<String> getManagersWith(PermissionType permissionType);

    boolean hasPermission(String username);

    void remove();

    Map<Integer, Offer> getOffers();

    void counterOffer(String userName, int offerId, int productQuantity, double productPrice) throws PermissionError, NonExistentData, DataError;

    Offer getProductOffer(int offerId) throws NonExistentData;

    IDiscount getStoreDiscount(Integer discountId) throws NonExistentData;

    String getFounderName(String userName) throws PermissionError, NonExistentData;

    //***************************************************************offers
    OwnerAppointmentContract publishMemberContract(String assigningOwner, String newOwnerUserName, String contract) throws NonExistentData, PermissionError, DataExistentError;

    void removeContract(String userName, int contractId) throws PermissionError;

    OwnerAppointmentContract consentContract(String userName, int contractId) throws PermissionError, NonExistentData, DataExistentError;

    OwnerAppointmentContract getContract(String userName, int contractId) throws NonExistentData, PermissionError;

    Map<Integer, OwnerAppointmentContract> getContracts(String userName) throws PermissionError;
}
