package Tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Acceptance tests for the Session Management of the system.
 * Tests opening and closing sessions, sessions that don't exist or concurrent sessions.
 */
public class Session extends TestBase{


    @Override
    @BeforeEach
    protected void setUp() {
        inject();
    }

    @Override
    @AfterEach
    protected void tearDown() {

    }

    @Test
    void openCloseSession_Success(){
        try {
            String session = createSession(bridge);
            bridge.closeSession(session);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void closingSession_WrongSessionID_Failure(){
        try {
            bridge.closeSession("-31209387");
            fail("Should not have succeeded. May occur when production is running by chance.");
        } catch (Exception ignored){
            //success
        }
    }

    @Test
    void openSession_10Concurrent_Success(){
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Set<String> ids = new HashSet<>();
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++){
            futures.add(executor.submit(() -> createSession(bridge)));
        }

        for (Future<String> future: futures) {
            try {
                ids.add(future.get());
            } catch (Exception e) {
                fail("Opening 10 sessions should succeed. Cause: " + e.getMessage());
            }
        }

        assertEquals(10, ids.size(), "No unique session identifiers.");

        for (String sessionID: ids)
            bridge.closeSession(sessionID);
    }
}
