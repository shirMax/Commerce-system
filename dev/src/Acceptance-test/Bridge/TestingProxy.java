package Bridge;

import Domain.Store.Purchase.PurchaseRule;
import util.Records.Transaction;
import Domain.Services.PaymentService.IPaymentService;
import Domain.Store.Category;
import Domain.Store.Discount.IDiscount;
import Domain.Store.Offer;
import util.Enums.PermissionType;
import util.Records.AddressRecord;
import util.Records.DateRange;
import util.Records.PaymentDetails;
import util.Records.StoreRecords.ProductFilterAttributes;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bridge implementation, may be injected at any point with another Bridge implementation to which it will delegate calls.
 * Implements the Bridge with basic default implementations so that testing would always run.
 */
public class TestingProxy implements TestingBridge{
    private TestingBridge realBridge;

    /**
     * Injects the Proxy with another implementation of Bridge.TestingBridge.
     *
     * @param bridge Bridge to be injected.
     */
    public void injectBridge(TestingBridge bridge){
        this.realBridge = bridge;
    }

    @Override
    public String openSession(String sessionID) {
        if (realBridge == null)
            return "sessionID";
        return realBridge.openSession(sessionID);
    }

    @Override
    public void closeSession(String session) {
        if (realBridge == null)
            return;
        realBridge.closeSession(session);
    }

    @Override
    public void login(String session, String username, String password) {
        if (realBridge == null)
            return;
        realBridge.login(session, username, password);
    }

    @Override
    public void register(String session, UserRecord user, String password) {
        if (realBridge == null)
            return;
        realBridge.register(session, user, password);
    }

    @Override
    public void deleteUser(String session, String username) {
        if (realBridge == null)
            return;
        realBridge.deleteUser(session, username);
    }

    @Override
    public void logout(String session) {
        if (realBridge == null)
            return;
        realBridge.logout(session);
    }

    @Override
    public int openStore(String session, StoreRecord store) {
        if (realBridge == null)
            return 1;
        return realBridge.openStore(session, store);
    }

    @Override
    public StoreRecord getStore(String session, int storeID) {
        if (realBridge == null)
            return new StoreRecord(1, "SomeStore", 3, "SomeDescription", true);
        return realBridge.getStore(session, storeID);
    }

    @Override
    public void closeStore(String session, int storeID) {
        if (realBridge == null)
            return;
        realBridge.closeStore(session, storeID);
    }

    @Override
    public void deleteStore(String session, int storeID) {
        if (realBridge == null)
            return;
        realBridge.deleteStore(session, storeID);
    }

    @Override
    public int addProduct(String session, Integer storeId, ProductRecord product) {
        if (realBridge == null)
            return 1;
        return realBridge.addProduct(session, storeId, product);
    }

    @Override
    public ProductRecord getProduct(String session, Integer storeID, int prodID) {
        if (realBridge == null)
            return new ProductRecord(1, 1, "SomeProduct", 1.0, Category.PARTY, 1, 1, 1);
        return realBridge.getProduct(session, storeID, prodID);
    }

    @Override
    public void removeProduct(String session, Integer storeID, int prodID) {
        if (realBridge == null)
            return;
        realBridge.removeProduct(session, storeID, prodID);
    }

    @Override
    public Set<String> getStoreOwners(String session, Integer storeID) {
        if (realBridge == null)
            return null;
        return realBridge.getStoreOwners(session, storeID);
    }

    @Override
    public String getStoreFounder(String session, Integer storeID) {
        if (realBridge == null)
            return null;
        return realBridge.getStoreFounder(session, storeID);
    }

    @Override
    public Set<String> getStoreManagers(String session, Integer storeID) {
        if (realBridge == null)
            return null;
        return realBridge.getStoreManagers(session, storeID);
    }

    @Override
    public void appointOwner(String session, Integer storeID, String newOwner) {
        if (realBridge == null)
            return;
        realBridge.appointOwner(session, storeID, newOwner);
    }

    @Override
    public Set<Integer> getOwnedStores(String session) {
        if (realBridge == null)
            return null;
        return realBridge.getOwnedStores(session);
    }

    @Override
    public void appointManager(String session, Integer storeID, String newManager) {
        if (realBridge == null)
            return;
        realBridge.appointManager(session, storeID, newManager);
    }

    @Override
    public Set<Integer> getManagedStores(String session) {
        if (realBridge == null)
            return null;
        return realBridge.getManagedStores(session);
    }

    @Override
    public void setManagerPermissions(String session, Integer storeID, String manager, Set<PermissionType> newPermissions) {
        if (realBridge == null)
            return;
        realBridge.setManagerPermissions(session, storeID, manager, newPermissions);
    }

