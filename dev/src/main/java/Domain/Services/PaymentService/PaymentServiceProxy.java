package Domain.Services.PaymentService;

import Domain.MarketLogger;
import Domain.Services.Response;
import util.Records.PaymentDetails;

import java.io.IOException;

public class PaymentServiceProxy implements IPaymentService {

  private IPaymentService paymentServiceAdapter;

  //paymentStrategy: can be "F", "T", "regular"
  //"F" - all calls return false, "T" - all calls return true
  public PaymentServiceProxy(String paymentStrategy) {
    switch (paymentStrategy){
      case "F":
        this.paymentServiceAdapter = new FPaymentService();
      case "T":
        this.paymentServiceAdapter = new TPaymentService();
      default:
        this.paymentServiceAdapter = new PaymentServiceAdapter(new PaymentService());
    }
  }

  public void updatePaymentServiceURL(String newUrl) throws IOException {
    String oldUrl = paymentServiceAdapter.getPaymentServiceURL();
    paymentServiceAdapter.updatePaymentServiceURL(newUrl);

    Response res = checkHandShake();
    if(res.isErrorOccurred()){
      paymentServiceAdapter.updatePaymentServiceURL(oldUrl);
      MarketLogger.logError("PaymentServiceProxy", "updatePaymentServiceURL", "cant change uel of payment service", newUrl);
      throw new IllegalArgumentException("cant change uel of payment service");
    }
  }

  @Override
  public Response checkHandShake() throws IOException {
    return paymentServiceAdapter.checkHandShake();
  }

  public void updateIPaymentService(IPaymentService iPaymentService){
    this.paymentServiceAdapter = iPaymentService;
  }

  @Override
  public Response processPayment(PaymentDetails paymentDetails) {
    Response res =  paymentServiceAdapter.processPayment(paymentDetails);
    res.setReturnObject(paymentDetails.id());
    if(res.isErrorOccurred()) {
      MarketLogger.logError("PaymentServiceProxy", "processPayment", res.getMessage(), paymentDetails.toString());
    }
    return res;
  }

  @Override
  public Response refundPayment(String paymentId) {
    Response res =  paymentServiceAdapter.refundPayment(paymentId);
    if(res.isErrorOccurred())
      MarketLogger.logError("PaymentServiceProxy", "refundPayment", res.getMessage(), paymentId);
    return res;
  }

  @Override
  public String getPaymentServiceURL() {
    return paymentServiceAdapter.getPaymentServiceURL();
  }
}
