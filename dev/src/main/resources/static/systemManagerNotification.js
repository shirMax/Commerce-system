$(document).ready(function() {
    // Website finished loading
    var socket = new WebSocket('ws://localhost:8080/systemManager');

    socket.onmessage = function(event) {
        window.location.href = "/";
    };
});
