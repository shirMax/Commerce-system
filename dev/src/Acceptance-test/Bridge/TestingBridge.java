package Bridge;

import Domain.Store.Purchase.PurchaseRule;
import util.Records.Transaction;
import Domain.Services.PaymentService.IPaymentService;
import Domain.Store.Discount.IDiscount;
import Domain.Store.Offer;
import util.Enums.PermissionType;
import util.Records.AddressRecord;
import util.Records.DateRange;
import util.Records.StoreRecords.ProductFilterAttributes;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface which defines methods the tests may use when accessing the 'App'
 */
public interface TestingBridge {
    String openSession(String sessionID);

    void closeSession(String session);

    void login(String session, String username, String password);

    void register(String session, UserRecord user, String password);

    void deleteUser(String session, String username);

    void logout(String session);

    int openStore(String session, StoreRecord store);

    StoreRecord getStore(String session, int storeID);

    void closeStore(String session, int storeID);

    void deleteStore(String session, int storeID);

    int addProduct(String session, Integer storeId, ProductRecord product);

    ProductRecord getProduct(String session, Integer storeID, int prodID);

    void removeProduct(String session, Integer storeID, int prodID);

    Set<String> getStoreOwners(String session, Integer storeID);

    String getStoreFounder(String session, Integer storeID);

    Set<String> getStoreManagers(String session, Integer storeID);

    void appointOwner(String session, Integer storeID, String newOwner);

    Set<Integer> getOwnedStores(String session);

    void appointManager(String session, Integer storeID, String newManager);

    Set<Integer> getManagedStores(String session);

    void setManagerPermissions(String session, Integer storeID, String manager, Set<PermissionType> newPermissions);

    Set<PermissionType> getManagerPermissions(String session, Integer storeID, String manager);

    Set<PermissionType> getPermissions(String session, String userName, Integer storeID);

    List<Transaction> getStoreTransactionHistory(String session, Integer storeID, DateRange range);

    Set<StoreRecord> getStores(String session);

    Set<ProductRecord> getProducts(String session, ProductFilterAttributes filterAttributes);

    void addToCart(String session, Integer storeID, Integer prodID, int quantity);

    Map<Integer, Map<Integer, ProductRecord>> getCart(String session);

    void removeFromCart(String session, Integer storeID, Integer prodID, int quantity);

    Double calculateCart(String session);

    void pay(String session);

    Set<String> getUsers(String session);

    Set<String> getLoggedUsers(String session);

    Set<String> getUnloggedUsers(String session);

    void removeOwner(String session, Integer storeID, String ownerToRemove);

    void removeManager(String session, Integer storeID, String managerToRemove);

    void updatePaymentService(IPaymentService paymentService);

    void addDiscount(String session, int storeId, IDiscount discount);

    List<IDiscount> getStoreDiscounts(String session, int storeID);

    void memberPublishOffer(String sessionId, int storeId, int productId, double offerdPrice, int quantity);

    void storeRejectOffer(String sessionId, int offerId, int storeId);

    void purchaseBid(String sessionId, util.Records.PaymentDetails paymentDetails, AddressRecord deliveryAddress, Offer offer);

    Map<Integer, Offer> getMemberOffers(String sessionID);

    void consentOffer(String sessionId, int offerId, int storeId);
    void addPurchaseRule(String sessionId, int storeId, PurchaseRule purchaseRule);
}