    @Override
    public Set<PermissionType> getManagerPermissions(String session, Integer storeID, String manager) {
        if (realBridge == null)
            return null;
        return realBridge.getManagerPermissions(session, storeID, manager);
    }

    @Override
    public Set<PermissionType> getPermissions(String session, String userName, Integer storeID) {
        if (realBridge == null)
            return null;
        return realBridge.getPermissions(session, userName, storeID);
    }

    @Override
    public List<Transaction> getStoreTransactionHistory(String session, Integer storeID, DateRange range) {
        if (realBridge == null)
            return null;
        return realBridge.getStoreTransactionHistory(session, storeID, range);
    }

    @Override
    public Set<StoreRecord> getStores(String session) {
        if (realBridge == null)
            return null;
        return realBridge.getStores(session);
    }

    @Override
    public Set<ProductRecord> getProducts(String session, ProductFilterAttributes filterAttributes) {
        if (realBridge == null)
            return null;
        return realBridge.getProducts(session, filterAttributes);
    }

    @Override
    public void addToCart(String session, Integer storeID, Integer prodID, int quantity) {
        if (realBridge == null)
            return;
        realBridge.addToCart(session, storeID, prodID, quantity);
    }

    @Override
    public Map<Integer, Map<Integer, ProductRecord>> getCart(String session) {
        if (realBridge == null)
            return null;
        return realBridge.getCart(session);
    }

    @Override
    public void removeFromCart(String session, Integer storeID, Integer prodID, int quantity) {
        if (realBridge == null)
            return;
        realBridge.removeFromCart(session, storeID, prodID, quantity);
    }

    @Override
    public Double calculateCart(String session) {
        if (realBridge == null)
            return null;
        return realBridge.calculateCart(session);
    }

    @Override
    public void pay(String session) {
        if (realBridge == null)
            return;
        realBridge.pay(session);
    }

    @Override
    public Set<String> getUsers(String session) {
        if (realBridge == null)
            return null;
        return realBridge.getUsers(session);
    }

    @Override
    public Set<String> getLoggedUsers(String session) {
        if (realBridge == null)
            return null;
        return realBridge.getLoggedUsers(session);
    }

    @Override
    public Set<String> getUnloggedUsers(String session) {
        if (realBridge == null)
            return null;
        return realBridge.getUnloggedUsers(session);
    }

    @Override
    public void removeOwner(String session, Integer storeID, String ownerToRemove) {
        if (realBridge == null)
            return;
        realBridge.removeOwner(session, storeID, ownerToRemove);
    }

    @Override
    public void removeManager(String session, Integer storeID, String managerToRemove) {
        if (realBridge == null)
            return;
        realBridge.removeManager(session, storeID, managerToRemove);
    }

    @Override
    public void updatePaymentService(IPaymentService paymentService) {
        if (realBridge == null)
            return;
        realBridge.updatePaymentService(paymentService);
    }

    @Override
    public void addDiscount(String session, int storeId, IDiscount discount) {
        if (realBridge == null)
            return;
        realBridge.addDiscount(session, storeId, discount);
    }

    @Override
    public List<IDiscount> getStoreDiscounts(String session, int storeID) {
        if (realBridge == null)
            return null;
        return realBridge.getStoreDiscounts(session, storeID);
    }

    @Override
    public void memberPublishOffer(String sessionId, int storeId, int productId, double offerdPrice, int quantity){
        if (realBridge == null)
            return;
        realBridge.memberPublishOffer(sessionId, storeId, productId, offerdPrice, quantity);
    }

    @Override
    public void storeRejectOffer(String sessionId, int offerId, int storeId) {
        if (realBridge == null)
            return;
        realBridge.storeRejectOffer(sessionId, offerId,storeId);
    }

    @Override
    public void purchaseBid(String sessionId, PaymentDetails paymentDetails, AddressRecord deliveryAddress, Offer offer) {
        if (realBridge == null)
            return;
        realBridge.purchaseBid(sessionId, paymentDetails, deliveryAddress, offer);
    }

    @Override
    public Map<Integer, Offer> getMemberOffers(String sessionID) {
        if (realBridge == null)
            return null;
        return  realBridge.getMemberOffers(sessionID);
    }

    @Override
    public void consentOffer(String sessionId, int offerId, int storeId) {
        if (realBridge == null)
            return;
        realBridge.consentOffer(sessionId, offerId, storeId);
    }

    @Override
    public void addPurchaseRule(String sessionId, int storeId, PurchaseRule purchaseRule) {
        if (realBridge == null)
            return;
        realBridge.addPurchaseRule(sessionId, storeId, purchaseRule);
    }



}
