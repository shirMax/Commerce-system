package Domain.Services.PaymentService;

import Domain.Services.HttpRequestSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PaymentService {

  private String url = "https://external-systems.000webhostapp.com/";

  public void PaymentService() {}

  /**
   * This action type is used for charging a payment for purchases
   * @param cardNumber
   * @param month
   * @param year
   * @param holder
   * @param ccv
   * @param id
   * @return transaction id - an integer in the range [10000, 100000] which indicates a
   * transaction number if the transaction succeeds or -1 if the transaction has failed
   * @throws IOException
   */
  public int charge(String cardNumber, String month, String year, String holder, String ccv, Long id) throws IOException {
    handshake();
    Map<String, String> body = new HashMap<>();
    body.put("action_type", "pay");
    body.put("card_number", cardNumber);
    body.put("month", month);
    body.put("year", year);
    body.put("holder", holder);
    body.put("ccv", ccv);
    body.put("id", id.toString());
    return Integer.valueOf(HttpRequestSender.sendPostRequest(url, body));
  }

  /**
   * This action type is used for cancelling a payment transaction.
   * @param transaction_id
   * @return 1 if the cancelation has been successful or -1 if the cancelation has failed
   * @throws IOException
   */
  public int cancelPay(String transaction_id) throws IOException {
    handshake();
    Map<String, String> body = new HashMap<>();
    body.put("action_type", "cancel_pay");
    body.put("transaction_id", transaction_id);
    return Integer.valueOf(HttpRequestSender.sendPostRequest("https://external-systems.000webhostapp.com/", body));
  }

  /**
   * This action type is used for check the availability of the external systems.
   * @return “OK” message to signify that the handshake has been successful
   * @throws IOException
   */
  public String handshake() throws IOException {
    Map<String, String> body = new HashMap<>();
    body.put("action_type", "handshake");
    return HttpRequestSender.sendPostRequest("https://external-systems.000webhostapp.com/", body);
  }

  public String getURL(){
    return url;
  }

  public void setURL(String newUrl) {
    url = newUrl;
  }
}
