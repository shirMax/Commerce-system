package Domain.Services.PaymentService;

import Domain.Services.Response;
import org.checkerframework.org.apache.commons.lang3.NotImplementedException;
import util.Records.PaymentDetails;

import java.io.IOException;
import java.util.concurrent.*;

public class PaymentServiceAdapter implements IPaymentService {
  PaymentService paymentService;
  ExecutorService executor;

  public PaymentServiceAdapter(PaymentService paymentService) {
    this.paymentService = paymentService;
    this.executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public Response processPayment(PaymentDetails paymentDetails) {
    Response response = new Response("", false, 200);
    try {
      Future<Integer> future = executor.submit(() -> paymentService.charge(
              paymentDetails.card_number(),
              String.valueOf(paymentDetails.expiry_date().month()),
              String.valueOf(paymentDetails.expiry_date().year()),
              paymentDetails.card_owner(),
              paymentDetails.cvv(),
              paymentDetails.id()
      ));

      int res = future.get(10, TimeUnit.SECONDS); // Set the timeout to 10 seconds
      response.setStatus(res);
      if (res < 10000 || res > 100000) {
        response.setErrorOccurred(true);
        response.setMessage("Error occurred");
      }
      response.setMessage("Purchase Successfully");
    } catch (TimeoutException e) {
      response.setErrorOccurred(true);
      response.setMessage("Timeout occurred while waiting for the payment service response");
      response.setStatus(408); // Request Timeout
    } catch (Exception e) {
      response.setErrorOccurred(true);
      response.setMessage("Unexpected problem occurred");
      response.setStatus(400);
    }

    return response;
  }

  @Override
  public Response refundPayment(String paymentId) {
    Response response = new Response("", false, 200);
    try {
      Future<Integer> future = executor.submit(() -> paymentService.cancelPay(paymentId));

      int res = future.get(10, TimeUnit.SECONDS); // Set the timeout to 10 seconds

      response.setReturnObject(res);
      response.setStatus(res);
      if (res != 1) {
        response.setErrorOccurred(true);
      }
      response.setMessage(String.valueOf(res));
    } catch (TimeoutException e) {
      response.setErrorOccurred(true);
      response.setMessage("Timeout occurred while waiting for the payment service response");
      response.setStatus(408); // Request Timeout
    } catch (Exception e) {
      response.setErrorOccurred(true);
      response.setMessage(e.getMessage());
      response.setStatus(400);
    }

    return response;
  }

  @Override
  public String getPaymentServiceURL() {
    return paymentService.getURL();
  }

  @Override
  public void updatePaymentServiceURL(String newUrl) {
    paymentService.setURL(newUrl);
  }

  @Override
  public Response checkHandShake() throws IOException {
    String ans = paymentService.handshake();
    Response res;
    if(ans.toLowerCase().equals("ok")){
      res = Response.createSuccessResponse();
    }
    else{
      res = Response.createErrorResponse("Hand shake doesnt success", -1);
    }
    return res;
  }
}

