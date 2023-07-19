package util.Records.StoreRecords;

import DataLayer.Store.ORM.DataStore;
import Domain.Store.IStore;

import java.util.Objects;

public record StoreRecord(int storeId, String storeName, double storeRating, String storeDescription, boolean isActive) {

    public StoreRecord {
        Objects.requireNonNull(storeName);
        Objects.requireNonNull(storeDescription);
    }

    /**
     * Constructor only for information needed to open the store.
     */
    public StoreRecord(String storeName, String storeDescription){
        this(-1, storeName, -1, storeDescription, true);
    }

    /**
     * Constructor only for information needed to update the store.
     */
    public StoreRecord(int storeId, String storeName, String storeDescription, boolean isActive){
        this(storeId, storeName, -1, storeDescription, isActive);
    }

    public StoreRecord(IStore store){
        this(store.getStoreId(), store.getStoreName(), store.getStoreRating(), store.getStoreDescription(), store.isActive());
    }

    public StoreRecord(DataStore store) {
        this(store.getId(), store.getName(), store.getRating(), store.getDescription(), store.isActive_state());
    }
}
