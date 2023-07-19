$(document).ready(function() {
    $("#purchaseOfferBtn").click(function () {
        var selectedOffer = $('input[name="selectedOffers"]:checked');
        if(selectedOffer.length !== 1) {
            alert("Must select exactly one offer for accepting");
            return false;
        }

        var offerId = selectedOffer.val();
        var storeId = selectedOffer.data('store-id');

        var form = $('#purchaseOfferForm');
        alert('doing something!');
        form.append('<input type="hidden" name="offerId" value="' + offerId + '">');
        form.append('<input type="hidden" name="storeId" value="' + storeId + '">');
        form.submit();
    });

    $("#memberRejectOfferBtn").click(function () {
        var selectedOffers = [];
        var checkboxes = document.getElementsByName("selectedOffers");

        // Iterate through the checkboxes and collect the IDs of the selected discounts
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                selectedOffers.push(checkboxes[i].value);
            }
        }
        if(selectedOffers.length !== 1) {
            alert("Must select exactly one offer for accepting");
            return false;
        }

        var form = $('#memberRejectOfferForm');
        form.append('<input type="hidden" name="offerId" value="' + selectedOffers[0] + '">');
        form.submit();
    });
});