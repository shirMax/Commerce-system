package Domain.User;

import DataLayer.ORM.DataPermission;
import DataLayer.User.ORM.DataMember;
import DataLayer.User.ORM.DataMemberAddress;
import Domain.Store.Offer;
import util.Enums.ErrorStatus;
import util.Exceptions.DataError;
import util.Exceptions.DataExistentError;
import util.Exceptions.NonExistentData;
import util.Exceptions.SessionError;
import util.Records.AddressRecord;
import util.Records.StoreRecords.ProductRecord;
import util.Records.UserRecords.UserRecord;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Member extends User {
    private DataMember dataMember;
    private UserCart userCart;
    private final Map<Integer, MemberAddress> addresses; // <address_id, address>
    private final Set<Integer> storesWithRole;
    private final Map<Integer, Offer> offers; // <offer_id, offer>
    private boolean isLogged;

    // CTOR for data creation
    public Member(String sessionId, UserRecord userData, String password) {
        super(sessionId);
        dataMember = new DataMember(userData, encryptPass(password));
        dataMember = dataMember.persist();
        this.userCart = new UserCart(dataMember);
        addresses = new ConcurrentHashMap<>();
        storesWithRole = new HashSet<>();
        offers = new ConcurrentHashMap<>();
    }

    // CTOR for data pulled from DB
    public Member(DataMember dataMember) {
        super("");
        this.dataMember = dataMember;
        this.userCart = new UserCart(dataMember.getCart());
        addresses = new ConcurrentHashMap<>();
        storesWithRole = new HashSet<>();
        offers = new ConcurrentHashMap<>();

        for (Map.Entry<Integer, DataMemberAddress> entry : dataMember.getAddresses().entrySet())
            addresses.put(entry.getKey(), new MemberAddress(entry.getValue()));
        for (DataPermission permission :dataMember.getPermissions())
            storesWithRole.add(permission.getKey().getStore().getId());
    }

    @Override
    public String toString() {
        return "Member{" +
                "userName='" + getUserName() + '\'' +
                ", email='" + dataMember.getEmail() + '\'' +
                ", phoneNumber='" + dataMember.getPhone_no() + '\'' +
                ", birthday=" + dataMember.getBirthday() +
                ", isLoggedIn=" + isLoggedIn() +
                '}';
    }

    public synchronized void login(String sessionId, String password) throws SessionError, NoSuchAlgorithmException {
        if (isLoggedIn())
            throw new SessionError("the user is already logged in!", ErrorStatus.ALREADY_LOGGED_IN);
        if (!verifyPassword(password))
            throw new SessionError("password is incorrect!", ErrorStatus.USERNAME_PASSWORD_MISMATCH);
        isLogged = true;
        setSessionId(sessionId);
    }

    public synchronized void logout() throws SessionError {
        if (!isLoggedIn())
            throw new SessionError("the user is already disconnected!", ErrorStatus.NOT_LOGGED_IN);
        isLogged = false;
        setSessionId("");
    }

    public void changePassword(String oldPassword, String newPassword) throws DataError, NoSuchAlgorithmException {
        if (!verifyPassword(oldPassword))
            throw new DataError("old password is incorrect!", ErrorStatus.INVALID_PASSWORD);
        setPassword(newPassword);
        dataMember = dataMember.persist();
    }

    public boolean validateRightPassword(String pass) throws NoSuchAlgorithmException {
        return verifyPassword(pass);
    }

    public String getUserName() {
        return dataMember.getUsername();
    }

    @Override
    public void
    addProductsToStoreBasket(int storeId,
                             List<ProductRecord> products) {
        getUserCart().addProducts(storeId, products);
    }

    @Override
    public void removeProductFromStoreBasket(int storeId, int productId, int quantity) throws NonExistentData {
        getUserCart().removeProduct(storeId, productId, quantity);
    }

    @Override
    public void updateProductQuantityInStoreBasket(int storeId, int productId,
                                                   int quantity) throws NonExistentData {
        getUserCart().updateProductQuantity(storeId, productId, quantity);
    }

    @Override
    public List<IStoreBasket> getStoreBaskets() {
        return getUserCart().getStoreBaskets();
    }

    @Override
    public IStoreBasket getStoreBasket(int storeID) {
        return getUserCart().getStoreBasket(storeID);
    }

    @Override
    public Map<Integer, ProductRecord> getStoreBasketProducts(int storeId) {
        return getUserCart()
                .getStoreBasket(storeId)
                .getProducts()
                .values().stream()
                .map(IBaskedProduct::getAsRecord)
                .collect(Collectors.toMap(ProductRecord::productId, Function.identity()));
    }

    @Override
    public UserCart getUserCart() {
        return userCart;
    }

    @Override
    public void removeUserCart() {
        dataMember = dataMember.cleanCart();
        userCart = new UserCart(dataMember.getCart());
    }

    public int addAddress(AddressRecord addressData) {
        MemberAddress address = new MemberAddress(dataMember, addressData);
        if (addresses.containsKey(address.getId()))
            throw new RuntimeException("Two addresses with same ID");
        if (addresses.isEmpty())
            address.setPrimary(true);
        addresses.put(address.getId(), address);
        return address.getId();
    }

    public synchronized boolean isLoggedIn() {
        return isLogged;
    }

    private void setPassword(String password) throws NoSuchAlgorithmException {
        dataMember.setPassword(encryptPass(password));
    }

    public boolean verifyPassword(String password) throws NoSuchAlgorithmException {
        String passwordHash = encryptPass(password);
        return passwordHash.equals(dataMember.getPassword());
    }

    private String encryptPass(String pass) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ignored) {
        }
        assert digest != null;
        byte[] hash = digest.digest(pass.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte hashByte : hash) {
            String hex = Integer.toHexString(0xff & hashByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public void remove() {
        dataMember.remove();
    }

    public String getEmail() {
        return dataMember.getEmail();
    }

    public String getPhoneNumber() {
        return dataMember.getPhone_no();
    }

    public LocalDate getBirthday() {
        return dataMember.getBirthday();
    }

    public List<MemberAddress> getAllMemberAddresses() {
        return addresses.values().stream().toList();
    }

    public MemberAddress getAddress(int addressId) throws NonExistentData {
        if (!addresses.containsKey(addressId))
            throw new NonExistentData(String.format(
                    "Member %s has no address with ID '%d'", getUserName(), addressId),
                    ErrorStatus.ADDRESS_DOES_NOT_EXISTS
            );
        return addresses.get(addressId);
    }

    public void setPrimaryAddress(int addressId) throws NonExistentData {
        MemberAddress newPrimary = getAddress(addressId);
        for (MemberAddress userAddress : addresses.values()) {
            if (userAddress.getPrimary())
                userAddress.setPrimary(false);
        }
        newPrimary.setPrimary(true);
    }


    public MemberAddress getPrimaryAddress() throws NonExistentData {
        for (MemberAddress userAddress : addresses.values()) {
            if (userAddress.getPrimary())
                return userAddress;
        }
        throw new NonExistentData(
                "Member '" + getUserName() +"' does not have a primary address!",
                ErrorStatus.PRIMARY_ADDRESS_DOES_NOT_EXISTS
        );
    }


    public void removeAddress(int addressId) {
        MemberAddress address = addresses.remove(addressId);
        if (address != null) address.remove();
    }

    //************************************************bid
    public void addOffer(Offer offer) {
        offers.put(offer.getId(), offer);
    }

    public void removeOffer(Integer offerId) {
        Offer offer = offers.remove(offerId);
        if (offer != null) offer.remove();
    }

    public Map<Integer, Offer> getOffers() {
        return offers;
    }


    /// Roles

    public void addRole(int storeId) throws DataExistentError {
        if (storesWithRole.contains(storeId))
            throw new DataExistentError(
                    "the store already has a role for the user!",
                    ErrorStatus.MEMBMER_ALREADY_HAS_ROLE
            );
        storesWithRole.add(storeId);
    }

    public List<Integer> getMemberStores() {
        return storesWithRole.stream().toList();
    }

    public void removeRole(int storeId) throws NonExistentData {
        if (!storesWithRole.remove(storeId))
            throw new NonExistentData(
                    "the store doesn't have any role for the user!",
                    ErrorStatus.MEMBER_ROLE_NOT_EXISTS
            );
    }
}
