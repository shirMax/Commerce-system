document.addEventListener("DOMContentLoaded", function() {
    // Event listener for the addMemberAddressBtn button
    $(document).on('click', '#addMemberAddressBtn', function() {
        var fullName = $('#fullName').val();
        var street = $('#street').val();
        var city = $('#city').val();
        var country = $('#country').val();
        var zip = $('#zip').val();
        var phoneNumber = $('#phoneNumber').val();

        // Check if any input field is empty
        if (!fullName || !street || !city || !country || !zip || !phoneNumber) {
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
        if (!phoneNumber.match(phoneRegex)) {
            alert('Please enter a valid phone number (10 digits).');
            return false;
        }

        // Zip code validation
        var zipRegex = /^\d{7}$/;
        if (!zip.match(zipRegex)) {
            alert('Please enter a valid zip code (7 digits).');
            return false;
        }

        // Submit the form
        document.getElementById("addMemberAddress").submit();
        document.getElementById("addDeliveryAddressModal").modal('hide');
    });

    $(document).on('click', '#checkoutButton', function() {
        var cardNumber = document.getElementById('cardNumber').value;
        var cardHolderName = document.getElementById('cardHolderName').value;
        var typeExpMM = document.getElementById('typeExpMM').value;
        var typeExpYYYY = document.getElementById('typeExpYYYY').value;
        var expirationDate = typeExpMM + '/' + typeExpYYYY;
        var cvv = document.getElementById('cvv').value;

        var cardNumberPattern = /^\d{16}$/;
        var fullNameRegex = /^[a-zA-Z]+(\s[a-zA-Z]+){1,3}$/;
        var expirationPattern = /^(0[1-9]|1[0-2])\/\d{4}$/;
        var cvvPattern = /^\d{3}$/;

        var isValid = true;

        if (!cardNumberPattern.test(cardNumber)) {
            alert('Please enter a valid 16-digit card number');
            isValid = false;
        }

        if (!fullNameRegex.test(cardHolderName)) {
            alert('Please enter a valid cardholder name (First Name + Last Name)');
            isValid = false;
        }

        if (!expirationPattern.test(expirationDate)) {
            alert('Please enter a valid expiration date (MM/YYYY)');
            isValid = false;
        }

        if (!cvvPattern.test(cvv)) {
            alert('Please enter a valid 3-digit CVV');
            isValid = false;
        }

        // Retrieve selected address fields
        var selectedAddressBtn = document.querySelector('.address-btn.selected');
        if (!selectedAddressBtn) {
            alert('Please select a delivery address');
            isValid = false;
        } else {
            var fullName = selectedAddressBtn.querySelector('[data-field="fullName"]').textContent;
            var street = selectedAddressBtn.querySelector('[data-field="street"]').textContent;
            var city = selectedAddressBtn.querySelector('[data-field="city"]').textContent;
            var country = selectedAddressBtn.querySelector('[data-field="country"]').textContent;
            var zip = selectedAddressBtn.querySelector('[data-field="zip"]').textContent;
            var phoneNumber = selectedAddressBtn.querySelector('[data-field="phoneNumber"]').textContent;
        }
        if(isValid) {
            var form = $('#checkoutForm');
            form.append('<input type="hidden" name="fullName" value="' + fullName + '">');
            form.append('<input type="hidden" name="street" value="' + street + '">');
            form.append('<input type="hidden" name="city" value="' + city + '">');
            form.append('<input type="hidden" name="country" value="' + country + '">');
            form.append('<input type="hidden" name="zip" value="' + zip + '">');
            form.append('<input type="hidden" name="phoneNumber" value="' + phoneNumber + '">');
            form.append('<input type="hidden" name="card_owner" value="' + cardHolderName + '">');
            form.append('<input type="hidden" name="card_number" value="' + cardNumber + '">');
            form.append('<input type="hidden" name="expiry_date" value="' + expirationDate + '">');
            form.append('<input type="hidden" name="cvv" value="' + cvv + '">');
            form.submit();
        }
    });

    var input = document.getElementById('form1');
    var previousValue = input.value;

    input.addEventListener('change', function() {
        var updatedValue = this.value;
        var productId = this.dataset.productId;
        var storeId = this.dataset.storeId;
        if(updatedValue <= 0) {
            this.value = previousValue
            return false;
        }
        var difference = updatedValue - previousValue;

        //alert('Quantity changed for Product ID: ' + productId + ', Store ID: ' + storeId + '.\nPrevious value: ' + previousValue + '\nNew value: ' + updatedValue + '\nDifference: ' + difference);

        // Perform the POST request
        var url = '/editCartProduct';
        var data = new URLSearchParams({
            storeId: storeId,
            productId: productId,
            quantity: difference
        });

        fetch(url, {
            method: 'POST',
            body: data
        })
            .then(function(response) {
                previousValue = updatedValue; // Update the previous value for the next change
                window.location.href = "/cart";
            })
            .catch(function(error) {
                this.value = previousValue;
                alert("Error in changing the quantity!");
            });
    });
});

function selectAddress(button) {
    // Deselect all buttons
    var buttons = document.getElementsByClassName('address-btn');
    for (var i = 0; i < buttons.length; i++) {
        buttons[i].classList.remove('selected');
    }

    // Select the clicked button
    button.classList.add('selected');
}