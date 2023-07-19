package Domain.Services.PaymentService;

import Domain.Services.Response;
import util.Records.PaymentDetails;

import java.io.IOException;

public class TPaymentService implements IPaymentService {

    Response response = new Response("", false, 200);

    public TPaymentService(){
        response.setErrorOccurred(false);
        response.setMessage("proxy success response");
        response.setStatus(10000);
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
