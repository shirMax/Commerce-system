package DataLayer.Store;

import DataLayer.DbConfig;
import DataLayer.Store.ORM.DataStore;
import Domain.MarketLogger;
import Domain.Store.IStore;
import Domain.Store.Store;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import util.Enums.ErrorStatus;
import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.StoreRecord;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StoreRepo implements IStoreRepo {
    private final Map<Integer, IStore> stores;

    public StoreRepo() {
        stores = new ConcurrentHashMap<>();
    }

    @Override
    public List<IStore> getStores() {
        if (!DbConfig.shouldPersist())
            return stores.values().stream().toList();

        List<DataStore> dataStores;
        try (Session session = DbConfig.getSessionFactory().openSession()) {
            Query<DataStore> query = session.createQuery("FROM DataStore", DataStore.class);
            dataStores = query.list();
            for (DataStore dataStore : dataStores)
                if (!stores.containsKey(dataStore.getId()))
                    stores.put(dataStore.getId(), new Store(dataStore));
        }
        return stores.values().stream().toList();
    }

    @Override
    public IStore getStore(int storeId) throws NonExistentData {
        pullDataIfAbsent(storeId);
        IStore store = stores.get(storeId);
        if (store == null) {
            throw new NonExistentData("Store not found for ID: " + storeId, ErrorStatus.STORE_DOES_NOT_EXIST);
        }
        return store;
    }

    @Override
    public int openNewStore(String founder, StoreRecord storeDetails) {
        IStore store = new Store(founder, storeDetails);
        if (stores.containsKey(store.getStoreId())) {
            MarketLogger.logError("StoreRepo", "openNewStore", "New store got an ID of an existing store");
            store.remove();
            throw new RuntimeException("Couldn't get unique ID of store");
        }
        stores.put(store.getStoreId(), store);
        return store.getStoreId();
    }

    @Override
    public void removeStore(int storeId) throws NonExistentData {
        pullDataIfAbsent(storeId);
        IStore store = stores.remove(storeId);
        ;
        if (store == null) {
            throw new NonExistentData("Cannot close store", ErrorStatus.STORE_DOES_NOT_EXIST);
        }
        store.remove();
    }

    //For tests
    public Map<Integer, IStore> getStoreMap() {
        if (!DbConfig.shouldPersist()) return stores;

        List<DataStore> dataStores;
        try (Session session = DbConfig.getSessionFactory().openSession()) {
            Query<DataStore> query = session.createQuery("FROM DataStore", DataStore.class);
            dataStores = query.list();
            return dataStores.stream()
                    .map(Store::new)
                    .collect(Collectors.toMap(Store::getStoreId, Function.identity()));
        }
    }

    private void pullDataIfAbsent(int storeId) {
        if (!DbConfig.shouldPersist() || stores.containsKey(storeId)) return;

        synchronized (stores) {
            if (stores.containsKey(storeId))
                return;
            DataStore dataStore;
            Store store;
            try (Session session = DbConfig.getSessionFactory().openSession()) {
                dataStore = session.get(DataStore.class, storeId);
                if (dataStore == null) return;;
                store = new Store(dataStore);
                stores.put(storeId, store);
            } catch (NoResultException ignored) {
            }
        }
    }
}
