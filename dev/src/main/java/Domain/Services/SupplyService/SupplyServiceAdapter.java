package Domain.Services.SupplyService;

import Domain.Services.Response;
import Domain.Store.IStore;
import org.checkerframework.checker.nullness.qual.NonNull;
import util.Records.AddressRecord;
import util.Records.StoreRecords.ProductRecord;

import java.util.Map;
import java.util.concurrent.*;

public class SupplyServiceAdapter implements ISupplyService {

    private SupplyService supplyService;
    private ExecutorService executor;

    public SupplyServiceAdapter(SupplyService supplyService) {
        this.supplyService = supplyService;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public Response<Order> placeOrder(@NonNull IStore store,
                                      @NonNull AddressRecord deliveryAddress,
                                      @NonNull Map<Integer, ProductRecord> products) {
        Order order = new Order(store, deliveryAddress, products);
        Response response = new Response<>("", false, 200);
        try {
            Future<Integer> future = executor.submit(() -> supplyService.deliverOrder(
                    deliveryAddress.fullName(),
                    deliveryAddress.street(),
                    deliveryAddress.city(),
                    deliveryAddress.country(),
                    deliveryAddress.zip()
            ));

            int answer = future.get(10, TimeUnit.SECONDS); // Set the timeout to 10 seconds
            response.setStatus(answer);
            response.setReturnObject(answer);
            if (answer < 10000 || answer > 100000) {
                response.setErrorOccurred(true);
            }
            response.setMessage(String.valueOf(answer));
        } catch (TimeoutException e) {
            response.setErrorOccurred(true);
            response.setMessage("Timeout occurred while waiting for the supply service response");
            response.setStatus(408); // Request Timeout
        } catch (Exception e) {
            response.setErrorOccurred(true);
            response.setMessage(e.getMessage());
            response.setStatus(400);
        }
        return response;
    }

    @Override
    public Response cancelOrder(String orderId) {
        Response response = new Response("", false, 200);
        try {
            Future<Integer> future = executor.submit(() -> supplyService.cancelSupply(orderId));

            int res = future.get(10, TimeUnit.SECONDS); // Set the timeout to 10 seconds

            response.setReturnObject(res);
            response.setStatus(res);
            if (res != 1) {
                response.setErrorOccurred(true);
            }
            response.setMessage(String.valueOf(res));
        } catch (TimeoutException e) {
            response.setErrorOccurred(true);
            response.setMessage("Timeout occurred while waiting for the supply service response");
            response.setStatus(408); // Request Timeout
        } catch (Exception e) {
            response.setErrorOccurred(true);
            response.setMessage(e.getMessage());
            response.setStatus(400);
        }
        return response;
    }
}

