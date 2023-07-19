package Domain.Services.SupplyService;

import Domain.Store.IStore;
import org.checkerframework.checker.nullness.qual.NonNull;
import util.Records.AddressRecord;
import util.Records.StoreRecords.ProductRecord;

import java.util.Map;

public class Order {
  private IStore store;
  private AddressRecord deliveryAddress;
  private Map<Integer, ProductRecord> products;

  private String orderId;
  public Order(@NonNull IStore store, @NonNull AddressRecord deliveryAddress,
               @NonNull Map<Integer, ProductRecord> products) {
    this.store = store;
    this.products = products;
    this.deliveryAddress = deliveryAddress;
  }

  public IStore getStore() { return store; }

  public void setStore(IStore store) { this.store = store; }

  public AddressRecord getUser() { return deliveryAddress; }

  public void setUser(AddressRecord user) { this.deliveryAddress = user; }

  public Map<Integer, ProductRecord> getProducts() { return products; }

  public void setAllProducts(Map<Integer, ProductRecord> products) {
    this.products = products;
  }

  @Override
  public String toString() {
    return "Order{"
        + "store=" + store + ", deliveryAddress=" + deliveryAddress +
        ", products=" + products + '}';
  }
}
