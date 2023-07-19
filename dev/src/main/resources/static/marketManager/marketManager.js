$(document).ready(function() {
    const removeMemberButton = $("#removeMemberBtn");
    removeMemberButton.click(function() {
        $("#removeMemberForm").submit();
    });

    $("#addSystemManagerBtn").click(function() {
        $("#addSystemManagerForm").submit();
    });
});