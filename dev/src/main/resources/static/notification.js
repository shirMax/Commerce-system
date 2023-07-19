$(document).ready(function() {
    // Website finished loading
    var socket = new WebSocket('ws://localhost:8080/websocket');

    socket.onmessage = function(event) {
        var payloadString = event.data;
        var payload = JSON.parse(payloadString);
        var receivedMessage = payload.message;
        var sendingTime = payload.sendingTime;
        var sender = payload.sender;
        createToast(receivedMessage, sendingTime, sender);
    };
});

function createToast(message, sendingTime, sender) {
    var toastContainer = $('<div class="toast-container position-fixed bottom-0 end-0 p-3"></div>');
    var toast = $('<div class="toast" role="alert" aria-live="assertive" aria-atomic="true"></div>');
    var toastHeader = $('<div class="toast-header"></div>');
    var strong = $('<strong class="me-auto">Message from ' + sender + '</strong>');

    // Parse the sendingTime and format it as a LocalDateTime
    var parsedTime = new Date(sendingTime);
    var formattedSendingTime = parsedTime.toLocaleString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: 'numeric',
        minute: 'numeric',
        second: 'numeric'
    });

    var small = $('<small class="text-body-secondary">' + formattedSendingTime + '</small>'); // Display the formatted sendingTime
    var closeButton = $('<button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>');
    var toastBody = $('<div class="toast-body">' + message + '</div>');

    toastHeader.append(strong);
    toastHeader.append(small);
    toastHeader.append(closeButton);

    toast.append(toastHeader);
    toast.append(toastBody);

    toastContainer.append(toast);

    $('body').append(toastContainer);

    // Calculate the position of the toast container
    var offset = (toastContainer.outerWidth() + 330) * ($('.toast-container').length - 1); // Adjust the spacing as desired, e.g., 10px

    toastContainer.css('margin-right', offset + 'px'); // Adjust the margin-right for each new toast container

    $('.toast').toast('show');
}