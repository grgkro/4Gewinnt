var ws = new WebSocket("ws://h2903214.stratoserver.net:53112/login/berta/geheim");

ws.onopen = function() {
    alert("Opened!");
    ws.send("JOIN_GAME anton");
    ws.send("VIEW_GAME anton");
};

ws.onmessage = function (evt) {
    // alert("Message: " + evt.data);
    if (evt.data.startsWith('PLAYER_MOVED') )
        ws.send("MOVE 0");
};

ws.onclose = function() {
    alert("Closed!");
};

ws.onerror = function(err) {
    alert("Error: " + err);
};
