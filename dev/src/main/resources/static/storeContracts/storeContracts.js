$(document).ready(function() {
    $("#acceptContractBtn").click(function () {
        var selectedContracts = [];
        var checkboxes = document.getElementsByName("selectedContracts");

        // Iterate through the checkboxes and collect the IDs of the selected discounts
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                selectedContracts.push(checkboxes[i].value);
            }
        }
        if(selectedContracts.length !== 1) {
            alert("Must select exactly one contract for accepting");
            return false;
        }

        var form = $('#acceptContractForm');
        form.append('<input type="hidden" name="contractId" value="' + selectedContracts[0] + '">');
        form.submit();
    });

    $("#rejectContractBtn").click(function () {
        var selectedContracts = [];
        var checkboxes = document.getElementsByName("selectedContracts");

        // Iterate through the checkboxes and collect the IDs of the selected discounts
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                selectedContracts.push(checkboxes[i].value);
            }
        }
        if(selectedContracts.length !== 1) {
            alert("Must select exactly one contract for rejecting");
            return false;
        }

        var form = $('#rejectContractForm');
        form.append('<input type="hidden" name="contractId" value="' + selectedContracts[0] + '">');
        form.submit();
    });
});