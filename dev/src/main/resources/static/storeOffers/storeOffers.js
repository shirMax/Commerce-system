$(document).ready(function() {
    $("#acceptOfferBtn").click(function () {
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

        var form = $('#acceptOfferForm');
        form.append('<input type="hidden" name="offerId" value="' + selectedOffers[0] + '">');
        form.submit();
    });

    $("#rejectOfferBtn").click(function () {
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

        var form = $('#rejectOfferForm');
        form.append('<input type="hidden" name="offerId" value="' + selectedOffers[0] + '">');
        form.submit();
    });

    $(document).on("click", "#counterofferBtn", function() {
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

        var form = $('#counterofferForm');
        form.append('<input type="hidden" name="offerId" value="' + selectedOffers[0] + '">');
        form.submit();
    });
});