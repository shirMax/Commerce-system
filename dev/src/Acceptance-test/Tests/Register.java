package Tests;

import Bridge.TestingBridge;
import Exceptions.ATException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import util.Enums.ErrorStatus;
import util.Records.DateRecord;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests use-case II.1.3: Registering to the system
 * - Registering with legal input
 * - Registering with illegal input
 * - Registering concurrently with legal input.
 */
public class Register extends TestBase{
    //Actors
    static String SESSION;
    static String PASSWORD = "P2ssW0rd";
    UserRecord user1 = new UserRecord("test_user1", "testuser1@test.com", "0512345678", LocalDate.of(1997, 1, 19));

    public static Queue<String> toDelete = new LinkedList<>();

    /**
     * Help function which will login to an admin user and delete given user.
     *
     * @param app Bridge to app from which the user should be deleted.
     * @param toDelete User credentials to be deleted.
     */
    public static void deleteUser(TestingBridge app, String username){
        try {
            String session = createSession(app);
            app.login(session, "admin", "admin");
            app.deleteUser(session, username);
            app.logout(session);
            app.closeSession(session);
        } catch (Exception e) {
            System.out.println("Failed to remove test_user: " + username);
            System.out.println(e.getMessage());
        }
    }

    public static void cleanToDelete(TestingBridge bridge){
        while (!toDelete.isEmpty())
            deleteUser(bridge, toDelete.remove());
    }

    @Override
    @BeforeEach
    protected void setUp() {
        inject();
        SESSION = createSession(bridge);
    }

    @Override
    @AfterEach
    protected void tearDown() {
        bridge.closeSession(SESSION);
        cleanToDelete(bridge);
    }

    @Test
    void Register_NullFields_Failure(){
        try {
            UserRecord nullUName = new UserRecord(null, user1.email(), user1.phoneNumber(), user1.dobRecord());
            bridge.register(SESSION, nullUName, PASSWORD);
            fail("Registered a user with null username");
        } catch (Exception ignored) {
        }
        try {
            UserRecord nullMail = new UserRecord(user1.username(), null, user1.phoneNumber(), user1.dobRecord());
            bridge.register(SESSION, nullMail, PASSWORD);
            fail("Registered a user with null email");
        } catch (Exception ignored) {
        }
        try {
            UserRecord nullPhone = new UserRecord(user1.username(), user1.email(), null, user1.dobRecord());
            bridge.register(SESSION, nullPhone, PASSWORD);
            fail("Registered a user with null phone number");
        } catch (Exception ignored) {
        }
        try {
            UserRecord nullDOB = new UserRecord(user1.username(), user1.email(), user1.phoneNumber(), (DateRecord) null);
            bridge.register(SESSION, nullDOB, PASSWORD);
            fail("Registered a user with null date-of-birth");
        } catch (Exception ignored) {
        }
        try {
            bridge.register(SESSION, user1, null);
            fail("Registered a user with null password");
        } catch (Exception ignored) {
        }
    }

    @Test
    void Register_LegalInput_Success(){
        try {
            bridge.register(SESSION, user1, PASSWORD);
            toDelete.add(user1.username());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void Register_AlreadyExistingUser_Failure(){
        String session = createSession(bridge);
        bridge.register(session, user1, PASSWORD);
        toDelete.add(user1.username());
        bridge.closeSession(session);

        try {
            bridge.register(SESSION, user1, PASSWORD);
            fail("Registering with an already existing username should not succeed");
        } catch (ATException e) {
            if (e.status != ErrorStatus.USERNAME_EXISTS) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    @RepeatedTest(20)
    void Register10Concurrent_LegalInput_1Success(){
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();
        toDelete.add(user1.username());
        for (int i = 0; i < 10; i++){
            futures.add(executor.submit(() ->
            {
                String session = createSession(bridge);
                bridge.register(session, user1, PASSWORD);
                bridge.closeSession(session);
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

        assertTrue(1 >= successCounter, "Only one thread should have succeeded in registering.");
    }
}
