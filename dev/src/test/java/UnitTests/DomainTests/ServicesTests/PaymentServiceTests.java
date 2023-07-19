package UnitTests.DomainTests.ServicesTests;

import Domain.Services.PaymentService.PaymentService;
import UnitTests.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;

class PaymentServiceTests extends UnitTest {
    private static final String BASE_URL = "https://php-server-try.000webhostapp.com/";

    @Test
    void testCharge() throws IOException {
        PaymentService paymentService = new PaymentService();

        String cardNumber = "1234567812345678";
        String month = "12";
        String year = "2023";
        String holder = "John Doe";
        String ccv = "123";
        Long id = 12345L;

        int transactionId = paymentService.charge(cardNumber, month, year, holder, ccv, id);
        System.out.println(transactionId);
        Assertions.assertTrue(transactionId >= 10000 && transactionId <= 100000);
    }

    @Test
    void testCancelPay() throws IOException {
        PaymentService paymentService = new PaymentService();

        String transactionId = "123456789";

        int result = paymentService.cancelPay(transactionId);

        Assertions.assertEquals(1, result);
    }

    @Test
    void testHandshake() throws IOException {
        PaymentService paymentService = new PaymentService();

        String response = paymentService.handshake();

        Assertions.assertEquals("OK", response);
    }
}

