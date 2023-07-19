package UnitTests.DomainTests.UserTests;

import Domain.Store.Category;
import Domain.User.Member;
import UnitTests.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Exceptions.*;
import util.Records.StoreRecords.ProductRecord;
import util.Records.UserRecords.UserRecord;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MemberTest extends UnitTest {
    private Member member;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        UserRecord userData = new UserRecord("username", "email",
                "phone", LocalDate.now());
        member = new Member("1", userData, "password");
    }

    @Test
    void testLogin() {
        assertThrows(SessionError.class, () -> member.login("1", "wrong_password"),
                "Should throw SessionError with USERNAME_PASSWORD_MISMATCH status");
        assertFalse(member.isLoggedIn(), "Should not be logged in after failed login");

        assertDoesNotThrow(() -> member.login("1", "password"), "Should not throw an exception on successful login");
        assertTrue(member.isLoggedIn(), "Should be logged in after successful login");
    }

    @Test
    void testLogout() {
        try {
            assertThrows(SessionError.class, () -> member.logout(), "Should throw SessionError with NOT_LOGGED_IN status");
            member.login("1", "password");
            assertDoesNotThrow(() -> member.logout(), "Should not throw an exception on successful logout");
            assertFalse(member.isLoggedIn(), "Should not be logged in after successful logout");
        }
        catch (Exception e){
            fail();
        }
    }

    @Test
    void testChangePassword() {
        try {
            assertThrows(DataError.class, () -> member.changePassword("wrong_password", "new_password"),
                    "Should throw DataError with INVALID_PASSWORD status");
            assertTrue(member.validateRightPassword("password"), "Should not change password on failed changePassword");

            assertDoesNotThrow(() -> member.changePassword("password", "new_password"), "Should not throw an exception on successful changePassword");
            assertTrue(member.validateRightPassword("new_password"), "Should change password on successful changePassword");
        }
        catch (Exception e){
            fail();
        }
    }

    @Test
    void cartSave() {
        try {
            member.login("1", "password");
            ProductRecord productRecord = new ProductRecord(1, 1, "Test Product 1", 10.0, Category.PETS, 5, 10, 1);
            member.addProductsToStoreBasket(productRecord.storeId(), List.of(productRecord));
            assertEquals(1, member.getStoreBasket(productRecord.storeId()).getProductRecord(1).productId());
            member.logout();
            member.login("1", "password");
            assertEquals(1, member.getStoreBasket(productRecord.storeId()).getProductRecord(1).productId());
        }
        catch (Exception e){
            fail(e.getMessage());
        }
    }
}