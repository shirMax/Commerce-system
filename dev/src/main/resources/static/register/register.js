$(document).ready(function() {
  const registerButton = $("#register-button");
  const spinner = $("#spinner");
  registerButton.click(function() {
    try {
      var userName = $("#username-input").val();
      var password = $("#password-input").val();
      var email = $("#email-input").val();
      var phoneNumber = $("#phone-input").val();

      validateUserName(userName);
      validatePassword(password);
      validateEmail(email);
      validatePhoneNumber(phoneNumber);

      // If all validations pass, submit the form to the server
      spinner.css("display", "block");
    //setTimeout(function() {}, 4000);
      $("#register-form").submit();
    } catch (error) {
      // If any validation fails, display an error message to the user
      alert(error.message);
      return false;
    }
  });
});

  const USERNAME_PATTERN = /^[a-zA-Z0-9._-]{3,20}$/;
  const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/;
  const EMAIL_PATTERN = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;
  const PHONE_NUMBER_PATTERN = /^(\+\d{1,3}[- ]?)?\d{10}$/;

  function validateUserName(userName) {
    if (!userName || !USERNAME_PATTERN.test(userName)) {
      throw new Error("Username must be alphanumeric and have 3-20 characters");
    }
  }

  function validatePassword(password) {
    if (!password || !PASSWORD_PATTERN.test(password)) {
      throw new Error("Password must contain at least 8 characters, including at least one letter and one digit");
    }
  }

  function validateEmail(email) {
    if (!email || !EMAIL_PATTERN.test(email)) {
      throw new Error("Invalid email format");
    }
  }

  function validatePhoneNumber(phoneNumber) {
    if (!phoneNumber || !PHONE_NUMBER_PATTERN.test(phoneNumber)) {
      throw new Error("Invalid phone number format");
    }
  }
