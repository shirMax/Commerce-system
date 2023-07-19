package Tests;

import Exceptions.ATException;
import org.junit.jupiter.api.*;
import util.Enums.ErrorStatus;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Login extends TestBase{

    static String SESSION;
    UserRecord USER1 = new UserRecord("test_user1", "testuser1@test.com", "0512345678", LocalDate.of(1997, 1, 19));
    static String USER1_PASS = "P2ssW0rd";

    @Override
    @BeforeEach
    protected void setUp() {
        inject();
        SESSION = createSession(bridge);
        bridge.register(SESSION, USER1, USER1_PASS);
    }

    @Override
    @AfterEach
    protected void tearDown() {
        bridge.closeSession(SESSION);
        Register.deleteUser(bridge, USER1.username());
    }

    @Test
    void Login_IncorrectPassword_Failure(){
        try {
            bridge.login(SESSION, USER1.username(), USER1_PASS.concat("a"));
            fail("Succeeded login with incorrect password");
        } catch (Exception success){
            //success
        }
    }

    @Test
    void Login_NonExistingUser_Failure(){
        try {
            bridge.login(SESSION, USER1.username().concat("2"), USER1_PASS);
            fail("Succeeded login to non-existing user.");
        } catch (Exception success){
            //success
        }
    }

    @Test
    void LoginLogout_CorrectCredentials_Success(){
        try {
            bridge.login(SESSION, USER1.username(), USER1_PASS);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        try {
            bridge.logout(SESSION);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void Login_AlreadyLoggedIn_Failure(){
        String session = createSession(bridge);
        bridge.login(session, USER1.username(), USER1_PASS);

        try {
            bridge.login(SESSION, USER1.username(), USER1_PASS);
        } catch (ATException e) {
            if (e.status != ErrorStatus.ALREADY_LOGGED_IN)
                fail(e.getMessage());
        }

        bridge.logout(session);
    }

    @Test
    @RepeatedTest(20)
    void Login10Concurrent_CorrectCredentials_1Success(){
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++){
            futures.add(executor.submit(() ->
            {
                String session = createSession(bridge);
                bridge.login(session, USER1.username(), USER1_PASS);
            }));
        }

        int successCounter = 0;
        for (Future<?> future: futures) {
            try {
                future.get();
                successCounter++;
            } catch (Exception ignored) {
            }
        }

        assertEquals(1, successCounter, "Only one thread should have succeeded in Logging in.");
    }
}
