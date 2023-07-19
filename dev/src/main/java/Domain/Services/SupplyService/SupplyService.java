package Domain.Services.SupplyService;

import Domain.Services.HttpRequestSender;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
public class SupplyService {

  /**
   * This action type is used for dispatching a delivery to a costumer
   * @param name
   * @param address
   * @param city
   * @param country
   * @param zip
   * @return transaction id - an integer in the range [10000, 100000] which indicates a
   * transaction number if the transaction succeeds or -1 if the transaction has failed
   * @throws IOException
   */
  public int deliverOrder(String name, String address, String city, String country, String zip) throws IOException {
    handshake();
    Map<String, String> body = new HashMap<>();
    body.put("action_type", "supply");
    body.put("name", name);
    body.put("address", address);
    body.put("city", city);
    body.put("country", country);
    body.put("zip", zip);
    return Integer.valueOf(HttpRequestSender.sendPostRequest("https://external-systems.000webhostapp.com/", body));
  }

  /**
   * This action type is used for cancelling a supply transaction
   * @param transactionId - the id of the transaction id of the
   * transaction to be canceled
   * @return : 1 if the cancelation has been successful or -1 if the cancelation has failed
   * @throws IOException
   */
  public int cancelSupply(String transactionId) throws IOException {
    handshake();
    Map<String, String> body = new HashMap<>();
    body.put("action_type", "cancel_supply");
    body.put("transaction_id", transactionId);
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
}
