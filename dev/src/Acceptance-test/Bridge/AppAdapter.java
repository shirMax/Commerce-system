package Bridge;

import Domain.Store.Purchase.PurchaseRule;
import util.Records.Transaction;
import Domain.Services.PaymentService.IPaymentService;
import Domain.Store.Discount.IDiscount;
import Domain.Store.Offer;
import Exceptions.ATException;
import Service.ISystemFacade;
import Service.Result;
import Service.SystemFacade;
import org.checkerframework.org.apache.commons.lang3.NotImplementedException;
import util.Enums.PermissionType;
import util.Enums.RoleType;
import util.Records.AddressRecord;
import util.Records.DateRange;
import util.Records.PaymentDetails;
import util.Records.StoreRecords.ProductFilterAttributes;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bridge implementation that adapts the calls to the SystemFacade
 */
public class AppAdapter implements TestingBridge{

    private final ISystemFacade facade;

    public AppAdapter(){
        facade = new SystemFacade();
    }

    @Override
    public String openSession(String sessionID) {
        Result<String> result = facade.connect(sessionID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return sessionID;
    }

    @Override
    public void closeSession(String session) {
        Result result = facade.terminate(session);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public void login(String session, String username, String password) {
        Result result = facade.login(session, username, password);
        if (result.errorOccured()) {
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        }
    }

    @Override
    public void register(String session, UserRecord user, String password) throws ATException {
        Result result = facade.register(session, user, password);
        if (result.errorOccured())
        {
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        }
    }

    @Override
    public void deleteUser(String session, String username) {
        Result result = facade.removeMember(session, username);
        if(result.errorOccured())
        {
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        }
    }

    @Override
    public void logout(String session) {
        Result result = facade.logout(session);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public int openStore(String session, StoreRecord store) {
        Result<Integer> result = facade.openStore(session, store);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public StoreRecord getStore(String session, int storeID) {
        Result<StoreRecord> result = facade.getStoreInfo(session, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public void closeStore(String session, int storeID) {
        Result result = facade.closeStore(session, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public void deleteStore(String session, int storeID) {
        //TODO: does closing a store permanently fully deletes the store?
        Result result = facade.closeStorePermanently(session, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public int addProduct(String session, Integer storeId, ProductRecord product) {
        Result<Integer> result = facade.addNewProduct(session, product);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public ProductRecord getProduct(String session, Integer storeID, int prodID) {
        Result<ProductRecord> result = facade.getProductInfo(session, prodID, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public void removeProduct(String session, Integer storeID, int prodID) {
        Result result = facade.removeProduct(session, storeID, prodID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public Set<String> getStoreOwners(String session, Integer storeID) {
        //TODO: facade should return Set and not a list.
        Result<List<String>> result = facade.getStoreOwners(session, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return new HashSet<>(result.getValue());
    }

    @Override
    public String getStoreFounder(String session, Integer storeID) {
        Result<String> result = facade.getStoreFounder(session, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public Set<String> getStoreManagers(String session, Integer storeID) {
        //TODO: facade should return Set and not a list.
        Result<List<String>> result = facade.getStoreManagers(session, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return new HashSet<>(result.getValue());
    }

    @Override
    public void appointOwner(String session, Integer storeID, String newOwner) {
        Result result = facade.appointOwner(session, newOwner, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public Set<Integer> getOwnedStores(String session) {
        Result<Set<Integer>> result = facade.getStoresAccordingToRole(session, RoleType.STORE_OWNER);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return new HashSet<>(result.getValue());
    }

    @Override
    public void appointManager(String session, Integer storeID, String newManager) {
        Result result = facade.appointManager(session, newManager, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public Set<Integer> getManagedStores(String session) {
        Result<Set<Integer>> result = facade.getStoresAccordingToRole(session, RoleType.STORE_MANAGER);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return new HashSet<>(result.getValue());
    }

    @Override
    public void setManagerPermissions(String session, Integer storeID, String manager, Set<PermissionType> newPermissions) {
        Result result = facade.modifyPermissionsFor(session, manager, storeID, PermissionType.collectionToBitmap(newPermissions));
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public Set<PermissionType> getManagerPermissions(String session, Integer storeID, String manager) {
        Result<Integer> result = facade.getManagerPermissions(session, manager, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return PermissionType.bitmapToSet(result.getValue());
    }

    @Override
    public Set<PermissionType> getPermissions(String session,String userName, Integer storeID) {
        Result<Set<PermissionType>> result = facade.getUserPermissionsTypes(session, userName, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public List<Transaction> getStoreTransactionHistory(String session, Integer storeID, DateRange range) {
        Result<List<Transaction>> result;
        if(range != null) {
            if (range.earliest() == null && range.latest() == null)
                result = facade.getStoreTransactionHistory(session, storeID, null,  null);
            else if (range.earliest() == null)
                result = facade.getStoreTransactionHistory(session, storeID, null,  range.latest().toLocalDate().atStartOfDay());
            else if (range.latest() == null)
                result = facade.getStoreTransactionHistory(session, storeID, range.earliest().toLocalDate().atStartOfDay(),  null);
            else
                result = facade.getStoreTransactionHistory(session, storeID, range.earliest().toLocalDate().atStartOfDay(),  range.latest().toLocalDate().atStartOfDay());
        }
        else
            result = facade.getStoreTransactionHistory(session, storeID, null,  null);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public Set<StoreRecord> getStores(String session) {
        Result<Set<StoreRecord>> result = facade.getStores();
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return new HashSet<>(result.getValue());
    }

    @Override
    public Set<ProductRecord> getProducts(String session, ProductFilterAttributes filterAttributes) {
        Result<List<ProductRecord>> result = facade.getProductsBy(session, filterAttributes);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return new HashSet<>(result.getValue());
    }

    @Override
    public void addToCart(String session, Integer storeID, Integer prodID, int quantity) {
        Result result = facade.addProductToCart(session, prodID, storeID, quantity);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public Map<Integer, Map<Integer, ProductRecord>> getCart(String session) {
        Result<Map<Integer, Map<Integer, ProductRecord>>> result = facade.getCartContent(session);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public void removeFromCart(String session, Integer storeID, Integer prodID, int quantity) {
        Result result = facade.removeProductFromCart(session, storeID, prodID, quantity);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public Double calculateCart(String session) {
        Result<Double> result = facade.getCartPrice(session);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    private static final PaymentDetails DUMMY_PAYMENT_DETAILS = new PaymentDetails("Dummy", "1234567891234567", LocalDate.of(2030, 1, 1), "012");
    private static final AddressRecord DUMMY_ADDRESS = new AddressRecord("Dummy", "Dummy", "Dummy", "Dummy", "Dummy", "0123456789");

    @Override
    public void pay(String session) {
        //test_user buying from a test_store should always succeed and not process payment;
        Result result = facade.pay(session, DUMMY_PAYMENT_DETAILS, DUMMY_ADDRESS);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public Set<String> getUsers(String session) {
        Result<Set<String>> result = facade.getUsers(session);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public Set<String> getLoggedUsers(String session) {
        Result<Set<String>> result = facade.getLoggedUsers(session);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public Set<String> getUnloggedUsers(String session) {
        Result<Set<String>> result = facade.getDisconnectedUsers(session);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public void removeOwner(String session, Integer storeID, String ownerToRemove) {
        Result result = facade.removeStoreOwner(session, ownerToRemove, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public void removeManager(String session, Integer storeID, String managerToRemove) {
        Result result = facade.removeStoreManager(session, managerToRemove, storeID);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public void updatePaymentService(IPaymentService paymentService) {
        Result result = facade.updatePaymentService(paymentService);
    }

    @Override
    public void addDiscount(String session, int storeId, IDiscount discount){
        Result result = facade.addDiscount(session, storeId, discount);
        if (result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public List<IDiscount> getStoreDiscounts(String session, int storeID){
        Result<List<IDiscount>> discounts = facade.getStoreDiscounts(session, storeID);
        if (discounts.errorOccured())
            throw new ATException(discounts.getStatus().name() + ": " + discounts.getErrorMessage(), discounts.getStatus());
        return discounts.getValue();
    }

    @Override
    public void memberPublishOffer(String sessionId, int storeId, int productId, double offerdPrice, int quantity){
        Result result = facade.memberPublishOffer(sessionId, storeId, productId, offerdPrice, quantity);
        if(result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public void storeRejectOffer(String sessionId, int offerId, int storeId) {
        Result result = facade.storeRejectOffer(sessionId, offerId,storeId);
        if(result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public void purchaseBid(String sessionId, PaymentDetails paymentDetails, AddressRecord deliveryAddress, Offer offer) {
        Result result = facade.purchaseBid(sessionId, paymentDetails, deliveryAddress, offer.getProduct().storeId(), offer.getId());
        if(result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public Map<Integer, Offer> getMemberOffers(String sessionID) {
        Result<Map<Integer, Offer>> result = facade.getMemberOffers(sessionID);
        if(result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
        return result.getValue();
    }

    @Override
    public void consentOffer(String sessionId, int offerId, int storeId) {
        Result result = facade.consentOffer(sessionId, offerId, storeId);
        if(result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

    @Override
    public void addPurchaseRule(String sessionId, int storeId, PurchaseRule purchaseRule) {
        Result result = facade.addPurchaseRule(sessionId, storeId, purchaseRule);
        if(result.errorOccured())
            throw new ATException(result.getStatus().name() + ": " + result.getErrorMessage(), result.getStatus());
    }

}
