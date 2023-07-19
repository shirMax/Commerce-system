$(document).ready(function() {
    $('.double-width-table').css('width', '200%'); // Adjust the value as per your requirement
    $("#addProductDiscountBtn").click(function () {
        $("#addProductDiscountForm").submit();
    });

    $("#addCategoryDiscountBtn").click(function () {
        $("#addCategoryDiscountForm").submit();
    });

    $("#addStoreDiscountBtn").click(function () {
        $("#addStoreDiscountForm").submit();
    });

    $("#removeDiscountBtn").click(function () {
        $("#removeDiscountForm").submit();
    });

    $("#addOrDiscountRuleFormBtn").click(function () {
        var selectedDiscounts = [];
        var checkboxes = document.getElementsByName("selectedDiscounts");
        var modal = document.getElementById('addOrDiscountRuleModal');

        // Iterate through the checkboxes and collect the IDs of the selected discounts
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                selectedDiscounts.push(checkboxes[i].value);
            }
        }
        if(selectedDiscounts.length !== 1) {
            alert("Must select exactly one discount for adding or discount");
            return false;
        }
        // Check if the category selection changed
        var categorySelect = $('#addOrDiscountRuleModal select[name="category"]');
        var selectedCategory = categorySelect.val();
        const MinimumCategoryQuantity = modal.querySelector('input[name="MinimumCategoryQuantity"]');
        if ((selectedCategory !== null && MinimumCategoryQuantity.value.trim() === '') || (selectedCategory === null && MinimumCategoryQuantity.value.trim() !== '')) {
            alert("Must enter Category and quantity together!");
            return false;
        }
        const MinimumProductQuantity = modal.querySelector('input[name="MinimumProductQuantity"]');
        const MinimumProductId = modal.querySelector('input[name="MinimumProductId"]');
        if((MinimumProductQuantity.value.trim() === '' && MinimumProductId.value.trim() !== '') || MinimumProductQuantity.value.trim() !== '' && MinimumProductId.value.trim() === '') {
            alert("Must enter product id and quantity together!");
            return false;
        }
        const minimumBasketPriceInput = modal.querySelector('input[name="MinimumBasketPrice"]');
        if (minimumBasketPriceInput.value.trim() === '') {
            // Remove the input element
            minimumBasketPriceInput.remove();
        }
        if(MinimumProductQuantity.value.trim() === '' && MinimumProductId.value.trim() === '') {
            MinimumProductQuantity.remove();
            MinimumProductId.remove();
        }
        if(MinimumCategoryQuantity.value.trim() === '') {
            MinimumCategoryQuantity.remove();
            categorySelect.remove();
        }
        var form = $('#addOrDiscountRuleForm');
        form.append('<input type="hidden" name="discountId" value="' + selectedDiscounts[0] + '">');
        form.submit();
    });

    $("#addAndDiscountRuleFormBtn").click(function () {
        var selectedDiscounts = [];
        var checkboxes = document.getElementsByName("selectedDiscounts");
        var modal = document.getElementById('addAndDiscountRuleModal');

        // Iterate through the checkboxes and collect the IDs of the selected discounts
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                selectedDiscounts.push(checkboxes[i].value);
            }
        }
        if(selectedDiscounts.length !== 1) {
            alert("Must select exactly one discount for adding or discount");
            return false;
        }
        // Check if the category selection changed
        var categorySelect = $('#addAndDiscountRuleModal select[name="category"]');
        var selectedCategory = categorySelect.val();
        const MinimumCategoryQuantity = modal.querySelector('input[name="MinimumCategoryQuantity"]');
        if ((selectedCategory !== null && MinimumCategoryQuantity.value.trim() === '') || (selectedCategory === null && MinimumCategoryQuantity.value.trim() !== '')) {
            alert("Must enter Category and quantity together!");
            return false;
        }
        const MinimumProductQuantity = modal.querySelector('input[name="MinimumProductQuantity"]');
        const MinimumProductId = modal.querySelector('input[name="MinimumProductId"]');
        if((MinimumProductQuantity.value.trim() === '' && MinimumProductId.value.trim() !== '') || MinimumProductQuantity.value.trim() !== '' && MinimumProductId.value.trim() === '') {

            ("Must enter product id and quantity together!");
            return false;
        }
        const minimumBasketPriceInput = modal.querySelector('input[name="MinimumBasketPrice"]');
        if (minimumBasketPriceInput.value.trim() === '') {
            // Remove the input element
            minimumBasketPriceInput.remove();
        }
        if(MinimumProductQuantity.value.trim() === '' && MinimumProductId.value.trim() === '') {
            MinimumProductQuantity.remove();
            MinimumProductId.remove();
        }
        if(MinimumCategoryQuantity.value.trim() === '') {
            MinimumCategoryQuantity.remove();
            categorySelect.remove();
        }
        var form = $('#addAndDiscountRuleForm');
        form.append('<input type="hidden" name="discountId" value="' + selectedDiscounts[0] + '">');
        form.submit();
    });

    $("#addIfThenDiscountRuleFormBtn").click(function () {
        var selectedDiscounts = [];
        var checkboxes = document.getElementsByName("selectedDiscounts");
        var numOfConditions = 0;
        var modal = document.getElementById('addIfThenDiscountRuleModal');

        // Iterate through the checkboxes and collect the IDs of the selected discounts
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                selectedDiscounts.push(checkboxes[i].value);
            }
        }
        if(selectedDiscounts.length !== 1) {
            alert("Must select exactly one discount for adding if then discount");
            return false;
        }
        // Check if the category selection changed
        var categorySelect = $('#addIfThenDiscountRuleModal select[name="category"]');
        var selectedCategory = categorySelect.val();
        const MinimumCategoryQuantity = modal.querySelector('input[name="MinimumCategoryQuantity"]');
        if ((selectedCategory !== null && MinimumCategoryQuantity.value.trim() === '') || (selectedCategory === null && MinimumCategoryQuantity.value.trim() !== '')) {
            alert("Must enter Category and quantity together!");
            return false;
        }
        const MinimumProductQuantity = modal.querySelector('input[name="MinimumProductQuantity"]');
        const MinimumProductId = modal.querySelector('input[name="MinimumProductId"]');
        if((MinimumProductQuantity.value.trim() === '' && MinimumProductId.value.trim() !== '') || MinimumProductQuantity.value.trim() !== '' && MinimumProductId.value.trim() === '') {
            alert("Must enter product id and quantity together!");
            return false;
        }
        const minimumBasketPriceInput = modal.querySelector('input[name="MinimumBasketPrice"]');
        if(minimumBasketPriceInput.value.trim() !=='') {
            numOfConditions++;
        }
        if(MinimumProductQuantity.value.trim() !=='') {
            numOfConditions++;
        }
        if(MinimumCategoryQuantity.value.trim() !=='') {
            numOfConditions++;
        }
        if(numOfConditions !== 1) {
            alert("If then discount must have exactly one condition only!");
            return false;
        }
        if (minimumBasketPriceInput.value.trim() === '') {
            // Remove the input element
            minimumBasketPriceInput.remove();
        }
        if(MinimumProductQuantity.value.trim() === '' && MinimumProductId.value.trim() === '') {
            MinimumProductQuantity.remove();
            MinimumProductId.remove();
        }
        if(MinimumCategoryQuantity.value.trim() === '') {
            MinimumCategoryQuantity.remove();
            categorySelect.remove();
        }
        var form = $('#addIfThenDiscountRuleForm');
        form.append('<input type="hidden" name="discountId" value="' + selectedDiscounts[0] + '">');
        form.submit();
    });

    $("#addXorDiscountRuleFormBtn").click(function () {
        var selectedDiscounts = [];
        var checkboxes = document.getElementsByName("selectedDiscounts");

        // Iterate through the checkboxes and collect the IDs of the selected discounts
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                selectedDiscounts.push(checkboxes[i].value);
            }
        }
        if(selectedDiscounts.length !== 2) {
            alert("Must select 2 discounts for adding xor discount");
            return false;
        }
        var form = $('#addXorDiscountRuleForm');
        form.append('<input type="hidden" name="discountId1" value="' + selectedDiscounts[0] + '">');
        form.append('<input type="hidden" name="discountId2" value="' + selectedDiscounts[1] + '">');
        form.submit();
    });

    $("#addMaxDiscountRuleFormBtn").click(function () {
        var selectedDiscounts = [];
        var checkboxes = document.getElementsByName("selectedDiscounts");

        // Iterate through the checkboxes and collect the IDs of the selected discounts
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                selectedDiscounts.push(checkboxes[i].value);
            }
        }
        if(selectedDiscounts.length < 2) {
            alert("Must select at least 2 discounts for adding max discount");
            return false;
        }
        var form = $('#addMaxDiscountRuleForm');
        form.append('<input type="hidden" name="discountIds" value="' + selectedDiscounts + '">');
        form.submit();
    });

    $("#addAddDiscountRuleFormBtn").click(function () {
        var selectedDiscounts = [];
        var checkboxes = document.getElementsByName("selectedDiscounts");

        // Iterate through the checkboxes and collect the IDs of the selected discounts
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].checked) {
                selectedDiscounts.push(checkboxes[i].value);
            }
        }
        if(selectedDiscounts.length < 2) {
            alert("Must select at least 2 discounts for adding add discount");
            return false;
        }
        var form = $('#addAddDiscountRuleForm');
        form.append('<input type="hidden" name="discountIds" value="' + selectedDiscounts + '">');
        form.submit();
    });
});
