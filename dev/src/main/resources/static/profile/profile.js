$(document).ready(function() {
    $(document).on('click', '#changePasswordBtn', function() {
        var newPassword = $('#changePasswordModal input[name="newPassword"]').val();
        var confirmPassword = $('#changePasswordModal input[name="confirmPassword"]').val();

        if (newPassword !== confirmPassword) {
            alert('New password and confirm password do not match.');
            return false;
        } else {
            // Send the post request to /changePassword
            $("#changePassword").submit();

            // Close the modal
            $('#changePasswordModal').modal('hide');
        }
    });

    $(document).on('click', '#addMemberAddressBtn', function() {
        var fullName = $('#fullName').val();
        var street = $('#street').val();
        var city = $('#city').val();
        var country = $('#country').val();
        var zip = $('#zip').val();
        var phone = $('#phoneNumber').val();

        // Check if any input field is empty
        if (!fullName || !street || !city || !country || !zip || !phone) {
            alert('Please fill in all the fields.');
            return false;
        }

        // Full name, city, and country validation
        var fullNameRegex = /^[a-zA-Z]+(\s[a-zA-Z]+){1,3}$/;
        var cityNameRegex = /^[a-zA-Z\s]+$/;
        var countryNameRegex = /^[a-zA-Z\s]+$/;
        if (!fullName.match(fullNameRegex)) {
            alert('Please enter a valid full name (both first name and last name separated by a space, only alphabetic characters are allowed).');
            return false;
        }
        if (!city.match(cityNameRegex)) {
            alert('Please enter a valid city name (only alphabetic characters are allowed).');
            return false;
        }
        if (!country.match(countryNameRegex)) {
            alert('Please enter a valid country name (only alphabetic characters are allowed).');
            return false;
        }

        // Street validation
        var streetRegex = /^[a-zA-Z0-9\s]+$/;
        if (!street.match(streetRegex)) {
            alert('Please enter a valid street address (only alphabetic characters, digits, and spaces are allowed).');
            return false;
        }
        // Phone number validation
        var phoneRegex = /^[0-9]{10}$/;
        if (!phone.match(phoneRegex)) {
            alert('Please enter a valid phone number (10 digits).');
            return false;
        }

        // Zip code validation
        var zipRegex = /^\d{7}$/;
        if (!zip.match(zipRegex)) {
            alert('Please enter a valid zip code (7 digits).');
            return false;
        }
        $("#addMemberAddress").submit();

        // Close the modal
        $('#addDeliveryAddressModal').modal('hide');
    });
});