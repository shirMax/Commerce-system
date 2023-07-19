package Domain.User;

import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.ProductRecord;

import java.time.LocalDate;
import java.util.Map;

public interface IStoreBasket {

  /**
   * Returns the store ID.
   *
   * @return the store ID
   */
  int getStoreId();

  /**
   * Returns the map of products in the store basket.
   *
   * @return the map of products in the store basket
   */
  Map<Integer, IBaskedProduct> getProducts();

  /**
   * Returns the map of products in the store basket.
   *
   * @return the map of products in the store basket
   */
  Map<Integer, ProductRecord> getProductsAsRecords();

  /**
   * Adds a product to the store basket.
   *
   * @param product the product record to add
   */
  void addProduct(ProductRecord product);

  /**
   * Removes a product from the store basket.
   *
   * @param productId the ID of the product to remove
   * @throws NonExistentData if the product does not exist in the store basket
   */
  void removeProduct(int productId) throws NonExistentData;

  /**
   * Checks if a product exists in the store basket.
   *
   * @param productId the ID of the product
   * @return true if the product exists, false otherwise
   */
  boolean isProductExists(int productId);

  /**
   * Returns the price of the store basket without the discounts.
   *
   * @return the last calculated price
   */
  double getBasketPrice();

  /**
   * Returns the price of the store basket with the discounts.
   *
   * @return the last calculated price
   */
  double getBasketPriceAfterDiscount();


  /**
   * Updates the product record in the store basket.
   *
   * @param product the updated product record
   */
  void updateProductRecord(ProductRecord product);


  /**
   * Returns the product record for the given product ID.
   *
   * @param productId the ID of the product
   * @return the product record
   */
  ProductRecord getProductRecord(int productId) throws NonExistentData;

  IStoreBasket clone();

  LocalDate getUserAge();

  void setUserAge(LocalDate userAge);

  /**
   * Returns a string representation of the store basket.
   *
   * @return a string representation of the store basket
   */
  @Override
  String toString();

  void updateProductQuantity(int productId, int quantity) throws NonExistentData;

  boolean isEmpty();

  void remove();

  boolean productsAreEqual(IStoreBasket basket);
}
