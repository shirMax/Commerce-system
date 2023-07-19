$(document).ready(function() {
    const appointStoreOwnerBtn = $("#appointStoreOwnerBtn");
    appointStoreOwnerBtn.click(function() {
        $("#appointStoreOwnerForm").submit();
    });

    const removeStoreOwnerBtn = $("#removeStoreOwnerBtn");
    removeStoreOwnerBtn.click(function() {
        $("#removeStoreOwnerForm").submit();
    });

});
