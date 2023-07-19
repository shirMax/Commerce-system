package Domain.Services.PaymentService;

import Domain.Services.Response;
import util.Records.PaymentDetails;

import java.io.IOException;

public interface IPaymentService {

  /**
   * Processes a payment with the specified payment details.
   *
   * @param paymentDetails the payment details to be used for processing the
   *     payment
   * @return a Response object containing the status and result of the payment
   *     processing with the payyment ID
   */
  Response processPayment(PaymentDetails paymentDetails);

  /**
   * Refunds a payment with the specified payment ID.
   *
   * @param paymentId the unique ID of the payment to be refunded
   * @return a Response object containing the status and result of the payment
   *     refund
   */
  Response refundPayment(String paymentId);

  String getPaymentServiceURL();

  void updatePaymentServiceURL(String newUrl) throws IOException;

  Response checkHandShake() throws IOException;
}
