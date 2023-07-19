/**
 * Interface for registration validation operations, including validation of
 * username, password, email, and phone number.
 */
package Domain.User;

import util.Exceptions.*;

public interface IRegistrationValidator {

  /**
   * Validates the username according to the defined rules.
   *
   * @param userName The username to be validated.
   */
  void validateUserName(String userName) throws DataError;

  /**
   * Validates the password according to the defined rules.
   *
   * @param password The password to be validated.
   */
  void validatePassword(String password) throws DataError;

  /**
   * Validates the email according to the defined rules.
   *
   * @param email The email to be validated.
   */
  void validateEmail(String email) throws DataError;

  /**
   * Validates the phone number according to the defined rules.
   *
   * @param phoneNumber The phone number to be validated.
   */
  void validatePhoneNumber(String phoneNumber) throws DataError;
}