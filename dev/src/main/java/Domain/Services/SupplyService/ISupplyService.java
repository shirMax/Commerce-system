package Domain.Services.SupplyService;

import Domain.Services.Response;
import Domain.Store.IStore;
import org.checkerframework.checker.nullness.qual.NonNull;
import util.Records.AddressRecord;
import util.Records.StoreRecords.ProductRecord;

import java.util.Map;

public interface ISupplyService {

  /**
   * Places an order with the specified store for the given user and products.
   *
   * @param store the store to order from
   * @param deliveryAddress the user address who is placing the order
   * @param products the list of products to order
   * @return the order that was placed
   */
  public Response<Order>
  placeOrder(@NonNull IStore store, @NonNull AddressRecord deliveryAddress,
             @NonNull Map<Integer, ProductRecord> products); // Map<prodID, quantity>

  /**
   * Cancels the specified order.
   *
   * @param orderId the order to cancel
   */
  public Response cancelOrder(String orderId);
}
