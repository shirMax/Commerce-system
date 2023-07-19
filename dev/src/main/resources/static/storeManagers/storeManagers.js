$(document).ready(function() {
    const registerButton = $("#removeStoreManagerBtn");
    registerButton.click(function() {
        $("#removeStoreManagerForm").submit();
    });
});

$(document).ready(function() {
    const registerButton = $("#appointStoreManagerBtn");
    registerButton.click(function() {
        $("#appointStoreManagerForm").submit();
    });
});


$(document).ready(function() {
    const registerButton = $("#editStoreManagerPermissionsBtn");
    registerButton.click(function() {
        $("#editStoreManagerPermissionsForm").submit();
    });
});