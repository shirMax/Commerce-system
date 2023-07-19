package Domain.User;

import util.Enums.ErrorStatus;
import util.Exceptions.DataError;

public class RegistrationValidator implements IRegistrationValidator {

  private static final String USERNAME_PATTERN = "^[a-zA-Z0-9._-]{2,20}$";
  private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
  private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
  private static final String PHONE_NUMBER_PATTERN = "^(\\+\\d{1,3}[- ]?)?\\d{10}$";

  @Override
  public void validateUserName(String userName) throws DataError {
    if (userName == null || userName.isEmpty()) {
      throw new DataError("Username cannot be null or empty", ErrorStatus.INVALID_USERNAME);
    }

    if (!userName.matches(USERNAME_PATTERN)) {
      throw new DataError("Username must be alphanumeric and have 2-20 characters", ErrorStatus.INVALID_USERNAME);
    }

    if(userName.toLowerCase().contains("system"))
      throw new DataError("Username cant contains \"System\"", ErrorStatus.INVALID_USERNAME);
  }

  @Override
  public void validatePassword(String password) throws DataError {
    if (password == null || password.isEmpty()) {
      throw new DataError("Password cannot be null or empty", ErrorStatus.INVALID_PASSWORD);
    }

    if (!password.matches(PASSWORD_PATTERN)) {
      throw new DataError("Password must contain at least 8 characters, and only letters or digits, including at least one letter and one digit", ErrorStatus.INVALID_PASSWORD);
    }
  }

  @Override
  public void validateEmail(String email) throws DataError {
    if (email == null || email.isEmpty()) {
      throw new DataError("Email cannot be null or empty", ErrorStatus.INVALID_EMAIL);
    }

    if (!email.matches(EMAIL_PATTERN)) {
      throw new DataError("Invalid email format", ErrorStatus.INVALID_EMAIL);
    }
  }

  @Override
  public void validatePhoneNumber(String phoneNumber) throws DataError {
    if (phoneNumber == null || phoneNumber.isEmpty()) {
      throw new DataError("Phone number cannot be null or empty", ErrorStatus.INVALID_PHONE_NUMBER);
    }

    if (!phoneNumber.matches(PHONE_NUMBER_PATTERN)) {
      throw new DataError("Invalid phone number format", ErrorStatus.INVALID_PHONE_NUMBER);
    }
  }
}