package Service;

import DataLayer.DbConfig;
import DataLayer.User.*;
import Domain.*;
import Domain.Services.NotificationService.NotificationObserver;
import Domain.Services.SupplyService.SupplyServiceProxy;
import util.ConfigReader;
import Domain.Store.*;
import util.Enums.PermissionType;
import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;
import util.Exceptions.PermissionError;
import util.Exceptions.SessionError;
import util.Records.Transaction;
import Domain.Services.NotificationService.NotificationService;
import Domain.Services.PaymentService.IPaymentService;
import Domain.Services.PaymentService.PaymentServiceProxy;
import Domain.Store.Discount.IDiscount;
import Domain.Store.Purchase.PurchaseRule;
import Domain.User.IUserController;
import Domain.User.UserController;
import org.checkerframework.org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import util.CreateDataForTest;
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

@Service
public class SystemFacade implements ISystemFacade{
    //TODO: decide what should be done if illegal session was given

    private final UserService userService;

    private final StoreService storeService;

    private final MarketService marketService;

    public SystemFacade(){
        DbConfig.setPERSIST(false);
        ISystemManagerRepo systemManagerRepo = new SystemManagerRepo();
        IUserRepo userRepo = new UserRepo();
        ISessionRepo sessionRepo = new SessionRepo();
        IMarket market = MarketImpl.getInstance();
        IUserController userController = new UserController(systemManagerRepo, userRepo, sessionRepo, market);
        NotificationService notificationService = new NotificationService(userController);
        userController.updateNotificationService(notificationService);
        IStoreController storeController = new StoreController(userController);
        storeController.updateNotificationService(notificationService);

        initMarket(market, userController, notificationService, storeController);

        userService = new UserService(userController);
        storeService = new StoreService(storeController);
        marketService = new MarketService(market, userController);
        ConfigReader.ReadConfig(this);
        CreateDataForTest.createData(this);
    }
    public Result updatePaymentServiceURL(String url) {
        return marketService.updatePaymentServiceURL(url);
    }

    @Override
    public Result<Set<String>> getUsers(String session) {
        logEntry("getUsers", String.format("[sessionID=%s]", session));
        return userService.getUsers(session);
    }

    @Override
    public Result<Set<String>> getLoggedUsers(String session) {
        logEntry("getLoggedUsers", String.format("[sessionID=%s]", session));
        return userService.getLoggedUsers(session);
    }

    @Override
    public Result<Set<String>> getDisconnectedUsers(String session) {
        logEntry("getDisconnectedUsers", String.format("[sessionID=%s]", session));
        return userService.getDisconnectedUsers(session);
    }

    @Override
    public Result<Set<PermissionType>> getUserPermissionsTypes(String session, String userName,Integer storeID) {
        logEntry("getPermissions", String.format("[sessionID=%s]", session));
        return storeService.getUserPermissionsTypes(session, userName, storeID);
    }

    private static void initMarket(IMarket market, IUserController userController, NotificationService notificationService, IStoreController storeController) {
        //init market
        market.initControllers(userController, storeController);
        market.updateNotificationService(notificationService);
        market.updatePaymentService(new PaymentServiceProxy("regular"));
        market.updateSupplyService(new SupplyServiceProxy());
    }

    @Override
    public Result updatePaymentService(IPaymentService paymentService) {
        return marketService.updatePaymentService(paymentService);
    }

    @Override
    public Result getAllPendingMessages(String sessionId) {
        return userService.getAllPendingMessages(sessionId);
    }

