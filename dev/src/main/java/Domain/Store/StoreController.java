package Domain.Store;

import DataLayer.Store.*;
import util.Enums.ErrorStatus;
import util.Records.Transaction;
import Domain.MarketLogger;
import Domain.Permission;
import Domain.Services.NotificationService.INotificationService;
import Domain.Store.Discount.IDiscount;
import util.Exceptions.PurchaseLimitation;
import Domain.Store.Purchase.PurchaseRule;
import org.checkerframework.org.apache.commons.lang3.NotImplementedException;
import Domain.User.IStoreBasket;
import Domain.User.IUserCart;
import Domain.User.IUserController;
import util.Enums.PermissionType;
import util.Enums.RoleType;
import util.Exceptions.*;
import util.Records.DateRange;
import util.Records.StoreRecords.ProductFilterAttributes;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StoreController implements IStoreController {
    private IStoreRepo storeRepo;
    private IUserController userController;
    private INotificationService notificationService;

    public StoreController(IUserController userController) {
        this.userController = userController;
        storeRepo = new StoreRepo();
    }

    @Override
    public void updateNotificationService(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public List<StoreRecord> getMyStores(String sessionId) throws NonExistentData, SessionError {
        List<Integer> storeIds = userController.getMemberStores(sessionId);
        String userName = userController.getMemberUserName(sessionId);
        List<StoreRecord> storeRecords = new ArrayList<>();
        for (int storeId : storeIds) {
            IStore store = storeRepo.getStore(storeId);
            Map <String, Permission> permissions = store.getPermissions();
            if(!store.isActive() && permissions.get(userName).isStoreManager())
                continue;
            StoreRecord storeRecord = new StoreRecord(store.getStoreId(), store.getStoreName(), store.getStoreRating(), store.getStoreDescription(), store.isActive());
            storeRecords.add(storeRecord);
        }
        return storeRecords;
    }

    @Override
    public Set<Integer> getStoresAccordingToRole(String sessionId,
                                                 RoleType role)
            throws MarketException {
        String username = userController.getMemberUserName(sessionId);
        return getMyStores(sessionId)
                .stream()
                .filter(store -> {
                    try {
                        return getStore(store.storeId())
                                .getPermissions()
                                .get(username)
                                .getRoleType() == role;
                    } catch (NonExistentData e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(StoreRecord::storeId)
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized int openNewStore(String sessionId, StoreRecord storeRecord) throws SessionError {
        String userName = getUserName(sessionId);
        int storeId = storeRepo.openNewStore(userName, storeRecord);
        try {
            userController.addRoleToMember(userName, storeId);
        } catch (NonExistentData | DataExistentError ignored) {
        }
        return storeId;
    }

    @Override
    public void syncCart(IUserCart cart) {
        try {
            for (IStoreBasket basket : cart.getStoreBaskets()) {
                IStore store = getStore(basket.getStoreId());
                store.calculateBasketPrice(basket);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Permission getMemberPermissions(String sessionId, Integer storeId) throws SessionError, NonExistentData {
        String userName = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        return store.getMemberPermissions(userName);
    }

    @Override
    public void updateStoreFields(String sessionId, StoreRecord updatedStore) throws SessionError, NonExistentData, PermissionError {
        String userName = getUserName(sessionId);
        int storeId = updatedStore.storeId();
        IStore store = storeRepo.getStore(storeId);
        store.updateFields(userName, updatedStore);
    }

    @Override
    public void closeStore(String sessionId, int storeId) throws SessionError, NonExistentData, PermissionError {
        String userName = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        store.closeStore(userName);
        notificationService.broadcastMessage(
                store.getPermissions().keySet().stream().toList(),
                "The store was closed",
                store.getStoreName()
        );
    }

    @Override
    public void closeStorePermanently(String sessionId, int storeId)
            throws SessionError, NonExistentData, PermissionError {
        IStore store = storeRepo.getStore(storeId);
        if (userController.isMemberIsSystemManager(sessionId) || store.getFounderName("System").equals(getUserName(sessionId))) {
            Set<String> workers =  store.getPermissions().keySet();
            for(String worker : workers)
                userController.getMember(worker).removeRole(storeId);
            storeRepo.removeStore(storeId);
            notificationService.broadcastMessage(
                    workers.stream().toList(),
                    "The store was permanently closed",
                    store.getStoreName()
            );
        }
        else{
            MarketLogger.logError("StoreController", "closeStorePermanently", "user with this session id: " + sessionId +", not have permission to close store: "+storeId+", permanently", sessionId, storeId);
            throw new PermissionError("user with this session id: " + sessionId +", not have permission to close store: "+storeId+", permanently", ErrorStatus.NO_PERMISSION);
        }
    }

    @Override
    public int addNewProduct(String sessionId,
                                          ProductRecord productRecord)
            throws SessionError, NonExistentData, DataError, PermissionError {
        String userName = getUserName(sessionId);
        int storeId = productRecord.storeId();
        IStore store = storeRepo.getStore(storeId);
        synchronized(store) {
            return store.addNewProduct(userName, productRecord);
        }
    }

    @Override
    public void removeProduct(String sessionId, int storeId, int productId)
            throws SessionError, NonExistentData, PermissionError {
        String userName = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        store.removeProduct(userName, productId);
    }

    @Override
    public void addProductQuantity(String sessionId, int storeId, int productId,
                                   int quantity)
            throws SessionError, NonExistentData, PermissionError, DataError {
        String userName = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        store.addProductQuantity(userName, productId, quantity);
    }

    @Override
    public void reduceProductQuantity(String sessionId, int storeId,
                                      int productId, int quantity)
            throws SessionError, NonExistentData, DataError, PermissionError {
        String userName = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        store.reduceProductQuantity(userName, productId, quantity);
    }

    @Override
    public void updateProductFields(String sessionId, ProductRecord productRecord)
            throws SessionError, NonExistentData, DataError, PermissionError {
        String userName = getUserName(sessionId);
        int storeId = productRecord.storeId();
        IStore store = storeRepo.getStore(storeId);
        store.updateProductFields(userName, productRecord);
    }

    @Override
    public void addCartQuantity(IUserCart userCart)
            throws NonExistentData, DataError, PermissionError {
        Map<Integer, Map<Integer, Integer>> storeBasketCache = new HashMap<>();
        List<IStoreBasket> storeBaskets = userCart.getStoreBaskets();
        for (IStoreBasket storeBasket : storeBaskets) {
            int storeId = storeBasket.getStoreId();
            storeBasketCache.put(storeId, new HashMap<>());
            try {
                storeRepo.getStore(storeId).addBasketQuantity(
                        storeBasket, storeBasketCache.get(storeId));
            } catch (NonExistentData e) {
                removeProductsFromCache(storeBasketCache);
                MarketLogger.logError(
                        "StoreController", "addCartQuantity",
                        "couldn't restore products quantity from user cart",
                        userCart.toString());
            }
        }
    }

    @Override
    public void removeCartQuantity(IUserCart userCart) throws NonExistentData, DataError{
        Map<Integer, Map<Integer, Integer>> storeBasketCache = new HashMap<>();
        List<IStoreBasket> storeBaskets = userCart.getStoreBaskets();
        for (IStoreBasket storeBasket : storeBaskets) {
            int storeId = storeBasket.getStoreId();
            storeBasketCache.put(storeId, new HashMap<>());
            try {
                storeRepo.getStore(storeId).removeBasketQuantity(
                        storeBasket, storeBasketCache.get(storeId));
            } catch (DataError e) {
                try {
                    addProductsFromCache(storeBasketCache);
                } catch (MarketException e2) {
                    throw new RuntimeException(
                            e.getMessage() + "and" + e2.getMessage() +
                                    "Cannot remove cart quantity, store with ID " + storeId);
                }
                throw e;
            }
        }
    }

    private void
    addProductsFromCache(Map<Integer, Map<Integer, Integer>> storeBasketCache)
            throws NonExistentData, DataError, PermissionError {
        for (int storeId : storeBasketCache.keySet()) {
            for (int productId : storeBasketCache.get(storeId).keySet()) {
                int quantity = storeBasketCache.get(storeId).get(productId);
                storeRepo.getStore(storeId).addProductQuantity("System", productId,
                        quantity);
            }
        }
    }

    private void
    removeProductsFromCache(Map<Integer, Map<Integer, Integer>> storeBasketCache)
            throws NonExistentData, DataError, PermissionError {
        for (int storeId : storeBasketCache.keySet()) {
            for (int productId : storeBasketCache.get(storeId).keySet()) {
                int quantity = storeBasketCache.get(storeId).get(productId);
                storeRepo.getStore(storeId).reduceProductQuantity("System", productId,
                        quantity);
            }
        }
    }

    @Override
    public IStore getStore(int storeId) throws NonExistentData {
        return storeRepo.getStore(storeId);
    }

    @Override
    public StoreRecord getStoreInfo(int storeId) throws NonExistentData {
        IStore store = storeRepo.getStore(storeId);
        String storeName = store.getStoreName();
        double storeRating = store.getStoreRating();
        String storeDesc = store.getStoreDescription();
        return new StoreRecord(storeId, storeName, storeRating, storeDesc, store.isActive());
    }

    @Override
    public List<IProduct> getStoreProducts(String sessionId, int storeId) throws NonExistentData, PermissionError, SessionError {
        IStore store = storeRepo.getStore(storeId);
        if (!store.isActive())
        {
            String userName = userController.getMemberUserName(sessionId);
            Permission permission = store.getMemberPermissions(userName);
            if (permission.isStoreManager())
                throw new PermissionError("You dont have permission to retrieve products of inactive store", ErrorStatus.NO_PERMISSION);
        }
        return store.getProducts();
    }

    @Override
    public void assignStoreOwner(String sessionId, String newOwnerUsername,
                                 int storeId)
            throws SessionError, NonExistentData, PermissionError, DataExistentError {
        userController.isMemberExists(newOwnerUsername);
        String currentOwnerUsername = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        store.assignStoreOwner(currentOwnerUsername, newOwnerUsername);
        userController.addRoleToMember(newOwnerUsername, storeId);
    }

    @Override
    public void assignStoreManager(String sessionId, String newManagerUsername,
                                   int storeId)
            throws SessionError, NonExistentData, PermissionError, DataExistentError {
        userController.isMemberExists(newManagerUsername);
        String currentOwnerUsername = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        store.assignStoreManager(currentOwnerUsername, newManagerUsername);
        userController.addRoleToMember(newManagerUsername, storeId);
    }

    @Override
    public void removeStoreOwnerAppointment(String sessionId,
                                            String removedOwnerUsername,
                                            int storeId)
            throws SessionError, NonExistentData, PermissionError, DataExistentError {
        userController.isMemberExists(removedOwnerUsername);
        String currentOwnerUsername = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        store.removeStoreOwnerAppointment(currentOwnerUsername,
                removedOwnerUsername);
        userController.removeRoleFromMember(removedOwnerUsername, storeId);
        notificationService.notify(
                "You removed from store owner appointment by " + currentOwnerUsername,
                removedOwnerUsername,
                store.getStoreName()
        );
    }

    @Override
    public void removeStoreManagerAppointment(String sessionId, String removedManagerUsername, int storeId)
            throws SessionError, NonExistentData, PermissionError {
        userController.isMemberExists(removedManagerUsername);
        String currentOwnerUsername = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        store.removeStoreManagerAppointment(currentOwnerUsername, removedManagerUsername);
        notificationService.notify(
                "You removed from store manager appointment by " + currentOwnerUsername,
                removedManagerUsername,
                store.getStoreName()
        );
        userController.removeRoleFromMember(removedManagerUsername, storeId);
    }

    @Override
    public void reopenStore(String sessionId, int storeId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<IStore> getActiveStores() {
        List<IStore> storeList = storeRepo.getStores();
        return storeList.stream()
                .filter(IStore::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<IProduct>
    getFilteredProducts(ProductFilterAttributes productFilterAttributes) {
        List<IStore> storeList = getActiveStores();
        List<IStore> filteredStores = filterStoresByName(productFilterAttributes.storeName(),
                filterStoresByRating(productFilterAttributes.lowStoreRating(), storeList));
        List<IProduct> filteredProducts = new ArrayList<>();

        for (IStore store : filteredStores) {
            List<IProduct> products = new ArrayList<>();
            try {
                products = store.getProducts();
            } catch (Exception e) {
            }
            // apply product filters
            products = filterProductsByName(
                    productFilterAttributes.productName(),
                    filterProductsByCategories(
                            productFilterAttributes.productCategories(),
                            filterProductsByRating(
                                    productFilterAttributes.lowProductRating(),
                                    filterProductsByPrice(productFilterAttributes.lowPrice(),
                                            productFilterAttributes.highPrice(),
                                            products))));
            filteredProducts.addAll(products);
        }
        return filteredProducts;
    }

    @Override
    public double calculateBasketPrice(IStoreBasket storeBasket) {
        int storeId = storeBasket.getStoreId();
        try {
            return storeRepo.getStore(storeId).calculateBasketPrice(storeBasket);
        } catch (Exception e) {
            MarketLogger.logError("StoreController", "calculateBasketPrice",
                    "could not calculate basket price", "storeBasket");
            return -1;
        }
    }

    @Override
    public void checkPurchaseRules(IStoreBasket storeBasket) throws NonExistentData, PurchaseLimitation {
        int storeId = storeBasket.getStoreId();
        storeRepo.getStore(storeId).checkPurchaseRules(storeBasket);
    }

    @Override
    public Map<String, Permission> getStoreRoles(String sessionId,
                                                 int storeId) throws SessionError, NonExistentData, PermissionError {
        String userName = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        return store.getStoreRoles(userName);
    }

    @Override
    public Map<String, Permission> getManagersPermissions(String sessionId,
                                                          int storeId)
            throws SessionError, NonExistentData, PermissionError {
        String userName = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        return store.getManagersPermissions(userName);
    }

    @Override
    public void setManagerPermissions(String sessionId, String managerUserName,
                                      int storeId,
                                      Set<PermissionType> permissions)
            throws SessionError, NonExistentData, PermissionError {
        String userName = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        store.setManagerPermissions(userName, managerUserName, permissions);
    }

    @Override
    public String getStoreFounder(String sessId, int storeId) throws NonExistentData, SessionError, PermissionError {
        // Only members with permission can extrapolate the founder name, maybe we want to change that
        String userName = getUserName(sessId);
        IStore store = getStore(storeId);
        return store.getFounderName(userName);
    }

    @Override
    public List<StoreRecord> getStores() {
        List<IStore> storeList = getActiveStores();

        return storeList.stream()
                .map(StoreRecord::new)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Map<Integer, IProduct>> getStoresProducts() {
        return getActiveStores().stream()
                .collect(Collectors.toMap(IStore::getStoreId,
                        s -> s.getProducts().stream()
                                .collect(Collectors.toMap(IProduct::getProductId, Function.identity()))));
    }

    @Override
    public void checkProductsExist(int storeId, Map<Integer, ProductRecord> products)
            throws NonExistentData, DataError {
        IStore store = getStore(storeId);
        store.areProductsExist(products);
    }

    // private methods

    private List<IStore> filterStoresByRating(Double rating, List<IStore> storeList) {
        if (rating == null) {
            return storeList;
        }
        return storeList.stream()
                .filter(store -> store.getStoreRating() >= rating)
                .collect(Collectors.toList());
    }

    private List<IStore> filterStoresByName(String storeName,
                                              List<IStore> storeList) {
        if (storeName == null) {
            return storeList;
        }
        return storeList.stream()
                .filter(store -> store.getStoreName().equalsIgnoreCase(storeName))
                .collect(Collectors.toList());
    }

    private List<IProduct> filterProductsByName(String productName,
                                                List<IProduct> products) {
        if (productName == null)
            return products;
        return products.stream()
                .filter(product -> product.getProductName().equals(productName))
                .collect(Collectors.toList());
    }

    private List<IProduct> filterProductsByCategories(List<Category> categories,
                                                      List<IProduct> products) {
        if (categories == null || categories.size() == 0) {
            return products;
        }
        return products.stream()
                .filter(product -> categories.contains(product.getProductCategory()))
                .collect(Collectors.toList());
    }

    private List<IProduct> filterProductsByRating(Double rating,
                                                  List<IProduct> products) {
        if (rating == null) {
            return products;
        }
        return products.stream()
                .filter(product -> product.getProductRating() >= rating)
                .collect(Collectors.toList());
    }

    private List<IProduct> filterProductsByPrice(Double lowPrice,
                                                 Double highPrice,
                                                 List<IProduct> products) {
        if (lowPrice == null && highPrice == null)
            return products;
        double finalLowPrice = lowPrice == null ? Double.MIN_VALUE : lowPrice;
        double finalHighPrice = highPrice == null ? Double.MAX_VALUE : highPrice;
        return products.stream()
                .filter(product
                        -> product.getProductPrice() <= finalHighPrice &&
                        product.getProductPrice() >= finalLowPrice)
                .collect(Collectors.toList());
    }

    private String getUserName(String sessionId) throws SessionError {
        return userController.getMemberUserName(sessionId);
    }

    // for tests
    public void addStore(IStore store) {
        storeRepo.getStoreMap().putIfAbsent(store.getStoreId(), store);
    }


    public void removeDiscount(String session, int storeID, int discountId) throws MarketException {
        getStore(storeID).removeDiscount(userController.getMemberUserName(session), discountId);
    }

    public void checkCartPurchaseRules(String session) throws SessionError, NonExistentData, PurchaseLimitation {
        IUserCart cart = userController.getUserCart(session);
        for (IStoreBasket basket : cart.getStoreBaskets()) {
            checkPurchaseRules(basket);
        }
    }

    public void removePurchaseRule(String session, int storeID, int purchaseRuleID) throws NonExistentData, SessionError, PermissionError {
        getStore(storeID).removePurchaseRule(getUserName(session), purchaseRuleID);
    }

    @Override
    public List<Transaction> getStoreTransactions(String sessionId, int storeId, DateRange range) {
        throw new NotImplementedException("Not yet implemented");
    }
    //*********************************************************Transactions

    //@Override
    public List<Transaction> getStoreTransactions(String sessionId, int storeId, LocalDateTime optionalStart, LocalDateTime optionalEnd) throws SessionError, NonExistentData, DataError, PermissionError {
        String userName = getUserName(sessionId);
        IStore store = storeRepo.getStore(storeId);
        return store.getStoreTransactionsByDate(userName, optionalStart, optionalEnd);

    }

    public void addDiscount(String session, int storeId, IDiscount discount) throws NonExistentData, SessionError, PermissionError {
        IStore store = getStore(storeId);
        String userName = userController.getMemberUserName(session);
        store.addDiscount(userName, discount);
    }

    public List<IDiscount> getDiscounts(String session, int storeID) throws NonExistentData {
        IStore store = getStore(storeID);
        return store.getStoreDiscounts().values().stream().toList();
    }

    @Override
    public IDiscount getDiscount(String sessionId, Integer storeId, Integer discountId) throws NonExistentData {
        IStore store = getStore(storeId);
        return store.getStoreDiscount(discountId);
    }

    @Override
    public Set<PermissionType> getUserPermissionsTypes(String session, String userName, Integer storeID) throws NonExistentData{
        IStore store = storeRepo.getStore(storeID);
        return PermissionType.bitmapToSet(store.getMemberPermissions(userName).permissionsBitMap());
    }

    public void addPurchaseRule(String session, int storeId, PurchaseRule rule) throws NonExistentData, SessionError, PermissionError {
        IStore store = getStore(storeId);
        store.addPurchaseRule(userController.getMemberUserName(session), rule);
    }

    public List<PurchaseRule> getStorePurchaseRules(String session, int storeID) throws NonExistentData {
        IStore store = getStore(storeID);
        return store.getStorePurchaseRules().values().stream().toList();
    }

    //*********************************************Bid

    @Override
    public void removeOfferQuantity(Offer offer) throws NonExistentData, DataError, PermissionError {
        ProductRecord productRecord = offer.getProduct();
        IStore store = getStore(productRecord.storeId());
        store.reduceProductQuantity("System", productRecord.productId(), offer.getOfferedQuantity());
    }

    @Override
    public void restoreOfferQuantity(Offer offer) throws NonExistentData, PermissionError, DataError {
        ProductRecord productRecord = offer.getProduct();
        IStore store = getStore(productRecord.storeId());
        store.addProductQuantity("System", productRecord.productId(), offer.getOfferedQuantity());
    }

    @Override
    public void removeOffer(String sessionId, int storeId, int offerId) throws NonExistentData, PermissionError, SessionError {
        String userName = userController.getMemberUserName(sessionId);
        getStore(storeId).removeOffer(userName, offerId);
    }

    @Override
    public void consentOffer(String sessionId, int offerId, int storeId) throws NonExistentData, PermissionError, SessionError {
        String userName = userController.getMemberUserName(sessionId);
        IStore store = getStore(storeId);
        store.consentOffer(userName, offerId);
        Offer offer = getStore(storeId).getOffers(userName).get(offerId);
        if (offer.isStoreConsent())
            notificationService.notify("The store approved your offer", offer.getOfferingMember(), store.getStoreName());
    }

    @Override
    public Map<Integer, Offer> getOffers(String sessionId, int storeId) throws NonExistentData, PermissionError, SessionError {
        String userName = userController.getMemberUserName(sessionId);
        return getStore(storeId).getOffers(userName);
    }

    @Override
    public void counterOffer(String sessionId, int storeId, int offerId, int productQuantity, double productPrice) throws NonExistentData, PermissionError, SessionError, DataError {
        String userName = userController.getMemberUserName(sessionId);
        IStore store = getStore(storeId);
        store.counterOffer(userName, offerId, productQuantity, productPrice);
        notificationService.notify(
                "you have a new offer",
                store.getProductOffer(offerId).getOfferingMember(),
                store.getStoreName()
        );
    }

    //**********************************************************Contract
    @Override
    public OwnerAppointmentContract publishMemberContract(String sessionId, int storeId, String newOwnerUserName, String contract) throws SessionError, NonExistentData, PermissionError, DataExistentError {
        if (!userController.isMemberExists(newOwnerUserName)) {
            MarketLogger.logError(
                    "StoreController",
                    "publishMemberContract",
                    String.format("Tried to appoint member '%s' to store '%d' while no such member", newOwnerUserName, storeId),
                    sessionId, storeId, newOwnerUserName, contract
                    );
            throw new NonExistentData(
                    String.format("Member '%s' doesn't exist and thus cannot be appointed", newOwnerUserName),
                    ErrorStatus.USERNAME_DOES_NOT_EXIST
            );
        }
        String userName = userController.getMemberUserName(sessionId);
        return getStore(storeId).publishMemberContract(userName, newOwnerUserName, contract);
    }

    @Override
    public void removeContract(String sessionId, int storeId, int contractId) throws NonExistentData, PermissionError, SessionError {
        String userName = userController.getMemberUserName(sessionId);
        getStore(storeId).removeContract(userName, contractId);
    }

    @Override
    public void consentContract(String sessionId, int storeId, int contractId) throws NonExistentData, PermissionError, SessionError, DataExistentError {
        String userName = userController.getMemberUserName(sessionId);
        IStore store = getStore(storeId);
        OwnerAppointmentContract contract = store.consentContract(userName, contractId);
        if (contract.isStoreConsent())
            notificationService.notify("The store approved your contract", contract.getNewOwner(), store.getStoreName());
    }

    @Override
    public Map<Integer, OwnerAppointmentContract> getContracts(String sessionId, int storeId) throws NonExistentData, PermissionError, SessionError {
        String userName = userController.getMemberUserName(sessionId);
        return getStore(storeId).getContracts(userName);
    }
}
