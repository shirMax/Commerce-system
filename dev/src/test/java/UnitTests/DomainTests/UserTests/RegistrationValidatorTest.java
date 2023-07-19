package UnitTests.DomainTests.UserTests;

import Domain.User.IRegistrationValidator;
import Domain.User.RegistrationValidator;
import UnitTests.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Exceptions.DataError;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RegistrationValidatorTest extends UnitTest {

    private IRegistrationValidator registrationValidator;

    @BeforeEach
    void setUp() {
        registrationValidator = new RegistrationValidator();
    }

    @Test
    void validateUserName_ValidUserName_NoExceptionThrown() {
        // Arrange
        String userName = "john_doe";

        // Act & Assert
        assertDoesNotThrow(() -> registrationValidator.validateUserName(userName));
    }

    @Test
    void validateUserName_NullUserName_ExceptionThrown() {
        // Arrange
        String userName = null;

        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validateUserName(userName));
    }

    @Test
    void validateUserName_EmptyUserName_ExceptionThrown() {
        // Arrange
        String userName = "";

        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validateUserName(userName));
    }

    @Test
    void validateUserName_InvalidUserName_ExceptionThrown() {
        // Arrange
        String userName = "a";

        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validateUserName(userName));
    }

    @Test
    void validatePassword_ValidPassword_NoExceptionThrown() {
        // Arrange
        String password = "Pass1234";

        // Act & Assert
        assertDoesNotThrow(() -> registrationValidator.validatePassword(password));
    }

    @Test
    void validatePassword_NullPassword_ExceptionThrown() {
        // Arrange
        String password = null;

        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validatePassword(password));
    }

    @Test
    void validatePassword_EmptyPassword_ExceptionThrown() {
        // Arrange
        String password = "";

        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validatePassword(password));
    }

    @Test
    void validatePassword_InvalidPassword_ExceptionThrown() {
        // Arrange
        String password = "password";

        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validatePassword(password));
    }

    @Test
    void validateEmail_ValidEmail_NoExceptionThrown() {
        // Arrange
        String email = "john.doe@example.com";

        // Act & Assert
        assertDoesNotThrow(() -> registrationValidator.validateEmail(email));
    }

    @Test
    void validateEmail_NullEmail_ExceptionThrown() {
        // Arrange
        String email = null;

        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validateEmail(email));
    }

    @Test
    void validateEmail_EmptyEmail_ExceptionThrown() {
        // Arrange
        String email = "";

        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validateEmail(email));
    }

    @Test
    void validateEmail_InvalidEmail_ExceptionThrown() {
        // Arrange
        String email = "johndoe@";

        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validateEmail(email));
    }

    @Test
    void validatePhoneNumber_ValidPhoneNumber_NoExceptionThrown() {
        // Arrange
        String phoneNumber = "1234567890";

        // Act & Assert
        assertDoesNotThrow(() -> registrationValidator.validatePhoneNumber(phoneNumber));
    }

    @Test
    void validatePhoneNumber_NullPhoneNumber_ExceptionThrown() {
        // Arrange
        String phoneNumber = null;
        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validatePhoneNumber(phoneNumber));
    }

    @Test
    void validatePhoneNumber_EmptyPhoneNumber_ExceptionThrown() {
        // Arrange
        String phoneNumber = "";

        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validatePhoneNumber(phoneNumber));
    }

    @Test
    void validatePhoneNumber_InvalidPhoneNumber_ExceptionThrown() {
        // Arrange
        String phoneNumber = "12345";

        // Act & Assert
        assertThrows(DataError.class, () -> registrationValidator.validatePhoneNumber(phoneNumber));
    }
}