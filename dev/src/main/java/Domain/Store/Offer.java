package Domain.Store;

import DataLayer.Store.ORM.DataOffer;
import DataLayer.Store.ORM.DataOfferConsent;
import DataLayer.Store.ORM.DataProduct;
import util.Enums.ErrorStatus;
import util.Exceptions.DataError;
import util.Records.StoreRecords.ProductRecord;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Offer {

    private ProductRecord product;

    private final String offeringMember;

    private final Map<String, Boolean> storeConsent;  // <Manager name, doesConsent>

    private DataOffer dataOffer;

    // CTOR for data creation
    public Offer(String offeringMember, ProductRecord product, double offeredPrice, int offeredQuantity, Set<String> ownersToConsent) {
        dataOffer = new DataOffer(offeringMember, product, offeredPrice, offeredQuantity, ownersToConsent);
        dataOffer = dataOffer.persist();
        this.offeringMember = offeringMember;
        this.product = product.updatePrice(offeredPrice).updateQuantity(offeredQuantity);
        storeConsent = new ConcurrentHashMap<>();
        for (String owner : ownersToConsent)
            storeConsent.put(owner, false);
    }

    // CTOR for data recovery
    public Offer(DataOffer dataOffer){
        this.dataOffer = dataOffer;
        DataProduct dataProduct = dataOffer.getKey().getProduct();
        product =
                new ProductRecord(
                        dataProduct.getKey().getStore().getId(),
                        dataProduct.getKey().getId(),
                        dataProduct.getName(),
                        dataOffer.getOfferedPrice(),
                        dataProduct.getCategory(),
                        dataOffer.getOfferedQuantity(),
                        dataOffer.getOfferedPrice(),
                        dataProduct.getRating()
                );
        offeringMember = dataOffer.getKey().getOfferingMember().getUsername();
        storeConsent = new ConcurrentHashMap<>();
        for (Map.Entry<String, DataOfferConsent> entry: dataOffer.getConsents().entrySet())
            storeConsent.put(entry.getKey(), entry.getValue().isConsent());
    }

    public void updateOffer(double newPrice, int newQuantity) throws DataError {
        if (newPrice < 0)
            throw new DataError(
                    "Cannot offer negative price",
                    ErrorStatus.INVALID_PRODUCT_PRICE
            );
        if (newQuantity <= 0)
            throw new DataError(
                    "Offer's quantity must be positive",
                    ErrorStatus.NEGATIVE_QUANTITY
            );
        dataOffer.update(newPrice, newQuantity);
        dataOffer = dataOffer.persist();
        product = product.updatePrice(newPrice).updateQuantity(newQuantity);
        storeConsent.replaceAll((key, value) -> false);
    }

    public int getId() {
        return dataOffer.getKey().getId();
    }

    public String getOfferingMember() {
        return offeringMember;
    }

    public ProductRecord getProduct() {
        return product;
    }

    public double getOfferedPrice() {
        return dataOffer.getOfferedPrice();
    }

    public int getOfferedQuantity() {
        return dataOffer.getOfferedQuantity();
    }

    public LocalDateTime getTimeOfOffer() {
        return dataOffer.getTimeOfOffer();
    }

    public Map<String, Boolean> getStoreConsent() {
        return storeConsent;
    }

    public Boolean isStoreConsent() {
        for (Map.Entry<String, Boolean> entry : storeConsent.entrySet())
            if (!entry.getValue())
                return false;
        return true;
    }

    public void setStoreConsent(String userName, boolean isConsent) {
        dataOffer.updateConsent(userName, isConsent);
        storeConsent.replace(userName, isConsent);
    }

    public void remove() {
        dataOffer.remove();
    }

    public void removeFromConsent(String ownerToRemove) {
        dataOffer.removeFromConsent(ownerToRemove);
        storeConsent.remove(ownerToRemove);
    }

    public void addToConsent(String ownerToAdd) {
        dataOffer.addToConsent(ownerToAdd);
        storeConsent.putIfAbsent(ownerToAdd, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Offer offer = (Offer) o;
        return getId() == offer.getId() &&
                product.storeId() == offer.product.storeId() &&
                product.productId() == offer.product.productId() &&
                getOfferedPrice() == offer.getOfferedPrice() &&
                getOfferedQuantity() == offer.getOfferedQuantity() &&
                Objects.equals(offeringMember, offer.offeringMember) &&
                Objects.equals(storeConsent, offer.storeConsent) &&
                Objects.equals(getTimeOfOffer().truncatedTo(ChronoUnit.SECONDS),
                        offer.getTimeOfOffer().truncatedTo(ChronoUnit.SECONDS));
    }

    @Override
    public String toString() {
        StringBuilder consentState = new StringBuilder("Consents=[");
        for (Map.Entry<String, Boolean> entry: storeConsent.entrySet())
            consentState.append(String.format("{%s, %s},", entry.getKey(), entry.getValue()));
        consentState.append("]");
        return
                String.format("Offer[storeID=%d, productID=%d, offeringMember=%s, offeredPrice=%f, offeredQuantity=%d, timeOfOffer=%s, %s",
                        product.storeId(),
                        product.productId(),
                        offeringMember,
                        getOfferedPrice(),
                        getOfferedQuantity(),
                        getTimeOfOffer(),
                        consentState
                );
    }
}
