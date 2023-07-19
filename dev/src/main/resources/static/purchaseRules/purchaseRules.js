$(document).ready(function() {
    // Function to handle adding a new condition input
    function addConditionInput(condition) {
        var conditionCount = $('.'+condition+'').length;
        if (conditionCount >= 2 && condition === 'IfThenCondition') {
            alert('Maximum number of conditions reached.');
            return;
        }

        var conditionContainer = $('<div class="'+condition+'"></div>');
        var conditionSelect = $('<select class="'+condition+'-select" name="'+condition+'[]"></select>');

        // Add options to the condition select
        conditionSelect.append('<option value="basketNotContainsMoreThenByProduct">Basket Not Contains More Than by Product</option>');
        conditionSelect.append('<option value="basketNotContainsMoreThenByCategory">Basket Not Contains More Than by Category</option>');
        conditionSelect.append('<option value="basketNotContainsMoreThenByAllBasket">Basket Not Contains More Than by All Basket</option>');
        conditionSelect.append('<option value="basketNotContainsLessThenByProduct">Basket Not Contains Less Than by Product</option>');
        conditionSelect.append('<option value="basketNotContainsLessThenByCategory">Basket Not Contains Less Than by Category</option>');
        conditionSelect.append('<option value="basketNotContainsLessThenByAllBasket">Basket Not Contains Less Than by All Basket</option>');
        conditionSelect.append('<option value="notAllowedToBuyAlcoholUnder18">Not Allowed to Buy Alcohol Under 18</option>');
        conditionSelect.append('<option value="notAllowedToBuyAlcohol23To06">Not Allowed to Buy Alcohol from 23:00 to 06:00</option>');

        var parametersDiv = $('<div class="parameters"></div>');

        conditionContainer.append('<label for="'+condition+'">Select Condition:</label>');
        conditionContainer.append(conditionSelect);
        conditionContainer.append(parametersDiv);

        // Add a horizontal line between condition inputs
        $('#' + condition + 's').children().last().after('<hr>');
        $('#' + condition + 's').append(conditionContainer);

        var selectedValue = conditionSelect.val();
        generateParameterInputs(selectedValue, parametersDiv);
    }

    // Function to handle removing a condition input
    function removeConditionInput(container) {
        container.remove();
    }

    // Function to generate the input fields for condition parameters
    function generateParameterInputs(condition, parametersDiv) {
        parametersDiv.empty(); // Clear previous parameter inputs

        if (condition === 'basketNotContainsMoreThenByProduct' ||
            condition === 'basketNotContainsLessThenByProduct') {
            parametersDiv.append('<input type="number" name="productId" placeholder="Product ID">');
            parametersDiv.append('<input type="number" name="totalQuantity" placeholder="Total Quantity">');
        } else if (condition === 'basketNotContainsMoreThenByCategory' ||
            condition === 'basketNotContainsLessThenByCategory') {
            parametersDiv.append('<input type="text" name="category" placeholder="Category Name">');
            parametersDiv.append('<input type="number" name="totalQuantity" placeholder="Total Quantity">');
        } else if (condition === 'basketNotContainsMoreThenByAllBasket' ||
            condition === 'basketNotContainsLessThenByAllBasket') {
            parametersDiv.append('<input type="number" name="totalQuantity" placeholder="Total Quantity">');
        }
    }

    $('#addIfThenConditionBtn').click(function() {
        addConditionInput('IfThenCondition');
    });

    $('#ifThenConditionBtn').click(function() {
        addConditionInput('IfThenCondition');
        addConditionInput('IfThenCondition');
    });

    $('#addOrConditionBtn').click(function() {
        addConditionInput('OrCondition');
    });

    $('#orConditionBtn').click(function() {
        addConditionInput('OrCondition');
    });

    $('#addAndConditionBtn').click(function() {
        addConditionInput('AndCondition');
    });

    $('#andConditionBtn').click(function() {
        addConditionInput('AndCondition');
    });

    $(document).on('click', '.remove-ifThenCondition-btn', function() {
        var conditionContainer = $(this).closest('.IfThenCondition');
        removeConditionInput(conditionContainer);
    });

    $(document).on('click', '.remove-OrCondition-btn', function() {
        var conditionContainer = $(this).closest('.OrCondition');
        removeConditionInput(conditionContainer);
    });

    $(document).on('click', '.remove-AndCondition-btn', function() {
        var conditionContainer = $(this).closest('.AndCondition');
        removeConditionInput(conditionContainer);
    });

    $(document).on('change', '.IfThenCondition-select', function() {
        var selectedValue = $(this).val();
        var parametersDiv = $(this).closest('.IfThenCondition').find('.parameters');

        generateParameterInputs(selectedValue, parametersDiv);
    });

    $(document).on('change', '.OrCondition-select', function() {
        var selectedValue = $(this).val();
        var parametersDiv = $(this).closest('.OrCondition').find('.parameters');

        generateParameterInputs(selectedValue, parametersDiv);
    });

    $(document).on('change', '.AndCondition-select', function() {
        var selectedValue = $(this).val();
        var parametersDiv = $(this).closest('.AndCondition').find('.parameters');

        generateParameterInputs(selectedValue, parametersDiv);
    });

    $('#addIfThenPurchaseRuleBtn').click(function() {
        // Collect the conditions
        var conditions = [];
        var conditionData = [];

        $('.IfThenCondition').each(function() {
            var conditionSelect = $(this).find('.IfThenCondition-select');
            var selectedValue = conditionSelect.val();
            conditions.push(selectedValue);

            var parameterInputs = $(this).find('.parameters input');
            var parameters = [];
            parameterInputs.each(function() {
                parameters.push($(this).val());
            });
            conditionData.push(parameters);
        });

        // Set the values in the form
        for (var i = 0; i < conditions.length; i++) {
            $('<input>').attr({
                type: 'hidden',
                name: 'conditions',
                value: conditions[i]
            }).appendTo('#addIfThenPurchaseRuleForm');
        }

        for (var j = 0; j < conditionData.length; j++) {
            $('<input>').attr({
                type: 'hidden',
                name: 'conditionData',
                value: conditionData[j]
            }).appendTo('#addIfThenPurchaseRuleForm');
        }

        // Submit the form
        $('#addIfThenPurchaseRuleForm').submit();
    });

    $('#addOrPurchaseRuleBtn').click(function() {
        // Collect the conditions
        var conditions = [];
        var conditionData = [];

        $('.OrCondition').each(function() {
            var conditionSelect = $(this).find('.OrCondition-select');
            var selectedValue = conditionSelect.val();
            conditions.push(selectedValue);

            var parameterInputs = $(this).find('.parameters input');
            var parameters = [];
            parameterInputs.each(function() {
                parameters.push($(this).val());
            });
            conditionData.push(parameters);
        });

        // Set the values in the form
        for (var i = 0; i < conditions.length; i++) {
            $('<input>').attr({
                type: 'hidden',
                name: 'conditions',
                value: conditions[i]
            }).appendTo('#addOrPurchaseRuleForm');
        }

        for (var j = 0; j < conditionData.length; j++) {
            $('<input>').attr({
                type: 'hidden',
                name: 'conditionData',
                value: conditionData[j]
            }).appendTo('#addOrPurchaseRuleForm');
        }

        $('<input>').attr({
            type: 'hidden',
            name: 'conditionData',
            value: []
        }).appendTo('#addOrPurchaseRuleForm');

        // Submit the form
        $('#addOrPurchaseRuleForm').submit();
    });

    $('#addAndPurchaseRuleBtn').click(function() {
        // Collect the conditions
        var conditions = [];
        var conditionData = [];

        $('.AndCondition').each(function() {
            var conditionSelect = $(this).find('.AndCondition-select');
            var selectedValue = conditionSelect.val();
            conditions.push(selectedValue);

            var parameterInputs = $(this).find('.parameters input');
            var parameters = [];
            parameterInputs.each(function() {
                parameters.push($(this).val());
            });
            conditionData.push(parameters);
        });

        // Set the values in the form
        for (var i = 0; i < conditions.length; i++) {
            $('<input>').attr({
                type: 'hidden',
                name: 'conditions',
                value: conditions[i]
            }).appendTo('#addAndPurchaseRuleForm');
        }

        for (var j = 0; j < conditionData.length; j++) {
            $('<input>').attr({
                type: 'hidden',
                name: 'conditionData',
                value: conditionData[j]
            }).appendTo('#addAndPurchaseRuleForm');
        }

        $('<input>').attr({
            type: 'hidden',
            name: 'conditionData',
            value: []
        }).appendTo('#addAndPurchaseRuleForm');

        // Submit the form
        $('#addAndPurchaseRuleForm').submit();
    });
    $("#removePurchaseRuleBtn").click(function () {
        $("#removePurchaseRuleForm").submit();
    });
});