    @Override
    public Result connect(String sessionId) {
        return userService.connect(sessionId);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     */
    public Result terminate(String sessId) {
        logEntry("terminate", String.format("[sessionID=%s]", sessId));
            return userService.terminate(sessId);
    }

    @Override
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
        logEntry("register", String.format("[sessionID=%s, %s]", sessId, userDetails));
        return userService.register(sessId, userDetails, pass);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>ALREADY_LOGGED_IN
     * <br>USERNAME_PASSWORD_MISMATCH
     */
    public Result login(String sessId, String uname, String pass) {
        logEntry("login", String.format("[sessionID=%s, username=%s]", sessId, uname));
            return userService.login(sessId, uname, pass);
    }

    @Override
    public Result subscribeToNotifications(String sessId, NotificationObserver observer) {
        logEntry("subscribeToNotifications", String.format("[sessionID=%s, observer=%s]", sessId, observer));
        return userService.sub(sessId, observer);
    }

    @Override
    public Result unsubscribeFromNotifications(String sessId, NotificationObserver observerToRemove) {
        logEntry("unsubscribeFromNotifications", String.format("[sessionID=%s, observer=%s]", sessId, observerToRemove));
        return userService.unsub(sessId, observerToRemove);
    }

    @Override
    public Result<List<Integer>> getStoresBy(String sessId, String name, List<String> categories, List<String> keywords) {
        throw new NotImplementedException("Not yet implemented");
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>STORE_DOES_NOT_EXIST
     */
    public Result<StoreRecord> getStoreInfo(String sessId, int storeId) {
        logEntry("getStoreInfo", String.format("[sessionID=%s, storeID=%d]", sessId, storeId));
        //TODO: check session with sessId
        return storeService.getStoreInfo(storeId);
    }

    @Override
    public Result<List<ProductRecord>> getProductsBy(String sessId, ProductFilterAttributes filters) {
        logEntry("getProductsBy", String.format("[sessionID=%s, %s]", sessId, filters));
        //TODO: check session with sessId
        return storeService.getProductsBy(filters);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     */
    public Result<ProductRecord> getProductInfo(String sessId, int productId, int storeId) {
        logEntry("getProductsInfo", String.format("[sessionID=%s, productID=%d, storeID=%d]", sessId, productId, storeId));
        //TODO: check session with sessId
        return storeService.getProductInfo(sessId, productId, storeId);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     */
    public Result addProductToCart(String sessId, int productId, int storeId, int quantity) {
        logEntry("addProductToCart", String.format("[sessionID=%s, productID=%d, storeID=%d, quantity=%d]", sessId, productId, storeId, quantity));
        Result<ProductRecord> res = storeService.getProductInfo(sessId, productId, storeId);
        if(res.errorOccured())
            return res;
        ProductRecord product = res.getValue();
        product = product.updateQuantity(quantity);
        return userService.addProductToCart(sessId, product);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     */
    public Result<Map<Integer, Map<Integer, ProductRecord>>> getCartContent(String sessId) {
        logEntry("getCartContent", String.format("[sessionID=%s]", sessId));
        return userService.getCartContent(sessId);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>USERCART_DOES_NOT_EXISTS
     * <br>PRODUCT_DOES_NOT_EXIST
     */
    public Result removeProductFromCart(String sessId, int storeId, int productId, int quantity) {
        logEntry("removeProductFromCart", String.format("[sessionID=%s, storeID=%d, productID=%d]", sessId, storeId, productId));
        return userService.removeProductFromCart(sessId, storeId, productId, quantity);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     */
    public Result<Double> getCartPrice(String sessId) {
        logEntry("getCartPrice", String.format("[sessionID=%s]", sessId));
        return userService.getCartPrice(sessId);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>USERCART_DOES_NOT_EXISTS
     */
    public Result pay(String sessId, PaymentDetails paymentDetails,
                      AddressRecord deliveryAddress) {
        logEntry("pay", String.format("[sessionID=%s]", sessId));
        return marketService.pay(sessId, paymentDetails, deliveryAddress);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>GUEST_SESSION
     */
    public Result logout(String sessId) {
        logEntry("logout", String.format("[sessionID=%s]", sessId));
        return userService.logout(sessId);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>GUEST_SESSION
     */
    public Result<Integer> openStore(String sessId, StoreRecord store) {
        logEntry("openStore", String.format("[sessionID=%s, %s]", sessId, store));
        return storeService.openStore(sessId, store);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     */
    public Result updateStore(String sessId, StoreRecord updatedStore) {
        logEntry("updateStore", String.format("[sessionID=%s, %s]", sessId, updatedStore));
        return storeService.updateStore(sessId, updatedStore);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>INVALID_PRODUCT_NAME
     * <br>INVALID_PRODUCT_PRICE
     * <br>INVALID_PRODUCT_QUANTITY
     * <br>INVALID_PRODUCT_CATEGORY
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    public Result<Integer> addNewProduct(String sessId, ProductRecord product) {
        logEntry("addNewProduct", String.format("[sessionID=%s, %s]", sessId, product));
        return storeService.addNewProduct(sessId, product);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    public Result removeProduct(String sessId, int storeId, int productId) {
        logEntry("removeProduct", String.format("[sessionID=%s, storeID=%d, productID=%d]", sessId, storeId, productId));
        return storeService.removeProduct(sessId, storeId, productId);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     * <br>INVALID_PRODUCT_NAME
     * <br>INVALID_PRODUCT_PRICE
     * <br>INVALID_PRODUCT_CATEGORY
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    public Result updateProduct(String sessId, ProductRecord updatedProduct) {
        logEntry("updateProduct", String.format("[sessionID=%s, %s]", sessId, updatedProduct));
        return storeService.updateProduct(sessId, updatedProduct);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     * <br>INVALID_PRODUCT_QUANTITY
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    public Result addProductQuantity(String sessId, int storeId, int productId, int quantity) {
        logEntry("addProductQuantity", String.format("[sessionID=%s, storeID=%d, productID=%d, quantity=%d]", sessId, storeId, productId, quantity));
        return storeService.addProductQuantity(sessId, storeId, productId, quantity);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     * <br>INVALID_PRODUCT_QUANTITY
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    public Result removeProductQuantity(String sessId, int storeId, int productId, int quantity){
        logEntry("removeProductQuantity", String.format("[sessionID=%s, storeID=%d, productID=%d, quantity=%d]", sessId, storeId, productId, quantity));
        return storeService.removeProductQuantity(sessId, storeId, productId, quantity);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_ADD_OR_REMOVE_STORE_OWNER
     * <br>USERNAME_DOES_NOT_EXIST
     * <br>MEMBMER_ALREADY_HAS_ROLE
     */
    public Result appointOwner(String sessId, String uname, int storeId) {
        logEntry("appointOwner", String.format("[sessionID=%s, storeID=%d, new_owner=%s]", sessId, storeId, uname));
        return storeService.appointOwner(sessId, uname, storeId);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_ADD_OR_REMOVE_STORE_MANAGER
     * <br>USERNAME_DOES_NOT_EXIST
     * <br>MEMBMER_ALREADY_HAS_ROLE
     */
    public Result appointManager(String sessId, String uname, int storeId) {
        logEntry("appointManager", String.format("[sessionID=%s, storeID=%d, new_manager=%s]", sessId, storeId, uname));
        return storeService.appointManager(sessId, uname, storeId);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_GET_EMPLOYEES_DATA
     * <br>USERNAME_DOES_NOT_EXIST
     */
    public Result<Integer> getManagerPermissions(String sessId, String uname, int storeId) {
        logEntry("getManagerPermissions", String.format("[sessionID=%s, storeID=%d, manager=%s]", sessId, storeId, uname));
        return storeService.getManagerPermissions(sessId, uname, storeId);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>USERNAME_DOES_NOT_EXIST
     * <br>NO_PERMISSION_CHANGE_OWNER_PERMISSIONS
     */
    public Result modifyPermissionsFor(String sessId, String uname, int storeId, int newPerms) {
        logEntry("modifyPermissionsFor", String.format("[sessionID=%s, storeID=%d, manager=%s, new_permissions=%d]", sessId, storeId, uname, newPerms));
        return storeService.modifyPermissionsFor(sessId, uname, storeId, newPerms);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_MAKE_STORE_INACTIVE
     */
    public Result closeStore(String sessId, int storeId) {
        logEntry("closeStore", String.format("[sessionID=%s, storeID=%d]", sessId, storeId));
        return storeService.closeStore(sessId, storeId);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_GET_EMPLOYEES_DATA
     */
    public Result<List<String>> getStoreOwners(String sessId, int storeId) {
        logEntry("getStoreOwners", String.format("[sessionID=%s, storeID=%d]", sessId, storeId));
        return storeService.getStoreOwners(sessId, storeId);
    }

    @Override
    public Result<String> getStoreFounder(String sessId, int storeId) {
        logEntry("getStoreFounder", String.format("[sessionID=%s, storeID=%d]", sessId, storeId));
        return storeService.getStoreFounder(sessId, storeId);
    }

    @Override
    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_GET_EMPLOYEES_DATA
     */
    public Result<List<String>> getStoreManagers(String sessId, int storeId) {
        logEntry("getStoreManagers", String.format("[sessionID=%s, storeID=%d]", sessId, storeId));
        return storeService.getStoreManagers(sessId, storeId);
    }

    @Override
    public Result<List<Transaction>> getStoreTransactionHistory(String sessId, int storeId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return marketService.getStoreTransactionHistory(sessId, storeId, startDateTime, endDateTime);
    }

    @Override
    public Result<List<Transaction>> getUserTransactionHistory(String sessId, String uname, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return marketService.getUserTransactionHistory(sessId, uname, startDateTime, endDateTime);
    }

    @Override
    public Result reopenStore(String sessionId, int storeId) {
        return storeService.reopenStore(sessionId, storeId);
    }

    public Result<Boolean> isGuestSession(String sessionId) {
        return userService.isGuestSession(sessionId);
    }

    public Result<Boolean> isMemberSession(String sessionId) {
        return userService.isMemberSession(sessionId);
    }

    public Result<Boolean> isSystemManagerSession(String sessionId) {
        return userService.isSystemManagerSession(sessionId);
    }

    @Override
    public Result<List<StoreRecord>> getMyStores(String sessionId) {
        return storeService.getMyStores(sessionId);
    }

    @Override
    public Result removeStoreOwner(String sessionId, String username, Integer storeId) {
        return storeService.removeStoreOwner(sessionId, username, storeId);
    }

    @Override
    public Result removeStoreManager(String sessionId, String username, Integer storeId) {
        return storeService.removeStoreManager(sessionId, username, storeId);
    }

    private static void logEntry(String function, String arguments){
        MarketLogger.logEvent(SystemFacade.class.getName(), function, arguments);
    }

    @Override
    public Result closeStorePermanently(String sessionId, int storeId){
        return storeService.closeStorePermanently(sessionId, storeId);
    }

    @Override
    public Result<Set<Integer>>
    getStoresAccordingToRole(String sessionId, RoleType role){
        return storeService.getStoresAccordingToRole(sessionId, role);
    }

    @Override
    public Result removeMember(String session, String username){
        return userService.removeMember(session, username);
    }

    @Override
    public Result<Integer> getMemberPermissions(String sessionId, Integer storeId) {
        return storeService.getMemberPermissions(sessionId, storeId);
    }

    @Override
    public Result changePassword(String sessionId, String oldPassword, String newPassword) {
        return userService.changePassword(sessionId, oldPassword, newPassword);
    }

    @Override
    public Result<Integer> addMemberAddress(String sessionId, AddressRecord addressData) {
        return userService.addMemberAddress(sessionId, addressData);
    }

    @Override
    public Result<Map<String,Integer>> getManagersPermissions(String sessionId, int storeId) {
        return storeService.getManagersPermissions(sessionId, storeId);
    }

    @Override
    public Result<List<ProductRecord>> getStoreProducts(String sessionId, int storeId) {
        return storeService.getStoreProducts(sessionId, storeId);
    }

    @Override
    public Result<Set<StoreRecord>> getStores() {return storeService.getStores();}

    @Override
    public Result<Integer> getAmountOfConnectedMembers(String sessionId) {
        return userService.getAmountOfConnectedMembers(sessionId);
    }

    @Override
    public Result<Integer> getAmountOfConnectedGuests(String sessionId) {
        return userService.getAmountOfConnectedGuests(sessionId);
    }

    @Override
    public Result<UserRecord> getMemberDetails(String sessionId) {
        return userService.getMemberDetails(sessionId);
    }

    @Override
    public Result<List<Transaction>> getTransactionHistory(String sessionId) {
        return marketService.getTransactionHistory(sessionId);
    }

    public Result removeDiscount(String session, int storeID, int discountId) {
        return storeService.removeDiscount(session, storeID, discountId);
    }

    @Override
    public Result checkCartPurchaseRules(String session) {
        return storeService.checkCartPurchaseRules(session);
    }

    @Override
    public Result removePurchaseRule(String session, int storeID, int purchaseRuleID) {
        return storeService.removePurchaseRule(session, storeID, purchaseRuleID);
    }

    @Override
    public Result addDiscount(String session, int storeId, IDiscount discount){
        return storeService.addDiscount(session, storeId, discount);
    }

    @Override
    public Result<List<IDiscount>> getStoreDiscounts(String session, int storeID){
        return storeService.getDiscounts(session, storeID);
    }

    @Override
    public Result<IDiscount> getStoreDiscount(String sessionId, Integer storeId, Integer discountId) {
        return storeService.getDiscount(sessionId, storeId, discountId);
    }

    @Override
    public Result addPurchaseRule(String session, int storeId, PurchaseRule rule){
        return storeService.addPurchaseRule(session, storeId, rule);
    }

    @Override
    public Result<List<PurchaseRule>> getStorePurchaseRules(String session, int storeID){
        return storeService.getStorePurchaseRules(session, storeID);
    }

    @Override
    public Result memberPublishOffer(String sessionId, int storeId, int productId, double offerdPrice, int quantity){
        return marketService.memberPublishOffer(sessionId, storeId, productId, offerdPrice, quantity);
    }

    @Override
    public Result memberRejectOffer(String sessionId, int offerId){
        return marketService.memberRejectOffer(sessionId, offerId);
    }

    @Override
    public Result storeRejectOffer(String sessionId, int offerId, int storeId){
        return marketService.storeRejectOffer(sessionId, offerId, storeId);
    }

    @Override
    public Result<Map<Integer, Offer>> getMemberOffers(String sessionID){
        return userService.getMemberOffers(sessionID);
    }

    @Override
    public Result consentOffer(String sessionId, int offerId, int storeId){
        return storeService.consentOffer(sessionId, offerId, storeId);
    }

    @Override
    public Result<Map<Integer, Offer>> getOffers(String sessionId, int storeId) {
        return storeService.getOffers(sessionId, storeId);
    }

    @Override
    public Result createSystemManager(String sessId, String username, String password) {
        return userService.createSystemManager(sessId, username, password);
    }

    @Override
    public Result purchaseBid(String sessionId, PaymentDetails paymentDetails, AddressRecord deliveryAddress, int storeID, int offerID){
        return marketService.purchaseBid(sessionId, paymentDetails, deliveryAddress, storeID, offerID);
    }

    @Override
    public Result counterOffer(String sessionId, int storeId, int offerId, int productQuantity, double productPrice) {
        return storeService.counterOffer(sessionId, storeId, offerId, productQuantity, productPrice);
    }

    @Override
    public Result sendMessage(String sessionId, String msg, String receiver) {
        return userService.sendMessage(sessionId, msg, receiver);
    }

    //****************************************************************** Contracts
    @Override
    public Result publishMemberContract(String sessionId, int storeId, String newOwnerUserName, String contract) {
        return storeService.publishMemberContract(sessionId, storeId, newOwnerUserName, contract);
    }

    @Override
    public Result removeContract(String sessionId, int storeId, int contractId) {
        return storeService.removeContract(sessionId, storeId, contractId);
    }

    @Override
    public Result consentContract(String sessionId, int storeId, int contractId) {
        return storeService.consentContract(sessionId, storeId, contractId);
    }

    @Override
    public Result getContracts(String sessionId, int storeId) {
        return storeService.getContracts(sessionId, storeId);
    }

    public Result<List<Transaction>> getMyTransactionHistory(String sessionId,  LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return marketService.getMyTransactionHistory(sessionId, startDateTime, endDateTime);
    }
}
