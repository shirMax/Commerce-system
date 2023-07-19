package Domain.User;

import util.Exceptions.NonExistentData;
import util.Records.StoreRecords.ProductRecord;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Guest extends User {
  private UserCart userCart;
  public Guest(String sessionId) {
    super(sessionId);
    this.userCart = new UserCart();
  }

  @Override
  public String getUserName() {
    throw new IllegalArgumentException("Guest doesn't have a user name!");
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
            .entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getAsRecord()));
  }

  @Override
  public UserCart getUserCart() {
    return userCart;
  }

  @Override
  public void removeUserCart() {
    userCart = new UserCart();
  }

  @Override
  public String toString() {
    return "Guest"+getSessionId();
  }
}
