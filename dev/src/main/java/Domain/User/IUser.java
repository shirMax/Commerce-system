/**
 * Interface for user operations, including managing store baskets and user cart.
 */
package Domain.User;

import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.ProductRecord;

import java.util.List;
import java.util.Map;

public interface IUser {

  /**
   * Retrieves the username of the user.
   *
   * @return The username of the user.
   */
  String getUserName();

  /**
   * Adds products to the store basket for a specific store.
   *
   * @param storeId The ID of the store.
   * @param productsIdAndQuantity A map containing product IDs as keys and their corresponding quantities as values.
   */
  void addProductsToStoreBasket(int storeId, List<ProductRecord> productsIdAndQuantity);

  /**
   * Removes a product from the store basket for a specific store.
   *
   * @param storeId   The ID of the store.
   * @param productId The ID of the product to be removed.
   * @param quantity
   * @throws NonExistentData If the product does not exist in the store basket.
   * @throws NonExistentData If the user cart does not exist.
   */
  void removeProductFromStoreBasket(int storeId, int productId, int quantity) throws NonExistentData;

  /**
   * Updates the quantity of a product in the store basket for a specific store.
   *
   * @param storeId The ID of the store.
   * @param productId The ID of the product to be updated.
   * @param quantity The new quantity of the product.
   * @throws NonExistentData If the product does not exist in the store basket.
   * @throws NonExistentData If the user cart does not exist.
   */
  void updateProductQuantityInStoreBasket(int storeId, int productId, int quantity) throws NonExistentData;

  /**
   * Retrieves a list of store baskets for the user.
   *
   * @return A list of store baskets for the user.
   */
  List<IStoreBasket> getStoreBaskets();

  IStoreBasket getStoreBasket(int storeID);

  /**
   * Retrieves the products in the store basket for a specific store.
   *
   * @param storeId The ID of the store.
   * @return A map containing product IDs as keys and their corresponding quantities as values.
   */
  Map<Integer, ProductRecord> getStoreBasketProducts(int storeId);

  /**
   * Retrieves the user cart.
   *
   * @return The user cart.
   */
  UserCart getUserCart();

  /**
   * Removes the user's cart for the current session.
   */
    void removeUserCart();

    public String getSessionId();
}