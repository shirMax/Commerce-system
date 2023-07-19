package Domain.Store;

import DataLayer.ORM.DataPermission;
import DataLayer.Store.ORM.Contract.DataAppointment;
import DataLayer.Store.ORM.DataOffer;
import DataLayer.Store.ORM.DataProduct;
import DataLayer.Store.ORM.DataPurchaseRule;
import DataLayer.Store.ORM.DataStore;
import DataLayer.Store.ORM.Discount.DataDiscount;
import Domain.Store.Discount.DiscountFactory;
import util.Records.Transaction;
import Domain.MarketImpl;
import Domain.MarketLogger;
import Domain.Permission;
import Domain.Store.Discount.DiscountTypes.Simple.ProductDiscount;
import Domain.Store.Discount.IDiscount;
import util.Exceptions.PurchaseLimitation;
import Domain.Store.Purchase.PurchaseRule;
import Domain.User.IStoreBasket;
import org.checkerframework.org.apache.commons.lang3.NotImplementedException;
import util.Enums.ErrorStatus;
import util.Enums.PermissionType;
import util.Enums.RoleType;
import util.Exceptions.DataError;
import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;
import util.Exceptions.PermissionError;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Store implements IStore {
    private final Map<Integer, IProduct> products; // <product_id, product>
    private final Map<String, Permission> permissions; // <username, permission>
    private final Map<Integer, IDiscount> discounts; // <discount_id, discount>
    private final Map<Integer, PurchaseRule> purchaseRules; // <rule_id, rule>
    private final Map<Integer, Offer> offers; // <offer_id, offer>
    private final Map<Integer, OwnerAppointmentContract> contracts; // <contract_id, contract>

    private DataStore dataStore;


    // CTOR for data creation
    public Store(String founderName, StoreRecord storeRecord) {
        dataStore = new DataStore(storeRecord.storeName(), storeRecord.storeDescription(), 0, true);
        dataStore = dataStore.persist();
        products = new ConcurrentHashMap<>();
        permissions = new ConcurrentHashMap<>();
        discounts = new ConcurrentHashMap<>();
        purchaseRules = new ConcurrentHashMap<>();
        offers = new ConcurrentHashMap<>();
        contracts = new ConcurrentHashMap<>();
        Permission founderPermission = new Permission(dataStore, founderName, founderName);
        founderPermission.initStoreFounder();
        permissions.put(founderName, founderPermission);
    }

    public Store(DataStore dataStore) {
        this.dataStore = dataStore;
        products = new ConcurrentHashMap<>();
        permissions = new ConcurrentHashMap<>();
        offers = new ConcurrentHashMap<>();
        contracts = new ConcurrentHashMap<>();
        discounts = new ConcurrentHashMap<>();
        purchaseRules = new ConcurrentHashMap<>();

        for (Map.Entry<Integer, DataProduct> entry : dataStore.getProducts().entrySet()) {
            products.put(entry.getKey(), new Product(entry.getValue(), getStoreId()));
            for (Map.Entry<Integer, DataOffer> entry2 : entry.getValue().getOffers().entrySet())
                offers.put(entry2.getKey(), new Offer(entry2.getValue()));
        }
        for (Map.Entry<String, DataPermission> entry : dataStore.getPermissions().entrySet())
            permissions.put(entry.getKey(), new Permission(entry.getValue()));
        for (Map.Entry<Integer, DataAppointment> entry : dataStore.getContracts().entrySet())
            contracts.put(entry.getKey(), new OwnerAppointmentContract(entry.getValue()));
        for (Map.Entry<Integer, DataPurchaseRule> entry : dataStore.getPurchaseRules().entrySet())
            purchaseRules.put(entry.getKey(), PurchaseRule.recover(entry.getValue()));
        for (Map.Entry<Integer, DataDiscount> entry : dataStore.getDiscounts().entrySet())
            discounts.put(entry.getKey(), DiscountFactory.recover(entry.getValue()));
    }

    @Override
    public void setFounderPermissions(String userName) {
        throw new NotImplementedException("setFounderPermissions");
    }

    /// Store

    //Getters

    @Override
    public int getStoreId() {
        return dataStore.getId();
    }

    @Override
    public String getStoreName() {
        return dataStore.getName();
    }

    @Override
    public String getStoreDescription() {
        return dataStore.getDescription();
    }

    @Override
    public double getStoreRating() {
        return dataStore.getRating();
    }

    @Override
    public boolean isActive() {
        return dataStore.isActive_state();
    }

    // Setters
    @Override
    public void setStoreName(String storeName) {
        dataStore.setName(storeName);
        dataStore = dataStore.persist();
    }

    @Override
    public void setStoreDescription(String storeDescription) {
        dataStore.setDescription(storeDescription);
        dataStore = dataStore.persist();
    }

    @Override
    public void updateFields(String userName, StoreRecord updatedStore) throws PermissionError {
        //TODO: add permissionType
        checkPermission(userName, "updateStoreFields", PermissionType.STORE_MANAGEMENT);
        dataStore.setName(updatedStore.storeName());
        dataStore.setDescription(updatedStore.storeDescription());
        dataStore = dataStore.persist();
    }

    // Actions

    @Override
    public void closeStore(String userName) throws PermissionError, NonExistentData {
        checkPermission(userName, "closeStore", PermissionType.MAKE_STORE_INACTIVE);
        dataStore.setActive_state(false);
        dataStore = dataStore.persist();
    }


    @Override
    public void removeStoreOwnerAppointment(String removingOwner,
                                            String ownerToRemove) throws PermissionError, NonExistentData {
        Permission toRemovePermission = getMemberPermissions(ownerToRemove);
        if (!toRemovePermission.getPermissionGiverName().equals(removingOwner)) {
            try {
                checkPermission(removingOwner, "removeStoreOwnerAppointment", PermissionType.ADD_OR_REMOVE_STORE_OWNER);
            } catch (PermissionError e) {
                MarketLogger.logError("Store", "removeStoreOwnerAppointment",
                        removingOwner + "dont have pesmission to remove " + ownerToRemove,
                        removingOwner, ownerToRemove
                );
                throw e;
            }
        }
        if (!toRemovePermission.isStoreOwner()) {
            MarketLogger.logError("Store", "removeStoreOwnerAppointment",
                    "User " + ownerToRemove + " is not a store owner. Cant remove from ownership",
                    removingOwner, ownerToRemove
            );
            throw new RuntimeException("User " + ownerToRemove + " is not a store owner.");
        }
        removePermission(ownerToRemove);
    }

    @Override
    public void removeStoreManagerAppointment(String removingOwner,
                                              String managerToRemove) throws NonExistentData, PermissionError {
        Permission toRemovePermission = getMemberPermissions(managerToRemove);
        if (!toRemovePermission.getPermissionGiverName().equals(removingOwner)) {
            try {
                checkPermission(removingOwner, "removeStoreManagerAppointment", PermissionType.MANAGE_STORE_MANAGER);
            } catch (PermissionError e) {
                MarketLogger.logError(
                        "Store", "removeStoreOwnerAppointment",
                        removingOwner + "dont have pesmission to remove " + managerToRemove,
                        removingOwner, managerToRemove
                );
                throw e;
            }
        }
        if (!toRemovePermission.isStoreManager()) {
            MarketLogger.logError("Store", "removeStoreManagerAppointment",
                    "User " + managerToRemove + " is not a store manager. Cant remove from managing",
                    removingOwner, managerToRemove
            );
            throw new RuntimeException("User " + managerToRemove + " is not a store manager.");
        }
        removePermission(managerToRemove);
    }

    public void assignStoreOwnerContract(OwnerAppointmentContract contract) throws DataExistentError {
        String assigningOwner = contract.getAssigningOwner();
        String ownerToAdd = contract.getNewOwner();
        contracts.remove(contract.getId());
        contract.remove();
        synchronized (permissions) {
            if (hasPermission(ownerToAdd)) {
                throw new DataExistentError(
                        String.format("User '%s' is already have role at store '%d'.", ownerToAdd, getStoreId()),
                        ErrorStatus.MEMBMER_ALREADY_HAS_ROLE
                );
            }

            Permission newOwnerPermission = new Permission(dataStore, assigningOwner, ownerToAdd);
            newOwnerPermission.initStoreOwner();

            permissions.put(ownerToAdd, newOwnerPermission);
            addMemberToOffers(ownerToAdd);
            addMemberToContract(ownerToAdd);
        }
    }

    @Override
    public void assignStoreOwner(String assigningOwner,
                                 String ownerToAdd) throws NonExistentData, PermissionError, DataExistentError {
        checkPermission(assigningOwner, "assignStoreOwner", PermissionType.ADD_OR_REMOVE_STORE_OWNER);
        synchronized (permissions) {
            if (hasPermission(ownerToAdd)) {
                throw new DataExistentError(
                        String.format("User '%s' is already have role at store '%d'.", ownerToAdd, getStoreId()),
                        ErrorStatus.MEMBMER_ALREADY_HAS_ROLE
                );
            }

            Permission newOwnerPermission = new Permission(dataStore, assigningOwner, ownerToAdd);
            newOwnerPermission.initStoreOwner();

            permissions.put(ownerToAdd, newOwnerPermission);
            addMemberToOffers(ownerToAdd);
            addMemberToContract(ownerToAdd);
        }
    }

    @Override
    public void assignStoreManager(String assigningOwner,
                                   String managerToAdd) throws DataExistentError, PermissionError {
        checkPermission(assigningOwner, "assignStoreManager", PermissionType.MANAGE_STORE_MANAGER);
        synchronized (permissions) {
            if (hasPermission(managerToAdd)) {
                throw new DataExistentError(
                        String.format("User '%s' is already have role at store '%d'.", managerToAdd, getStoreId()),
                        ErrorStatus.MEMBMER_ALREADY_HAS_ROLE
                );
            }

            Permission newManagerPermission = new Permission(dataStore, assigningOwner, managerToAdd);
            newManagerPermission.initStoreManager();
            permissions.put(managerToAdd, newManagerPermission);
        }
    }

    private void removePermission(String userToRemove) {
        synchronized (permissions) {
            Permission toRemovePermission = permissions.remove(userToRemove);
            if (toRemovePermission == null) return;
            for (String subUser :
                    permissions.entrySet().stream()
                            .filter(e -> e.getValue().getPermissionGiverName().equals(userToRemove))
                            .map(Map.Entry::getKey)
                            .toList()
            )
                removePermission(subUser);

            if (toRemovePermission.hasPermission(PermissionType.MANAGE_OFFERS)) {
                removeMemberFromOffers(userToRemove);
            }

            if (toRemovePermission.hasPermission(PermissionType.MANAGE_CONTRACTS)) {
                removeMemberFromContract(userToRemove);
            }
            toRemovePermission.remove();
        }
    }

    /// Products

    @Override
    public Integer addNewProduct(String userName, ProductRecord productRecord) throws DataError, PermissionError {
        checkPermission(userName, "addNewProduct", PermissionType.STORAGE_MANAGEMENT);
        validateProductFields(productRecord);
        IProduct product = new Product(dataStore, productRecord);
        products.put(product.getProductId(), product);
        return product.getProductId();
    }

    @Override
    public IProduct getProduct(int productId) throws NonExistentData {
        if (!products.containsKey(productId))
            throw new NonExistentData("Product with ID " + productId +
                    " does not exist in store " + getStoreName(), ErrorStatus.PRODUCT_DOES_NOT_EXIST);
        return products.get(productId);
    }

    @Override
    public List<IProduct> getProducts() {
        return products.values().stream().toList();
    }


    @Override
    public void updateProductFields(String userName, ProductRecord updated) throws DataError, NonExistentData, PermissionError {
        checkPermission(userName, "updateProductFields", PermissionType.STORAGE_MANAGEMENT);
        validateProductFields(updated);
        int productId = updated.productId();
        IProduct product = products.get(productId);
        if (product == null)
            throw new NonExistentData("Product with ID " + productId +
                    " does not exist in store " + getStoreName(), ErrorStatus.PRODUCT_DOES_NOT_EXIST);
        product.update(updated);
    }

    @Override
    public void removeProduct(String userName, int productId) throws NonExistentData, PermissionError {
        checkPermission(userName, "removeProduct", PermissionType.STORAGE_MANAGEMENT);
        IProduct product = products.remove(productId);
        if (product != null) product.remove();
        List<Integer> discountsToRemove =
                discounts.values().stream()
                        .filter(d -> d.isDependentOnProduct(productId))
                        .map(IDiscount::getDiscountId)
                        .toList();
        for (int discountID : discountsToRemove)
            removeDiscount("System", discountID);
    }

    @Override
    public void addBasketQuantity(IStoreBasket storeBasket, Map<Integer, Integer> storeBasketCache) throws DataError {
        Map<Integer, Integer> productsAndQuantity = storeBasket.getProductsAsRecords().values()
                .stream().collect(Collectors.toMap(ProductRecord::productId, ProductRecord::quantity));
        List<Exception> exceptions = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : productsAndQuantity.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();

            try {
                IProduct product = getProduct(productId);
                product.addingProductQuantity(quantity);
            } catch (DataError | NonExistentData e) {
                exceptions.add(e);
            }
        }

        if (!exceptions.isEmpty()) {
            throw new DataError("Multiple exceptions occurred while adding basket quantity", ErrorStatus.INVALID_PRODUCT_QUANTITY);
        }
    }

    @Override
    public void removeBasketQuantity(IStoreBasket storeBasket, Map<Integer, Integer> productsCache) throws NonExistentData, DataError {
        Map<Integer, Integer> productsAndQuantity = storeBasket.getProductsAsRecords().values()
                .stream().collect(Collectors.toMap(ProductRecord::productId, ProductRecord::quantity));

        for (Map.Entry<Integer, Integer> entry : productsAndQuantity.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            reduceProductQuantityInRepo(productId, quantity);
            productsCache.put(productId, quantity);
        }
    }

    @Override
    public double calculateBasketPrice(IStoreBasket storeBasket) {
        syncBasket(storeBasket);
        applyDiscountOnBasket(storeBasket);
        return storeBasket.getBasketPriceAfterDiscount();
    }

    public void syncBasket(IStoreBasket storeBasket) {
        try {
            for (ProductRecord record : storeBasket.getProductsAsRecords().values()) {
                IProduct product = getProduct(record.productId());
                ProductRecord syncRecord = new ProductRecord(
                        getStoreId(),
                        product.getProductId(),
                        product.getProductName(),
                        product.getProductPrice(),
                        product.getProductCategory(),
                        record.quantity(),
                        product.getProductPrice(),
                        product.getProductRating()
                );
                storeBasket.updateProductRecord(syncRecord);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void addProductQuantity(String userName, int productId, int quantity) throws NonExistentData, PermissionError, DataError {
        checkPermission(userName, "addProductQuantity", PermissionType.STORAGE_MANAGEMENT);
        IProduct product = getProduct(productId);
        product.addingProductQuantity(quantity);
    }

    @Override
    public void reduceProductQuantity(String userName, int productId,
                                      int quantity) throws NonExistentData, DataError, PermissionError {
        checkPermission(userName, "reduceProductQuantity", PermissionType.STORAGE_MANAGEMENT);
        reduceProductQuantityInRepo(productId, quantity);
    }

    @Override
    public void areProductsExist(Map<Integer, ProductRecord> products) throws NonExistentData, DataError {
        for (ProductRecord productRecord : products.values()) {
            if (!products.containsKey(productRecord.productId())) {
                MarketLogger.logError("Store", "areProductsExist", "product " + productRecord.productId() + " does not exist int the repository", products);
                throw new NonExistentData("product " + productRecord.productId() + " does not exist int the repository",
                        ErrorStatus.PRODUCT_DOES_NOT_EXIST);
            } else if (productRecord.quantity() < 1) {
                MarketLogger.logError("Store", "areProductsExist", "product " + productRecord.productId() + " quantity is less then 1", productRecord);
                throw new DataError("product " + productRecord.productId() + " quantity is less then 1",
                        ErrorStatus.INVALID_PRODUCT_QUANTITY);
            } else if (productRecord.quantity() > this.products.get(productRecord.productId()).getProductQuantity()) {
                MarketLogger.logError("Store", "areProductsExist", "product " + productRecord.productId() + " quantity is more then exist in store", productRecord);
                throw new DataError("product " + productRecord.productId() + " quantity is more then exist in store",
                        ErrorStatus.INVALID_PRODUCT_QUANTITY);
            }
        }
    }

    //private methods
    private void reduceProductQuantityInRepo(int productId, int quantity) throws NonExistentData, DataError {
        IProduct product = getProduct(productId);
        product.reduceProductQuantity(quantity);
    }

    // Permissions

    @Override
    public boolean hasPermission(String username) {
        return permissions.containsKey(username);
    }

    @Override
    public void remove() {
        dataStore.remove();
    }

    @Override
    public Map<Integer, Offer> getOffers() {
        return offers;
    }

    @Override
    public String getFounderName(String userName) throws PermissionError, NonExistentData {
        checkPermission(userName, "getFounderName", PermissionType.GET_EMPLOYEES_DATA);
        for (Map.Entry<String, Permission> entry : permissions.entrySet()) {
            if (entry.getValue().isStoreFounder()) {
                return entry.getKey();
            }
        }
        MarketLogger.logError("Store", "getFounderName", String.format("store founder didnt found, store id: %d", getStoreId()), userName);
        throw new NonExistentData(
                String.format("Store '%d' does not have founder", getStoreId()),
                ErrorStatus.INVALID_STORE_FOUNDER
        );
    }

    @Override
    public Permission getMemberPermissions(String userName) throws NonExistentData {
        if (!permissions.containsKey(userName)) {
            MarketLogger.logError("Store", "getMemberPermissions", String.format("Tried to get permissions of user '%s' at store '%d' while permissions don't exist", userName, getStoreId()), userName);
            throw new NonExistentData(
                    String.format("User '%s' does not have permissions for store '%d'", userName, getStoreId()),
                    ErrorStatus.USERNAME_DOES_NOT_EXIST
            );
        }
        return permissions.get(userName);
    }

    @Override
    public Map<String, Permission> getStoreRoles(String userName) throws PermissionError {
        checkPermission(userName, "getStoreRoles", PermissionType.GET_EMPLOYEES_DATA);
        return permissions;
    }

    @Override
    public Map<String, Permission> getManagersPermissions(String userName) throws NonExistentData, PermissionError {
        checkPermission(userName, "getManagersPermissions", PermissionType.GET_EMPLOYEES_DATA);
        return permissions.entrySet().stream()
                .filter(entry -> entry.getValue().getRoleType() == RoleType.STORE_MANAGER)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public List<String> getManagersWith(PermissionType permissionType) {
        return permissions.entrySet().stream()
                .filter(e -> e.getValue().hasPermission(permissionType))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public void setManagerPermissions(String storeOwner, String storeManager, Set<PermissionType> permissions) throws NonExistentData, PermissionError {
        //todo - change function name, and change the checkPermission (CHANGE_OWNER_PERMISSIONS) - not correct
        checkPermission(storeOwner, "setManagerPermissions", PermissionType.CHANGE_OWNER_PERMISSIONS);
        Permission permissionForChangeUser = getMemberPermissions(storeManager);
        if (!permissionForChangeUser.isStoreManager()) {
            throw new PermissionError(
                    "User " + storeManager + " is not a store manager of the store " + getStoreId(),
                    ErrorStatus.NO_MANAGER_PERMISSION
            );
        }

        if (permissionForChangeUser.hasPermission(PermissionType.MANAGE_OFFERS)) {
            if (!permissions.contains(PermissionType.MANAGE_OFFERS)) {
                removeMemberFromOffers(storeManager);
            }
        } else {
            if (permissions.contains(PermissionType.MANAGE_OFFERS)) {
                addMemberToOffers(storeManager);
            }
        }

        if (permissionForChangeUser.hasPermission(PermissionType.MANAGE_CONTRACTS)) {
            if (!permissions.contains(PermissionType.MANAGE_CONTRACTS)) {
                removeMemberFromContract(storeManager);
            }
        } else {
            if (permissions.contains(PermissionType.MANAGE_CONTRACTS)) {
                addMemberToContract(storeManager);
            }
        }
        permissionForChangeUser.setPermissions(permissions);
    }

    private Permission checkPermission(String userName, String functionName, PermissionType type) throws PermissionError {
        if (userName.contains("System"))
            return null; //TODO: create singleton for "ADMIN_PERMISSIONS"
        Permission permission = null;
        try {
            permission = getMemberPermissions(userName);
        } catch (NonExistentData ignored) {
        }
        if (permission == null || !permission.hasPermission(type)) {
            MarketLogger.logError("Store", functionName, String.format("the User: %s, doesn't have permission to perform %s", userName, functionName));
            throw new PermissionError(String.format("the User: %s, doesn't have permission to perform %s", userName, functionName), ErrorStatus.NO_PERMISSION);
        }
        return permission;
    }

    //**************************************************************Transactions
    @Override
    public List<Transaction> getStoreTransactionsByDate(String userName, LocalDateTime optionalStartDateTime,
                                                        LocalDateTime optionalEndDateTime) throws PermissionError, NonExistentData {
        checkPermission(userName, "getStoreTransactions", PermissionType.GET_PURCHASE_HISTORY);
        return MarketImpl.getInstance().getStoreTransactionsBetween(getStoreId(), optionalStartDateTime, optionalEndDateTime);
    }

    //**************************************************************Discount Function

    private void applyDiscountOnBasket(IStoreBasket basket) {
        for (IDiscount discount : discounts.values())
            discount.applyDiscountOnBasket(basket);
    }

    @Override
    public int addDiscount(String userName, IDiscount discount) throws PermissionError, NonExistentData {
        checkPermission(userName, "addDiscount", PermissionType.CHANGE_STORE_POLICY);
        for (int productID : discount.getDependentProducts())
            getProduct(productID);
        for (int discountID : discount.getChildDiscountIds())
            discounts.remove(discountID);
        discount.persist(dataStore);
        discounts.put(discount.getDiscountId(), discount);
        return discount.getDiscountId();
    }


    @Override
    public void removeDiscount(String userName, int discountId) throws PermissionError {
        checkPermission(userName, "removeDiscount", PermissionType.CHANGE_STORE_POLICY);
        IDiscount toRemove = discounts.remove(discountId);
        if (toRemove != null) toRemove.remove();
    }

    @Override
    public Map<Integer, IDiscount> getStoreDiscounts() {
        return discounts;
    }

    @Override
    public IDiscount getStoreDiscount(Integer discountId) throws NonExistentData {
        if (!discounts.containsKey(discountId))
            throw new NonExistentData(
                    "No Discount found for store with ID " + getStoreId(),
                    ErrorStatus.DISCOUNT_DOES_NOT_EXIST
            );
        return discounts.get(discountId);
    }

    //**************************************************************Purchase Rules Function

    @Override
    public void checkPurchaseRules(IStoreBasket basket) throws PurchaseLimitation {
        for (PurchaseRule rule : purchaseRules.values()) {
            rule.checkCondition(basket);
        }
    }

    @Override
    public int addPurchaseRule(String userName, PurchaseRule rule) throws PermissionError {
        checkPermission(userName, "addPurchaseRule", PermissionType.CHANGE_STORE_POLICY);
        rule.persist(dataStore);
        purchaseRules.put(rule.getId(), rule);
        return rule.getId();
    }


    @Override
    public void removePurchaseRule(String userName, int purchaseRuleId) throws PermissionError {
        checkPermission(userName, "removePurchaseRule", PermissionType.CHANGE_STORE_POLICY);
        PurchaseRule toRemove = purchaseRules.remove(purchaseRuleId);
        if (toRemove != null) toRemove.remove();
    }

    @Override
    public Map<Integer, PurchaseRule> getStorePurchaseRules() {
        return purchaseRules;
    }

    @Override
    public Map<String, Permission> getPermissions() {
        return permissions;
    }

    private void validateProductFields(ProductRecord productRecord) throws DataError {
        if (productRecord.productPrice() < 0) {
            throw new DataError("Product price cannot be negative", ErrorStatus.INVALID_PRODUCT_PRICE);
        }
        if (productRecord.productName().equals("")) {
            throw new DataError("product name cannot be empty", ErrorStatus.INVALID_PRODUCT_NAME);
        }
    }

    //***************************************************************offers
    @Override
    public Offer publishMemberOffer(String offeringMember, int productID, double offeredPrice, int offeredQuantity) throws NonExistentData {
        ProductRecord product = new ProductRecord(getProduct(productID));
        Set<String> ownersToConsent =
                permissions.entrySet()
                        .stream()
                        .filter(e -> e.getValue().hasPermission(PermissionType.MANAGE_OFFERS))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());

        Offer offer = new Offer(offeringMember, product, offeredPrice, offeredQuantity, ownersToConsent);
        this.offers.put(offer.getId(), offer);
        return offer;
    }

    @Override
    public void removeOffer(String userName, int offerId) throws PermissionError {
        checkPermission(userName, "removeOffer", PermissionType.MANAGE_OFFERS);
        Offer offer = offers.remove(offerId);
        if (offer != null) offer.remove();
    }

    @Override
    public void consentOffer(String userName, int offerId) throws PermissionError {
        checkPermission(userName, "consentOffer", PermissionType.MANAGE_OFFERS);
        offers.get(offerId).setStoreConsent(userName, true);
    }

    @Override
    public Offer getProductOffer(int offerId) throws NonExistentData {
        if (!offers.containsKey(offerId)) {
            throw new NonExistentData(
                    String.format("No offer with ID '%s' in store '%s'", offerId, getStoreName()),
                    ErrorStatus.OFFER_DOES_NOT_EXIST
            );
        }
        return offers.get(offerId);
    }

    @Override
    public Map<Integer, Offer> getOffers(String userName) throws PermissionError {
        checkPermission(userName, "removeOffer", PermissionType.MANAGE_OFFERS);
        return offers;
    }

    @Override
    public void counterOffer(String userName, int offerId, int productQuantity, double productPrice) throws PermissionError, NonExistentData, DataError {
        checkPermission(userName, "counterOffer", PermissionType.MANAGE_OFFERS);
        Offer productOffer = getProductOffer(offerId);
        if (productOffer.getOfferedPrice() > productPrice)
            throw new IllegalArgumentException("counter offer price cant be higher than original offer");
        productOffer.updateOffer(productPrice, productQuantity);
    }

    private void removeMemberFromOffers(String ownerToRemove) {
        for (Offer offer : offers.values()) {
            offer.removeFromConsent(ownerToRemove);
        }
    }

    private void addMemberToOffers(String ownerToAdd) {
        for (Offer offer : offers.values()) {
            offer.addToConsent(ownerToAdd);
        }
    }

    //***************************************************************offers
    @Override
    public OwnerAppointmentContract publishMemberContract(String assigningOwner, String newOwner, String contract) throws NonExistentData, PermissionError, DataExistentError {
        Set<String> ownersToConsent =
                permissions.entrySet()
                        .stream()
                        .filter(e -> e.getValue().hasPermission(PermissionType.MANAGE_CONTRACTS))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());

        checkPermission(assigningOwner, "publishMemberContract", PermissionType.ADD_OR_REMOVE_STORE_OWNER);
        checkPermission(assigningOwner, "publishMemberContract", PermissionType.MANAGE_CONTRACTS);
        synchronized (contracts) {
            if (hasActiveContract(newOwner)) {
                throw new DataExistentError(
                        String.format("User '%s' is already have active contract with this store '%d'.", newOwner, getStoreId()),
                        ErrorStatus.MEMBMER_ALREADY_HAS_CONTRACT
                );
            }
        }
        synchronized (permissions) {
            if (hasPermission(newOwner)) {
                throw new DataExistentError(
                        String.format("User '%s' is already have role at store '%d'.", newOwner, getStoreId()),
                        ErrorStatus.MEMBMER_ALREADY_HAS_ROLE
                );
            }
        }
        OwnerAppointmentContract ownerAppointmentContract = new OwnerAppointmentContract(getStoreId(), assigningOwner, newOwner, contract, ownersToConsent);
        contracts.put(ownerAppointmentContract.getId(), ownerAppointmentContract);
        consentContract(assigningOwner, ownerAppointmentContract.getId());
        return ownerAppointmentContract;
    }

    private boolean hasActiveContract(String newOwnerUserName) {
        for (OwnerAppointmentContract contract : contracts.values()) {
            if (contract.newOwner.equals(newOwnerUserName))
                return true;
        }
        return false;
    }

    @Override
    public void removeContract(String userName, int contractId) throws PermissionError {
        checkPermission(userName, "removeContract", PermissionType.MANAGE_CONTRACTS);
        OwnerAppointmentContract contract = contracts.remove(contractId);
        contract.remove();
    }

    @Override
    public OwnerAppointmentContract consentContract(String userName, int contractId) throws PermissionError, DataExistentError {
        checkPermission(userName, "consentContract", PermissionType.MANAGE_CONTRACTS);
        OwnerAppointmentContract contract = contracts.get(contractId);
        synchronized (contract) {
            contract.setStoreConsent(userName, true);
            if (contract.isStoreConsent())
                assignStoreOwnerContract(contract);
        }
        return contract;
    }

    @Override
    public OwnerAppointmentContract getContract(String userName, int contractId) throws NonExistentData, PermissionError {
        checkPermission(userName, "OwnerAppointmentContract", PermissionType.MANAGE_CONTRACTS);
        if (!contracts.containsKey(contractId)) {
            throw new NonExistentData(
                    String.format("No contract with ID '%s' in store '%s'", contractId, getStoreName()),
                    ErrorStatus.CONTRACT_DOES_NOT_EXIST
            );
        }
        return contracts.get(contractId);
    }

    @Override
    public Map<Integer, OwnerAppointmentContract> getContracts(String userName) throws PermissionError {
        checkPermission(userName, "getContracts", PermissionType.MANAGE_CONTRACTS);
        return contracts;
    }

    private void removeMemberFromContract(String ownerToRemove) {
        for (OwnerAppointmentContract contract : contracts.values()) {
            contract.removeFromConsent(ownerToRemove);
        }
    }

    private void addMemberToContract(String ownerToAdd) {
        for (OwnerAppointmentContract contract : contracts.values()) {
            contract.addToConsent(ownerToAdd);
        }
    }

    @Override
    public String toString() {
        return "Store{" +
                "storeId=" + getStoreId() +
                ", storeName='" + getStoreName() + '\'' +
                ", storeRating=" + getStoreRating() +
                ", storeDescription='" + getStoreDescription() + '\'' +
                ", isActive=" + isActive() +
                '}';
    }
}
