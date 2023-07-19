package Service;

import Domain.Permission;
import Domain.Store.Discount.IDiscount;
import util.Exceptions.PurchaseLimitation;
import Domain.Store.IProduct;
import Domain.Store.IStoreController;
import Domain.Store.Offer;
import Domain.Store.Purchase.PurchaseRule;
import util.Enums.ErrorStatus;
import util.Enums.PermissionType;
import util.Enums.RoleType;
import util.Exceptions.*;
import util.Records.StoreRecords.ProductFilterAttributes;
import util.Records.StoreRecords.ProductRecord;
import util.Records.StoreRecords.StoreRecord;

import java.util.*;
import java.util.stream.Collectors;

public class StoreService {
    private final IStoreController controller;

    public StoreService(IStoreController controller){
        this.controller = controller;
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>STORE_DOES_NOT_EXIST
     */
    public Result<StoreRecord> getStoreInfo(int storeId) {
        try {
            return Result.makeGood(controller.getStoreInfo(storeId));
        }
        catch (NonExistentData e) {
            return Result.makeBad(e);
        }
    }

    public Result<List<ProductRecord>> getProductsBy(ProductFilterAttributes filters) {
        return Result.makeGood(controller.getFilteredProducts(filters).stream().map(ProductRecord::new).collect(Collectors.toList()));
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     */
    public Result<ProductRecord> getProductInfo(String sessionId, int productId, int storeId) {
        try {
            //TODO: implement this filter at controller level
            Optional<IProduct> product = controller.getStoreProducts(sessionId, storeId).stream().filter(p -> p.getProductId() == productId).findFirst();
            return product.map(p -> Result.makeGood(new ProductRecord(p)))
                    .orElseGet(() -> Result.makeBad(ErrorStatus.PRODUCT_DOES_NOT_EXIST, "No product with ID '" + productId + "' in store '" + storeId + "'"));
            //TODO: implement product info getter
        } catch (NonExistentData | PermissionError | SessionError e) {
            return Result.makeBad(e);
        }
    }

    public Result<List<ProductRecord>> getStoreProducts(String sessionId, int storeId) {
        try{
            List<IProduct> products = controller.getStoreProducts(sessionId, storeId);
            return Result.makeGood(products.stream().map(ProductRecord::new).collect(Collectors.toList()));
        } catch (NonExistentData | PermissionError | SessionError e) {
            return Result.makeBad(e);
        }
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>GUEST_SESSION
     */
    public Result<Integer> openStore(String sessId, StoreRecord store) {
        try {
            return Result.makeGood(controller.openNewStore(sessId, store));
        } catch (SessionError e) {
            return Result.makeBad(e);
        }
    }

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
        try {
            return Result.makeGood(controller.addNewProduct(sessId, product));
        } catch (NonExistentData | DataError | PermissionError | SessionError e) {
            return Result.makeBad(e);
        }
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    public Result removeProduct(String sessId, int storeId, int productId) {
        try {
            controller.removeProduct(sessId, storeId, productId);
        } catch (NonExistentData | PermissionError | SessionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

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
        try {
            controller.updateProductFields(sessId, updatedProduct);
        } catch (SessionError | NonExistentData | DataError | PermissionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     * <br>INVALID_PRODUCT_QUANTITY
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    public Result addProductQuantity(String sessId, int storeId, int productId, int quantity) {
        try {
            controller.addProductQuantity(sessId, storeId, productId, quantity);
        } catch (NonExistentData | PermissionError | SessionError | DataError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>PRODUCT_DOES_NOT_EXIST
     * <br>INVALID_PRODUCT_QUANTITY
     * <br>NO_PERMISSION_STORAGE_MANAGEMENT
     */
    public Result removeProductQuantity(String sessId, int storeId, int productId, int quantity) {
        try {
            controller.reduceProductQuantity(sessId, storeId, productId, quantity);
        } catch (SessionError | NonExistentData | DataError | PermissionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_ADD_OR_REMOVE_STORE_OWNER
     * <br>USERNAME_DOES_NOT_EXIST
     * <br>MEMBMER_ALREADY_HAS_ROLE
     */
    public Result appointOwner(String sessId, String uname, int storeId) {
        try {
            controller.assignStoreOwner(sessId, uname, storeId);
        } catch (NonExistentData | PermissionError | SessionError | DataExistentError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_ADD_OR_REMOVE_STORE_MANAGER
     * <br>USERNAME_DOES_NOT_EXIST
     * <br>MEMBMER_ALREADY_HAS_ROLE
     */
    public Result appointManager(String sessId, String uname, int storeId) {
        try {
            controller.assignStoreManager(sessId, uname, storeId);
        } catch (NonExistentData | PermissionError | SessionError | DataExistentError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_GET_EMPLOYEES_DATA
     * <br>USERNAME_DOES_NOT_EXIST
     */
    public Result<Integer> getManagerPermissions(String sessId, String uname, int storeId) {
        try {
            Map<String, Permission> userToPerm = controller.getManagersPermissions(sessId, storeId);
            return Result.makeGood(userToPerm.get(uname).permissionsBitMap());
        } catch (NonExistentData | PermissionError | SessionError e) {
            return Result.makeBad(e);
        }
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_GET_EMPLOYEES_DATA
     */
    public Result<Map<String,Integer>> getManagersPermissions(String sessId, int storeId) {
        try {
            Map<String, Permission> userToPerm = controller.getManagersPermissions(sessId, storeId);
            Map<String, Integer> resultMap = userToPerm.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().permissionsBitMap()
                    ));
            return Result.makeGood(resultMap);
        } catch (NonExistentData | PermissionError | SessionError e) {
            return Result.makeBad(e);
        }
    }


    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>USERNAME_DOES_NOT_EXIST
     * <br>NO_PERMISSION_CHANGE_OWNER_PERMISSIONS
     */
    public Result modifyPermissionsFor(String sessId, String uname, int storeId, int newPerms) {
        try {
            controller.setManagerPermissions(sessId, uname, storeId, PermissionType.bitmapToSet(newPerms));
        } catch (NonExistentData | PermissionError | SessionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_MAKE_STORE_INACTIVE
     */
    public Result closeStore(String sessId, int storeId) {
        try {
            controller.closeStore(sessId, storeId);
        } catch (NonExistentData | PermissionError | SessionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION
     */
    public Result closeStorePermanently(String sessId, int storeId) {
        try {
            controller.closeStorePermanently(sessId, storeId);
        } catch (NonExistentData | PermissionError | SessionError e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_GET_EMPLOYEES_DATA
     */
    public Result<List<String>> getStoreOwners(String sessId, int storeId) {
        try {
            Map<String, Permission> userToPerm = controller.getStoreRoles(sessId, storeId);
            List<String> owners = new ArrayList<>();
            for(String user : userToPerm.keySet())
                if (userToPerm.get(user).isStoreOwner())
                    owners.add(user);
            return Result.makeGood(owners);
        } catch (NonExistentData | PermissionError | SessionError e) {
            return Result.makeBad(e);
        }
    }

    public Result<String> getStoreFounder(String sessId, int storeId) {
        String funder = null;
        try {
            funder = controller.getStoreFounder(sessId, storeId);
        } catch (PermissionError | SessionError | NonExistentData e) {
            return Result.makeBad(e);
        }
        return Result.makeGood(funder);
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     * <br>NO_PERMISSION_GET_EMPLOYEES_DATA
     */
    public Result<List<String>> getStoreManagers(String sessId, int storeId) {
        try {
            Map<String, Permission> userToPerm = controller.getStoreRoles(sessId, storeId);
            List<String> managers = new ArrayList<>();
            for(String user : userToPerm.keySet())
                if (userToPerm.get(user).isStoreManager())
                    managers.add(user);
            return Result.makeGood(managers);
        } catch (NonExistentData | PermissionError | SessionError e) {
            return Result.makeBad(e);
        }
    }

    /**
     * @implNote Possible expected failure codes:
     * <br>SESSION_ID_DOES_NOT_EXIST
     * <br>STORE_DOES_NOT_EXIST
     */
    public Result updateStore(String sessId, StoreRecord updatedStore) {
        try {
            controller.updateStoreFields(sessId, updatedStore);
        } catch (PermissionError | SessionError | NonExistentData e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result<List<StoreRecord>> getMyStores(String sessionId) {
        try {
            return Result.makeGood(controller.getMyStores(sessionId));
        } catch (NonExistentData | SessionError e) {
            return Result.makeBad(e);
        }
    }

    public Result<Set<StoreRecord>> getStores() {
        return Result.makeGood(new HashSet<>(controller.getStores()));
    }


    public Result<Set<Integer>>
    getStoresAccordingToRole(String sessionId, RoleType role) {
        try {
            return Result.makeGood(
                    controller.getStoresAccordingToRole(sessionId, role));
        } catch (MarketException e) {
            return Result.makeBad(e);
        }
    }


    public Result<Integer> getMemberPermissions(String sessionId, Integer storeId) {
        try {
            Permission memberPermissions = controller.getMemberPermissions(sessionId, storeId);
            return Result.makeGood(memberPermissions.permissionsBitMap());
        } catch (NonExistentData | SessionError e) {
            return Result.makeBad(e);
        }
    }

    public Result removeDiscount(String session, int storeID, int discountId) {
        try {
            controller.removeDiscount(session, storeID, discountId);
        }catch (MarketException e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result checkCartPurchaseRules(String session) {
        try {
            controller.checkCartPurchaseRules(session);
        }
        catch (MarketException e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result removePurchaseRule(String session, int storeID, int purchaseRuleID) {
        try {
            controller.removePurchaseRule(session, storeID, purchaseRuleID);
        }
        catch (MarketException e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result removeStoreOwner(String sessionId, String username, Integer storeId) {
        try {
            controller.removeStoreOwnerAppointment(sessionId, username, storeId);
        }
        catch (MarketException e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result reopenStore(String sessionId, int storeId) {
        try {
            controller.reopenStore(sessionId, storeId);
        }
        catch (Exception e){
            return Result.makeBad(ErrorStatus.NO_ERROR, e.getMessage());
        }
        return Result.makeGood();
    }

    public Result removeStoreManager(String sessionId, String username, Integer storeId) {
        try {
            controller.removeStoreManagerAppointment(sessionId, username, storeId);
        }
        catch (MarketException e)
        {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result addDiscount(String session, int storeId, IDiscount discount) {
        try {
            controller.addDiscount(session, storeId, discount);
        }catch (MarketException e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result<List<IDiscount>> getDiscounts(String session, int storeID) {
        try{
            return Result.makeGood(controller.getDiscounts(session, storeID));
        }catch (MarketException e) {
            return Result.makeBad(e);
        }
    }

    public Result<IDiscount> getDiscount(String sessionId, Integer storeId, Integer discountId) {
        try{
            return Result.makeGood(controller.getDiscount(sessionId, storeId, discountId));
        }catch (MarketException e) {
            return Result.makeBad(e);
        }
    }

    public Result addPurchaseRule(String session, int storeId, PurchaseRule rule) {
        try {
            controller.addPurchaseRule(session, storeId, rule);
        }catch (MarketException e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result<List<PurchaseRule>> getStorePurchaseRules(String session, int storeID) {
        try {
            return Result.makeGood(controller.getStorePurchaseRules(session, storeID));
        }catch (MarketException e){
            return Result.makeBad(e);
        }
    }

    public Result consentOffer(String sessionId, int offerId, int storeId) {
        try {
            controller.consentOffer(sessionId, offerId, storeId);
        }catch (MarketException e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result<Map<Integer, Offer>> getOffers(String sessionId, int storeId) {
        try{
            return Result.makeGood(controller.getOffers(sessionId, storeId));
        }catch (MarketException e){
            return Result.makeBad(e);
        }
    }

    public Result counterOffer(String sessionId, int storeId, int offerId, int productQuantity, double productPrice) {
        try{
            controller.counterOffer(sessionId, storeId, offerId, productQuantity, productPrice);
        }catch (MarketException e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result<Set<PermissionType>> getUserPermissionsTypes(String session, String userName, Integer storeID) {
        try {
            return Result.makeGood(controller.getUserPermissionsTypes(session, userName, storeID));
        } catch (NonExistentData e) {
            return Result.makeBad(e);
        }
    }

    public Result publishMemberContract(String session, int storeId, String newOwnerUserName, String contract) {
        try {
            return Result.makeGood(controller.publishMemberContract(session, storeId, newOwnerUserName, contract));
        } catch (MarketException e) {
            return Result.makeBad(e);
        }
    }

    public Result removeContract(String session, int storeId, int contractId) {
        try {
            controller.removeContract(session, storeId, contractId);
        }catch (MarketException e){
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result consentContract(String session, int storeId, int contractId) {
        try {
            controller.consentContract(session, storeId, contractId);
        } catch (MarketException e) {
            return Result.makeBad(e);
        }
        return Result.makeGood();
    }

    public Result getContracts(String session, int storeId) {
        try {
            return Result.makeGood(controller.getContracts(session, storeId));
        } catch (MarketException e) {
            return Result.makeBad(e);
        }
    }
}

