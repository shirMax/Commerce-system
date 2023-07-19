package Domain.Services.PaymentService;

import Domain.Services.HttpRequestSender;
import Domain.Services.Response;
import util.Records.PaymentDetails;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FPaymentService implements IPaymentService {

    Response response = new Response("", false, 200);

    public FPaymentService(){
        response.setErrorOccurred(true);
        response.setMessage("proxy fail response");
        response.setStatus(400);
    }

    @Override
    public Response processPayment(PaymentDetails paymentDetails) {
        return response;
    }

    @Override
    public Response refundPayment(String paymentId) {
        return response;
    }

    @Override
    public String getPaymentServiceURL() {
        return null;
    }

    @Override
    public void updatePaymentServiceURL(String newUrl) throws IOException {

    }

    @Override
    public Response checkHandShake() throws IOException {
        return response;
    }
}
