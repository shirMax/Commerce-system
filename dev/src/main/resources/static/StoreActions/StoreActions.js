$(document).ready(function() {
    const registerButton = $("#appointStoreOwnerBtn");
    registerButton.click(function() {
        $("#appointStoreOwnerForm").submit();
    });
  
    const removeButton = $("#removeStoreOwnerBtn");
    removeButton.click(function() {
        $("#removeStoreOwnerForm").submit();
    });
});