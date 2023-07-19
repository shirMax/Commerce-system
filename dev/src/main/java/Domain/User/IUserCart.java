package Domain.User;

import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.ProductRecord;

import java.util.List;

/**
 * The IUserCart interface represents a user cart in the system.
 * It provides methods to retrieve store baskets associated with the user cart.
 */
public interface IUserCart {

  /**
   * Retrieves a list of store baskets associated with the user cart.
   *
   * @return List of IStoreBasket objects representing the store baskets
   */
  List<IStoreBasket> getStoreBaskets();

  /**
   * Retrieves user's basket for specific store.
   *
   * @param storeID ID of the store the basket is associated with.
   * @return Basket holding items of given store.
   */
  IStoreBasket getStoreBasket(int storeID);

    void addProducts(int storeId, List<ProductRecord> products);

  void removeProduct(int storeId, int productId, int quantity) throws NonExistentData;

  void updateProductQuantity(int storeId, int productId, int quantity) throws NonExistentData;
}