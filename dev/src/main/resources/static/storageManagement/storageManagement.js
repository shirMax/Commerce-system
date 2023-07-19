$(document).ready(function() {
    $('.double-width-table').css('width', '200%'); // Adjust the value as per your requirement

    $("#addNewProductBtn").click(function() {
        $("#addNewProductForm").submit();
    });

    $("#removeProductBtn").click(function() {
        if (confirm('Are you sure you want to delete this product?')) {
            var selectedProducts = [];
            var checkboxes = document.getElementsByName("selectedProducts");

            // Iterate through the checkboxes and collect the IDs of the selected products
            for (var i = 0; i < checkboxes.length; i++) {
                if (checkboxes[i].checked) {
                    selectedProducts.push(checkboxes[i].value);
                }
            }
            // Add the selected products as hidden inputs dynamically
            var form = $('#removeProduct');
            for (var i = 0; i < selectedProducts.length; i++) {
                form.append('<input type="hidden" name="productsId" value="' + selectedProducts[i] + '">');
            }

            // Submit the form
            form.submit();
        }
    });

    $('#editProductModalBtn').click(function() {
        var selectedProducts = $('input[name="selectedProducts"]:checked');

        // Check the number of selected rows
        if (selectedProducts.length === 1) {
            // Only one row selected, open the modal
            $('#editProductModal').modal('show');
        } else {
            // Display an alert warning if no row or multiple rows are selected
            alert('Please select one product to edit.');
        }
    });

    $("#editProductBtn").click(function() {
        var selectedProducts = [];
        var checkboxes = document.getElementsByName("selectedProducts");

        // Iterate through the checkboxes and collect the IDs of the selected products
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                selectedProducts.push(checkboxes[i].value);
                break;
            }
        }
        // Add the selected product as hidden inputs dynamically
        var form = $('#editProductForm');
        form.append('<input type="hidden" name="productId" value="' + selectedProducts[0] + '">');
        // Submit the form
        form.submit();
    });
});