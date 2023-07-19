// Create a variable to store the original table rows
var originalTableRows = [];

// Function to initialize the original table rows array
function initializeTableRows() {
    var table = document.getElementById('purchaseHistoryTable');
    var rows = table.querySelectorAll('tbody tr');
    originalTableRows = Array.from(rows);
}

// Function to filter the table based on the inputs
function filterTable() {
    var username = document.getElementById('usernameInput').value.toLowerCase();
    var storeId = document.getElementById('storeIdInput').value.toLowerCase();
    var tableRows = originalTableRows;

    for (var i = 0; i < tableRows.length; i++) {
        var row = tableRows[i];
        var rowUsername = row.cells[2].textContent.toLowerCase();
        var rowStoreId = row.cells[1].textContent.toLowerCase();

        if ((username && rowUsername.includes(username)) || (storeId && rowStoreId.includes(storeId))) {
            row.style.display = "";
        } else {
            row.style.display = "none";
        }
    }
}

// Function to clear the input fields and show all table rows
function clearFilter() {
    document.getElementById('usernameInput').value = '';
    document.getElementById('storeIdInput').value = '';

    var tableRows = document.querySelectorAll('#purchaseHistoryTable tbody tr');
    for (var i = 0; i < tableRows.length; i++) {
        tableRows[i].style.display = "";
    }
}
