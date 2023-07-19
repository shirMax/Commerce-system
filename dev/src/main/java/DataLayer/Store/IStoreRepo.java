package DataLayer.Store;

import Domain.Store.IStore;
import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.StoreRecord;

import java.util.List;
import java.util.Map;

public interface IStoreRepo {
  /**
   * Retrieves a list of all stores in the system.
   *
   * @return A list of all stores in the system.
   */
  public List<IStore> getStores();

  /**
   * Retrieves a store with the specified ID.
   *
   * @param storeId The ID of the store to retrieve.
   * @return The store with the specified ID.
   */
  public IStore getStore(int storeId) throws NonExistentData;

  /**
   * Adds a new store to the system.
   * @param founder username of the store founder.
   * @param storeDetails name and description of the store to add.
   * @return ID of the newly added store.
   */
  public int openNewStore(String founder, StoreRecord storeDetails);

  /**
   * Removes the store with the specified ID from the system.
   *
   * @param storeId The ID of the store to remove.
   */
  public void removeStore(int storeId) throws NonExistentData;

  Map<Integer, IStore> getStoreMap();
}
