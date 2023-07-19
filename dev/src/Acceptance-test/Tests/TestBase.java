package Tests;

import Bridge.AppAdapter;
import Bridge.TestingBridge;
import Bridge.TestingProxy;
import DataLayer.DbConfig;
import Domain.Services.PaymentService.IPaymentService;
import Domain.Services.Response;
import org.checkerframework.com.google.common.hash.Hashing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Abstract class to provide the base for every test.
 * Testing each use-case should be done in a different class extending Tests.UnitTests.TestBase.
 */
public abstract class TestBase {
    /**
     * Some connector to application
     */
    public final TestingBridge bridge;

    public IPaymentService paymentServiceMock = Mockito.mock(IPaymentService.class);

    public static final Response SUCCESS_RESPONSE = new Response("It's all good, man.", false, 0);

    /**
     * CTOR that initiates the test with a given bridge.
     *
     * @param bridge Bridge to be set.
     */
    public TestBase(TestingBridge bridge){
        Objects.requireNonNull(bridge);
        this.bridge = bridge;
    }

    /**
     * Default CTOR that initiates the test with a proxy which may be injected later.
     */
    public TestBase(){
        bridge = new TestingProxy();
    }

    /**
     * Setup method for before running the test.
     */
    @BeforeEach
    abstract protected void setUp();

    /**
     * Tear Down method for after the test was run.
     */
    @AfterEach
    abstract protected void tearDown();


    protected static TestingBridge toInject = null;
    @BeforeAll
    static void prepareToInject(){
        try {
            DbConfig.setPERSIST(false);
        } catch (Exception ignored) {
        }
        toInject = new AppAdapter();
    }

    protected void inject(){
        ((TestingProxy)bridge).injectBridge(toInject);
        bridge.updatePaymentService(paymentServiceMock);
        when(paymentServiceMock.processPayment(any())).thenReturn(SUCCESS_RESPONSE);
        when(paymentServiceMock.refundPayment(any())).thenReturn(SUCCESS_RESPONSE);
    }

    static Integer seed = Integer.MIN_VALUE;
    protected static String generateHash(){
        return Hashing.sha256()
                .hashString(seed.toString(), StandardCharsets.UTF_8)
                .toString();
    }

    protected static String createSession(TestingBridge bridge){
        while (true){
            try {
                seed++;
                return bridge.openSession(generateHash());
            } catch (Exception ignored) {}
        }
    }
}
