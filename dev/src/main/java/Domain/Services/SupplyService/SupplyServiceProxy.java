package Domain.Services.SupplyService;

import Domain.MarketLogger;
import Domain.Services.Response;
import Domain.Store.IStore;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.org.apache.commons.lang3.NotImplementedException;
import util.Records.AddressRecord;
import util.Records.StoreRecords.ProductRecord;

import java.util.Map;

public class SupplyServiceProxy implements ISupplyService {
  SupplyServiceAdapter supplyServiceAdapter;

  public SupplyServiceProxy(String supllyServiceStrategy) {
    SupplyService supplyService = new SupplyService();
    this.supplyServiceAdapter = new SupplyServiceAdapter(supplyService);
    throw new NotImplementedException("SupplyServiceProxy");
  }

  public SupplyServiceProxy() {
    this.supplyServiceAdapter = new SupplyServiceAdapter(new SupplyService());
  }

  @Override
  public Response<Order> placeOrder(@NonNull IStore store,
                                    @NonNull AddressRecord deliveryAddress,
                                    @NonNull Map<Integer, ProductRecord> products) {
    Response res =  supplyServiceAdapter.placeOrder(store, deliveryAddress, products);
    if(res.isErrorOccurred())
      MarketLogger.logError("SupplyServiceProxy", "placeOrder", res.getMessage(),
              store.toString(), deliveryAddress.toString(), products.toString());
    return res;
  }

  @Override
  public Response cancelOrder(String orderId) {
    Response res =  supplyServiceAdapter.cancelOrder(orderId);
    if(res.isErrorOccurred())
      MarketLogger.logError("SupplyServiceProxy", "cancelOrder", res.getMessage(), orderId);
    return res;
  }
}
