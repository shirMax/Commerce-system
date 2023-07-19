package Tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing System-Admin use-cases
 * Tests Use-Case I.1: System initiation
 * Tests Use-Case II.6.2: Delete user
 * Tests Use-Case II.6.6: Query logged/unlogged users
 */
public class SystemAdmin extends TestBase {

    static String SESSION = null;
    static final UserRecord USER1 = new UserRecord("test_user1", "testuser1@test.com", "0512345678", LocalDate.of(1997, 1, 19));
    static final StoreRecord STORE1 = new StoreRecord("test_store1", "Store for testing");

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
        OpenStore.cleanToDelete(bridge);
        Register.cleanToDelete(bridge);
    }

    @Test
    void sysAdminLogin_CorrectInput_Success(){
        try {
            bridge.login(SESSION, "admin", "admin");
        } catch (Exception e){
            fail(e.getMessage());
        }
    }

    @Test
    void sysAdminLogin_WrongInput_Failure(){
        try {
            bridge.login(SESSION, "admin", "admin1");
            fail("Unless changed by production, logging in with these credentials should fail.");
        } catch (Exception e){
            //success
        }
    }

    // Testing Use-Case II.6.2

    @Test
    void DeleteUser_NoRoles_Successs(){
        String session = createSession(bridge);
        bridge.register(session, USER1, "P2ssW0rd");
        bridge.closeSession(session);

        bridge.login(SESSION, "admin", "admin");
        bridge.deleteUser(SESSION, USER1.username());

        //check user was deleted

        Set<String> users = bridge.getUsers(SESSION);
        assertFalse(users.contains(USER1.username()), "User was not deleted. ");
    }

    @Test
    void DeleteUser_OwnsStore_Failure(){
        String session = createSession(bridge);
        bridge.register(session, USER1, "P2ssW0rd");
        Register.toDelete.add(USER1.username());
        bridge.login(session, USER1.username(), "P2ssW0rd");
        Integer storeID = bridge.openStore(session, STORE1);
        OpenStore.toDelete.add(storeID);
        bridge.logout(session);
        bridge.closeSession(session);

        try {
            bridge.deleteUser(SESSION, USER1.username());
            fail("User deletion shouldn't succeed if user has role");
        } catch (Exception success){
        }

    }

    // Testing use-case II.6.6

    @Test
    void QueryLoggedUsers_Success(){
        String session = createSession(bridge);
        bridge.register(session, USER1, "P2ssW0rd");
        Register.toDelete.add(USER1.username());
        bridge.login(session, USER1.username(), "P2ssW0rd");

        bridge.login(SESSION, "admin", "admin");
        Set<String> logged = bridge.getLoggedUsers(SESSION);
        Set<String> unlogged = bridge.getUnloggedUsers(SESSION);

        assertTrue(logged.contains(USER1.username()));
        assertFalse(unlogged.contains(USER1.username()));

        bridge.logout(session);
        bridge.closeSession(session);

        logged = bridge.getLoggedUsers(SESSION);
        unlogged = bridge.getUnloggedUsers(SESSION);

        assertFalse(logged.contains(USER1.username()));
        assertTrue(unlogged.contains(USER1.username()));

    }
}
