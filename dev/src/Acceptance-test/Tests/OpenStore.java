package Tests;

import Bridge.TestingBridge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Records.StoreRecords.StoreRecord;
import util.Records.UserRecords.UserRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests functionality of opening stores
 * Tests Use-case II.3.2
 */
public class OpenStore extends TestBase{


    static String SESSION;
    static UserRecord USER1 = new UserRecord("test_user1", "testuser1@test.com", "0512345678", LocalDate.of(1997, 1, 19));
    static String USER1_PASS = "P2ssW0rd";
    static StoreRecord STORE1 = new StoreRecord("test_store1", "Store for testing");
    static Queue<Integer> toDelete = new LinkedList<>();

    public static void deleteStore(TestingBridge app, Integer storeId){
        try {
            String session = createSession(app);
            app.login(session, "admin", "admin");
            app.deleteStore(session, storeId);
            app.logout(session);
            app.closeSession(session);
        } catch (Exception e) {
            System.out.printf("Failed to remove test_store: ID: %d, ", storeId);
            System.out.println(e.getMessage());
        }
    }

    public static void cleanToDelete(TestingBridge bridge){
        while (!toDelete.isEmpty()){
            deleteStore(bridge, toDelete.remove());
        }
    }

    @Override
    @BeforeEach
    protected void setUp() {
        inject();
        SESSION = createSession(bridge);
        bridge.register(SESSION, USER1, USER1_PASS);
        bridge.login(SESSION, USER1.username(), USER1_PASS);
    }

    @Override
    @AfterEach
    protected void tearDown() {
        bridge.logout(SESSION);
        bridge.closeSession(SESSION);
        Register.deleteUser(bridge, USER1.username());
        cleanToDelete(bridge);
    }

    @Test
    void OpenStore_NullParameters_Failure(){
        try {
            StoreRecord nullSName = new StoreRecord(null, STORE1.storeDescription());
            int storeID = bridge.openStore(SESSION, nullSName);
            toDelete.add(storeID);
            fail("Opened store with null name");
        } catch (Exception ignored) {
        }
        try {
            StoreRecord nullSDesc = new StoreRecord(STORE1.storeName(), null);
            int storeID = bridge.openStore(SESSION, nullSDesc);
            toDelete.add(storeID);
            fail("Opened store with null description");
        } catch (Exception ignored) {
        }
    }

    @Test
    void OpenStore_FullParameters_Success(){
        int storeID = -1;
        try {
            storeID = bridge.openStore(SESSION, STORE1);
            if (storeID == -1){
                fail("Opening the store didn't return ID.");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
        //checking opened store details:

        StoreRecord storeRecord = bridge.getStore(SESSION, storeID);
        assertEquals(storeID, storeRecord.storeId(), "Bridge didn't return store with the requested ID.");
        assertEquals(STORE1.storeName(), storeRecord.storeName(), "Opened store's name don't match given name.");
        assertEquals(STORE1.storeDescription(), storeRecord.storeDescription(), "Opened store's description don't match given description.");
        deleteStore(bridge, storeID);
    }

    @Test
    void Open2Stores_SameDetails_Success(){
        int storeID1 = -1;
        int storeID2 = -1;
        try {
            storeID1 = bridge.openStore(SESSION, STORE1);
            storeID2 = bridge.openStore(SESSION, STORE1);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        //checking opened store details:

        StoreRecord storeRecord1 = bridge.getStore(SESSION, storeID1);
        StoreRecord storeRecord2 = bridge.getStore(SESSION, storeID2);
        assertNotEquals(storeRecord1.storeId(), storeRecord2.storeId(), "2nd store returns same store and not a new one.");
        assertEquals(storeRecord1.storeName(), storeRecord2.storeName(), "Names don't match.");
        assertEquals(storeRecord1.storeDescription(), storeRecord2.storeDescription(), "Descriptions don't match.");
        deleteStore(bridge, storeID1);
        deleteStore(bridge, storeID2);
    }
}
