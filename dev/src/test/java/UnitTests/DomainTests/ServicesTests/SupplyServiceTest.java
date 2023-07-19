package UnitTests.DomainTests.ServicesTests;

import Domain.Services.SupplyService.SupplyService;
import UnitTests.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class SupplyServiceTest extends UnitTest {

    private SupplyService supplyService;

    @BeforeEach
    void setUp() {
        supplyService = new SupplyService();
    }

    @Test
    void testDeliverOrder() throws IOException {
        String name = "John Doe";
        String address = "123 Main Street";
        String city = "New York";
        String country = "USA";
        String zip = "12345";

        int transactionId = supplyService.deliverOrder(name, address, city, country, zip);

        // Assert that the transaction ID is within the valid range
        assertTrue(transactionId >= 10000 && transactionId <= 100000);
    }

    @Test
    void testCancelSupply() throws IOException {
        String transactionId = "12345";

        int cancellationResult = supplyService.cancelSupply(transactionId);

        // Assert that the cancellation result is either 1 (successful) or -1 (failed)
        assertTrue(cancellationResult == 1 || cancellationResult == -1);
    }

    @Test
    void testHandshake() throws IOException {
        String response = supplyService.handshake();

        // Assert that the response is "OK"
        assertEquals("OK", response);
    }
}

